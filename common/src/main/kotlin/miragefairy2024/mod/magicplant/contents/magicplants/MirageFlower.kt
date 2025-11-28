package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.biome.DeepFairyForestBiomeCard
import miragefairy2024.mod.biome.FairyForestBiomeCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Registration
import miragefairy2024.util.center
import miragefairy2024.util.count
import miragefairy2024.util.createItemStack
import miragefairy2024.util.defaultTraits
import miragefairy2024.util.end
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.nether
import miragefairy2024.util.not
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.place
import miragefairy2024.util.plus
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.material.MapColor

object MirageFlowerCard : AbstractMirageFlowerCard<MirageFlowerBlock>() {
    override fun getBlockPath() = "mirage_flower"
    override val blockName = EnJa("Mirage Flower", "妖花ミラージュ")
    override fun getItemPath() = "mirage_bulb"
    override val itemName = EnJa("Mirage Bulb", "妖花ミラージュの球根")
    override val tier = 1
    override val poem = EnJa("Evolution to escape extermination", "可憐にして人畜無害たる魔物。")

    override val blockCodec = MirageFlowerBlock.CODEC
    override fun createBlock() = MirageFlowerBlock(createCommonSettings().instabreak().mapColor(MapColor.DIAMOND).sound(SoundType.GLASS))

    override val drops = listOf(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.MIRAGE_LEAVES.item, MaterialCard.FAIRY_CRYSTAL.item)
    override fun getFruitDrops(count: Int, random: RandomSource) = getMirageFlour(count, random)
    override fun getLeafDrops(count: Int, random: RandomSource) = listOf(MaterialCard.MIRAGE_LEAVES.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.FAIRY_CRYSTAL.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0b00101000, // 中温適応
        TraitCard.MESIC_ADAPTATION.trait to 0b00101000, // 中湿適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0b00101000, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0b00101000, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.ETHER_RESPIRATION.trait to 0b00101000, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait to 0b00101000, // 光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.AIR_ADAPTATION.trait to 0b00101000, // 空間適応
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.PLANTS_WITH_SELF_AWARENESS.trait to 0b00101000, // 自我を持つ植物
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.MESIC_ADAPTATION.trait to 0.05, // 中湿適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0.05, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.SEEDS_DILUTION.trait to 0.05, // 種子希釈
        TraitCard.NATURAL_ABSCISSION.trait to 0.05, // 自然落果
        TraitCard.ETHER_RESPIRATION.trait to 0.05, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.AIR_ADAPTATION.trait to 0.05, // 空間適応
        TraitCard.FAIRY_BLESSING.trait to 0.05, // 妖精の祝福
        TraitCard.FOUR_LEAFED.trait to 0.05, // 四つ葉
        TraitCard.SPINY_LEAVES.trait to 0.05, // 棘のある葉
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.PROSPERITY_OF_SPECIES.trait to 0.05, // 種の繁栄
        TraitCard.PLANTS_WITH_SELF_AWARENESS.trait to 0.05, // 自我を持つ植物
    )

    private val FAIRY_RING_FEATURE = FairyRingFeature(FairyRingFeatureConfig.CODEC)

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("A World Ruled by Plants", "植物の支配する世界"),
        //name = EnJa("Terraformer of the Fantasy World", "幻想世界のテラフォーマー"), // TODO どこかで使う
        description = EnJa("Find the Mirage flower", "妖花ミラージュを摘もう"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Registration(BuiltInRegistries.FEATURE, MirageFairy2024.identifier("fairy_ring")) { FAIRY_RING_FEATURE }.register() // Fairy Ring
        Feature.FLOWER.generator(blockIdentifier) {
            registerConfiguredFeature("cluster", { RandomPatchConfiguration(6, 6, 2, placer) }).generator { // 小さな塊
                registerPlacedFeature("cluster", { per(16) + flower(square, surface) }).place { (overworld + end * !+Biomes.THE_END) * defaultTraits }  // 地上・エンド外縁の島々に通常クラスタ
                registerPlacedFeature("nether_cluster", { per(64) + flower(square, nether) }).place { nether * defaultTraits } // ネザーにネザー用クラスタ
                registerPlacedFeature("fairy_forest_cluster", { count(4) + flower(square, surface) }).place { (+FairyForestBiomeCard.key + +DeepFairyForestBiomeCard.key) * defaultTraits } // 妖精の森
            }
        }
        FAIRY_RING_FEATURE.generator(blockIdentifier) {
            registerConfiguredFeature("fairy_ring", { FairyRingFeatureConfig(100, 6F, 8F, 3, placer) }).generator { // Fairy Ring
                registerPlacedFeature("fairy_ring", { per(600) + flower(center, surface) }).place { overworld * defaultTraits }  // 地上にFairy Ring
            }
        }
    }
}

class MirageFlowerBlock(settings: Properties) : SimpleMagicPlantBlock(MirageFlowerCard, settings) {
    companion object {
        val CODEC: MapCodec<MirageFlowerBlock> = simpleCodec(::MirageFlowerBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
