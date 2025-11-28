package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.center
import miragefairy2024.util.createItemStack
import miragefairy2024.util.defaultTraits
import miragefairy2024.util.flower
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.surface
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.material.MapColor

object SarraceniaCard : AbstractVeropedaCard<SarraceniaBlock>() {
    override fun getBlockPath() = "sarracenia"
    override val blockName = EnJa("Sarracenia", "瓶子草サラセニア")
    override fun getItemPath() = "sarracenia_bulb"
    override val itemName = EnJa("Sarracenia Bulb", "瓶子草サラセニアの球根")
    override val tier = 1
    override val poem = EnJa("Waiting for a flying creature...", "妖精たちの憩いの場。")

    override val blockCodec = SarraceniaBlock.CODEC
    override fun createBlock() = SarraceniaBlock(createCommonSettings().instabreak().mapColor(MapColor.NETHER).sound(SoundType.CROP))

    override val drops = listOf(MaterialCard.SARRACENIA_LEAF.item, MaterialCard.FAIRY_SCALES.item)
    override fun getLeafDrops(count: Int, random: RandomSource) = listOf(MaterialCard.SARRACENIA_LEAF.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.FAIRY_SCALES.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0b00101000, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.HUMID_ADAPTATION.trait to 0b00101000, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.LEAVES_PRODUCTION.trait to 0b00101000, // 葉面生成
        TraitCard.PHOTOSYNTHESIS.trait to 0b00101000, // 光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.WATERLOGGING_TOLERANCE.trait to 0b00101000, // 浸水耐性
        TraitCard.CARNIVOROUS_PLANT.trait to 0b00101000, // 食虫植物
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.HUMID_ADAPTATION.trait to 0.05, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.LEAVES_PRODUCTION.trait to 0.05, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.FOUR_LEAFED.trait to 0.05, // 四つ葉
        TraitCard.NODED_STEM.trait to 0.05, // 節状の茎
        TraitCard.WATERLOGGING_TOLERANCE.trait to 0.05, // 浸水耐性
        TraitCard.CARNIVOROUS_PLANT.trait to 0.05, // 食虫植物
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { VeropedaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Temptation of Sugar and Acid", "糖と酸の誘惑"),
        description = EnJa("Find Sarracenia in the swampland", "沼地のサラセニアを見つける"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()

        Feature.FLOWER.generator(blockIdentifier) {
            configuredFeature("cluster", { RandomPatchConfiguration(20, 8, 3, placer) }) {
                placedFeature("cluster", { per(8) + flower(center, surface) }) { (+ConventionalBiomeTags.IS_SWAMP + +ConventionalBiomeTags.IS_JUNGLE + +Biomes.MANGROVE_SWAMP) * defaultTraits }
            }
        }
    }
}

class SarraceniaBlock(settings: Properties) : SimpleMagicPlantBlock(SarraceniaCard, settings) {
    companion object {
        val CODEC: MapCodec<SarraceniaBlock> = simpleCodec(::SarraceniaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
