package miragefairy2024.mod.materials.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.Model
import miragefairy2024.util.ModelData
import miragefairy2024.util.ModelElementData
import miragefairy2024.util.ModelElementsData
import miragefairy2024.util.ModelFaceData
import miragefairy2024.util.ModelFacesData
import miragefairy2024.util.ModelTexturesData
import miragefairy2024.util.ResourceLocation
import miragefairy2024.util.getIdentifier
import miragefairy2024.util.isIn
import miragefairy2024.util.string
import miragefairy2024.util.times
import miragefairy2024.util.toBlockTag
import miragefairy2024.util.with
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.data.models.model.TextureSlot
import net.minecraft.data.models.model.TexturedModel
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

val LOCAL_VACUUM_DECAY_RESISTANT_BLOCK_TAG = MirageFairy2024.identifier("local_vacuum_decay_resistant").toBlockTag()

@Suppress("OVERRIDE_DEPRECATION")
class LocalVacuumDecayBlock(settings: Properties) : Block(settings) {
    companion object {
        val CODEC: MapCodec<LocalVacuumDecayBlock> = simpleCodec(::LocalVacuumDecayBlock)
    }

    override fun codec() = CODEC

    override fun isRandomlyTicking(state: BlockState) = true

    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        @Suppress("DEPRECATION")
        super.randomTick(state, world, pos, random)

        val direction = Direction.getRandom(random)
        val targetBlockPos = pos.relative(direction)
        val targetBlockState = world.getBlockState(targetBlockPos)
        if (targetBlockState.isAir) return
        if (targetBlockState.getDestroySpeed(world, targetBlockPos) < 0) return
        if (targetBlockState isIn state.block) return
        if (targetBlockState isIn LOCAL_VACUUM_DECAY_RESISTANT_BLOCK_TAG) return
        world.setBlockAndUpdate(targetBlockPos, state)
    }

    override fun stepOn(world: Level, pos: BlockPos, state: BlockState, entity: Entity) {
        if (!entity.isSteppingCarefully) {
            entity.hurt(world.damageSources().magic(), 1.0f)
        }
        super.stepOn(world, pos, state, entity)
    }
}

val localVacuumDecayTexturedModelFactory = TexturedModel.Provider { block ->
    Model { textureMap ->
        ModelData(
            parent = ResourceLocation("minecraft", "block/block"),
            textures = ModelTexturesData(
                TextureSlot.PARTICLE.id to textureMap.get(TextureSlot.BACK).string,
                TextureSlot.BACK.id to textureMap.get(TextureSlot.BACK).string,
                TextureSlot.FRONT.id to textureMap.get(TextureSlot.FRONT).string,
            ),
            elements = ModelElementsData(
                ModelElementData(
                    from = listOf(0, 0, 0),
                    to = listOf(16, 16, 16),
                    faces = ModelFacesData(
                        down = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "down"),
                        up = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "up"),
                        north = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "north"),
                        south = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "south"),
                        west = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "west"),
                        east = ModelFaceData(texture = TextureSlot.BACK.string, cullface = "east"),
                    ),
                ),
                ModelElementData(
                    from = listOf(0, 0, 0),
                    to = listOf(16, 16, 16),
                    faces = ModelFacesData(
                        down = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "down"),
                        up = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "up"),
                        north = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "north"),
                        south = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "south"),
                        west = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "west"),
                        east = ModelFaceData(texture = TextureSlot.FRONT.string, cullface = "east"),
                    ),
                ),
            ),
        )
    }.with(
        TextureSlot.BACK to "block/" * block.getIdentifier() * "_base",
        TextureSlot.FRONT to "block/" * block.getIdentifier() * "_spark",
    )
}
