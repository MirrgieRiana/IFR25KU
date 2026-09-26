package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.mod.materials.BlockMaterialCard
import miragefairy2024.util.isIn
import net.minecraft.core.BlockPos
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.SaplingBlock
import net.minecraft.world.level.block.grower.TreeGrower
import net.minecraft.world.level.block.state.BlockState

class PlasticTreeFamilySaplingBlock(treeGrower: TreeGrower, settings: Properties) : SaplingBlock(treeGrower, settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeFamilySaplingBlock> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                TreeGrower.CODEC.fieldOf("tree").forGetter { it.treeGrower },
                propertiesCodec(),
            ).apply(instance, ::PlasticTreeFamilySaplingBlock)
        }
    }

    override fun codec() = CODEC

    override fun mayPlaceOn(state: BlockState, level: BlockGetter, pos: BlockPos) = super.mayPlaceOn(state, level, pos) || state isIn BlockMaterialCard.RESIN_CEMENT.block()
}
