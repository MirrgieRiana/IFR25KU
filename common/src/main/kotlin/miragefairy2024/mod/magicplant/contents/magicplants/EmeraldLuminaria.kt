package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.count
import miragefairy2024.util.createItemStack
import miragefairy2024.util.defaultTraits
import miragefairy2024.util.flower
import miragefairy2024.util.getOr
import miragefairy2024.util.per
import miragefairy2024.util.plus
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.underground
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Items
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.material.MapColor

object EmeraldLuminariaCard : AbstractLuminariaCard<EmeraldLuminariaBlock>() {
    override fun getBlockPath() = "emerald_luminaria"
    override val blockName = EnJa("Emerald Luminaria", "翠玉輝草エメラルドルミナリア")
    override fun getItemPath() = "emerald_luminaria_bulb"
    override val itemName = EnJa("Emerald Luminaria Bulb", "翠玉輝草エメラルドルミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Makes Berryllium by unknown means", "幸福もたらす、栄光の樹。")

    override val blockCodec = EmeraldLuminariaBlock.CODEC
    override fun createBlock() = EmeraldLuminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.EMERALD).sound(SoundType.CROP))

    override val drops = listOf(MaterialCard.LUMINITE.item, { Items.EMERALD })
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.LUMINITE.item().createItemStack(count))
    override fun getSpecialDrops(count: Int, random: RandomSource) = listOf(Items.EMERALD.createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0b00101000, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.HUMID_ADAPTATION.trait to 0b00101000, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.ETERNAL_TREASURE.trait to 0b00101000, // 悠久の秘宝
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.HUMID_ADAPTATION.trait to 0.05, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.NODED_STEM.trait to 0.05, // 節状の茎
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.ETERNAL_TREASURE.trait to 0.05, // 悠久の秘宝
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DiamondLuminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Money Tree", "お金のなる木"),
        description = EnJa("Search for Emerald Luminaria in a plant-rich biome", "植物の繁茂するバイオームでエメラルドルミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER.generator(blockIdentifier) {
            registerConfiguredFeature("cluster", { RandomPatchConfiguration(1, 0, 0, placer) }) {
                registerPlacedFeature("cluster", { per(128) + flower(square, surface) }) { (+ConventionalBiomeTags.IS_JUNGLE + +ConventionalBiomeTags.IS_SWAMP) * defaultTraits }  // 地上
                registerPlacedFeature("underground_cluster", { count(16) + flower(square, underground) }) { +Biomes.LUSH_CAVES * defaultTraits } // 地下
            }
        }
    }
}

class EmeraldLuminariaBlock(settings: Properties) : SimpleMagicPlantBlock(EmeraldLuminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<EmeraldLuminariaBlock> = simpleCodec(::EmeraldLuminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
