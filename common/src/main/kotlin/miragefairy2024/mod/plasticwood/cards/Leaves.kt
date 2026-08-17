package miragefairy2024.mod.plasticwood.cards

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.plasticwood.PlasticWoodBlockCard
import miragefairy2024.mod.wood.WoodBlockConfiguration
import miragefairy2024.mod.wood.cards.WoodLeavesBlock
import miragefairy2024.mod.wood.cards.WoodLeavesBlockCard
import net.minecraft.world.level.block.state.BlockBehaviour

class PlasticTreeLeavesBlockCard(configuration: WoodBlockConfiguration) : WoodLeavesBlockCard(configuration, { PlasticWoodBlockCard.SAPLING }) {
    override suspend fun createBlock(properties: BlockBehaviour.Properties) = PlasticTreeLeavesBlock(properties)
}

class PlasticTreeLeavesBlock(settings: Properties) : WoodLeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<PlasticTreeLeavesBlock> = simpleCodec(::PlasticTreeLeavesBlock)
    }

    override fun codec() = CODEC

    override val sapParticleOptions = ParticleTypeCard.DRIPPING_PLASTIC_TREE_SAP.particleType
}
