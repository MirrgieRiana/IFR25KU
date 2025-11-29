package miragefairy2024.mod.biome

import com.mojang.datafixers.util.Pair
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.toBiomeTag
import miragefairy2024.util.toBlockTag
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Climate
import net.minecraft.world.level.levelgen.SurfaceRules
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import terrablender.api.SurfaceRuleManager
import java.util.function.Consumer

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

    MiragidianLampFeatureCard.init()
    RetrospectiveCitySmallRuinFeatureCard.init()
    RetrospectiveCityTinyRuinFeatureCard.init()

    FairyForestBiomeCard.init()
    DeepFairyForestBiomeCard.init()
    RetrospectiveCityBiomeCard.init()

}

context(ModContext)
fun registerOverworldSurfaceRules(namespace: String, rulesCreator: () -> SurfaceRules.RuleSource) {
    ModEvents.onTerraBlenderInitialized {
        val rule = rulesCreator()
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, namespace, rule)
    }
}
