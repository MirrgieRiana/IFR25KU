package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
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

object GoldProminariaCard : AbstractProminariaCard<GoldProminariaBlock>() {
    override fun getBlockPath() = "gold_prominaria"
    override val blockName = EnJa("Gold Prominaria", "金炎草ゴールドプロミナリア")
    override fun getItemPath() = "gold_prominaria_bulb"
    override val itemName = EnJa("Gold Prominaria Bulb", "金炎草ゴールドプロミナリアの球根")
    override val tier = 3
    override val poem = EnJa("Those who have become livestock of gold.", "地獄の沙汰も金次第。")

    override val blockCodec = GoldProminariaBlock.CODEC
    override fun createBlock() = GoldProminariaBlock(createCommonSettings().strength(0.2F).lightLevel { getLuminance(it.getOr(BlockStateProperties.AGE_3) { 0 }) }.mapColor(MapColor.GOLD).sound(SoundType.CROP))

    override val baseGrowth = super.baseGrowth / 4
    override val baseSeedGeneration = 0.0

    override val drops = listOf(MaterialCard.GOLD_PROMINARIA_BERRY.item, MaterialCard.PROMINITE.item)
    override fun getFruitDrops(count: Int, random: RandomSource) = listOf(MaterialCard.GOLD_PROMINARIA_BERRY.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.PROMINITE.item().createItemStack(count))

    override val defaultTraitBits = super.defaultTraitBits + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0b00101000, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0b00101000, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0b00101000, // 果実生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.GOLDEN_APPLE.trait to 0b00101000, // 金のリンゴ
        TraitCard.ETHER_PREDATION.trait to 0b00101000, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait to 0b00101000, // アスファルトに咲く花
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.HOT_ADAPTATION.trait to 0.05, // 高温適応
        TraitCard.ARID_ADAPTATION.trait to 0.05, // 乾燥適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.CRYSTAL_ABSORPTION.trait to 0.05, // 鉱物吸収
        TraitCard.GOLDEN_APPLE.trait to 0.05, // 金のリンゴ
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.PAVEMENT_FLOWERS.trait to 0.05, // アスファルトに咲く花
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { ProminariaCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Gold Tree", "金のなる木"),
        description = EnJa("Use a gold ingot on the Prominaria", "プロミナリアに金インゴットを使用する"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.GOAL,
    )
}

class GoldProminariaBlock(settings: Properties) : SimpleMagicPlantBlock(GoldProminariaCard, settings) {
    companion object {
        val CODEC: MapCodec<GoldProminariaBlock> = simpleCodec(::GoldProminariaBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
