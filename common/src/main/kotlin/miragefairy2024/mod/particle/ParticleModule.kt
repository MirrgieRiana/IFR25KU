package miragefairy2024.mod.particle

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import miragefairy2024.util.registerServerToClientPayloadType
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import mirrg.kotlin.gson.hydrogen.jsonArray
import mirrg.kotlin.gson.hydrogen.jsonElement
import mirrg.kotlin.gson.hydrogen.jsonObject
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.registries.BuiltInRegistries

class ParticleTypeCard<P : ParticleType<T>, T : ParticleOptions>(
    path: String,
    textureNames: List<String>,
    creator: () -> P,
) {
    companion object {
        val entries = mutableListOf<ParticleTypeCard<*, *>>()
        private operator fun <P : ParticleType<T>, T : ParticleOptions> ParticleTypeCard<P, T>.not() = this.also { entries.add(this) }

        val MISSION = !ParticleTypeCard("mission", listOf("mission")) { FabricParticleTypes.simple(true) }
        val COLLECTING_MAGIC = !ParticleTypeCard("collecting_magic", listOf("magic")) { FabricParticleTypes.simple(false) }
        val DESCENDING_MAGIC = !ParticleTypeCard("descending_magic", listOf("magic")) { FabricParticleTypes.simple(false) }
        val MIRAGE_FLOUR = !ParticleTypeCard("mirage_flour", listOf("mirage_flour")) { FabricParticleTypes.simple(false) }
        val ATTRACTING_MAGIC = !ParticleTypeCard("attracting_magic", listOf("mission")) { FabricParticleTypes.simple(false) }
        val AURA = !ParticleTypeCard("aura", listOf("mission")) { FabricParticleTypes.simple(false) }
        val CHAOS_STONE = !ParticleTypeCard("chaos_stone", listOf("chaos_stone")) { FabricParticleTypes.simple(false) }
        val HAIMEVISKA_BLOSSOM = !ParticleTypeCard("haimeviska_blossom", listOf("haimeviska_blossom")) { FabricParticleTypes.simple(false) }
        val DRIPPING_HAIMEVISKA_SAP = !ParticleTypeCard("dripping_haimeviska_sap", listOf("minecraft:drip_hang")) { FabricParticleTypes.simple(false) }
        val FALLING_HAIMEVISKA_SAP = !ParticleTypeCard("falling_haimeviska_sap", listOf("minecraft:drip_fall")) { FabricParticleTypes.simple(false) }
        val LANDING_HAIMEVISKA_SAP = !ParticleTypeCard("landing_haimeviska_sap", listOf("minecraft:drip_land")) { FabricParticleTypes.simple(false) }
        val MAGIC_SQUARE = !ParticleTypeCard("magic_square", (1..7).map { "magic_square_$it" }) { MagicSquareParticleType(true) }
    }

    val identifier = MirageFairy2024.identifier(path)
    val textures = textureNames.map { if (":" in it) it.toIdentifier() else MirageFairy2024.identifier(it) }
    val particleType = creator()
}

context(ModContext)
fun initParticleModule() {
    ParticleTypeCard.entries.forEach { card ->
        Registration(BuiltInRegistries.PARTICLE_TYPE, card.identifier) { card.particleType }.register()
        DataGenerationEvents.onGenerateParticles {
            val data = jsonObject(
                "textures" to card.textures.map { it.string.jsonElement }.jsonArray,
            )
            it(card.identifier, data)
        }
    }
    MagicSquareParticleChannel.registerServerToClientPayloadType()
}
