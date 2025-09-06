package miragefairy2024.mod.haimeviska.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_BLOCK_TAG
import miragefairy2024.mod.haimeviska.HAIMEVISKA_LOGS_ITEM_TAG
import miragefairy2024.mod.haimeviska.HaimeviskaBlockCard
import miragefairy2024.mod.haimeviska.HaimeviskaBlockConfiguration
import miragefairy2024.mod.haimeviska.createBaseWoodSetting
import miragefairy2024.util.generator
import miragefairy2024.util.isNotIn
import miragefairy2024.util.on
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDefaultLootTableGeneration
import miragefairy2024.util.registerFlammable
import miragefairy2024.util.registerShapedRecipeGeneration
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toItemTag
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.data.models.BlockModelGenerators.WoodProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.stats.Stats
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult

abstract class AbstractHaimeviskaLogBlockCard(configuration: HaimeviskaBlockConfiguration) : HaimeviskaBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = createBaseWoodSetting().strength(2.0F)

    context(ModContext)
    override fun init() {
        super.init()

        // レシピ
        block.registerDefaultLootTableGeneration()

        // 性質
        block.registerFlammable(5, 5)

        // タグ
        HAIMEVISKA_LOGS_BLOCK_TAG.generator.registerChild(block)
        HAIMEVISKA_LOGS_ITEM_TAG.generator.registerChild(item)

    }

    context(ModContext)
    protected fun registerModelGeneration(parent: () -> Block, initializer: (WoodProvider) -> WoodProvider) = DataGenerationEvents.onGenerateBlockModel {
        initializer(it.woodProvider(parent()))
    }

    context(ModContext)
    protected fun initWood(input: () -> Item) {
        registerShapedRecipeGeneration(item, 3) {
            pattern("##")
            pattern("##")
            define('#', input())
        } on input
    }

    context(ModContext)
    protected fun initStripped(input: () -> Block) {
        ModEvents.onInitialize {
            StrippableBlockRegistry.register(input(), block())
        }
    }
}

class HaimeviskaLogBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { if (it.getValue(RotatedPillarBlock.AXIS) === Direction.Axis.Y) MapColor.RAW_IRON else MapColor.TERRACOTTA_ORANGE }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = HaimeviskaLogBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
        BlockTags.OVERWORLD_NATURAL_LOGS.generator.registerChild(block)
    }
}

class HaimeviskaStrippedLogBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.RAW_IRON }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(block) { it.logWithHorizontal(block()) }
        ResourceLocation.fromNamespaceAndPath("c", "stripped_logs").toBlockTag().generator.registerChild(block)
        ResourceLocation.fromNamespaceAndPath("c", "stripped_logs").toItemTag().generator.registerChild(item)
        initStripped(LOG.block)
    }
}

class HaimeviskaWoodBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.TERRACOTTA_ORANGE }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(LOG.block) { it.wood(block()) }
        initWood(LOG.item)
    }
}

class HaimeviskaStrippedWoodBlockCard(configuration: HaimeviskaBlockConfiguration) : AbstractHaimeviskaLogBlockCard(configuration) {
    override fun createSettings(): BlockBehaviour.Properties = super.createSettings().mapColor { MapColor.RAW_IRON }
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = RotatedPillarBlock(properties)

    context(ModContext)
    override fun init() {
        super.init()
        registerModelGeneration(STRIPPED_LOG.block) { it.wood(block()) }
        ResourceLocation.fromNamespaceAndPath("c", "stripped_woods").toBlockTag().generator.registerChild(block)
        ResourceLocation.fromNamespaceAndPath("c", "stripped_woods").toItemTag().generator.registerChild(item)
        initStripped(WOOD.block)
        initWood(STRIPPED_LOG.item)
    }
}

@Suppress("OVERRIDE_DEPRECATION")
class HaimeviskaLogBlock(settings: Properties) : RotatedPillarBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLogBlock> = simpleCodec(::HaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        if (state.getValue(AXIS) != Direction.Axis.Y) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 縦方向でなければスルー
        if (stack isNotIn ItemTags.SWORDS) @Suppress("DEPRECATION") return super.useItemOn(stack, state, level, pos, player, hand, hitResult) // 剣でなければスルー
        if (level.isClientSide) return ItemInteractionResult.SUCCESS
        val direction = if (hitResult.direction.axis === Direction.Axis.Y) player.direction.opposite else hitResult.direction

        // 加工
        stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand))
        level.setBlock(pos, HaimeviskaBlockCard.INCISED_LOG.block().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, direction), UPDATE_ALL or UPDATE_IMMEDIATE)
        player.awardStat(Stats.ITEM_USED.get(stack.item))

        // エフェクト
        level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F)

        return ItemInteractionResult.CONSUME
    }
}
