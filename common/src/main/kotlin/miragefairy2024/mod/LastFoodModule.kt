package miragefairy2024.mod

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mixins.api.EatFoodCallback
import miragefairy2024.util.INSTANT_CODEC
import miragefairy2024.util.INSTANT_STREAM_CODEC
import miragefairy2024.util.get
import miragefairy2024.util.optional
import miragefairy2024.util.register
import miragefairy2024.util.set
import mirrg.kotlin.java.hydrogen.orNull
import mirrg.kotlin.java.hydrogen.toOptional
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import java.time.Instant
import java.util.Optional

context(ModContext)
fun initLastFoodModule() {
    LAST_FOOD_ATTACHMENT_TYPE.register()
    EatFoodCallback.EVENT.register { entity, level, stack, foodProperties ->
        if (level.isClientSide) return@register
        if (entity !is Player) return@register
        entity as ServerPlayer
        entity.lastFood.set(LastFood(stack.copy(), Instant.now()))
    }
}

val LAST_FOOD_ATTACHMENT_TYPE: AttachmentType<LastFood> = AttachmentRegistry.create(MirageFairy2024.identifier("last_food")) {
    it.persistent(LastFood.CODEC)
    it.initializer(::LastFood)
    it.syncWith(LastFood.STREAM_CODEC, AttachmentSyncPredicate.targetOnly())
}

val Entity.lastFood get() = this[LAST_FOOD_ATTACHMENT_TYPE]

class LastFood(var itemStack: ItemStack? = null, var time: Instant? = null) {
    companion object {
        val CODEC: Codec<LastFood> = RecordCodecBuilder.create { instance ->
            instance.group(
                ItemStack.CODEC.optionalFieldOf("item_stack").forGetter { it.itemStack.toOptional() },
                INSTANT_CODEC.optionalFieldOf("time").forGetter { it.time.toOptional() },
            ).apply(instance, ::LastFood)
        }
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, LastFood> = StreamCodec.composite(
            ItemStack.STREAM_CODEC.optional(),
            { it.itemStack.toOptional() },
            INSTANT_STREAM_CODEC.optional(),
            { it.time.toOptional() },
            ::LastFood,
        )
    }

    constructor(itemStack: Optional<ItemStack>, time: Optional<Instant>) : this(itemStack.orNull, time.orNull)
}
