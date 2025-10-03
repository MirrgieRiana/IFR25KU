package miragefairy2024.util

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.core.RegistryCodecs
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.ItemStack
import java.math.BigInteger
import java.time.Instant
import java.util.Optional

fun <B : ByteBuf, V> StreamCodec<B, V>.optional(): StreamCodec<B, Optional<V>> = ByteBufCodecs.optional(this)
fun <B : ByteBuf, V> StreamCodec<B, V>.list(): StreamCodec<B, List<V>> = this.apply(ByteBufCodecs.list())

val INSTANT_CODEC: Codec<Instant> = Codec.LONG.xmap(Instant::ofEpochMilli, Instant::toEpochMilli)
val INSTANT_STREAM_CODEC: StreamCodec<ByteBuf, Instant> = ByteBufCodecs.VAR_LONG.map(Instant::ofEpochMilli, Instant::toEpochMilli)

val BIG_INTEGER_CODEC: Codec<BigInteger> = Codec.withAlternative(
    Codec.withAlternative(
        Codec.STRING.comapFlatMap(
            a@{
                val bigInteger = it.toBigIntegerOrNull() ?: return@a DataResult.error { "invalid BigInteger: $it" }
                DataResult.success(bigInteger)
            },
            BigInteger::toString
        ),
        Codec.LONG,
        Long::toBigInteger,
    ),
    Codec.INT,
    Int::toBigInteger,
)
val BIG_INTEGER_STREAM_CODEC: StreamCodec<ByteBuf, BigInteger> = ByteBufCodecs.STRING_UTF8.map(String::toBigInteger, BigInteger::toString)

fun <A, B> pairCodecOf(codecA: Codec<A>, codecB: Codec<B>): Codec<Pair<A, B>> {
    return RecordCodecBuilder.create { instance ->
        instance.group(
            codecA.fieldOf("A").forGetter { it.first },
            codecB.fieldOf("B").forGetter { it.second }
        ).apply(instance, ::Pair)
    }
}


data class ItemStacks(val itemStacks: List<ItemStack>) {
    companion object {
        val EMPTY = ItemStacks(listOf())

        val CODEC: Codec<ItemStacks> = Slot.CODEC.listOf().xmap(::fromSlots, ::toSlots)
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, ItemStacks> = Slot.STREAM_CODEC.list().map(::fromSlots, ::toSlots)

        fun fromSlots(slots: List<Slot>): ItemStacks {
            val maxSlot = slots.maxOfOrNull { it.slot } ?: return EMPTY
            val table = slots.associateBy { it.slot }
            val list = (0..maxSlot).map { table[it]?.item ?: EMPTY_ITEM_STACK }
            return ItemStacks(list)
        }

        fun toSlots(itemStacks: ItemStacks): List<Slot> {
            return itemStacks.itemStacks
                .withIndex()
                .filter { it.value.isNotEmpty }
                .map { Slot(it.value, it.index) }
        }
    }

    data class Slot(val item: ItemStack, val slot: Int) {
        companion object {
            val CODEC: Codec<Slot> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ItemStack.CODEC.fieldOf("item").forGetter { it.item },
                    Codec.INT.fieldOf("slot").forGetter { it.slot }
                ).apply(instance, ::Slot)
            }
            val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Slot> = StreamCodec.composite(
                ItemStack.STREAM_CODEC,
                { it.item },
                ByteBufCodecs.INT,
                { it.slot },
                ::Slot
            )
        }
    }
}

/**
 * ダミーのペイロードを追加するStreamCodecです。
 * NeoForgeにおいてMenuを開く際にペイロードが空であるとバニラの機構を使うため、これを抑制するために用いられます。
 *
 * @see net.minecraft.server.level.ServerPlayer.openMenu
 */
fun dummyUnitStreamCodec(): StreamCodec<ByteBuf, Unit> = ByteBufCodecs.VAR_INT.map({ }, { 0 })

fun <T> ResourceKey<Registry<T>>.toHolderSetCodec(): Codec<HolderSet<T>> = RegistryCodecs.homogeneousList(this)
