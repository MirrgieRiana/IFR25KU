package miragefairy2024.util

import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.platformProxy
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.BlockFamily
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.Property

/** レジストリに登録する前に呼び出すことはできません。 */
fun Block.getIdentifier() = BuiltInRegistries.BLOCK.getKey(this)

fun ResourceLocation.toBlock() = BuiltInRegistries.BLOCK.get(this)

context(ModContext)
fun (() -> Block).registerFlammable(burn: Int, spread: Int) = ModEvents.onInitialize {
    FlammableBlockRegistry.getDefaultInstance().add(this(), 30, 60)
}

operator fun <T : Comparable<T>> BlockState.get(property: Property<T>): T = this.getValue(property)

fun <T : Comparable<T>> BlockState.getOrNull(property: Property<T>): T? {
    val value = this.values[property] ?: return null
    return property.valueClass.cast(value)
}

fun <T : Comparable<T>> BlockState.getOr(property: Property<T>, default: () -> T) = this.getOrNull(property) ?: default()

infix fun BlockState.isIn(block: Block) = this.`is`(block)
infix fun BlockState.isNotIn(block: Block) = !(this isIn block)
infix fun BlockState.isIn(tag: TagKey<Block>) = this.`is`(tag)
infix fun BlockState.isNotIn(tag: TagKey<Block>) = !(this isIn tag)

context(ModContext)
fun registerBlockFamily(texturedModelProvider: TexturedModel.Provider, baseBlock: () -> Block, initializer: (BlockFamily.Builder) -> BlockFamily.Builder) {
    val family by lazy { initializer(BlockFamily.Builder(baseBlock())).family }
    DataGenerationEvents.onGenerateBlockModel {
        val texturedModel = texturedModelProvider[baseBlock()]
        val blockFamilyProvider = it.BlockFamilyProvider(texturedModel.mapping)
        platformProxy!!.setFullBlock(blockFamilyProvider, "block/" * baseBlock().getIdentifier())
        blockFamilyProvider.generateFor(family)
    }
    DataGenerationEvents.onGenerateRecipe {
        RecipeProvider.generateRecipes(it, family, FeatureFlagSet.of(FeatureFlags.VANILLA))
    }
}
