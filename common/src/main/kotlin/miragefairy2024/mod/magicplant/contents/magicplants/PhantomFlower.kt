package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.ModContext
import miragefairy2024.mod.biome.FairyForestBiomeCard
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.materials.contents.LOCAL_VACUUM_DECAY_RESISTANT_BLOCK_TAG
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.createItemStack
import miragefairy2024.util.defaultTraits
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.RandomSource
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.material.MapColor

object PhantomFlowerCard : AbstractMirageFlowerCard<PhantomFlowerBlock>() {
    override fun getBlockPath() = "phantom_flower"
    override val blockName = EnJa("Phantom Flower", "幻花ファントム")
    override fun getItemPath() = "phantom_bulb"
    override val itemName = EnJa("Phantom Bulb", "幻花ファントムの球根")
    override val tier = 3
    override val poem = EnJa("Illusory telepathy", "――おいでよ、僕たちのところへ")

    override val blockCodec = PhantomFlowerBlock.CODEC
    override fun createBlock() = PhantomFlowerBlock(createCommonSettings().instabreak().mapColor(MapColor.COLOR_PINK).sound(SoundType.GLASS))

    override val baseGrowth = super.baseGrowth / 8.0
    override val baseFruitGeneration = super.baseFruitGeneration * 9.0

    override val drops = listOf(MaterialCard.MIRAGE_FLOUR.item, MaterialCard.PHANTOM_LEAVES.item, MaterialCard.PHANTOM_DROP.item)
    override fun getFruitDrops(count: Int, random: RandomSource) = getMirageFlour(count, random)
    override fun getLeafDrops(count: Int, random: RandomSource) = listOf(MaterialCard.PHANTOM_LEAVES.item().createItemStack(count))
    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.PHANTOM_DROP.item().createItemStack(count))

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
        TraitCard.PHANTOM_FLOWER.trait to 0b00101000, // 幻の花
        TraitCard.PLANTS_WITH_SELF_AWARENESS.trait to 0b00101000, // 自我を持つ植物
    )
    override val randomTraitChances = super.randomTraitChances + mapOf(
        TraitCard.COLD_ADAPTATION.trait to 0.05, // 低温適応
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.MESIC_ADAPTATION.trait to 0.05, // 中湿適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.FRUITS_PRODUCTION.trait to 0.05, // 果実生成
        TraitCard.LEAVES_PRODUCTION.trait to 0.05, // 葉面生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.EXPERIENCE_PRODUCTION.trait to 0.05, // 経験値生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.ETHER_RESPIRATION.trait to 0.05, // エーテル呼吸
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.PHAEOSYNTHESIS.trait to 0.05, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.AIR_ADAPTATION.trait to 0.05, // 空間適応
        TraitCard.FAIRY_BLESSING.trait to 0.05, // 妖精の祝福
        TraitCard.SPINY_LEAVES.trait to 0.05, // 棘のある葉
        TraitCard.ETHER_PREDATION.trait to 0.05, // エーテル捕食
        TraitCard.PHANTOM_FLOWER.trait to 0.05, // 幻の花
        TraitCard.PLANTS_WITH_SELF_AWARENESS.trait to 0.05, // 自我を持つ植物
        TraitCard.FLOWER_OF_THE_END.trait to 0.05, // 終焉の花
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { FairyForestBiomeCard.advancement!!.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Selective Pressure of Cuteness", "かわいいの淘汰圧"),
        description = EnJa("Search for the Phantom Flower hidden in the Fairy Forest", "妖精の森に隠れている幻花ファントムを探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.GOAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        LOCAL_VACUUM_DECAY_RESISTANT_BLOCK_TAG.generator.registerChild(block)
        Feature.FLOWER.generator(blockIdentifier) {
            registerConfiguredFeature("cluster", { RandomPatchConfiguration(6, 6, 2, placer) }).generator {
                registerPlacedFeature("cluster", { per(16) + flower(square, surface) }).placeWhenVegetalDecoration { +FairyForestBiomeCard.key * defaultTraits }
            }
        }
    }
}

class PhantomFlowerBlock(settings: Properties) : SimpleMagicPlantBlock(PhantomFlowerCard, settings) {
    companion object {
        val CODEC: MapCodec<PhantomFlowerBlock> = simpleCodec(::PhantomFlowerBlock)
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_3
}
