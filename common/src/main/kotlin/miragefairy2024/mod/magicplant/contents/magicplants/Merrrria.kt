package miragefairy2024.mod.magicplant.contents.magicplants

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.magicplant.contents.TraitCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.rootAdvancement
import miragefairy2024.util.AdvancementCard
import miragefairy2024.util.AdvancementCardType
import miragefairy2024.util.EnJa
import miragefairy2024.util.center
import miragefairy2024.util.createCuboidShape
import miragefairy2024.util.createItemStack
import miragefairy2024.util.defaultTraits
import miragefairy2024.util.flower
import miragefairy2024.util.get
import miragefairy2024.util.per
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration
import net.minecraft.world.level.material.MapColor
import kotlin.math.pow

object MerrrriaCard : SimpleMagicPlantCard<MerrrriaBlock>() {
    override fun getBlockPath() = "merrrria"
    override val blockName = EnJa("Merrrria", "月鈴花メルルルリア")
    override fun getItemPath() = "merrrria_bulb"
    override val itemName = EnJa("Merrrria Bulb", "月鈴花メルルルリアの球根")
    override val tier = 3
    override val poem = EnJa("Windswept concert.", "風の知らせる演奏会。")
    override val classification = EnJa("Order Miragales, family Merrrriaceae", "妖花目メルルルリア科")

    override val ageProperty: IntegerProperty = BlockStateProperties.AGE_4
    override val blockCodec = MerrrriaBlock.CODEC
    override fun createBlock() = MerrrriaBlock(createCommonSettings().instabreak().mapColor(MapColor.ICE).lightLevel { if (it[ageProperty] == 4) 6 else 0 }.emissiveRendering { it, _, _ -> it[ageProperty] == 4 }.sound(SoundType.AMETHYST))

    override val outlineShapes = listOf(
        createCuboidShape(3.0, 7.0),
        createCuboidShape(4.0, 11.0),
        createCuboidShape(5.0, 13.0),
        createCuboidShape(7.0, 16.0),
        createCuboidShape(7.0, 16.0),
    )

    override val drops = listOf(MaterialCard.MERRRRIA_DROP.item)

    override fun getRareDrops(count: Int, random: RandomSource) = listOf(MaterialCard.MERRRRIA_DROP.item().createItemStack(count))

    override val family = MirageFairy2024.identifier("merrrria")
    override val defaultTraitBits = mapOf(
        TraitCard.COLD_ADAPTATION.trait to 0b00101000, // 低温適応
        TraitCard.MESIC_ADAPTATION.trait to 0b00101000, // 中湿適応
        TraitCard.SEEDS_PRODUCTION.trait to 0b00101000, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0b00101000, // 希少品生成
        TraitCard.PHOTOSYNTHESIS.trait to 0b00101000, // 光合成
        TraitCard.SELENOSYNTHESIS.trait to 0b00101000, // 月光合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0b00101000, // 養分吸収
        TraitCard.ADVERSITY_FLOWER.trait to 0b00101000, // 高嶺の花
    )
    override val randomTraitChances = mapOf(
        TraitCard.COLD_ADAPTATION.trait to 0.05, // 低温適応
        TraitCard.WARM_ADAPTATION.trait to 0.05, // 中温適応
        TraitCard.MESIC_ADAPTATION.trait to 0.05, // 中湿適応
        TraitCard.SEEDS_PRODUCTION.trait to 0.05, // 種子生成
        TraitCard.RARE_PRODUCTION.trait to 0.05, // 希少品生成
        TraitCard.CROSSBREEDING.trait to 0.05, // 交雑
        TraitCard.MUTATION.trait to 0.05, // 突然変異
        TraitCard.SEEDS_DILUTION.trait to 0.05, // 種子希釈
        TraitCard.NATURAL_ABSCISSION.trait to 0.05, // 自然落果
        TraitCard.PHOTOSYNTHESIS.trait to 0.05, // 光合成
        TraitCard.SELENOSYNTHESIS.trait to 0.05, // 月光合成
        TraitCard.PHAEOSYNTHESIS.trait to 0.05, // 闇合成
        TraitCard.OSMOTIC_ABSORPTION.trait to 0.05, // 養分吸収
        TraitCard.NODED_STEM.trait to 0.05, // 節状の茎
        TraitCard.ADVERSITY_FLOWER.trait to 0.05, // 高嶺の花
    )

    override fun createAdvancement(identifier: ResourceLocation) = AdvancementCard(
        identifier = identifier,
        context = AdvancementCard.Sub { rootAdvancement.await() },
        icon = { iconItem().createItemStack() },
        name = EnJa("Hill Where a Moon Blooms", "月の咲く丘"),
        description = EnJa("Search for Merrrria flowers blooming in the windswept", "吹きさらしの丘に咲くメルルルリアの花を探す"),
        criterion = AdvancementCard.hasItem { item() },
        type = AdvancementCardType.NORMAL,
    )

    context(ModContext)
    override fun init() {
        super.init()
        Feature.FLOWER.generator(blockIdentifier) {
            registerConfiguredFeature("cluster", { RandomPatchConfiguration(1, 0, 0, placer) }) {
                registerPlacedFeature("cluster", { per(8) + flower(square, surface) }) { +ConventionalBiomeTags.IS_WINDSWEPT * defaultTraits }
            }
            registerConfiguredFeature("large_cluster", { RandomPatchConfiguration(40, 8, 3, placer) }) {
                registerPlacedFeature("large_cluster", { per(64) + flower(center, surface) }) { +ConventionalBiomeTags.IS_WINDSWEPT * defaultTraits }
            }
        }
    }
}

class MerrrriaBlock(settings: Properties) : SimpleMagicPlantBlock(MerrrriaCard, settings) {
    companion object {
        val CODEC: MapCodec<MerrrriaBlock> = simpleCodec(::MerrrriaBlock)
        private val PITCHES = listOf(
            listOf(0, 2, 4, 5, 7, 9, 11).map { it + 12 * 0 },
            listOf(0, 2, 4, 5, 7, 9, 11).map { it + 12 * 1 },
            listOf(0).map { it + 12 * 2 },
        ).flatten().map { 2F.pow(it / 12F) }
    }

    override fun codec() = CODEC

    override fun getAgeProperty(): IntegerProperty = BlockStateProperties.AGE_4

    override fun canGrow(blockState: BlockState) = getAge(blockState) < 3 // 0→3までは自然成長

    override fun move(world: ServerLevel, blockPos: BlockPos, blockState: BlockState, speed: Double, autoPick: Boolean) {
        super.move(world, blockPos, blockState, speed, autoPick)

        // 3と4の間は昼と夜で繰り返す
        if (getAge(blockState) >= 3) {
            val newBlockState = withAge(if (world.isNight) 4 else 3)
            if (newBlockState != blockState) {
                world.setBlock(blockPos, newBlockState, UPDATE_CLIENTS)
            }
        }

    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, world, pos, random)
        if (getAge(state) == 4) {
            if (random.nextInt(50) == 0) {
                // クライアント側ではisNightが機能しないので夜だけ演奏はできない
                val pitch = PITCHES[random.nextInt(PITCHES.size)]
                world.playLocalSound(
                    pos.x.toDouble() + 0.5,
                    pos.y.toDouble() + 0.5,
                    pos.z.toDouble() + 0.5,
                    SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS,
                    0.25F,
                    0.3F * pitch,
                    false,
                )
                repeat(4) {
                    world.addParticle(
                        ParticleTypeCard.AURA.particleType,
                        pos.x.toDouble() + random.nextDouble(),
                        pos.y.toDouble() + random.nextDouble(),
                        pos.z.toDouble() + random.nextDouble(),
                        0.0,
                        0.1,
                        0.0,
                    )
                }
            }
        }
    }
}
