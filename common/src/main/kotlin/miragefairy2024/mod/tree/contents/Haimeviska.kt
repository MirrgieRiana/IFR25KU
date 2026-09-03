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

    override fun getIncisedLog() = TreeBlockCard.INCISED_LOG
}

class IncisedHaimeviskaLogBlock(settings: Properties) : IncisedLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<IncisedHaimeviskaLogBlock> = simpleCodec(::IncisedHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun getDrippingLog() = TreeBlockCard.DRIPPING_LOG
}

class DrippingHaimeviskaLogBlock(settings: Properties) : DrippingLogBlock(settings) {
    companion object {
        val CODEC: MapCodec<DrippingHaimeviskaLogBlock> = simpleCodec(::DrippingHaimeviskaLogBlock)
    }

    override fun codec() = CODEC

    override fun getIncisedLog() = TreeBlockCard.INCISED_LOG
    override fun getSap() = MaterialCard.HAIMEVISKA_SAP.item()
    override fun getRosin() = MaterialCard.HAIMEVISKA_ROSIN.item()
}
