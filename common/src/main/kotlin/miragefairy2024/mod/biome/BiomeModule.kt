package miragefairy2024.mod.biome

import com.mojang.datafixers.util.Pair
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.EnJa
import miragefairy2024.util.SURFACE_NOISE_STANDARD_DEVIATIONS
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.invoke
import miragefairy2024.util.registerServerDebugItem
import miragefairy2024.util.text
import miragefairy2024.util.toBiomeTag
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.toTextureSource
import mirrg.kotlin.helium.join
import mirrg.kotlin.hydrogen.formatAs
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.util.RandomSource
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Climate
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.SurfaceRules
import net.minecraft.world.level.levelgen.XoroshiroRandomSource
import net.minecraft.world.level.levelgen.synth.NormalNoise
import org.apache.logging.log4j.LogManager.getLogger
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import terrablender.api.SurfaceRuleManager
import java.util.function.Consumer
import kotlin.math.sqrt

val OVERWORLD_BIOME_OVERRIDES = mutableMapOf<ResourceKey<Biome>, ResourceKey<Biome>>()

context(ModContext, BiomeCard)
fun registerOverworldBiomeOverride(biome: ResourceKey<Biome>) {
    OVERWORLD_BIOME_OVERRIDES[biome] = key
}

val FAIRY_BIOME_TAG = MirageFairy2024.identifier("fairy").toBiomeTag()
val RETROSPECTIVE_CITY_BUILDING_BLOCK_TAG = MirageFairy2024.identifier("retrospective_city_building").toBlockTag()
val RETROSPECTIVE_CITY_FLOOR_BLOCK_TAG = MirageFairy2024.identifier("retrospective_city_floor").toBlockTag()

context(ModContext)
fun initBiomeModule() {

    // 地上世界用の共通RegionをTerraBlenderに登録
    ModEvents.onTerraBlenderInitialized {
        Regions.register(object : Region(MirageFairy2024.identifier("overworld"), RegionType.OVERWORLD, 1) {
            override fun addBiomes(registry: Registry<Biome>, mapper: Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>>) {
                addModifiedVanillaOverworldBiomes(mapper) {
                    OVERWORLD_BIOME_OVERRIDES.forEach { (old, new) ->
                        it.replaceBiome(old, new)
                    }
                }
            }
        })
    }

    FAIRY_BIOME_TAG.enJa(EnJa("Fairy", "妖精"))
    RETROSPECTIVE_CITY_BUILDING_BLOCK_TAG.enJa(EnJa("Retrospective City Building", "過去を見つめる都市の建物"))
    RETROSPECTIVE_CITY_FLOOR_BLOCK_TAG.enJa(EnJa("Retrospective City Floor", "過去を見つめる都市の床"))

    BirdNestFeatureCard.init()
    ElevatedSpawnerFeatureCard.init()
    MiragidianLampFeatureCard.init()
    RetrospectiveCityGazingWallFeatureCard.init()
    RetrospectiveCitySmallRuinFeatureCard.init()
    RetrospectiveCityTinyRuinFeatureCard.init()
    XarperiaObeliskFeatureCard.init()

    FairyForestBiomeCard.init()
    DeepFairyForestBiomeCard.init()
    RetrospectiveCityBiomeCard.init()
    OldGrowthAmberForestBiomeCard.init()

    // ランダムなシードとランダムな座標で、地表ルールで使う2種のノイズの標準偏差を実測するのだ～🌱
    // ノイズの平均は理論上厳密に0だから、標準偏差の導出に使う平均にも0を使うのだ✨
    // 平均に0を使うと分散が二乗和を個数で割ったものそのものになるから、標準偏差と個数だけで複数の結果を正しく集約できるのだ🌱
    registerServerDebugItem("debug_surface_noise_statistics", Blocks.COARSE_DIRT.toTextureSource(), 0xFFFFAA00.toInt()) { level, player, _, _ ->
        val random = RandomSource.create()
        SURFACE_NOISE_STANDARD_DEVIATIONS.forEach { (noiseKey, constantStandardDeviation) ->
            val noiseParameters = level.registryAccess()[Registries.NOISE, noiseKey].value()
            var count = 0L
            var squaredSum = 0.0
            repeat(1000) {
                val noise = NormalNoise.create(XoroshiroRandomSource(random.nextLong()), noiseParameters)
                repeat(1000) {
                    fun nextCoordinate() = random.nextIntBetweenInclusive(-1_000_000, 1_000_000).toDouble()
                    val value = noise.getValue(nextCoordinate(), 0.0, nextCoordinate())
                    count++
                    squaredSum += value * value
                }
            }
            val standardDeviation = sqrt(squaredSum / count.toDouble())
            run {
                val body = listOf(
                    "noise=${noiseKey.location()}",
                    "standardDeviation=${standardDeviation formatAs "%.6f"}",
                    "count=$count",
                    "constant=$constantStandardDeviation",
                    "ratio=${(standardDeviation / constantStandardDeviation) formatAs "%.4f"}",
                ).join(", ")
                player.displayClientMessage(text { body() }, false)
            }
            run {
                val body = listOf(
                    "noise=${noiseKey.location()}",
                    "standardDeviation=$standardDeviation",
                    "count=$count",
                    "constant=$constantStandardDeviation",
                    "ratio=${standardDeviation / constantStandardDeviation}",
                ).join(", ")
                getLogger(object {}).info(body)
            }
        }
    }

}

context(ModContext)
fun registerOverworldSurfaceRules(namespace: String, rulesCreator: () -> SurfaceRules.RuleSource) {
    ModEvents.onTerraBlenderInitialized {
        val rule = rulesCreator()
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, namespace, rule)
    }
}
