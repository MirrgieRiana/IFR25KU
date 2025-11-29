package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.lib.PlacedItemFeature
import miragefairy2024.mod.biome.FAIRY_BIOME_TAG
import miragefairy2024.mod.biome.RetrospectiveCityBiomeCard
import miragefairy2024.mod.biome.retrospectiveCityFloorPlacementModifiers
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.BiomeSelectorScope
import miragefairy2024.util.PlacementModifiersScope
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.overworld
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.surface
import miragefairy2024.util.unaryPlus
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.tags.BiomeTags
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import java.util.function.Predicate

val DEBRIS_FEATURE = DebrisFeature(DebrisFeature.Config.CODEC)

enum class DebrisCard(
    path: String,
    val perChunks: Int,
    val count: IntRange,
    val itemStackGetter: () -> ItemStack,
    val biomeSelectorCreator: BiomeSelectorScope.() -> Predicate<BiomeSelectionContext>,
    val extraPlacementModifier: PlacementModifiersScope.() -> List<PlacementModifier> = { emptyList() },
) {
    STICK("stick", 32, 2..6, { Items.STICK.createItemStack() }, { overworld }),
    STICK_DENSE("stick_dense", 32 / 8, 2..6, { Items.STICK.createItemStack() }, { +BiomeTags.IS_FOREST }),
    BONE("bone", 64, 2..6, { Items.BONE.createItemStack() }, { overworld }),
    STRING("string", 64, 2..6, { Items.STRING.createItemStack() }, { overworld }),
    FLINT("flint", 64, 2..6, { Items.FLINT.createItemStack() }, { overworld }),
    RAW_IRON("raw_iron", 128, 2..6, { Items.RAW_IRON.createItemStack() }, { overworld }),
    RAW_IRON_DENSE("raw_iron_dense", 128 / 2, 8..24, { Items.RAW_IRON.createItemStack() }, { +BiomeTags.IS_MOUNTAIN }),
    RAW_COPPER("raw_copper", 128, 2..6, { Items.RAW_COPPER.createItemStack() }, { overworld }),
    RAW_COPPER_DENSE("raw_copper_dense", 128 / 2, 8..24, { Items.RAW_COPPER.createItemStack() }, { +BiomeTags.IS_MOUNTAIN }),
    XARPITE("xarpite", 128, 2..6, { MaterialCard.XARPITE.item().createItemStack() }, { overworld }),
    FAIRY_SCALES("fairy_scales", 128, 2..6, { MaterialCard.FAIRY_SCALES.item().createItemStack() }, { overworld }),
    FAIRY_SCALES_DENSE("fairy_scales_dense", 128 / 2, 8..24, { MaterialCard.FAIRY_SCALES.item().createItemStack() }, { +FAIRY_BIOME_TAG }),
    MIRAGIDIAN_SHARD("miragidian_shard", 8, 1..4, { MaterialCard.MIRAGIDIAN_SHARD.item().createItemStack() }, { +RetrospectiveCityBiomeCard.key }),
    COBBLED_AURA_RESISTANT_CERAMIC("cobbled_aura_resistant_ceramic", 1, 8..24, { BlockMaterialCard.COBBLED_AURA_RESISTANT_CERAMIC.item().createItemStack() }, { +RetrospectiveCityBiomeCard.key }, extraPlacementModifier = { retrospectiveCityFloorPlacementModifiers }),
    AURA_RESISTANT_CERAMIC_BRICKS("aura_resistant_ceramic_bricks", 2, 4..12, { BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS.item().createItemStack() }, { +RetrospectiveCityBiomeCard.key }, extraPlacementModifier = { retrospectiveCityFloorPlacementModifiers }),
    ;

    val identifier = MirageFairy2024.identifier("${path}_debris")
}

// TODO rei
context(ModContext)
fun initDebrisModule() {

    Registration(BuiltInRegistries.FEATURE, MirageFairy2024.identifier("debris")) { DEBRIS_FEATURE }.register()

    DebrisCard.entries.forEach { card ->
        DEBRIS_FEATURE.generator(card.identifier) {
            registerConfiguredFeature { DebrisFeature.Config(UniformInt.of(card.count.first, card.count.last), card.itemStackGetter()) }.generator {
                registerPlacedFeature {
                    per(card.perChunks) + flower(square, surface) + card.extraPlacementModifier(this)
                }.placeWhenVegetalDecoration(card.biomeSelectorCreator)
            }
        }
    }

}

class DebrisFeature(codec: Codec<Config>) : PlacedItemFeature<DebrisFeature.Config>(codec) {
    class Config(val count: IntProvider, val itemStack: ItemStack) : FeatureConfiguration {
        companion object {
            val CODEC: Codec<Config> = RecordCodecBuilder.create { instance ->
                instance.group(
                    IntProvider.codec(1, 256).fieldOf("count").forGetter { it.count },
                    ItemStack.CODEC.fieldOf("item").forGetter { it.itemStack },
                ).apply(instance, ::Config)
            }
        }
    }

    override fun getCount(context: FeaturePlaceContext<Config>) = context.config().count.sample(context.random())
    override fun createItemStack(context: FeaturePlaceContext<Config>) = context.config().itemStack.copy()
}
