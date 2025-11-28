package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.center
import miragefairy2024.util.createItemStack
import miragefairy2024.util.defaultTraits
import miragefairy2024.util.flower
import miragefairy2024.util.nether
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.material.MapColor

object VeropedaCard : AbstractVeropedaCard<VeropedaBlock>() {
    override fun getBlockPath() = "veropeda"
    override val blockName = EnJa("Veropeda", "呪草ヴェロペダ")
    override fun getItemPath() = "veropeda_bulb"
    override val itemName = EnJa("Veropeda Bulb", "呪草ヴェロペダの球根")
    override val tier = 1
    override val poem = EnJa("Contains strong acids made from insects", "毒を喰らい、毒と化す。")

    override val blockCodec = VeropedaBlock.CODEC
    override fun createBlock() = VeropedaBlock(createCommonSettings().instabreak().mapColor(MapColor.NETHER).sound(SoundType.CROP))

    override val drops = listOf(MaterialCard.VEROPEDA_BERRIES.item, MaterialCard.VEROPEDA_LEAF.item)
    override fun getFruitDrops(count: Int, random: RandomSource) = listOf(MaterialCard.VEROPEDA_BERRIES.item().createItemStack(count))
    override fun getLeafDrops(count: Int, random: RandomSource) = listOf(MaterialCard.VEROPEDA_LEAF.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0b00101000, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0b00101000, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0b00101000, // 葉面生成
        TraitCard.CARNIVOROUS_PLANT.trait to 0b00101000, // 食虫植物
        TraitCard.PAVEMENT_FLOWERS.trait to 0b00101000, // アスファルトに咲く花
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0.05, // 葉面生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.SEEDS_DILUTION.trait to 0.05, // 種子希釈
        TraitCard.NATURAL_ABSCISSION.trait to 0.05, // 自然落果
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait to 0.05, // 闇合成
        TraitCard.FOUR_LEAFED.trait to 0.05, // 四つ葉
        TraitCard.FRUIT_OF_KNOWLEDGE.trait to 0.05, // 禁断の果実
        TraitCard.SPINY_LEAVES.trait to 0.05, // 棘のある葉
        TraitCard.FLESHY_LEAVES.trait to 0.05, // 肉厚の葉
        TraitCard.CARNIVOROUS_PLANT.trait to 0.05, // 食虫植物
        TraitCard.PAVEMENT_FLOWERS.trait to 0.05, // アスファルトに咲く花
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Artificial Plant Species", "人造植物"), // TODO 未使用ポエム：人間が作り出したモンスター
        description = EnJa("Harvest Veropeda the cursed grass, which grows in arid biomes and the nether", "乾燥系バイオームとネザーに分布する呪草ヴェロペダを収穫する"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER.generator(blockIdentifier) {
            registerConfiguredFeature("cluster", { RandomPatchConfiguration(6, 6, 2, placer) }) { // 小さな塊
                registerPlacedFeature("cluster", { per(16) + flower(square, surface) }) { (+ConventionalBiomeTags.IS_DESERT + +ConventionalBiomeTags.IS_SAVANNA + +ConventionalBiomeTags.IS_BADLANDS) * defaultTraits } // 地上用クラスタ
            }
            registerConfiguredFeature("large_cluster", { RandomPatchConfiguration(40, 8, 3, placer) }) { // 大きな塊
                registerPlacedFeature("nether_cluster", { per(8) + flower(center, nether) }) { nether * defaultTraits } // ネザー用クラスタ
            }
        }
    }
}

class VeropedaBlock(settings: Properties) : SimpleMagicPlantBlock(VeropedaCard, settings) {
    companion object {
        val CODEC: MapCodec<VeropedaBlock> = simpleCodec(::VeropedaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
