package miragefairy2024.util

import com.google.gson.JsonElement
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import mirrg.kotlin.helium.join
import mirrg.kotlin.helium.or
import net.minecraft.core.Direction
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.data.models.blockstates.BlockStateGenerator as BlockModelDefinitionGenerator // MIGRATION2612 ReplaceImport net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.Property


// registerBlockStateGeneration

context(_: ModContext)
fun (() -> Block).registerBlockStateGeneration(creator: () -> JsonElement) = DataGenerationEvents.onGenerateBlockModel {
    it.blockStateOutput.accept(object : BlockModelDefinitionGenerator {
        override fun get() = creator()
        override fun getBlock() = this@registerBlockStateGeneration()
    })
}

context(_: ModContext)
fun (() -> Block).registerVariantsBlockStateGeneration(entriesGetter: VariantsBlockStateGenerationRegistrationScope.() -> List<BlockStateVariantEntry>) = this.registerBlockStateGeneration {
    jsonObject(
        "variants" to jsonObject(
            *entriesGetter(VariantsBlockStateGenerationRegistrationScope)
                .map { (propertiesMap, modelId) ->
                    val propertiesString = propertiesMap
                        .sortedBy { it.keyName }
                        .map { "${it.keyName}=${it.valueName}" }
                        .join(",")
                    propertiesString to modelId
                }
                .sortedBy { (propertiesString, _) -> propertiesString }
                .map { (propertiesString, value) -> propertiesString to value.toJson() }
                .toTypedArray()
        )
    )
}

context(_: ModContext)
fun (() -> Block).registerSingletonBlockStateGeneration() = DataGenerationEvents.onGenerateBlockModel {
    it.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(this(), "block/" * this().getIdentifier()))
}


// BlockStateVariantEntry

data class BlockStateVariantEntry(val properties: List<PropertyEntry<*>>, val variant: BlockStateVariant)

infix fun List<PropertyEntry<*>>.with(variant: BlockStateVariant) = BlockStateVariantEntry(this, variant)


// BlockStateVariant

enum class BlockStateVariantRotation(val degrees: Int) {
    R0(0),
    R90(90),
    R180(180),
    R270(270),
}

class BlockStateVariant(
    private val parent: BlockStateVariant? = null,
    private val model: Identifier? = null,
    private val x: BlockStateVariantRotation? = null,
    private val y: BlockStateVariantRotation? = null,
    private val uvlock: Boolean? = null,
    private val weight: Int? = null,
) {
    fun getModel() = model.or { parent?.model }
    fun getX() = x.or { parent?.x }
    fun getY() = y.or { parent?.y }
    fun getUvlock() = uvlock.or { parent?.uvlock }
    fun getWeight() = weight.or { parent?.weight }
}

fun BlockStateVariant.with(
    model: Identifier? = null,
    x: BlockStateVariantRotation? = null,
    y: BlockStateVariantRotation? = null,
    uvlock: Boolean? = null,
    weight: Int? = null,
) = BlockStateVariant(
    parent = this,
    model = model ?: this.getModel(),
    x = x ?: this.getX(),
    y = y ?: this.getY(),
    uvlock = uvlock ?: this.getUvlock(),
    weight = weight ?: this.getWeight(),
)

fun BlockStateVariant.toJson(): JsonElement = jsonObjectNotNull(
    getModel()?.let { "model" to it.string.jsonElement },
    getX()?.let { "x" to it.degrees.jsonElement },
    getY()?.let { "y" to it.degrees.jsonElement },
    getUvlock()?.let { "uvlock" to it.jsonElement },
    getWeight()?.let { "weight" to it.jsonElement },
)


// PropertyEntry

class PropertyEntry<T : Comparable<T>>(val key: Property<T>, val value: T)

val PropertyEntry<*>.keyName: String get() = this.key.name
val <T : Comparable<T>> PropertyEntry<T>.valueName: String get() = this.key.getName(this.value)

infix fun <T : Comparable<T>> Property<T>.with(value: T) = PropertyEntry(this, value)

fun propertiesOf(vararg properties: PropertyEntry<*>) = listOf(*properties)


// VariantsBlockStateGeneration

object VariantsBlockStateGenerationRegistrationScope

context(_: VariantsBlockStateGenerationRegistrationScope)
fun normal(model: Identifier) = listOf(propertiesOf() with BlockStateVariant(model = model))

context(_: VariantsBlockStateGenerationRegistrationScope)
fun <T : Comparable<T>> List<BlockStateVariantEntry>.with(property: Property<T>, modelFunction: (Identifier, PropertyEntry<T>) -> Identifier): List<BlockStateVariantEntry> {
    return property.possibleValues.flatMap { value ->
        this.map { (properties, variant) ->
            val entry = property with value
            propertiesOf(*properties.toTypedArray(), entry) with variant.with(model = modelFunction(variant.getModel()!!, entry))
        }
    }
}

context(_: VariantsBlockStateGenerationRegistrationScope)
infix fun <T : Comparable<T>> List<BlockStateVariantEntry>.with(property: Property<T>): List<BlockStateVariantEntry> {
    return this.with(property) { model, entry -> model * "_${entry.keyName}${entry.valueName}" }
}

context(_: VariantsBlockStateGenerationRegistrationScope)
fun List<BlockStateVariantEntry>.withHorizontalRotation(property: Property<Direction>): List<BlockStateVariantEntry> {
    return property.possibleValues.flatMap { value ->
        this.map { (properties, variant) ->
            val entry = property with value
            val y = when (value) {
                Direction.NORTH -> BlockStateVariantRotation.R0
                Direction.EAST -> BlockStateVariantRotation.R90
                Direction.SOUTH -> BlockStateVariantRotation.R180
                Direction.WEST -> BlockStateVariantRotation.R270
                else -> BlockStateVariantRotation.R0
            }
            propertiesOf(*properties.toTypedArray(), entry) with variant.with(y = y)
        }
    }
}
