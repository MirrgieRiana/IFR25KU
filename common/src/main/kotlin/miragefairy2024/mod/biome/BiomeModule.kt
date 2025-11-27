package miragefairy2024.mod.biome

import com.mojang.datafixers.util.Pair
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.EnJa
import miragefairy2024.util.enJa
import miragefairy2024.util.generator
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerDynamicGeneration
import miragefairy2024.util.toBiomeTag
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
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

val FAIRY_BIOME_TAG = MirageFairy2024.identifier("fairy").toBiomeTag()

context(ModContext)
fun initBiomeModule() {
    FAIRY_BIOME_TAG.enJa(EnJa("Fairy", "妖精"))
    BiomeCards.entries.forEach { card ->

        // バイオームの生成
        registerDynamicGeneration(card.key) {
            card.createBiome(lookup(Registries.PLACED_FEATURE), lookup(Registries.CONFIGURED_CARVER))
        }

        // このバイオームをタグに登録
        card.tags.forEach { tag ->
            tag.generator.registerChild(card.identifier)
        }

        // 翻訳生成
        card.translation.enJa()

        card.init()
    }
    ModEvents.onTerraBlenderInitialized {

        // 地上世界用の共通RegionをTerraBlenderに登録
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
}

context(ModContext)
fun registerOverworldSurfaceRules(namespace: String, rulesCreator: () -> SurfaceRules.RuleSource) {
    ModEvents.onTerraBlenderInitialized {
        val rule = rulesCreator()
        SurfaceRuleManager.addSurfaceRules(SurfaceRuleManager.RuleCategory.OVERWORLD, namespace, rule)
    }
}
