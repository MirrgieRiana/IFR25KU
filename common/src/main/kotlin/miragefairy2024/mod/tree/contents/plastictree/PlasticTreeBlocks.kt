package miragefairy2024.mod.tree.contents.plastictree

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.tree.TreeBlockCard
import miragefairy2024.mod.tree.contents.DrippingLogBlock
import miragefairy2024.mod.tree.contents.IncisableLogBlock
import miragefairy2024.mod.tree.contents.IncisedLogBlock
import miragefairy2024.mod.tree.contents.spawnDrippingSapParticle
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class PlasticTreeLogBlock(settings: Properties) : IncisableLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeLogBlock> = simpleCodec(::PlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_PLASTIC_TREE_LOG.block()
}

class IncisedPlasticTreeLogBlock(settings: Properties) : IncisedLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedPlasticTreeLogBlock> = simpleCodec(::IncisedPlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getDrippingLogBlock() = TreeBlockCard.DRIPPING_PLASTIC_TREE_LOG.block()
}

class DrippingPlasticTreeLogBlock(settings: Properties) : DrippingLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingPlasticTreeLogBlock> = simpleCodec(::DrippingPlasticTreeLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_PLASTIC_TREE_LOG.block()
    override fun getSapItem() = MaterialCard.PLASTIC_TREE_SAP.item()
    override fun getRosinItem() = MaterialCard.PLASTIC_TREE_SAP.item() // TODO レア素材を指定するのだ～🌱

    override fun animateTick(state: BlockState, level: Level, pos: BlockPos, random: RandomSource) {
        spawnDrippingSapParticle(state, level, pos, random) // TODO パーティクルの種類も、湧く位置も、ハイメヴィスカと共通のものをそのまま使っているのだ～🌱
    }
}
