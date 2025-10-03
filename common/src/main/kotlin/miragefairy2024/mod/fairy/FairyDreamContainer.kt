package miragefairy2024.mod.fairy

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.CommandEvents
import miragefairy2024.util.executesThrowable
import miragefairy2024.util.failure
import miragefairy2024.util.get
import miragefairy2024.util.getOrCreate
import miragefairy2024.util.invoke
import miragefairy2024.util.isNotIn
import miragefairy2024.util.list
import miragefairy2024.util.mutate
import miragefairy2024.util.obtain
import miragefairy2024.util.opposite
import miragefairy2024.util.register
import miragefairy2024.util.registerServerDebugItem
import miragefairy2024.util.sendToClient
import miragefairy2024.util.string
import miragefairy2024.util.success
import miragefairy2024.util.sync
import miragefairy2024.util.text
import miragefairy2024.util.toTextureSource
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Items

val FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE: AttachmentType<FairyDreamContainer> = AttachmentRegistry.create(MirageFairy2024.identifier("fairy_dream")) {
    it.persistent(FairyDreamContainer.CODEC)
    it.initializer(::FairyDreamContainer)
    it.syncWith(FairyDreamContainer.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
    it.copyOnDeath()
}

val Entity.fairyDreamContainer get() = this[FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE]

context(ModContext)
fun initFairyDreamContainer() {

    FAIRY_DREAM_CONTAINER_ATTACHMENT_TYPE.register()

    registerServerDebugItem("debug_clear_fairy_dream", Items.STRING.toTextureSource(), 0xFF0000DD.toInt()) { world, player, _, _ ->
        player.fairyDreamContainer.mutate { it.clear() }
        player.displayClientMessage(text { "Cleared fairy dream"() }, true)
    }
    registerServerDebugItem("debug_gain_fairy_dream", Items.STRING.toTextureSource(), 0xFF0000BB.toInt()) { world, player, hand, _ ->
        val fairyItemStack = player.getItemInHand(hand.opposite)
        if (fairyItemStack isNotIn FairyCard.item()) return@registerServerDebugItem
        val motif = fairyItemStack.getFairyMotif() ?: return@registerServerDebugItem

        if (!player.isShiftKeyDown) {
            player.fairyDreamContainer.mutate { it[motif] = true }
            GainFairyDreamChannel.sendToClient(player, motif)
        } else {
            player.fairyDreamContainer.mutate { it[motif] = false }
        }
    }

    CommandEvents.onRegisterSubCommand { builder ->
        builder
            .then(
                Commands.literal("dream")
                    .requires { it.hasPermission(2) }
                    .then(
                        Commands.literal("give")
                            .then(
                                Commands.literal("all")
                                    .executesThrowable { context ->
                                        val player = context.source.playerOrException
                                        val count = player.fairyDreamContainer.getOrCreate().gain(player, motifRegistry.toSet())
                                        context.source.sendSuccess({ text { GIVE_ALL_SUCCESS_TRANSLATION("$count") } }, true)
                                        success()
                                    }
                            )
                            .then(
                                Commands.argument("motif", ResourceLocationArgument.id())
                                    .suggests { _, builder ->
                                        motifRegistry.keySet().forEach {
                                            builder.suggest(it.string)
                                        }
                                        builder.buildFuture()
                                    }
                                    .executesThrowable { context ->
                                        val player = context.source.playerOrException
                                        val id = context.getArgument("motif", ResourceLocation::class.java)
                                        val motif = motifRegistry.get(id) ?: failure(text { UNKNOWN_MOTIF_TRANSLATION(id.string) })
                                        val count = player.fairyDreamContainer.getOrCreate().gain(player, setOf(motif))
                                        if (count == 1) {
                                            context.source.sendSuccess({ text { GIVE_ONE_SUCCESS_TRANSLATION(motif.displayName) } }, true)
                                        } else {
                                            failure(text { ALREADY_HAVE_DREAM_TRANSLATION(motif.displayName) })
                                        }
                                        success()
                                    }
                            )
                    )
                    .then(
                        Commands.literal("remove")
                            .then(
                                Commands.literal("all")
                                    .executesThrowable { context ->
                                        val player = context.source.playerOrException
                                        val count = player.fairyDreamContainer.getOrCreate().entries.size
                                        player.fairyDreamContainer.mutate { it.clear() }
                                        context.source.sendSuccess({ text { REMOVE_ALL_SUCCESS_TRANSLATION("$count") } }, true)
                                        success()
                                    }
                            )
                            .then(
                                Commands.argument("motif", ResourceLocationArgument.id())
                                    .suggests { _, builder ->
                                        motifRegistry.keySet().forEach {
                                            builder.suggest(it.string)
                                        }
                                        builder.buildFuture()
                                    }
                                    .executesThrowable { context ->
                                        val player = context.source.playerOrException
                                        val id = context.getArgument("motif", ResourceLocation::class.java)
                                        val motif = motifRegistry.get(id) ?: failure(text { UNKNOWN_MOTIF_TRANSLATION(id.string) })
                                        val have = player.fairyDreamContainer.getOrCreate()[motif]
                                        if (have) {
                                            player.fairyDreamContainer.mutate { it[motif] = false }
                                            context.source.sendSuccess({ text { REMOVE_ONE_SUCCESS_TRANSLATION(motif.displayName) } }, true)
                                        } else {
                                            failure(text { DO_NOT_HAVE_DREAM_TRANSLATION(motif.displayName) })
                                        }
                                        success()
                                    }
                            )
                    )
            )
    }

}

class FairyDreamContainer {
    companion object {
        val ENTRY_CODEC: Codec<Pair<ResourceLocation, Boolean>> = RecordCodecBuilder.create { instance ->
            instance.group(
                ResourceLocation.CODEC.fieldOf("motif").forGetter { it.first },
                Codec.LONG.xmap({ it > 0 }, { if (it) 1 else 0 }).fieldOf("gained").forGetter { it.second },
            ).apply(instance, ::Pair)
        }
        val ENTRY_STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Pair<ResourceLocation, Boolean>> = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC,
            { it.first },
            ByteBufCodecs.VAR_LONG.map({ it > 0 }, { if (it) 1 else 0 }),
            { it.second },
            ::Pair,
        )

        fun fromEntries(entries: List<Pair<ResourceLocation, Boolean>>): FairyDreamContainer {
            val fairyDreamContainer = FairyDreamContainer()
            entries.forEach { (key, value) ->
                val motif = motifRegistry[key] ?: return@forEach
                fairyDreamContainer[motif] = value
            }
            return fairyDreamContainer
        }

        fun toEntries(fairyDreamContainer: FairyDreamContainer): List<Pair<ResourceLocation, Boolean>> {
            val entries = mutableListOf<Pair<ResourceLocation, Boolean>>()
            fairyDreamContainer.entries.forEach { motif ->
                entries += Pair(motif.getIdentifier()!!, true)
            }
            return entries
        }

        val CODEC: Codec<FairyDreamContainer> = ENTRY_CODEC.listOf().xmap(::fromEntries, ::toEntries)
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, FairyDreamContainer> = ENTRY_STREAM_CODEC.list().map(::fromEntries, ::toEntries)
    }

    private val map = mutableSetOf<Motif>()

    operator fun get(motif: Motif) = motif in map

    val entries: Set<Motif> get() = map

    operator fun set(motif: Motif, value: Boolean) {
        if (value) {
            map += motif
        } else {
            map.remove(motif)
        }
    }

    fun gain(player: ServerPlayer, motifs: Set<Motif>): Int {
        val actualAdditionalMotifs = motifs - map
        actualAdditionalMotifs.forEach { motif ->
            set(motif, true)
            if (motif.rare <= 9) {
                player.obtain(motif.createFairyItemStack())
                player.displayClientMessage(text { GAIN_FAIRY_TRANSLATION(motif.displayName) }, true)
            }
            GainFairyDreamChannel.sendToClient(player, motif)
        }
        if (actualAdditionalMotifs.isNotEmpty()) player.fairyDreamContainer.sync()
        return actualAdditionalMotifs.size
    }

    fun clear() = map.clear()

}
