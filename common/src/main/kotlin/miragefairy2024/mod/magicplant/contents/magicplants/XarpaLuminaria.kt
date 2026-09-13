package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.structure.DripstoneCavesRuinCard
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.getOr
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.material.MapColor

object XarpaLuminariaCard : AbstractLuminariaCard<XarpaLuminariaBlock>() {
    override fun getBlockPath() = "xarpa_luminaria"
    override val blockName = EnJa("Xarpie Luminara", "紅天輝草シャルピエ・ルミナーラ")
    override fun getItemPath() = "xarpa_luminaria_bulb"
    override val itemName = EnJa("Xarpie Luminara Bulb", "紅天輝草シャルピエ・ルミナーラの球根")
    override val tier = 4
    override val poem = EnJa("The essence of a gene is matter.", "遺伝子のすべては、形而下に属する。")

    override val blockCodec = XarpaLuminariaBlock.CODEC
    override fun createBlock() = XarpaLuminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.TERRACOTTA_ORANGE).sound(SoundType.CROP))

    override val drops = listOf(MaterialCard.LUMINITE.item, MaterialCard.CALCULITE.item)
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.LUMINITE.item().createItemStack(count))
    override fun getSpecialDrops(count: Int, random: RandomSource) = listOf(MaterialCard.CALCULITE.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0b00101000, // 中温適応
        TraitCard.MESIC_ADAPTATION.trait to 0b00101000, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait to 0b00101000, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0b00101000, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0b00101000, // 交雑
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.TREASURE_OF_XARPA.trait to 0b00101000, // シャルパの秘宝
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.MESIC_ADAPTATION.trait to 0.05, // 中湿適応
        TraitCard.HUMID_ADAPTATION.trait to 0.05, // 湿潤適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.TREASURE_OF_XARPA.trait to 0.05, // シャルパの秘宝
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { DripstoneCavesRuinCard.advancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Destruction of \"Arcana\"", "「神秘」の破壊"),
        description = EnJa("Search for Xarpie Luminara in Dripstone Cave Ruin", "鍾乳洞の遺跡でシャルピエ・ルミナーラを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )
}

class XarpaLuminariaBlock(settings: Properties) : SimpleMagicPlantBlock(XarpaLuminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<XarpaLuminariaBlock> = simpleCodec(::XarpaLuminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
