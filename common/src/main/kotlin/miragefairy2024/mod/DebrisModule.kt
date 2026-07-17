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
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ColorPair
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.views.OutputSlotView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.XSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.util.BiomeSelectorScope
import miragefairy2024.util.EnJa
import miragefairy2024.util.PlacementModifiersScope
import miragefairy2024.util.Registration
import miragefairy2024.util.createItemStack
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.invoke
import miragefairy2024.util.per
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.register
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.string
import miragefairy2024.util.surface
import miragefairy2024.util.text
import miragefairy2024.util.translate
import miragefairy2024.util.unaryPlus
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext
import net.minecraft.core.RegistryAccess
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.BiomeTags
import net.minecraft.tags.TagKey
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration
import net.minecraft.world.level.levelgen.placement.PlacementModifier
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrElse

val DEBRIS_FEATURE = DebrisFeature(DebrisFeature.Config.CODEC)

sealed class DebrisBiomeCondition {
    abstract fun createBiomeSelector(): Predicate<BiomeSelectionContext>
    abstract fun getText(): Component
    abstract fun getTooltip(registryAccess: RegistryAccess): List<Component>

    class BiomeKey(val biome: ResourceKey<Biome>) : DebrisBiomeCondition() {
        override fun createBiomeSelector() = BiomeSelectorScope.run { +biome }
        override fun getText() = text { translate(biome.location().toLanguageKey("biome")) }
        override fun getTooltip(registryAccess: RegistryAccess) = listOf(text { biome.location().string() })
    }

    class BiomeTag(val biomeTag: TagKey<Biome>) : DebrisBiomeCondition() {
        override fun createBiomeSelector() = BiomeSelectorScope.run { +biomeTag }
        override fun getText() = text { biomeTag.location().path() }
        override fun getTooltip(registryAccess: RegistryAccess): List<Component> {
            val biomes = registryAccess.registryOrThrow(Registries.BIOME).getTag(biomeTag).getOrElse { listOf() }.map { it.unwrapKey().get() }.sortedBy { it.location() }
            val lines = biomes.take(10).map { text { it.location().string() } }.toMutableList()
            if (biomes.size > 10) lines += text { "..."() }
            return lines
        }
    }
}

enum class DebrisCard(
    path: String,
    val perChunks: Int,
    val count: IntRange,
    val itemStackGetter: () -> ItemStack,
    val biomeCondition: DebrisBiomeCondition,
    val extraPlacementModifier: PlacementModifiersScope.() -> List<PlacementModifier> = { emptyList() },
) {
    STICK("stick", 32, 2..6, { Items.STICK.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_OVERWORLD)),
    STICK_DENSE("stick_dense", 32 / 8, 2..6, { Items.STICK.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_FOREST)),
    BONE("bone", 64, 2..6, { Items.BONE.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_OVERWORLD)),
    STRING("string", 64, 2..6, { Items.STRING.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_OVERWORLD)),
    FLINT("flint", 64, 2..6, { Items.FLINT.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_OVERWORLD)),
    RAW_IRON("raw_iron", 128, 2..6, { Items.RAW_IRON.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_OVERWORLD)),
    RAW_IRON_DENSE("raw_iron_dense", 128 / 2, 8..24, { Items.RAW_IRON.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_MOUNTAIN)),
    RAW_COPPER("raw_copper", 128, 2..6, { Items.RAW_COPPER.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_OVERWORLD)),
    RAW_COPPER_DENSE("raw_copper_dense", 128 / 2, 8..24, { Items.RAW_COPPER.createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_MOUNTAIN)),
    XARPITE("xarpite", 128, 2..6, { MaterialCard.XARPITE.item().createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_OVERWORLD)),
    FAIRY_SCALES("fairy_scales", 128, 2..6, { MaterialCard.FAIRY_SCALES.item().createItemStack() }, DebrisBiomeCondition.BiomeTag(BiomeTags.IS_OVERWORLD)),
    FAIRY_SCALES_DENSE("fairy_scales_dense", 128 / 2, 8..24, { MaterialCard.FAIRY_SCALES.item().createItemStack() }, DebrisBiomeCondition.BiomeTag(FAIRY_BIOME_TAG)),

    RETROSPECTIVE_CITY_XARPITE("retrospective_city/xarpite", 8, 1..2, { MaterialCard.XARPITE.item().createItemStack() }, DebrisBiomeCondition.BiomeKey(RetrospectiveCityBiomeCard.key), extraPlacementModifier = { retrospectiveCityFloorPlacementModifiers }),
    RETROSPECTIVE_CITY_CHAOS_STONE("retrospective_city/chaos_stone", 8, 1..1, { MaterialCard.CHAOS_STONE.item().createItemStack() }, DebrisBiomeCondition.BiomeKey(RetrospectiveCityBiomeCard.key), extraPlacementModifier = { retrospectiveCityFloorPlacementModifiers }),
    RETROSPECTIVE_CITY_MIRAGIDIAN_SHARD("retrospective_city/miragidian_shard", 8, 1..4, { MaterialCard.MIRAGIDIAN_SHARD.item().createItemStack() }, DebrisBiomeCondition.BiomeKey(RetrospectiveCityBiomeCard.key), extraPlacementModifier = { retrospectiveCityFloorPlacementModifiers }),
    RETROSPECTIVE_CITY_COBBLED_AURA_RESISTANT_CERAMIC("retrospective_city/cobbled_aura_resistant_ceramic", 1, 8..24, { BlockMaterialCard.COBBLED_AURA_RESISTANT_CERAMIC.item().createItemStack() }, DebrisBiomeCondition.BiomeKey(RetrospectiveCityBiomeCard.key), extraPlacementModifier = { retrospectiveCityFloorPlacementModifiers }),
    RETROSPECTIVE_CITY_AURA_RESISTANT_CERAMIC_BRICKS("retrospective_city/aura_resistant_ceramic_bricks", 2, 4..12, { BlockMaterialCard.AURA_RESISTANT_CERAMIC_BRICKS.item().createItemStack() }, DebrisBiomeCondition.BiomeKey(RetrospectiveCityBiomeCard.key), extraPlacementModifier = { retrospectiveCityFloorPlacementModifiers }),
    ;

    val identifier = MirageFairy2024.identifier("${path}_debris")

    companion object {
        val CODEC: Codec<DebrisCard> = ResourceLocation.CODEC.xmap(
            { identifier -> DebrisCard.entries.first { it.identifier == identifier } },
            { card -> card.identifier },
        )
    }
}

context(ModContext)
fun initDebrisModule() {
    DebrisRecipeViewerCategoryCard.init()

    Registration(BuiltInRegistries.FEATURE, MirageFairy2024.identifier("debris")) { DEBRIS_FEATURE }.register()

    DebrisCard.entries.forEach { card ->
        DEBRIS_FEATURE.generator(card.identifier) {
            registerConfiguredFeature { DebrisFeature.Config(UniformInt.of(card.count.first, card.count.last), card.itemStackGetter()) }.generator {
                registerPlacedFeature {
                    per(card.perChunks) + flower(square, surface) + card.extraPlacementModifier(this)
                }.placeWhenVegetalDecoration { card.biomeCondition.createBiomeSelector() }
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

object DebrisRecipeViewerCategoryCard : RecipeViewerCategoryCard<DebrisCard>() {
    override fun getId() = MirageFairy2024.identifier("debris")
    override fun getName() = EnJa("Debris", "がれき")
    override fun getIcon() = MaterialCard.FAIRY_SCALES.item().createItemStack()
    override fun getRecipeCodec(registryAccess: RegistryAccess) = DebrisCard.CODEC
    override fun getOutputs(recipeEntry: RecipeEntry<DebrisCard>) = listOf(recipeEntry.recipe.itemStackGetter())

    override fun createRecipeEntries(registryAccess: RegistryAccess): Iterable<RecipeEntry<DebrisCard>> {
        return DebrisCard.entries.map { RecipeEntry(registryAccess, it.identifier, it, true) }
    }

    override fun createView(recipeEntry: RecipeEntry<DebrisCard>) = View {
        view += XListView().configure {
            view.sizingX = Sizing.FILL
            val condition = recipeEntry.recipe.biomeCondition
            view += TextView(condition.getText()).configure {
                position.alignmentY = Alignment.CENTER
                position.weight = 1.0
                view.sizingX = Sizing.FILL
                view.color = ColorPair.DARK_GRAY
                view.shadow = false
                view.scroll = true
                view.tooltip = condition.getTooltip(recipeEntry.registryAccess)
            }
            view += XSpaceView(2)
            view += OutputSlotView(recipeEntry.recipe.itemStackGetter())
        }
    }
}
