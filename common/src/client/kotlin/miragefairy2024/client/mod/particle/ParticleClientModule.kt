package miragefairy2024.client.mod.particle

import miragefairy2024.ModContext
import miragefairy2024.mod.particle.ParticleTypeCard
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry
import net.minecraft.client.particle.EndRodParticle
import net.minecraft.client.particle.FlyTowardsPositionParticle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SuspendedTownParticle

// 滴る原木のテクスチャに描かれた、垂れている樹液の主要色に合わせてあるのだ～🌱
private const val HAIMEVISKA_SAP_RGB = 0xFF9F32
private const val PLASTIC_TREE_SAP_RGB = 0xFFF432

context(ModContext)
fun initParticleClientModule() {
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MISSION.particleType, SuspendedTownParticle::HappyVillagerProvider)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.COLLECTING_MAGIC.particleType, FlyTowardsPositionParticle::EnchantProvider)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.DESCENDING_MAGIC.particleType, EndRodParticle::Provider)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MIRAGE_FLOUR.particleType, SuspendedTownParticle::HappyVillagerProvider)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.ATTRACTING_MAGIC.particleType, AttractingParticle::Factory)
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.AURA.particleType) { spriteProvider ->
        val factory = EndRodParticle.Provider(spriteProvider)
        ParticleProvider { parameters, level, x, y, z, velocityX, velocityY, velocityZ ->
            factory.createParticle(parameters, level, x, y, z, velocityX, velocityY, velocityZ)?.also { particle ->
                particle.lifetime = 20 + level.random.nextInt(12)
            }
        }
    }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.CHAOS_STONE.particleType, createRollingFallingParticleFactory(0.0F))
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType, createRollingFallingParticleFactory(1.0F))
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.DRIPPING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Dripping(level, x, y, z, spriteProvider, HAIMEVISKA_SAP_RGB, ParticleTypeCard.FALLING_HAIMEVISKA_SAP.particleType) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.FALLING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Falling(level, x, y, z, spriteProvider, HAIMEVISKA_SAP_RGB, ParticleTypeCard.LANDING_HAIMEVISKA_SAP.particleType) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.LANDING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Landing(level, x, y, z, spriteProvider, HAIMEVISKA_SAP_RGB) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.DRIPPING_PLASTIC_TREE_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Dripping(level, x, y, z, spriteProvider, PLASTIC_TREE_SAP_RGB, ParticleTypeCard.FALLING_PLASTIC_TREE_SAP.particleType) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.FALLING_PLASTIC_TREE_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Falling(level, x, y, z, spriteProvider, PLASTIC_TREE_SAP_RGB, ParticleTypeCard.LANDING_PLASTIC_TREE_SAP.particleType) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.LANDING_PLASTIC_TREE_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Landing(level, x, y, z, spriteProvider, PLASTIC_TREE_SAP_RGB) } }
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.MAGIC_SQUARE.particleType, createMagicSquareParticleFactory())
    ParticleFactoryRegistry.getInstance().register(ParticleTypeCard.SULFUR_SMOKE.particleType, createSulfurSmokeParticleFactory())

    initMagicSquareParticle()
}
