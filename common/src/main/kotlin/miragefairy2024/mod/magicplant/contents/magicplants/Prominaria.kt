package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.MagicPlantBlockEntity
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.Material
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.materials.Shape
import miragefairy2024.mod.materials.tagOf
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.defaultTraits
import miragefairy2024.util.flower
import miragefairy2024.util.getOr
import miragefairy2024.util.isNotIn
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.rangedNether
import miragefairy2024.util.square
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.minecraft.core.BlockPos
import net.minecraft.data.worldgen.placement.PlacementUtils
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.ItemInteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.gameevent.GameEvent
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.phys.BlockHitResult

object ProminariaCard : AbstractProminariaCard<ProminariaBlock>() {
    override fun getBlockPath() = "prominaria"
    override val blockName = EnJa("Prominaria", "紅炎草プロミナリア")
    override fun getItemPath() = "prominaria_bulb"
    override val itemName = EnJa("Prominaria Bulb", "紅炎草プロミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Cleansing of tainted souls.", "死霊を貪り、命に変える。")

    override val blockCodec = ProminariaBlock.CODEC
    override fun createBlock() = ProminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.CRIMSON_HYPHAE).sound(SoundType.CROP))

    override val drops = listOf(MaterialCard.PROMINARIA_BERRY.item, MaterialCard.PROMINITE.item)
    override fun getFruitDrops(count: Int, random: RandomSource) = listOf(MaterialCard.PROMINARIA_BERRY.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.PROMINITE.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0b00101000, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0b00101000, // 果実生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait to 0b00101000, // アスファルトに咲く花
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.GOLDEN_APPLE.trait to 0.05, // 金のリンゴ
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait to 0.05, // アスファルトに咲く花
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DiamondLuminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Subterranean Sun", "地獄にともる灯"),
        description = EnJa("Search for Prominaria in the bottom of Nether Wastes", "ネザーの荒地の最下層でプロミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER {
            configuredFeature("cluster", { RandomPatchConfiguration(6, 6, 2, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, SimpleBlockConfiguration(it))) }) {
                placedFeature("cluster", { per(4) + flower(square, rangedNether(32, 45)) }) { (+Biomes.NETHER_WASTES + +Biomes.CRIMSON_FOREST) * defaultTraits }
            }
        }
    }
}

class ProminariaBlock(settings: Properties) : SimpleMagicPlantBlock(ProminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<ProminariaBlock> = simpleCodec(::ProminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3

    override fun useItemOn(stack: ItemStack, state: BlockState, level: Level, pos: BlockPos, player: Player, hand: InteractionHand, hitResult: BlockHitResult): ItemInteractionResult {
        val blockEntity = level.getBlockEntity(pos)
        if (blockEntity !is MagicPlantBlockEntity) return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        if (stack isNotIn tagOf(Shape.INGOT, Material.GOLD)) return super.useItemOn(stack, state, level, pos, player, hand, hitResult)
        if (level.isClientSide) return ItemInteractionResult.SUCCESS

        val age = state.getOr(BlockStateProperties.AGE_3) { 0 }
        val traitStacks = blockEntity.getTraitStacks()
        val rare = blockEntity.isRare()
        val natural = blockEntity.isNatural()

        level.setBlock(pos, GoldProminariaCard.block().defaultBlockState().setValue(BlockStateProperties.AGE_3, age), 0b1011)
        val newBlockEntity = level.getBlockEntity(pos) as MagicPlantBlockEntity
        newBlockEntity.setTraitStacks(traitStacks)
        newBlockEntity.setRare(rare)
        newBlockEntity.setNatural(natural)

        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos)
        stack.consume(1, player)

        level.playSound(null, pos, SoundEvents.PUMPKIN_CARVE, SoundSource.BLOCKS, 1.0F, 1.0F) // TODO 音を変更

        return ItemInteractionResult.CONSUME
    }
}
