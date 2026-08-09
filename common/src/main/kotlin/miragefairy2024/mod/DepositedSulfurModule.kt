package miragefairy2024.mod

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.mirageFairy2024ItemGroupCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.util.BlockEntityType
import miragefairy2024.util.EnJa
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.Registration
import miragefairy2024.util.TextureMapping
import miragefairy2024.util.aboveLava
import miragefairy2024.util.checkType
import miragefairy2024.util.count
import miragefairy2024.util.createEmptyModel
import miragefairy2024.util.enJa
import miragefairy2024.util.flower
import miragefairy2024.util.generator
import miragefairy2024.util.isIn
import miragefairy2024.util.placeWhenVegetalDecoration
import miragefairy2024.util.register
import miragefairy2024.util.registerBlockGeneratedModelGeneration
import miragefairy2024.util.registerBlockStateGeneration
import miragefairy2024.util.registerChild
import miragefairy2024.util.registerColorProvider
import miragefairy2024.util.registerConfiguredFeature
import miragefairy2024.util.registerCutoutRenderLayer
import miragefairy2024.util.registerItemGroup
import miragefairy2024.util.registerLootTableGeneration
import miragefairy2024.util.registerModelGeneration
import miragefairy2024.util.registerOreLootTableGeneration
import miragefairy2024.util.registerPlacedFeature
import miragefairy2024.util.registerSingletonBlockStateGeneration
import miragefairy2024.util.square
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.unaryPlus
import miragefairy2024.util.with
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import mirrg.kotlin.gson.hydrogen.jsonObjectNotNull
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.models.model.ModelTemplates
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.BlockTags
import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.Biomes
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.MultifaceBlock
import net.minecraft.world.level.block.MultifaceSpreader
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration
import net.minecraft.world.level.material.MapColor
import net.minecraft.world.level.material.PushReaction
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.phys.shapes.VoxelShape

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
}

object SolfataraCard {
    val identifier = MirageFairy2024.identifier("solfatara")
    val block = Registration(BuiltInRegistries.BLOCK, identifier) {
        SolfataraBlock(
            BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .replaceable()
                .noCollission()
                .noOcclusion()
                .requiresCorrectToolForDrops()
                .strength(0.2F)
                .sound(SoundType.STONE)
                .pushReaction(PushReaction.DESTROY),
        )
    }
    val item = Registration(BuiltInRegistries.ITEM, identifier) { BlockItem(block.await(), Item.Properties()) }
    val blockEntityType = Registration(BuiltInRegistries.BLOCK_ENTITY_TYPE, identifier) { BlockEntityType(::SolfataraBlockEntity, setOf(block.await())) }
}

val DEPOSITED_SULFUR_FEATURE = DepositedSulfurFeature(NoneFeatureConfiguration.CODEC)

context(ModContext)
fun initDepositedSulfurModule() {

    Registration(BuiltInRegistries.BLOCK_TYPE, DepositedSulfurCard.identifier) { DepositedSulfurBlock.CODEC }.register()
    Registration(BuiltInRegistries.BLOCK_TYPE, SolfataraCard.identifier) { SolfataraBlock.CODEC }.register()
    Registration(BuiltInRegistries.FEATURE, DepositedSulfurCard.identifier) { DEPOSITED_SULFUR_FEATURE }.register()

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
                        "apply" to variant,
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
                        "apply" to variant,
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
            Model {
                val texture = ("block/" * card.identifier).string
                ModelData(
                    textures = ModelTexturesData(
                        TextureSlot.PARTICLE.id to texture,
                        TextureSlot.TEXTURE.id to texture,
                    ),
                    // ヒカリゴケの平面は貼り付け先から0.1だけ浮いているから、隣り合う面同士の継ぎ目に岩肌の角が覗いてしまうのだぁ🌧️ 平面を上下左右に0.11だけはみ出させて、直交する面の裏に隠すのだぁ✨
                    elements = ModelElementsData(
                        ModelElementData(
                            from = listOf(-0.11, -0.11, 0.1),
                            to = listOf(16.11, 16.11, 0.1),
                            faces = ModelFacesData(
                                north = ModelFaceData(uv = listOf(16, 0, 0, 16), texture = TextureSlot.TEXTURE.string),
                                south = ModelFaceData(uv = listOf(0, 0, 16, 16), texture = TextureSlot.TEXTURE.string),
                            ),
                        ),
                    ),
                )
            } with TextureMapping()
        }
        card.item.registerBlockGeneratedModelGeneration(card.block)
        card.block.registerCutoutRenderLayer()

        card.block.enJa(EnJa("Deposited Sulfur", "析出した硫黄"))

        card.block.registerOreLootTableGeneration(MaterialCard.SULFUR.item)

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(card.block)

    }

    SolfataraCard.let { card ->

        card.block.register()
        card.item.register()
        card.blockEntityType.register()

        card.item.registerItemGroup(mirageFairy2024ItemGroupCard.itemGroupKey)

        card.block.registerSingletonBlockStateGeneration()
        card.block.registerModelGeneration { createEmptyModel("block/" * DepositedSulfurCard.identifier) }
        card.item.registerModelGeneration(ModelTemplates.FLAT_ITEM) { TextureMapping(TextureSlot.LAYER0 to "block/" * DepositedSulfurCard.identifier) }
        card.item.registerColorProvider { _, _ -> 0xFFFFA500.toInt() }

        card.block.enJa(EnJa("Solfatara", "硫気孔"))

        card.block.registerLootTableGeneration { it, _ -> it.createSilkTouchOnlyTable(card.block()) }

        BlockTags.MINEABLE_WITH_PICKAXE.generator.registerChild(card.block)

    }

    DEPOSITED_SULFUR_FEATURE.generator(MirageFairy2024.identifier("deposited_sulfur")) {
        registerConfiguredFeature { NoneFeatureConfiguration.INSTANCE }.generator {
            registerPlacedFeature { count(8) + flower(square, aboveLava) }.placeWhenVegetalDecoration { +Biomes.BASALT_DELTAS }
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
 * 硫黄を含む火山ガスを噴き上げる噴気孔なのだぁ🌱 見た目を持たず、煙だけが見えるのだぁ✨
 *
 * @see net.minecraft.world.level.block.BarrierBlock
 */
@Suppress("OVERRIDE_DEPRECATION")
class SolfataraBlock(properties: Properties) : Block(properties), EntityBlock {
    companion object {
        val CODEC: MapCodec<SolfataraBlock> = simpleCodec(::SolfataraBlock)
        private val SHAPE: VoxelShape = box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0)
    }

    override fun codec() = CODEC

    override fun getShape(state: BlockState, level: BlockGetter, pos: BlockPos, context: CollisionContext) = SHAPE

    override fun getRenderShape(state: BlockState) = RenderShape.INVISIBLE

    override fun newBlockEntity(pos: BlockPos, state: BlockState) = SolfataraBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(level: Level, state: BlockState, blockEntityType: BlockEntityType<T>): BlockEntityTicker<T>? {
        if (!level.isClientSide) return null
        return checkType(blockEntityType, SolfataraCard.blockEntityType()) { level2, blockPos, _, blockEntity ->
            blockEntity.clientTick(level2, blockPos)
        }
    }
}

/**
 * 噴気孔から硫黄の色をした煙を噴き上げるのだぁ🌱
 */
class SolfataraBlockEntity(pos: BlockPos, state: BlockState) : BlockEntity(SolfataraCard.blockEntityType(), pos, state) {
    fun clientTick(level: Level, blockPos: BlockPos) {
        if (level.gameTime % 10L != 0L) return
        // 煙は10秒ほど滞留するから、これで常に40個ほどが宙に漂うのだぁ🌱
        // 0.5 秒おきに 2 個で、1 個が 10 秒生きるのだぁ～🌱 10 秒の間に 20 回出るから、2 かける 20 で 40 個なのだぁ～✨
        repeat(2) {
            level.addParticle(
                ParticleTypeCard.SULFUR_SMOKE.particleType,
                blockPos.x + level.random.nextDouble(),
                blockPos.y + level.random.nextDouble(),
                blockPos.z + level.random.nextDouble(),
                level.random.nextGaussian() * 0.02,
                0.08 + level.random.nextGaussian() * 0.02,
                level.random.nextGaussian() * 0.02,
            )
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

        // 塊の中央に、その源となる噴気孔を据えるのだぁ🌱 起点は溶岩の直上だから、そこがちょうど中央なのだぁ✨
        if (!level.getBlockState(originBlockPos).isAir) return false // PlacedFeatureの設定上ここは常に空気のはずだけど、将来そうでなくなったときに備えるのだぁ🌱
        level.setBlock(originBlockPos, SolfataraCard.block().defaultBlockState(), Block.UPDATE_ALL)

        val depositedSulfurBlock = DepositedSulfurCard.block()

        repeat(4) {
            fun nextOffset() = random.nextInt(4) + random.nextInt(4) - 3
            val centerBlockPos = originBlockPos.offset(nextOffset(), nextOffset(), nextOffset())
            val radius = random.nextIntBetweenInclusive(2, 5)
            val squaredRadius = radius * radius
            (-radius..radius).forEach { x ->
                (-radius..radius).forEach { y ->
                    (-radius..radius).forEach nextTarget@{ z ->
                        if (x * x + y * y + z * z > squaredRadius) return@nextTarget // 球の外側は対象外なのだ～
                        val targetBlockPos = centerBlockPos.offset(x, y, z)

                        if (!level.getBlockState(targetBlockPos).isAir) return@nextTarget // 配置先は空気じゃないとだめなのだぁ…🌧️

                        var newBlockState = depositedSulfurBlock.defaultBlockState()
                        var directionSucceeded = false
                        Direction.entries.forEach nextDirection@{ direction ->
                            val wallBlockState = level.getBlockState(targetBlockPos.relative(direction))
                            if (!(wallBlockState isIn Blocks.BASALT || wallBlockState isIn Blocks.BLACKSTONE)) return@nextDirection
                            newBlockState = newBlockState.with(MultifaceBlock.getFaceProperty(direction), true)
                            directionSucceeded = true
                        }
                        if (!directionSucceeded) return@nextTarget // 配置先の面が1個も見つからなかったのだぁ…🌧️

                        level.setBlock(targetBlockPos, newBlockState, Block.UPDATE_ALL)
                        level.getChunk(targetBlockPos).markPosForPostprocessing(targetBlockPos) // 貼り付け先が後から失われたときに、この面も消えるようにするのだぁ🌱
                    }
                }
            }
        }

        return true
    }
}
