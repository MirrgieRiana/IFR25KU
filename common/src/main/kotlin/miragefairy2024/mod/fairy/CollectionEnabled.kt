package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.mod.sendAttachmentChangedEvent
import miragefairy2024.util.Channel
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.get
import miragefairy2024.util.register
import miragefairy2024.util.registerServerPacketReceiver
import miragefairy2024.util.set
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.world.entity.Entity

val COLLECTION_ENABLED_TRANSLATION = Translation({ "${MirageFairy2024.identifier("collection_enabled").toLanguageKey()}" }, "Collection Enabled", "収集効果が有効")
val COLLECTION_DISABLED_TRANSLATION = Translation({ "${MirageFairy2024.identifier("collection_disabled").toLanguageKey()}" }, "Collection Disabled", "収集効果が無効")

val COLLECTION_ENABLED_ATTACHMENT_TYPE: AttachmentType<Boolean> = AttachmentRegistry.create(MirageFairy2024.identifier("collection_enabled")) {
    it.persistent(Codec.BOOL)
    it.initializer { true }
    it.syncWith(ByteBufCodecs.BOOL, AttachmentSyncPredicate.targetOnly())
    it.copyOnDeath()
}

val Entity.collectionEnabled get() = this[COLLECTION_ENABLED_ATTACHMENT_TYPE]

object SetCollectionEnabledChannel : Channel<Boolean>(MirageFairy2024.identifier("set_collection_enabled")) {
    override fun writeToBuf(buf: RegistryFriendlyByteBuf, packet: Boolean) = ByteBufCodecs.BOOL.encode(buf, packet)
    override fun readFromBuf(buf: RegistryFriendlyByteBuf): Boolean = ByteBufCodecs.BOOL.decode(buf)
}

val TOGGLE_COLLECTION_ENABLED_KEY_TRANSLATION = Translation({ "key.${MirageFairy2024.MOD_ID}.toggle_collection_enabled" }, "Toggle Collection", "収集のオンオフ")

context(ModContext)
fun initCollectionEnabled() {

    COLLECTION_ENABLED_TRANSLATION.enJa()
    COLLECTION_DISABLED_TRANSLATION.enJa()

    COLLECTION_ENABLED_ATTACHMENT_TYPE.register()

    ModEvents.onInitialize {
        SetCollectionEnabledChannel.registerServerPacketReceiver { player, value ->
            player.collectionEnabled.set(value)
            player.sendAttachmentChangedEvent(COLLECTION_ENABLED_ATTACHMENT_TYPE)
        }
    }

    TOGGLE_COLLECTION_ENABLED_KEY_TRANSLATION.enJa()

}
