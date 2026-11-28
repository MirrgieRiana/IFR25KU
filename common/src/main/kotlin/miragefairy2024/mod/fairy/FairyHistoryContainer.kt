package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.util.BIG_INTEGER_CODEC
import miragefairy2024.util.BIG_INTEGER_STREAM_CODEC
import miragefairy2024.util.get
import miragefairy2024.util.list
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.entity.Entity
import java.math.BigInteger

val FAIRY_HISTORY_CONTAINER_ATTACHMENT_TYPE: AttachmentType<FairyHistoryContainer> = AttachmentRegistry.create(MirageFairy2024.identifier("fairy_history_container")) {
    it.persistent(FairyHistoryContainer.CODEC)
    it.initializer(::FairyHistoryContainer)
    it.syncWith(FairyHistoryContainer.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    it.copyOnDeath()
}

val Entity.fairyHistoryContainer get() = this[FAIRY_HISTORY_CONTAINER_ATTACHMENT_TYPE]

class FairyHistoryContainer {
    companion object {
        val ENTRY_CODEC: Codec<Pair<Identifier, BigInteger>> = RecordCodecBuilder.create { instance ->
            instance.group(
                Identifier.CODEC.fieldOf("motif").forGetter { it.first },
                BIG_INTEGER_CODEC.fieldOf("gained").forGetter { it.second },
            ).apply(instance, ::Pair)
        }
        val ENTRY_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Pair<Identifier, BigInteger>> = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            { it.first },
            BIG_INTEGER_STREAM_CODEC,
            { it.second },
            ::Pair,
        )

        fun fromEntries(entries: List<Pair<Identifier, BigInteger>>): FairyHistoryContainer {
            val fairyHistoryContainer = FairyHistoryContainer()
            entries.forEach { (key, count) ->
                val motif = motifRegistry[key] ?: return@forEach
                fairyHistoryContainer[motif] = count
            }
            return fairyHistoryContainer
        }

        fun toEntries(fairyHistoryContainer: FairyHistoryContainer): List<Pair<Identifier, BigInteger>> {
            val entries = mutableListOf<Pair<Identifier, BigInteger>>()
            fairyHistoryContainer.entries.forEach { (motif, count) ->
                if (count > BigInteger.ZERO) entries += Pair(motif.getIdentifier()!!, count)
            }
            return entries
        }

        val CODEC: Codec<FairyHistoryContainer> = ENTRY_CODEC.listOf().xmap(::fromEntries, ::toEntries)
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, FairyHistoryContainer> = ENTRY_STREAM_CODEC.list().map(::fromEntries, ::toEntries)
    }

    private val map = mutableMapOf<Motif, BigInteger>()

    val entries get() = map.entries

    operator fun get(motif: Motif): BigInteger = map.getOrElse(motif) { BigInteger.ZERO }

    operator fun set(motif: Motif, count: BigInteger?) {
        if (count != null) {
            check(count >= BigInteger.ZERO)
            if (count == BigInteger.ZERO) {
                map.remove(motif)
            } else {
                map[motif] = count
            }
        } else {
            map.remove(motif)
        }
    }
}
