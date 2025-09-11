package miragefairy2024.mod

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.SimpleHorizontalFacingBlock
import miragefairy2024.mod.fairy.CondensedMotifChance
import miragefairy2024.mod.fairy.FairyStatueCard
import miragefairy2024.mod.fairy.Motif
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.MotifTableScreenHandler
import miragefairy2024.mod.fairy.setFairyMotif
import miragefairy2024.mod.fairyquest.FairyQuestCardCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.Chance
import miragefairy2024.util.CondensedItem
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.createItemStack
import miragefairy2024.util.enJa
import miragefairy2024.util.filled
import miragefairy2024.util.generator
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotIn
import miragefairy2024.util.isServer
import miragefairy2024.util.normal
import miragefairy2024.util.obtain
import miragefairy2024.util.on
import miragefairy2024.util.register
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.registerVariantsBlockStateGeneration
import miragefairy2024.util.text
import miragefairy2024.util.times
import miragefairy2024.util.weightedRandom
import miragefairy2024.util.withHorizontalRotation
import mirrg.kotlin.helium.Single
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.pathfinder.PathComputationType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

object FairyStatueFountainCard {
    val identifier = MirageFairy2024.identifier("fairy_statue_fountain")
    val block = Registration(BuiltInRegistries.BLOCK, identifier) { FairyStatueFountainBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0F).noOcclusion()) }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }
    val advancement = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { FairyQuestCardCard.advancement.await() },
        icon = { item().createItemStack() },
        name = EnJa("The Desire for Rerolls", "リセマラの誘い"),
        description = EnJa("Throw a Fairy Jewel into a fairy statue fountain", "妖精の像の泉にフェアリージュエルを投げ込もう"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.GOAL,
    )
}

context(ModContext)
fun initFairyFountainModule() {
    FairyStatueFountainBlock.USAGE_TRANSLATION.enJa()

    Registration(BuiltInRegistries.BLOCK_TYPE, MirageFairy2024.identifier("fairy_statue_fountain")) { FairyStatueFountainBlock.CODEC }.register()

    FairyStatueFountainCard.let { card ->

        card.block.register()
        card.item.register()

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerVariantsBlockStateGeneration { normal("block/" * card.block().getIdentifier()).withHorizontalRotation(HorizontalDirectionalBlock.FACING) }
        card.block.registerCutoutRenderLayer()

        card.block.enJa(EnJa("Fairy Statue Fountain", "妖精の像の泉"))
        val poemList = PoemList(1)
            .poem("Where does this water spring from...?", "この水は一体どこから湧いてくるのだろう…")
            .description("description1", "Can draw lottery with 100 Fairy Jewels", "100フェアリージュエルで抽選ができる")
            .description("description2", "Use while sneaking to show loot table", "スニーク中に使用時、提供割合を表示")
        card.item.registerPoem(poemList)
        card.item.registerPoemGeneration(poemList)

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(card.block)

        card.block.registerDefaultLootTableGeneration()

        card.advancement.init()

    }

    registerShapedRecipeGeneration(FairyStatueFountainCard.item) {
        pattern(" F ")
        pattern("SQS")
        pattern("SSS")
        define('F', MaterialCard.FAIRY_SCALES.item())
        define('Q', MaterialCard.FAIRY_QUEST_CARD_BASE.item())
        define('S', Items.COBBLESTONE)
    } on MaterialCard.FAIRY_SCALES.item
}

class FairyStatueFountainBlock(settings: Properties) : SimpleHorizontalFacingBlock(settings) {
    companion object {
        val CODEC: MapCodec<FairyStatueFountainBlock> = simpleCodec(::FairyStatueFountainBlock)
        val USAGE_TRANSLATION = Translation({ "block.${MirageFairy2024.identifier("fairy_statue_fountain").toLanguageKey()}.usage" }, "Please use it while holding %s", "%sを持って使用してください")
        private val SHAPE: VoxelShape = box(2.0, 0.0, 2.0, 14.0, 9.0, 14.0)
        val recipes = mutableListOf<Recipe>()
    }

    enum class Rarity {
        R, // 85%
        SR, // 9%
        PICKUP_SR, // 3%
        SSR, // 2%
        PICKUP_SSR, // 1%
    }

    class Recipe(val motif: Motif, val rarity: Rarity)


    override fun codec() = CODEC

    @Suppress("OVERRIDE_DEPRECATION")
    override fun isPathfindable(state: BlockState, pathComputationType: PathComputationType) = false

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext) = SHAPE

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {

        // 入力判定
        if (stack isNotIn MaterialCard.JEWEL_100.item()) { // 持っているアイテムが違う
            if (level.isServer) player.displayClientMessage(text { USAGE_TRANSLATION(MaterialCard.JEWEL_100.item().description) }, true)
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION // なぜかFAILにすると後続のイベントがキャンセルされない
        }
        if (stack.count < 1) { // 個数が足りない
            if (level.isServer) player.displayClientMessage(text { USAGE_TRANSLATION(MaterialCard.JEWEL_100.item().description) }, true)
            return ItemInteractionResult.CONSUME // なぜかFAILにすると後続のイベントがキャンセルされない
        }

        // 成立

        // 消費
        if (!player.isCreative) {
            if (level.isServer) stack.shrink(1)
        }

        // 生産
        if (level.isServer) {
            val outputItemStack = run {
                val chanceTable = getChanceTable()
                val entry = chanceTable.weightedRandom(level.random)?.first
                entry?.let { it.second.getFairyStatueCard().item().createItemStack().also { itemStack -> itemStack.setFairyMotif(it.first) } } ?: Items.IRON_INGOT.createItemStack()
            }
            player.obtain(outputItemStack)
        }

        // エフェクト
        if (level.isServer) level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.35F + 0.9F)
        if (level.isClientSide) {
            // TODO サーバーサイドで発火して、全プレイヤーの画面に表示する
            repeat(3) {
                level.addParticle(
                    ParticleTypeCard.MISSION.particleType,
                    pos.x + 2.0 / 16.0 + level.random.nextDouble() * 12.0 / 16.0,
                    pos.y + 2.0 / 16.0 + level.random.nextDouble() * 4.0 / 16.0,
                    pos.z + 2.0 / 16.0 + level.random.nextDouble() * 12.0 / 16.0,
                    level.random.nextGaussian() * 0.02,
                    level.random.nextGaussian() * 0.02,
                    level.random.nextGaussian() * 0.02,
                )
            }
        }

        return ItemInteractionResult.SUCCESS
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun useWithoutItem(state: BlockState, level: Level, pos: BlockPos, player: Player, hitResult: BlockHitResult): InteractionResult {
        if (player.isShiftKeyDown) {
            if (level.isClientSide) return InteractionResult.SUCCESS

            val chanceTable2 = getChanceTable()
            val chanceTable = chanceTable2.map {
                CondensedMotifChance(
                    showingItemStack = it.item.first?.let { entry -> entry.second.getFairyStatueCard().item().createItemStack().also { itemStack -> itemStack.setFairyMotif(entry.first) } } ?: Items.IRON_INGOT.createItemStack(),
                    item = Chance(
                        weight = it.weight,
                        item = CondensedItem(
                            count = 1.0,
                            item = it.item.first?.first ?: MotifCard.AIR,
                        ),
                    ),
                )
            }

            player.openMenu(object : ExtendedScreenHandlerFactory<List<CondensedMotifChance>> {
                override fun createMenu(syncId: Int, playerInventory: Inventory, player: Player) = MotifTableScreenHandler(syncId, chanceTable)
                override fun getDisplayName() = name
                override fun getScreenOpeningData(player: ServerPlayer) = chanceTable
            })

            return InteractionResult.CONSUME
        } else {
            if (level.isServer) player.displayClientMessage(text { USAGE_TRANSLATION(MaterialCard.JEWEL_100.item().description) }, true)
            return InteractionResult.CONSUME // なぜかFAILにすると後続のイベントがキャンセルされない
        }
    }

    /** 確率の合計が1.0+εであることが保証されます。 */
    private fun getChanceTable(): List<Chance<Single<Pair<Motif, Rarity>?>>> {
        val chanceTable = mutableListOf<Chance<Single<Pair<Motif, Rarity>?>>>()

        var consumedChance = 0.0

        fun f(threshold: Double, rarity: Rarity) {
            val recipes2 = recipes.filter { it.rarity == rarity }
            if (recipes2.isEmpty()) return
            val chancePerRecipe = (threshold - consumedChance) / recipes2.size.toDouble()
            chanceTable += recipes2.map { Chance(chancePerRecipe, Single(Pair(it.motif, rarity))) }
            consumedChance = threshold
        }
        f(0.01, Rarity.PICKUP_SSR)
        f(0.01 + 0.02, Rarity.SSR)
        f(0.01 + 0.02 + 0.03, Rarity.PICKUP_SR)
        f(0.01 + 0.02 + 0.03 + 0.09, Rarity.SR)
        f(0.01 + 0.02 + 0.03 + 0.09 + 0.85, Rarity.R)

        return chanceTable.filled { Single(null) }
    }

}

fun FairyStatueFountainBlock.Rarity.getFairyStatueCard() = when (this) {
    FairyStatueFountainBlock.Rarity.R -> FairyStatueCard.FAIRY_STATUE
    FairyStatueFountainBlock.Rarity.SR -> FairyStatueCard.GOLDEN_FAIRY_STATUE
    FairyStatueFountainBlock.Rarity.PICKUP_SR -> FairyStatueCard.GOLDEN_FAIRY_STATUE
    FairyStatueFountainBlock.Rarity.SSR -> FairyStatueCard.FANTASTIC_FAIRY_STATUE
    FairyStatueFountainBlock.Rarity.PICKUP_SSR -> FairyStatueCard.FANTASTIC_FAIRY_STATUE
}
