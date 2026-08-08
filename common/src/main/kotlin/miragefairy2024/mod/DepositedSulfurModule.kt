package miragefairy2024.mod

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.Registration
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.aboveLava
import miragefairy2024.util.count
import miragefairy2024.util.enJa
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.isIn
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
import miragefairy2024.util.times
import miragefairy2024.util.with
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
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
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction

object DepositedSulfurCard {
    val identifier = MirageFairy2024.identifier("deposited_sulfur")
    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        DepositedSulfurBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .replaceable()
                .noCollission()
                .requiresCorrectToolForDrops()
                .strength(0.2F)
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.DESTROY),
        )
    }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }
    val feature = DepositedSulfurFeature(NoneFeatureConfiguration.CODEC)
}

context(ModContext)
fun initDepositedSulfurModule() {

    Registration(BuiltInRegistries.BLOCK_TYPE, DepositedSulfurCard.identifier) { DepositedSulfurBlock.CODEC }.register()
    Registration(BuiltInRegistries.FEATURE, DepositedSulfurCard.identifier) { DepositedSulfurCard.feature }.register()

    DepositedSulfurCard.let { card ->

        card.block.register()
        card.item.register()

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerBlockStateGeneration {
            fun createParts(direction: String, x: Int?, y: Int?): List<JsonElement> {
                val variant = jsonObjectNotNull(
                    "model" to "${"block/" * card.identifier}".jsonElement,
                    "x" to x?.jsonElement,
                    "y" to y?.jsonElement,
                    "uvlock" to if (x != null || y != null) true.jsonElement else null,
                )
                return listOf(
                    jsonObject(
                        "when" to jsonObject(
                            direction to "true".jsonElement,
                        ),
                        "apply" to variant
                    ),
                    // どの面にも貼り付いていない状態を表すのだぁ🌱 本来そんな状態にはならないのだけど、バニラのヒカリゴケに倣って、そのときも全方向のモデルを出しておくのだぁ✨
                    jsonObject(
                        "when" to jsonObject(
                            "north" to "false".jsonElement,
                            "east" to "false".jsonElement,
                            "south" to "false".jsonElement,
                            "west" to "false".jsonElement,
                            "up" to "false".jsonElement,
                            "down" to "false".jsonElement,
                        ),
                        "apply" to variant
                    ),
                )
            }
            jsonObject(
                "multipart" to listOf(
                    createParts("north", null, null),
                    createParts("east", null, 90),
                    createParts("south", null, 180),
                    createParts("west", null, 270),
                    createParts("up", 270, null),
                    createParts("down", 90, null),
                ).flatten().jsonArray,
            )
        }
        card.block.registerModelGeneration {
            // バニラのヒカリゴケのモデルは、貼り付く平面のテクスチャを glow_lichen という名前のスロットで受け取るのだぁ🌱
            val textureSlot = TextureSlot.create("glow_lichen")
            Model(ResourceLocation("minecraft", "block/glow_lichen"), textureSlot, TextureSlot.PARTICLE).with(
                textureSlot to "block/" * card.identifier,
                TextureSlot.PARTICLE to "block/" * card.identifier,
            )
        }
        card.item.registerBlockGeneratedModelGeneration(card.block)
        card.block.registerCutoutRenderLayer()

        card.block.enJa(EnJa("Deposited Sulfur", "析出した硫黄"))

        card.block.registerOreLootTableGeneration(MaterialCard.SULFUR.item)

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(card.block)

        card.feature.generator(card.identifier) {
            registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
                registerPlacedFeature { count(32) + flower(square, aboveLava) }.placeWhenVegetalDecoration { nether }
            }
        }

    }

}

/**
 * 岩肌にこびりつく硫黄なのだぁ🌱
 *
 * @see net.minecraft.world.level.block.GlowLichenBlock
 */
class DepositedSulfurBlock(properties: Properties) : MultifaceBlock(properties) {
    companion object {
        val CODEC: MapCodec<DepositedSulfurBlock> = simpleCodec(::DepositedSulfurBlock)

        /** 破壊したときにドロップする経験値なのだぁ✨ ネザーの硫黄鉱石に揃えているのだぁ🌱 */
        private val XP_RANGE = UniformInt.of(2, 5)
    }

    private val multifaceSpreader = MultifaceSpreader(this)

    override fun codec() = CODEC

    override fun getSpreader() = multifaceSpreader

    override fun spawnAfterBreak(state: BlockState, level: ServerLevel, pos: BlockPos, stack: ItemStack, dropExperience: Boolean) {
        super.spawnAfterBreak(state, level, pos, stack, dropExperience)
        if (dropExperience) {
            tryDropExperience(level, pos, stack, XP_RANGE)
        }
    }
}

/**
 * 起点の周辺に析出した硫黄をばら撒くのだぁ🌱
 *
 * @see net.minecraft.world.level.levelgen.feature.MultifaceGrowthFeature
 */
class DepositedSulfurFeature(codec: Codec<NoneFeatureConfiguration>) : Feature<NoneFeatureConfiguration>(codec) {
    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        fun nextOffset() = random.nextInt(4) + random.nextInt(4) - 3

        val depositedSulfurBlock = DepositedSulfurCard.block()

        var succeeded = false
        repeat(16) {
            val targetBlockPos = originBlockPos.offset(nextOffset(), nextOffset(), nextOffset())

            if (!level.getBlockState(targetBlockPos).isAir) return@repeat // 配置先は空気じゃないとだめなのだぁ…🌧️

            // 面ごとに80%の確率で試行
            var newBlockState = depositedSulfurBlock.defaultBlockState()
            var directionSucceeded = false
            Direction.entries.forEach nextDirection@{ direction ->
                val wallBlockState = level.getBlockState(targetBlockPos.relative(direction))
                if (!(wallBlockState isIn Blocks.BASALT || wallBlockState isIn Blocks.BLACKSTONE)) return@nextDirection
                if (random.nextInt(10) >= 8) return@nextDirection
                newBlockState = newBlockState.with(MultifaceBlock.getFaceProperty(direction), true)
                directionSucceeded = true
            }
            if (!directionSucceeded) return@repeat // 配置先の面が1個も見つからなかったのだぁ…🌧️

            level.setBlock(targetBlockPos, newBlockState, Block.UPDATE_ALL)
            level.getChunk(targetBlockPos).markPosForPostprocessing(targetBlockPos) // 貼り付け先が後から失われたときに、この面も消えるようにするのだぁ🌱
            succeeded = true
        }

        return succeeded
    }
}
