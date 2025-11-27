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
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.biome.Climate
import terrablender.api.Region
import terrablender.api.RegionType
import terrablender.api.Regions
import java.util.function.Consumer

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
                    it.replaceBiome(Biomes.FOREST, FairyForestBiomeCard.key)
                    it.replaceBiome(Biomes.WINDSWEPT_FOREST, FairyForestBiomeCard.key)
                    it.replaceBiome(Biomes.FLOWER_FOREST, FairyForestBiomeCard.key)
                    it.replaceBiome(Biomes.BIRCH_FOREST, FairyForestBiomeCard.key)
                    it.replaceBiome(Biomes.OLD_GROWTH_BIRCH_FOREST, FairyForestBiomeCard.key)
                    it.replaceBiome(Biomes.DARK_FOREST, FairyForestBiomeCard.key)

                    it.replaceBiome(Biomes.TAIGA, DeepFairyForestBiomeCard.key)
                    it.replaceBiome(Biomes.OLD_GROWTH_PINE_TAIGA, DeepFairyForestBiomeCard.key)
                    it.replaceBiome(Biomes.OLD_GROWTH_SPRUCE_TAIGA, DeepFairyForestBiomeCard.key)
                    it.replaceBiome(Biomes.SNOWY_TAIGA, DeepFairyForestBiomeCard.key)

                    it.replaceBiome(Biomes.SAVANNA, RetrospectiveCityBiomeCard.key)
                    it.replaceBiome(Biomes.SAVANNA_PLATEAU, RetrospectiveCityBiomeCard.key)
                    it.replaceBiome(Biomes.WINDSWEPT_SAVANNA, RetrospectiveCityBiomeCard.key)
                    it.replaceBiome(Biomes.BADLANDS, RetrospectiveCityBiomeCard.key)
                    it.replaceBiome(Biomes.ERODED_BADLANDS, RetrospectiveCityBiomeCard.key)
                    it.replaceBiome(Biomes.WOODED_BADLANDS, RetrospectiveCityBiomeCard.key)
                }
            }
        })

    }
}
