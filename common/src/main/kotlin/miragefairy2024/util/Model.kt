package miragefairy2024.util

import com.google.gson.JsonElement
import miragefairy2024.DataGenerationEvents
import miragefairy2024.ModContext
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import net.minecraft.data.models.model.ModelTemplate
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureMapping
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import java.util.Optional
import java.util.function.BiConsumer
import java.util.function.Supplier


// Model Builder

fun Model(creator: (TextureMapping) -> ModelData): ModelTemplate = object : ModelTemplate(Optional.empty(), Optional.empty()) {
    override fun create(id: ResourceLocation, textures: TextureMapping, modelCollector: BiConsumer<ResourceLocation, Supplier<JsonElement>>): ResourceLocation {
        modelCollector.accept(id) { creator(textures).toJsonElement() }
        return id
    }
}

fun Model(parent: ResourceLocation, vararg textureKeys: TextureSlot) = ModelTemplate(Optional.of(parent), Optional.empty(), *textureKeys)

fun Model(parent: ResourceLocation, variant: String, vararg textureKeys: TextureSlot) = ModelTemplate(Optional.of(parent), Optional.of(variant), *textureKeys)

class ModelData(
    val parent: ResourceLocation,
    val textures: ModelTexturesData? = null,
    val elements: ModelElementsData? = null,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "parent" to parent.string.jsonElement,
        "textures" to textures?.toJsonElement(),
        "elements" to elements?.toJsonElement(),
    )
}

class ModelTexturesData(val textures: List<Pair<String, String>>) {
    fun toJsonElement(): JsonElement = textures.map { it.first to it.second.jsonElement }.jsonObject
}

fun ModelTexturesData(vararg textures: Pair<String, String>) = ModelTexturesData(textures.toList())

class ModelElementsData(val elements: List<ModelElementData>) {
    fun toJsonElement(): JsonElement = elements.map { it.toJsonElement() }.jsonArray
}

fun ModelElementsData(vararg elements: ModelElementData) = ModelElementsData(elements.toList())

class ModelElementData(
    val from: List<Number>,
    val to: List<Number>,
    val faces: ModelFacesData,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "from" to from.map { it.jsonElement }.jsonArray,
        "to" to to.map { it.jsonElement }.jsonArray,
        "faces" to faces.toJsonElement(),
    )
}

class ModelFacesData(
    val down: ModelFaceData? = null,
    val up: ModelFaceData? = null,
    val north: ModelFaceData? = null,
    val south: ModelFaceData? = null,
    val west: ModelFaceData? = null,
    val east: ModelFaceData? = null,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "down" to down?.toJsonElement(),
        "up" to up?.toJsonElement(),
        "north" to north?.toJsonElement(),
        "south" to south?.toJsonElement(),
        "west" to west?.toJsonElement(),
        "east" to east?.toJsonElement(),
    )
}

class ModelFaceData(
    val uv: List<Number>? = null,
    val rotation: Int? = null,
    val texture: String,
    val tintindex: Int? = null,
    val cullface: String? = null,
) {
    fun toJsonElement(): JsonElement = jsonObjectNotNull(
        "uv" to uv?.map { it.jsonElement }?.jsonArray,
        "rotation" to rotation?.jsonElement,
        "texture" to texture.jsonElement,
        "tintindex" to tintindex?.jsonElement,
        "cullface" to cullface?.jsonElement,
    )
}


// Util

fun TextureMapping(vararg entries: Pair<TextureSlot, ResourceLocation>, initializer: TextureMapping.() -> Unit = {}): TextureMapping {
    val textureMapping = TextureMapping()
    entries.forEach {
        textureMapping.put(it.first, it.second)
    }
    initializer(textureMapping)
    return textureMapping
}

val TextureSlot.string get() = this.toString()

infix fun ModelTemplate.with(textureMapping: TextureMapping): TexturedModel = TexturedModel.createDefault({ textureMapping }, this).get(Blocks.AIR)
fun ModelTemplate.with(vararg textureEntries: Pair<TextureSlot, ResourceLocation>) = this with TextureMapping(*textureEntries)


// registerModelGeneration

context(ModContext)
fun registerModelGeneration(identifierGetter: () -> ResourceLocation, texturedModelCreator: () -> TexturedModel) = DataGenerationEvents.onGenerateBlockModel {
    val texturedModel = texturedModelCreator()
    texturedModel.template.create(identifierGetter(), texturedModel.mapping, it.modelOutput)
}

context(ModContext)
@JvmName("registerItemModelGeneration")
fun (() -> Item).registerModelGeneration(texturedModelCreator: () -> TexturedModel) = registerModelGeneration({ "item/" * this().getIdentifier() }) { texturedModelCreator() }

context(ModContext)
@JvmName("registerItemModelGeneration")
fun (() -> Item).registerModelGeneration(model: ModelTemplate, textureMappingCreator: () -> TextureMapping = { TextureMapping.layer0(this()) }) = this.registerModelGeneration { model with textureMappingCreator() }

context(ModContext)
@JvmName("registerItemGeneratedModelGeneration")
fun (() -> Item).registerGeneratedModelGeneration() = this.registerModelGeneration(ModelTemplates.FLAT_ITEM)

context(ModContext)
@JvmName("registerItemBlockGeneratedModelGeneration")
fun (() -> Item).registerBlockGeneratedModelGeneration(block: () -> Block) = this.registerModelGeneration(ModelTemplates.FLAT_ITEM) { TextureMapping.layer0(block()) }

context(ModContext)
@JvmName("registerBlockModelGeneration")
fun (() -> Block).registerModelGeneration(texturedModelFactory: TexturedModel.Provider) = registerModelGeneration({ "block/" * this().getIdentifier() }) { texturedModelFactory.get(this()) }
