package miragefairy2024.mod.tree.contents

import com.mojang.serialization.MapCodec
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.particle.ParticleTypeCard
import miragefairy2024.mod.tree.TreeBlockCard

class HaimeviskaLeavesBlock(settings: Properties) : ChargeableLeavesBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLeavesBlock> = simpleCodec(::HaimeviskaLeavesBlock)
    }

    override fun codec() = CODEC

    override fun getBlossomParticleType() = ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType
}

class HaimeviskaLogBlock(settings: Properties) : IncisableLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<HaimeviskaLogBlock> = simpleCodec(::HaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_LOG.block()
}

class IncisedHaimeviskaLogBlock(settings: Properties) : IncisedLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedHaimeviskaLogBlock> = simpleCodec(::IncisedHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun getDrippingLogBlock() = TreeBlockCard.DRIPPING_LOG.block()
}

class DrippingHaimeviskaLogBlock(settings: Properties) : DrippingLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingHaimeviskaLogBlock> = simpleCodec(::DrippingHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLogBlock() = TreeBlockCard.INCISED_LOG.block()
    override fun getSapItem() = MaterialCard.HAIMEVISKA_SAP.item()
    override fun getRosinItem() = MaterialCard.HAIMEVISKA_ROSIN.item()
}

class HollowHaimeviskaLogBlock(settings: Properties) : HollowLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<HollowHaimeviskaLogBlock> = simpleCodec(::HollowHaimeviskaLogBlock)
    }

    override fun codec() = CODEC
}
