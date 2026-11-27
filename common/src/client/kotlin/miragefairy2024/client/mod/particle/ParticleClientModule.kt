package miragefairy2024.client.mod.particle

import miragefairy2024.ModContext
import miragefairy2024.mod.particle.ParticleTypeCard
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry as ParticleProviderRegistry // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry
import net.minecraft.client.particle.EndRodParticle
import net.minecraft.client.particle.FlyTowardsPositionParticle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.SuspendedTownParticle

// 滴る原木のテクスチャに描かれた、垂れている樹液の主要色に合わせてあるのだ～🌱
private const val PLASTIC_TREE_SAP_RGB = 0xFF9F32
private const val HAIMEVISKA_SAP_RGB = 0x80C8EE

context(_: ModContext)
fun initParticleClientModule() {
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.MISSION.particleType, SuspendedTownParticle::HappyVillagerProvider)
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.COLLECTING_MAGIC.particleType, FlyTowardsPositionParticle::EnchantProvider)
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.DESCENDING_MAGIC.particleType, EndRodParticle::Provider)
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.MIRAGE_FLOUR.particleType, SuspendedTownParticle::HappyVillagerProvider)
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.ATTRACTING_MAGIC.particleType, AttractingParticle::Factory)
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.AURA.particleType) { spriteProvider ->
        val factory = EndRodParticle.Provider(spriteProvider)
        ParticleProvider { parameters, level, x, y, z, velocityX, velocityY, velocityZ ->
            factory.createParticle(parameters, level, x, y, z, velocityX, velocityY, velocityZ)?.also { particle ->
                particle.lifetime = 20 + level.random.nextInt(12)
            }
        }
    }
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.CHAOS_STONE.particleType, createRollingFallingParticleFactory(0.0F))
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.HAIMEVISKA_BLOSSOM.particleType, createRollingFallingParticleFactory(1.0F))
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.DRIPPING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Dripping(level, x, y, z, spriteProvider, HAIMEVISKA_SAP_RGB, ParticleTypeCard.FALLING_HAIMEVISKA_SAP.particleType) } }
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.FALLING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Falling(level, x, y, z, spriteProvider, HAIMEVISKA_SAP_RGB, ParticleTypeCard.LANDING_HAIMEVISKA_SAP.particleType) } }
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.LANDING_HAIMEVISKA_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Landing(level, x, y, z, spriteProvider, HAIMEVISKA_SAP_RGB) } }
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.DRIPPING_PLASTIC_TREE_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Dripping(level, x, y, z, spriteProvider, PLASTIC_TREE_SAP_RGB, ParticleTypeCard.FALLING_PLASTIC_TREE_SAP.particleType) } }
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.FALLING_PLASTIC_TREE_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Falling(level, x, y, z, spriteProvider, PLASTIC_TREE_SAP_RGB, ParticleTypeCard.LANDING_PLASTIC_TREE_SAP.particleType) } }
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.LANDING_PLASTIC_TREE_SAP.particleType) { spriteProvider -> ParticleProvider { _, level, x, y, z, _, _, _ -> SapParticle.Landing(level, x, y, z, spriteProvider, PLASTIC_TREE_SAP_RGB) } }
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.MAGIC_SQUARE.particleType, createMagicSquareParticleFactory())
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.SULFUR_SMOKE.particleType, createSulfurSmokeParticleFactory())
    ParticleProviderRegistry.getInstance().register(ParticleTypeCard.SPARKLE.particleType, createSparkleParticleFactory())

    initMagicSquareParticle()
}
