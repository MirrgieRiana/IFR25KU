package miragefairy2024.mod

import com.google.gson.JsonElement
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.aboveLava
import miragefairy2024.util.count
import miragefairy2024.util.enJa
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.nether
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerBlockStateGeneration
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerOreLootTableGeneration
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.square
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.with
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderSet
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.MultifaceBlock
import net.minecraft.world.level.block.MultifaceSpreader
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.MultifaceGrowthConfiguration
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

object DepositedSulfurCard {
    val identifier = MirageFairy2024.identifier("deposited_sulfur")
    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        DepositedSulfurBlock(
            UniformInt.of(2, 5),
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .replaceable()
                .noCollission()
                .requiresCorrectToolForDrops()
                .strength(0.2F)
                .sound(SoundType.NETHER_ORE)
                .pushReaction(PushReaction.DESTROY),
        )
    }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }
}

context(ModContext)
fun initDepositedSulfurModule() {

    Registration(BuiltInRegistries.BLOCK_TYPE, DepositedSulfurCard.identifier) { DepositedSulfurBlock.CODEC }.register()

    DepositedSulfurCard.block.register()
    DepositedSulfurCard.item.register()

    DepositedSulfurCard.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

    DepositedSulfurCard.block.registerBlockStateGeneration {
        val model = "${"block/" * DepositedSulfurCard.identifier}".jsonElement

        // どの面にも貼り付いていない状態を表すのだぁ🌱 本来そんな状態にはならないのだけど、バニラのヒカリゴケに倣って、そのときも全方向のモデルを出しておくのだぁ✨
        val vacantCondition = jsonObject(
            "north" to "false".jsonElement,
            "east" to "false".jsonElement,
            "south" to "false".jsonElement,
            "west" to "false".jsonElement,
            "up" to "false".jsonElement,
            "down" to "false".jsonElement,
        )

        fun createParts(direction: String, x: Int?, y: Int?): List<JsonElement> {
            val variant = jsonObjectNotNull(
                "model" to model,
                "x" to x?.jsonElement,
                "y" to y?.jsonElement,
                "uvlock" to if (x != null || y != null) true.jsonElement else null,
            )
            return listOf(
                jsonObject("when" to jsonObject(direction to "true".jsonElement), "apply" to variant),
                jsonObject("when" to vacantCondition, "apply" to variant),
            )
        }

        jsonObject(
            "multipart" to (
                createParts("north", null, null) +
                    createParts("east", null, 90) +
                    createParts("south", null, 180) +
                    createParts("west", null, 270) +
                    createParts("up", 270, null) +
                    createParts("down", 90, null)
                ).jsonArray,
        )
    }
    registerModelGeneration({ "block/" * DepositedSulfurCard.identifier }) {
        depositedSulfurBlockModel.with(TextureSlot.TEXTURE to "block/" * DepositedSulfurCard.identifier)
    }
    DepositedSulfurCard.item.registerBlockGeneratedModelGeneration(DepositedSulfurCard.block)
    DepositedSulfurCard.block.registerCutoutRenderLayer()

    DepositedSulfurCard.block.enJa(EnJa("Deposited Sulfur", "析出した硫黄"))

    DepositedSulfurCard.block.registerOreLootTableGeneration(MaterialCard.SULFUR.item)

    BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(DepositedSulfurCard.block)
    ConventionalBlockTags.ORES.generator.registerChild(DepositedSulfurCard.block)
    ConventionalItemTags.ORES.generator.registerChild(DepositedSulfurCard.item)

    Feature.MULTIFACE_GROWTH.generator(DepositedSulfurCard.identifier) {
        registerConfiguredFeature {
            // 引数は、貼り付けるブロック、探索範囲、床・天井・壁のそれぞれに貼れるか、隣へ広がる確率、貼り付け先のブロック、の順なのだぁ🌱
            MultifaceGrowthConfiguration(DepositedSulfurCard.block(), 4, true, true, true, 0.5F, HolderSet.direct(Block::builtInRegistryHolder, Blocks.BASALT))
        }.generator {
            registerPlacedFeature { count(32) + flower(square, aboveLava) }.placeWhenVegetalDecoration { nether }
        }
    }

}

/**
 * 岩肌にこびりつく硫黄なのだぁ🌱
 *
 * @see net.minecraft.world.level.block.GlowLichenBlock
 */
class DepositedSulfurBlock(private val xpRange: IntProvider, properties: Properties) : MultifaceBlock(properties) {
    companion object {
        val CODEC: MapCodec<DepositedSulfurBlock> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                IntProvider.codec(0, 10000).fieldOf("experience").forGetter { it.xpRange },
                propertiesCodec(),
            ).apply(instance, ::DepositedSulfurBlock)
        }
    }

    private val multifaceSpreader = MultifaceSpreader(this)

    override fun codec() = CODEC

    override fun getSpreader() = multifaceSpreader

    override fun spawnAfterBreak(state: BlockState, level: ServerLevel, pos: BlockPos, stack: ItemStack, dropExperience: Boolean) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience)
        if (dropExperience) {
            tryDropExperience(level, pos, stack, xpRange)
        }
    }
}

val depositedSulfurBlockModel = Model { textureMapping ->
    ModelData(
        parent = ResourceLocation("block/block"),
        textures = ModelTexturesData(
            TextureSlot.PARTICLE.id to textureMapping.get(TextureSlot.TEXTURE).string,
            TextureSlot.TEXTURE.id to textureMapping.get(TextureSlot.TEXTURE).string,
        ),
        elements = ModelElementsData(
            ModelElementData(
                from = listOf(0, 0, 0.1),
                to = listOf(16, 16, 0.1),
                faces = ModelFacesData(
                    north = ModelFaceData(uv = listOf(16, 0, 0, 16), texture = TextureSlot.TEXTURE.string),
                    south = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureSlot.TEXTURE.string),
                ),
            ),
        ),
    )
}
