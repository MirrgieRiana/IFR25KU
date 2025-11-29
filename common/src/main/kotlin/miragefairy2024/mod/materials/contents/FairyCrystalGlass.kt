package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.isIn
import miragefairy2024.util.string
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.PipeBlock
import net.minecraft.world.level.block.TransparentBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties

class FairyCrystalGlassBlock(properties: Properties) : TransparentBlock(properties) {
    companion object {
        val CODEC: MapCodec<FairyCrystalGlassBlock> = simpleCodec(::FairyCrystalGlassBlock)
    }

    override fun codec() = CODEC

    init {
        registerDefaultState(
            defaultBlockState()
                .with(BlockStateProperties.NORTH, false)
                .with(BlockStateProperties.EAST, false)
                .with(BlockStateProperties.SOUTH, false)
                .with(BlockStateProperties.WEST, false)
                .with(BlockStateProperties.UP, false)
                .with(BlockStateProperties.DOWN, false)
        )
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(
            BlockStateProperties.NORTH,
            BlockStateProperties.EAST,
            BlockStateProperties.SOUTH,
            BlockStateProperties.WEST,
            BlockStateProperties.UP,
            BlockStateProperties.DOWN,
        )
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        return defaultBlockState()
            .with(BlockStateProperties.NORTH, ctx.level.getBlockState(ctx.clickedPos.north()) isIn this)
            .with(BlockStateProperties.EAST, ctx.level.getBlockState(ctx.clickedPos.east()) isIn this)
            .with(BlockStateProperties.SOUTH, ctx.level.getBlockState(ctx.clickedPos.south()) isIn this)
            .with(BlockStateProperties.WEST, ctx.level.getBlockState(ctx.clickedPos.west()) isIn this)
            .with(BlockStateProperties.UP, ctx.level.getBlockState(ctx.clickedPos.above()) isIn this)
            .with(BlockStateProperties.DOWN, ctx.level.getBlockState(ctx.clickedPos.below()) isIn this)
    }

    override fun updateShape(state: BlockState, direction: Direction, neighborState: BlockState, level: LevelAccessor, pos: BlockPos, neighborPos: BlockPos): BlockState {
        return state.with(PipeBlock.PROPERTY_BY_DIRECTION[direction]!!, neighborState isIn this)
    }
}

val fairyCrystalGlassFrameBlockModel = Model { textureMapping ->
    ModelData(
        parent = ResourceLocation("block/block"),
        textures = ModelTexturesData(
            TextureSlot.PARTICLE.id to textureMapping.get(TextureSlot.TEXTURE).string,
            TextureSlot.TEXTURE.id to textureMapping.get(TextureSlot.TEXTURE).string,
        ),
        elements = ModelElementsData(
            ModelElementData(
                from = listOf(0, 0, 0),
                to = listOf(16, 16, 16),
                faces = ModelFacesData(
                    north = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "north"),
                    south = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "south"),
                    west = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "west"),
                    east = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "east"),
                ),
            ),
        ),
    )
}

val fairyCrystalGlassBlockModel = Model { textureMapping ->
    fun createPart(rotation: Int) = ModelElementData(
        from = listOf(0, 0, 0),
        to = listOf(16, 16, 16),
        faces = ModelFacesData(
            north = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "north", rotation = rotation),
            south = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "south", rotation = rotation),
            west = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "west", rotation = rotation),
            east = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "east", rotation = rotation),
            up = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "up", rotation = rotation),
            down = ModelFaceData(texture = TextureSlot.TEXTURE.string, cullface = "down", rotation = rotation),
        ),
    )
    ModelData(
        parent = ResourceLocation("block/block"),
        textures = ModelTexturesData(
            TextureSlot.PARTICLE.id to textureMapping.get(TextureSlot.TEXTURE).string,
            TextureSlot.TEXTURE.id to textureMapping.get(TextureSlot.TEXTURE).string,
        ),
        elements = ModelElementsData(
            createPart(0),
            createPart(90),
            createPart(180),
            createPart(270),
        ),
    )
}
