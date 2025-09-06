package miragefairy2024.mod

import miragefairy2024.DataGenerationEvents
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Channel
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import miragefairy2024.util.registerServerToClientPayloadType
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

enum class SoundEventCard(val path: String, en: String, ja: String, soundPaths: List<String>) {
    MAGIC1("magic1", "Magic fired", "魔法が発射される", listOf("magic1")),
    MAGIC2("magic2", "Magic fired", "魔法が発射される", listOf("magic2")),
    MAGIC3("magic3", "Magic fired", "魔法が発射される", listOf("magic3")),
    MAGIC_HIT("magic_hit", "Magic hits", "魔法が当たる", listOf("magic_hit")),
    CANCEL("cancel", "TODO", "TODO", listOf("cancel")), // TODO
    COLLECT("collect", "Collect item", "アイテムを集める", listOf("collect")),
    ENTITY_CHAOS_CUBE_AMBIENT("entity.chaos_cube.ambient", "Chaos Cube roars", "混沌のキューブがうなる", listOf("entity_chaos_cube_ambient_1", "entity_chaos_cube_ambient_2")),
    ENTITY_CHAOS_CUBE_HURT("entity.chaos_cube.hurt", "Chaos Cube hurts", "混沌のキューブがダメージを受ける", listOf("entity_chaos_cube_hurt_1")),
    ENTITY_CHAOS_CUBE_DEATH("entity.chaos_cube.death", "Chaos Cube dies", "混沌のキューブが死ぬ", listOf("entity_chaos_cube_death_1")),
    ENTITY_CHAOS_CUBE_ATTACK("entity.chaos_cube.attack", "Chaos Cube chants", "混沌のキューブが詠唱する", listOf("entity_chaos_cube_attack_1")),
    ENTITY_ETHEROBALLISTIC_BOLT_HIT("entity.etheroballistic_bolt.hit", "Etheroballistic Bolt hits", "エテロバリスティック弾が当たる", listOf("entity_etheroballistic_bolt_hit_1")),
    ENTITY_ETHEROBALLISTIC_BOLT_SHOOT("entity.etheroballistic_bolt.shoot", "Etheroballistic Bolt fired", "エテロバリスティック弾が発射される", listOf("entity_etheroballistic_bolt_shoot_1")),
    ;

    val identifier = MirageFairy2024.identifier(path)
    val sounds = soundPaths.map { MirageFairy2024.identifier(it) }
    val translation = Translation({ identifier.toLanguageKey("subtitles") }, en, ja)
    val soundEvent: SoundEvent = SoundEvent.createVariableRangeEvent(identifier)
}

object SoundEventChannel : Channel<SoundEventPacket>(MirageFairy2024.identifier("sound")) {
    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: SoundEventPacket) {
        buf.writeResourceLocation(BuiltInRegistries.SOUND_EVENT.getKey(packet.soundEvent)!!)
        buf.writeBlockPos(packet.pos)
        buf.writeUtf(packet.category.name)
        buf.writeFloat(packet.volume)
        buf.writeFloat(packet.pitch)
        buf.writeBoolean(packet.useDistance)
    }

    override fun readFromBuf(buf: RegistryFriendlyByteBuf): SoundEventPacket {
        val soundEvent = BuiltInRegistries.SOUND_EVENT.get(buf.readResourceLocation())!!
        val pos = buf.readBlockPos()
        val category = buf.readUtf().let { name -> SoundSource.entries.first { it.name == name } }
        val volume = buf.readFloat()
        val pitch = buf.readFloat()
        val useDistance = buf.readBoolean()
        return SoundEventPacket(soundEvent, pos, category, volume, pitch, useDistance)
    }
}

class SoundEventPacket(
    val soundEvent: SoundEvent,
    val pos: BlockPos,
    val category: SoundSource,
    val volume: Float,
    val pitch: Float,
    val useDistance: Boolean,
)

context(ModContext)
fun initSoundEventModule() {
    SoundEventChannel.registerServerToClientPayloadType()
    SoundEventCard.entries.forEach { card ->
        Registration(BuiltInRegistries.SOUND_EVENT, card.identifier) { card.soundEvent }.register()
        DataGenerationEvents.onGenerateSound { it(card.path, card.translation.keyGetter(), card.sounds) }
        card.translation.enJa()
    }
}
