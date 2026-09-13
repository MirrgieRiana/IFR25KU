package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.defaultTraits
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.getOr
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
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

object TopazLuminariaCard : AbstractLuminariaCard<TopazLuminariaBlock>() {
    override fun getBlockPath() = "topaz_luminaria"
    override val blockName = EnJa("Topaz Luminaria", "黄玉輝草トパーズルミナリア")
    override fun getItemPath() = "topaz_luminaria_bulb"
    override val itemName = EnJa("Topaz Luminaria Bulb", "黄玉輝草トパーズルミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Applicability to phytoremediation", "猛々しく、砂漠の神秘。")

    override val blockCodec = TopazLuminariaBlock.CODEC
    override fun createBlock() = TopazLuminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.TERRACOTTA_WHITE).sound(SoundType.CROP))

    override val drops = listOf(MaterialCard.LUMINITE.item, MaterialCard.TOPAZ.item)
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.LUMINITE.item().createItemStack(count))
    override fun getSpecialDrops(count: Int, random: RandomSource) = listOf(MaterialCard.TOPAZ.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0b00101000, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.DESERT_GEM.trait to 0b00101000, // 砂漠の宝石
        TraitCard.ETERNAL_TREASURE.trait to 0b00101000, // 悠久の秘宝
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.COLD_ADAPTATION.trait to 0.05, // 低温適応
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.DESERT_GEM.trait to 0.05, // 砂漠の宝石
        TraitCard.ETERNAL_TREASURE.trait to 0.05, // 悠久の秘宝
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DiamondLuminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Golden Dust", "黄金の塵"),
        description = EnJa("Search for Topaz Luminaria in the desert", "砂漠でトパーズルミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER.generator(blockIdentifier) {
            registerConfiguredFeature("cluster") { RandomPatchConfiguration(1, 0, 0, placer) }.generator {
                registerPlacedFeature("cluster") { per(128) + flower(square, surface) }.placeWhenVegetalDecoration { +ConventionalBiomeTags.IS_DESERT * defaultTraits }
            }
        }
    }
}

class TopazLuminariaBlock(settings: Properties) : SimpleMagicPlantBlock(TopazLuminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<TopazLuminariaBlock> = simpleCodec(::TopazLuminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
