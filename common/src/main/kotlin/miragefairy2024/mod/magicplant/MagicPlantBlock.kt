package miragefairy2024.mod.magicplant

import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.createItemStack
import miragefairy2024.util.invoke
import miragefairy2024.util.isIn
import miragefairy2024.util.isNotIn
import miragefairy2024.util.isServer
import miragefairy2024.util.randomInt
import miragefairy2024.util.text
import miragefairy2024.util.toBlockPos
import miragefairy2024.util.toBox
import mirrg.kotlin.helium.or
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BonemealableBlock
import net.minecraft.world.level.block.BushBlock
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.SupportType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.BlockHitResult

abstract class MagicPlantBlock(private val configuration: MagicPlantCard<*>, settings: Properties) : BushBlock(settings), EntityBlock, BonemealableBlock {

    // Block Entity

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = MagicPlantBlockEntity(configuration, pos, state)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun triggerEvent(state: BlockState, world: Level, pos: BlockPos, type: Int, data: Int): Boolean {
        super.triggerEvent(state, world, pos, type, data)
        val blockEntity = world.getBlockEntity(pos) ?: return false
        return blockEntity.triggerEvent(type, data)
    }


    // Behaviour

    override fun mayPlaceOn(floor: BlockState, world: BlockGetter, pos: BlockPos) = world.getBlockState(pos).isFaceSturdy(world, pos, Direction.UP, SupportType.CENTER) || floor isIn Blocks.FARMLAND


    // Trait

    /** 隣接する同種の植物が交配種子を生産するときに参加できるか否か */
    protected abstract fun canCross(world: Level, blockPos: BlockPos, blockState: BlockState): Boolean

    /** あるワールド上の地点における特性の効果を計算する。 */
    protected fun calculateTraitEffects(world: Level, blockPos: BlockPos, blockEntity: MagicPlantBlockEntity?, traitStacks: TraitStacks): MutableTraitEffects {
        val allTraitEffects = MutableTraitEffects()
        traitStacks.traitStackMap.forEach { (trait, level) ->
            val traitEffects = trait.getTraitEffects(world, blockPos, blockEntity, level)
            if (traitEffects != null) allTraitEffects += traitEffects
        }
        return allTraitEffects
    }

    /** 種子によって置かれた際にその特性をコピーする。 */
    final override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack)
        run {
            if (world.isClientSide) return@run
            val blockEntity = world.getBlockEntity(pos) as? MagicPlantBlockEntity ?: return@run
            val traitStacks = itemStack.getTraitStacks() ?: return@run
            blockEntity.setTraitStacks(traitStacks)
            blockEntity.setRare(itemStack.isRare())
            blockEntity.setNatural(false)
        }
    }


    // Growth

    /** このサイズは成長が可能か。 */
    protected abstract fun canGrow(blockState: BlockState): Boolean

    /** 指定のサイズで成長した後のサイズを返す。 */
    protected abstract fun getBlockStateAfterGrowth(blockState: BlockState, amount: Int): BlockState

    /** 時間経過や骨粉などによって呼び出される成長と自動収穫などのためのイベントを処理します。 */
    protected open fun move(world: ServerLevel, blockPos: BlockPos, blockState: BlockState, speed: Double = 1.0, autoPick: Boolean = false) {
        val blockEntity = world.getMagicPlantBlockEntity(blockPos)
        val traitStacks = blockEntity?.getTraitStacks() ?: return
        val mainTraitEffects = calculateTraitEffects(world, blockPos, blockEntity, traitStacks)
        val traitEffectsListForGrowth = listOf(
            mainTraitEffects,
            calculateTraitEffects(world, blockPos.offset(-1, 0, 0), blockEntity, traitStacks),
            calculateTraitEffects(world, blockPos.offset(+1, 0, 0), blockEntity, traitStacks),
            calculateTraitEffects(world, blockPos.offset(0, 0, -1), blockEntity, traitStacks),
            calculateTraitEffects(world, blockPos.offset(0, 0, +1), blockEntity, traitStacks),
        )

        // 成長
        if (canGrow(blockState)) {
            val traitGrowth = traitEffectsListForGrowth.minOf { traitEffects ->
                val nutrition = traitEffects[TraitEffectKeyCard.NUTRITION.traitEffectKey]
                val temperature = traitEffects[TraitEffectKeyCard.TEMPERATURE.traitEffectKey]
                val humidity = traitEffects[TraitEffectKeyCard.HUMIDITY.traitEffectKey]
                val growthBoost = traitEffects[TraitEffectKeyCard.GROWTH_BOOST.traitEffectKey]
                nutrition * temperature * humidity * (1 + growthBoost)
            }
            val actualGrowthAmount = world.random.randomInt(configuration.baseGrowth * traitGrowth * speed)
            val newBlockState = getBlockStateAfterGrowth(blockState, actualGrowthAmount)
            if (newBlockState != blockState) {
                world.setBlock(blockPos, newBlockState, UPDATE_CLIENTS)
            }
        }

        // 自動収穫
        if (autoPick && canAutoPick(blockState)) run {
            if (world.getEntities(EntityType.ITEM, blockPos.toBox()) { true }.isNotEmpty()) return@run // アイテムがそこに存在する場合は中止
            if (world.getEntities(EntityType.EXPERIENCE_ORB, blockPos.toBox()) { true }.isNotEmpty()) return@run // 経験値がそこに存在する場合は中止
            val naturalAbscission = mainTraitEffects[TraitEffectKeyCard.NATURAL_ABSCISSION.traitEffectKey]
            if (!(world.random.nextDouble() < naturalAbscission)) return@run // 確率で失敗
            pick(world, blockPos, null, null, false)
        }

    }

    final override fun isRandomlyTicking(state: BlockState) = true

    @Suppress("OVERRIDE_DEPRECATION")
    final override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) = move(world, pos, state, autoPick = true)

    final override fun isValidBonemealTarget(world: LevelReader, pos: BlockPos, state: BlockState) = canGrow(state)
    final override fun isBonemealSuccess(world: Level, random: RandomSource, pos: BlockPos, state: BlockState) = true
    final override fun performBonemeal(world: ServerLevel, random: RandomSource, pos: BlockPos, state: BlockState) = move(world, pos, state, speed = 10.0)


    // Drop

    /** このサイズは収穫が可能か。 */
    protected abstract fun canPick(blockState: BlockState): Boolean

    /** このサイズは自動収穫が可能か。 */
    protected open fun canAutoPick(blockState: BlockState) = canPick(blockState)

    /** 指定のサイズで収穫した後のサイズを返す。 */
    protected abstract fun getBlockStateAfterPicking(blockState: BlockState): BlockState

    /** 確定で戻って来る本来の種子以外の追加種子及び生産物を計算する。 */
    protected abstract fun getAdditionalDrops(world: Level, blockPos: BlockPos, block: Block, blockState: BlockState, traitStacks: TraitStacks, traitEffects: MutableTraitEffects, randomTraitChances: Map<Trait, Double>, player: Player?, tool: ItemStack?): List<ItemStack>

    /** この植物本来の種子を返す。 */
    protected fun createSeed(traitStacks: TraitStacks, isRare: Boolean): ItemStack {
        val itemStack = this.asItem().createItemStack()
        itemStack.setTraitStacks(traitStacks)
        itemStack.setRare(isRare)
        return itemStack
    }

    /** 交配が可能であれば交配された種子、そうでなければこの植物本来の種子を返す。 */
    protected fun calculateCrossedSeed(world: Level, blockPos: BlockPos, traitStacks: TraitStacks, randomTraitChances: Map<Trait, Double>, crossbreedingRate: Double, mutation: Double, mutationFactor: Double): ItemStack {
        val targetTraitStacksList = mutableListOf<TraitStacks>()
        fun check(targetBlockPos: BlockPos) {
            val targetBlockState = world.getBlockState(targetBlockPos)
            val targetBlock = targetBlockState.block as? MagicPlantBlock ?: return
            run {
                if (targetBlock == this) {
                    return@run
                }
                if (crossbreedingRate > 0.0) {
                    if (targetBlock.configuration.family == this.configuration.family) {
                        if (world.random.nextDouble() < crossbreedingRate) {
                            return@run
                        }
                    }
                }
                return
            }
            if (!targetBlock.canCross(world, blockPos, targetBlockState)) return
            val targetTraitStacks = world.getMagicPlantBlockEntity(targetBlockPos)?.getTraitStacks() ?: return
            targetTraitStacksList += targetTraitStacks
        }
        check(blockPos.north())
        check(blockPos.south())
        check(blockPos.west())
        check(blockPos.east())

        val crossedBits = if (targetTraitStacksList.isEmpty()) {
            traitStacks.traitStackMap
        } else {
            val targetTraitStacks = targetTraitStacksList[world.random.nextInt(targetTraitStacksList.size)]
            crossTraitStacks(traitStacks.traitStackMap, targetTraitStacks.traitStackMap, world.random)
        }
        val (mutatedBits, rare) = if (world.random.nextDouble() < mutation) {
            applyMutation(crossedBits, randomTraitChances.mapValues { it.value * mutationFactor }, world.random)
        } else {
            Pair(crossedBits, false)
        }
        return createSeed(TraitStacks.of(mutatedBits), rare)
    }

    fun tryPick(world: Level, blockPos: BlockPos, player: Player?, tool: ItemStack?, dropExperience: Boolean, causingEvent: Boolean): Boolean {
        val result = canPick(world.getBlockState(blockPos))
        if (result && world.isServer) pick(world as ServerLevel, blockPos, player, tool, dropExperience)
        if (causingEvent) {
            if (tool != null) {
                val toolItem = tool.item
                if (toolItem is PostTryPickHandlerItem) {
                    toolItem.postTryPick(world, blockPos, player, tool, result)
                }
            }
        }
        return result
    }

    /** 成長段階を消費して収穫物を得てエフェクトを出す収穫処理。 */
    private fun pick(world: ServerLevel, blockPos: BlockPos, player: Player?, tool: ItemStack?, dropExperience: Boolean) {

        // ドロップアイテムを計算
        val blockState = world.getBlockState(blockPos)
        val block = blockState.block
        val blockEntity = world.getMagicPlantBlockEntity(blockPos) ?: return
        val traitStacks = blockEntity.getTraitStacks() ?: return
        val traitEffects = calculateTraitEffects(world, blockPos, blockEntity, traitStacks)
        val randomTraitChances = blockEntity.getRandomTraitChances()
        val drops = getAdditionalDrops(world, blockPos, block, blockState, traitStacks, traitEffects, randomTraitChances, player, tool)
        val experience = if (dropExperience) world.random.randomInt(traitEffects[TraitEffectKeyCard.EXPERIENCE_PRODUCTION.traitEffectKey]) else 0

        // アイテムを生成
        drops.forEach { itemStack ->
            popResource(world, blockPos, itemStack)
        }
        if (experience > 0) popExperience(world, blockPos, experience)

        // 成長段階を消費
        world.setBlock(blockPos, getBlockStateAfterPicking(blockState), UPDATE_CLIENTS)

        // 天然フラグを除去
        blockEntity.setNatural(false)

        // エフェクト
        world.playSound(null, blockPos, soundType.breakSound, SoundSource.BLOCKS, (soundType.volume + 1.0F) / 2.0F * 0.5F, soundType.pitch * 0.8F)

    }

    /** 右クリック時、スニーク中であれば特性GUIを出し、そうでない場合、収穫が可能であれば収穫する。 */
    @Suppress("OVERRIDE_DEPRECATION")
    final override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (player.isShiftKeyDown) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS
            } else {
                val traitStacks = run {
                    val blockEntity = level.getMagicPlantBlockEntity(pos) ?: return@run TraitStacks.EMPTY
                    blockEntity.getTraitStacks() ?: TraitStacks.EMPTY
                }
                player.openMenu(object : ExtendedScreenHandlerFactory<Pair<TraitStacks, BlockPos>> {
                    override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player): AbstractContainerMenu {
                        return TraitListScreenHandler(syncId, playerInventory, ContainerLevelAccess.create(level, player.blockPosition()), traitStacks, pos)
                    }

                    override fun getDisplayName() = text { traitListScreenTranslation() }

                    override fun getScreenOpeningData(player: ServerPlayer) = Pair(traitStacks, pos)
                })
                return InteractionResult.CONSUME
            }
        }
        if (!tryPick(level, pos, player, player.mainHandItem, true, true)) return InteractionResult.PASS
        return InteractionResult.sidedSuccess(level.isClientSide)
    }

    /** 中央クリックをした際は、この植物の本来の種子を返す。 */
    final override fun getCloneItemStack(world: LevelReader, pos: BlockPos, state: BlockState): ItemStack {
        val blockEntity = world.getMagicPlantBlockEntity(pos) ?: return EMPTY_ITEM_STACK
        val traitStacks = blockEntity.getTraitStacks() ?: return EMPTY_ITEM_STACK
        return createSeed(traitStacks, isRare = blockEntity.isRare())
    }

    /** 破損時、LootTableと同じところで収穫物を追加する。 */
    // 本来 LootTable を使ってすべて行う想定だが、他にドロップを自由に制御できる場所がないため苦肉の策でここでプログラムで生成する
    @Suppress("OVERRIDE_DEPRECATION")
    final override fun getDrops(state: BlockState, builder: LootParams.Builder): MutableList<ItemStack> {
        val itemStacks = mutableListOf<ItemStack>()
        @Suppress("DEPRECATION")
        itemStacks += super.getDrops(state, builder)
        run {
            val world = builder.level ?: return@run
            val blockPos = builder.getOptionalParameter(LootContextParams.ORIGIN).or { return@run }.toBlockPos()
            val blockState = builder.getOptionalParameter(LootContextParams.BLOCK_STATE) ?: return@run
            val block = blockState.block
            val blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY) as? MagicPlantBlockEntity ?: return@run
            val traitStacks = blockEntity.getTraitStacks() ?: return@run
            val traitEffects = calculateTraitEffects(world, blockPos, blockEntity, traitStacks)
            val randomTraitChances = blockEntity.getRandomTraitChances()
            val player = builder.getOptionalParameter(LootContextParams.THIS_ENTITY) as? Player
            val tool = builder.getOptionalParameter(LootContextParams.TOOL)

            itemStacks += createSeed(traitStacks, isRare = blockEntity.isRare())
            itemStacks += getAdditionalDrops(world, blockPos, block, blockState, traitStacks, traitEffects, randomTraitChances, player, tool)
        }
        return itemStacks
    }

    /** 破壊時、経験値をドロップする。 */
    // 経験値のドロップを onStacksDropped で行うと BlockEntity が得られないためこちらで実装する
    @Suppress("OVERRIDE_DEPRECATION")
    final override fun onRemove(state: BlockState, world: Level, pos: BlockPos, newState: BlockState, moved: Boolean) {
        if (state isNotIn newState.block) run {
            if (world !is ServerLevel) return@run
            if (!canPick(state)) return@run
            val blockEntity = world.getMagicPlantBlockEntity(pos)
            val traitStacks = blockEntity?.getTraitStacks() ?: return@run
            val traitEffects = calculateTraitEffects(world, pos, blockEntity, traitStacks)
            val experience = world.random.randomInt(traitEffects[TraitEffectKeyCard.EXPERIENCE_PRODUCTION.traitEffectKey])
            if (experience > 0) popExperience(world, pos, experience)
        }
        @Suppress("DEPRECATION")
        super.onRemove(state, world, pos, newState, moved)
    }


    // Visual

    // TODO パーティクル

}

interface PostTryPickHandlerItem {
    fun postTryPick(world: Level, blockPos: BlockPos, player: Player?, itemStack: ItemStack, succeed: Boolean)
}
