package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
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
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.material.MapColor

object DiamondLuminariaCard : AbstractLuminariaCard<DiamondLuminariaBlock>() {
    override fun getBlockPath() = "diamond_luminaria"
    override val blockName = EnJa("Diamond Luminaria", "金剛石輝草ダイヤモンドルミナリア")
    override fun getItemPath() = "diamond_luminaria_bulb"
    override val itemName = EnJa("Diamond Luminaria Bulb", "金剛石輝草ダイヤモンドルミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Fruits the crystallized carbon", "表土を飾る、凍てつく星。")

    override val blockCodec = DiamondLuminariaBlock.CODEC
    override fun createBlock() = DiamondLuminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getWeakLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.DIAMOND).sound(SoundType.CROP))

    override val drops = listOf(MaterialCard.LUMINITE.item, { Items.DIAMOND })
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.LUMINITE.item().createItemStack(count))
    override fun getSpecialDrops(count: Int, random: RandomSource) = listOf(Items.DIAMOND.createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.COLD_ADAPTATION.trait to 0b00101000, // 低温適応
        TraitCard.MESIC_ADAPTATION.trait to 0b00101000, // 中湿適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.ETERNAL_TREASURE.trait to 0b00101000, // 悠久の秘宝
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.COLD_ADAPTATION.trait to 0.05, // 低温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.MESIC_ADAPTATION.trait to 0.05, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait to 0.05, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.SEEDS_DILUTION.trait to 0.05, // 種子希釈
        TraitCard.NATURAL_ABSCISSION.trait to 0.05, // 自然落果
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.SPINY_LEAVES.trait to 0.05, // 棘のある葉
        TraitCard.HEATING_MECHANISM.trait to 0.05, // 発熱機構
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.ETERNAL_TREASURE.trait to 0.05, // 悠久の秘宝
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Even Carbon Freezes", "炭素も凍る季節"),
        description = EnJa("Search for Diamond Luminaria in a cold biome", "寒冷バイオームでダイヤモンドルミナリアを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER {
            configuredFeature("cluster", { RandomPatchConfiguration(1, 0, 0, placer) }) {
                placedFeature("cluster", { per(128) + flower(square, surface) }) { (+ConventionalBiomeTags.IS_SNOWY + +ConventionalBiomeTags.IS_ICY) * defaultTraits }
            }
        }
    }
}

class DiamondLuminariaBlock(settings: Properties) : SimpleMagicPlantBlock(DiamondLuminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<DiamondLuminariaBlock> = simpleCodec(::DiamondLuminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
