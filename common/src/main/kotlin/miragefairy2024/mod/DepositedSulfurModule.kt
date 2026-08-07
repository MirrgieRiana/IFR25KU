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
import net.minecraft.Util
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
                .sound(SoundType.NETHER_ORE)
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
            val model = "${"block/" * card.identifier}".jsonElement

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
    companion object {
        /** 1回の呼び出しで、析出した硫黄の配置を試みる回数なのだぁ🌱 */
        private const val TRIAL_COUNT = 16

        /** 析出した硫黄が貼り付くことのできるブロックなのだぁ✨ */
        private val CAN_BE_PLACED_ON_BLOCKS = listOf(Blocks.BASALT, Blocks.BLACKSTONE)
    }

    override fun place(context: FeaturePlaceContext<NoneFeatureConfiguration>): Boolean {
        val level = context.level()
        val originBlockPos = context.origin()
        val random = context.random()

        val depositedSulfurBlock = DepositedSulfurCard.block()

        // 0～3の乱数を2回引いて足してから3を引くことで、-3～3の範囲で、中央ほど厚い1:2:3:4:3:2:1の重みの分布になるのだぁ🌱
        fun nextOffset() = random.nextInt(4) + random.nextInt(4) - 3

        var succeeded = false
        repeat(TRIAL_COUNT) {
            // 起点を中心とする7x7x7の範囲から、配置先の候補を選ぶのだぁ🌱
            val blockPos = originBlockPos.offset(nextOffset(), nextOffset(), nextOffset())
            val blockState = level.getBlockState(blockPos)
            if (!blockState.isAir) return@repeat

            // 貼り付けられる面をランダムな順序で探して、最初に見つかった面に貼り付けるのだぁ✨
            val newBlockState = Util.shuffledCopy(Direction.entries.toTypedArray(), random).firstNotNullOfOrNull { direction ->
                val targetBlockState = level.getBlockState(blockPos.relative(direction))
                if (CAN_BE_PLACED_ON_BLOCKS.none { targetBlockState isIn it }) return@firstNotNullOfOrNull null
                depositedSulfurBlock.getStateForPlacement(blockState, level, blockPos, direction)
            } ?: return@repeat

            level.setBlock(blockPos, newBlockState, Block.UPDATE_ALL)

            // 貼り付け先が後から失われたときに、この面も消えるようにするのだぁ🌱
            level.getChunk(blockPos).markPosForPostprocessing(blockPos)

            succeeded = true
        }

        return succeeded
    }
}
