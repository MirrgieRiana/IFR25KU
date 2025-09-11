package miragefairy2024.util

import mirrg.kotlin.helium.castOrNull
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.DoubleTag
import net.minecraft.nbt.FloatTag
import net.minecraft.nbt.IntTag
import net.minecraft.nbt.ListTag
import net.minecraft.nbt.LongTag
import net.minecraft.nbt.NumericTag
import net.minecraft.nbt.ShortTag
import net.minecraft.nbt.StringTag
import net.minecraft.nbt.Tag
import kotlin.reflect.KProperty


// NbtWrapper

/**
 * [Tag]のインスタンスを提供するインターフェースです。
 * 参照先の[Tag]は実体が未生成である可能性があります。
 * [NbtWrapper]を使って参照先のNbtElementを生成することができます。
 */
interface NbtWrapper<out N : Tag> {
    fun getOrNull(): N?
    fun getOrCreate(): N
}

/**
 * 常に生成済みの[Tag]を返す[NbtWrapper]を生成します。
 */
val <N : Tag> N.wrapper get() = InstanceNbtWrapper(this)

class InstanceNbtWrapper<N : Tag>(private val element: N) : NbtWrapper<N>, NbtProperty<N, Nothing> {
    override fun getOrNull() = element
    override fun getOrCreate() = element
    override fun get() = element
    override fun set(value: Nothing) = value
}

/**
 * [CompoundTag]の子要素にアクセスする[NbtProperty]を生成します。
 * この[NbtProperty]はnullをセット可能であり、その場合はそのキーを削除します。
 */
operator fun NbtWrapper<CompoundTag>.get(key: String): NbtProperty<Tag?, Tag?> {
    val parent = this
    return object : NbtProperty<Tag?, Tag?> {
        override fun get() = parent.getOrNull()?.get(key)
        override fun set(value: Tag?) {
            if (value != null) {
                parent.getOrCreate().put(key, value)
            } else {
                parent.getOrCreate().remove(key)
            }
        }
    }
}

/**
 * [ListTag]の子要素にアクセスする[NbtProperty]を生成します。
 */
operator fun NbtWrapper<ListTag>.get(index: Int): NbtProperty<Tag?, Tag> {
    val parent = this
    return object : NbtProperty<Tag?, Tag> {
        override fun get() = parent.getOrNull()?.getOrNull(index)
        override fun set(value: Tag) {
            parent.getOrCreate()[index] = value
        }
    }
}


// NbtProperty

/**
 * 親NBT要素の子に対する取得および設定のアクセスを提供するインターフェースです。
 * [NbtProperty]は常に親NBT要素の存在を仮定し、設定アクセスは親NBT要素への改変を引き起こします。
 */
interface NbtProperty<out G, in S> {
    fun get(): G
    fun set(value: S)
}

operator fun <G> NbtProperty<G, *>.getValue(thisRef: Any?, property: KProperty<*>) = this.get()
operator fun <S> NbtProperty<*, S>.setValue(thisRef: Any?, property: KProperty<*>, value: S) = this.set(value)

inline fun <G, S> NbtProperty(crossinline getter: () -> G, crossinline setter: (S) -> Unit) = object : NbtProperty<G, S> {
    override fun get() = getter()
    override fun set(value: S) = setter(value)
}

/**
 * 親[NbtProperty]を[CompoundTag]と仮定し、その子要素への[NbtProperty]を返します。
 * 親NBT要素が[CompoundTag]でなかった場合、取得アクセスではnullが返却され、設定アクセスでは親NBT要素に新しい[CompoundTag]を割り当てます。
 */
operator fun NbtProperty<Tag?, Tag>.get(key: String): NbtProperty<Tag?, Tag?> {
    val parent = this
    val nbtWrapper = object : NbtWrapper<CompoundTag> {
        override fun getOrNull() = parent.get() as? CompoundTag
        override fun getOrCreate() = getOrNull() ?: CompoundTag().also { parent.set(it) }
    }
    return nbtWrapper[key]
}

/**
 * 親[NbtProperty]を[ListTag]と仮定し、その子要素への[NbtProperty]を返します。
 * 親NBT要素が[ListTag]でなかった場合、取得アクセスではnullが返却され、設定アクセスでは親NBT要素に新しい[ListTag]を割り当てます。
 */
operator fun NbtProperty<Tag?, Tag>.get(index: Int): NbtProperty<Tag?, Tag> {
    val parent = this
    val nbtWrapper = object : NbtWrapper<ListTag> {
        override fun getOrNull() = parent.get() as? ListTag
        override fun getOrCreate() = getOrNull() ?: ListTag().also { parent.set(it) }
    }
    return nbtWrapper[index]
}


// utilities

val NbtProperty<Tag?, Tag?>.list get() = NbtProperty<ListTag?, ListTag?>({ this.get()?.castOrNull() }, { this.set(it) })
val NbtProperty<Tag?, Tag?>.compound get() = NbtProperty<CompoundTag?, CompoundTag?>({ this.get()?.castOrNull() }, { this.set(it) })

val NbtProperty<Tag?, Tag?>.byte get() = NbtProperty<Byte?, Byte?>({ this.get()?.castOrNull<NumericTag>()?.asByte }, { this.set(it?.let { a -> ByteTag.valueOf(a) }) })
val NbtProperty<Tag?, Tag?>.short get() = NbtProperty<Short?, Short?>({ this.get()?.castOrNull<NumericTag>()?.asShort }, { this.set(it?.let { a -> ShortTag.valueOf(a) }) })
val NbtProperty<Tag?, Tag?>.int get() = NbtProperty<Int?, Int?>({ this.get()?.castOrNull<NumericTag>()?.asInt }, { this.set(it?.let { a -> IntTag.valueOf(a) }) })
val NbtProperty<Tag?, Tag?>.long get() = NbtProperty<Long?, Long?>({ this.get()?.castOrNull<NumericTag>()?.asLong }, { this.set(it?.let { a -> LongTag.valueOf(a) }) })
val NbtProperty<Tag?, Tag?>.float get() = NbtProperty<Float?, Float?>({ this.get()?.castOrNull<NumericTag>()?.asFloat }, { this.set(it?.let { a -> FloatTag.valueOf(a) }) })
val NbtProperty<Tag?, Tag?>.double get() = NbtProperty<Double?, Double?>({ this.get()?.castOrNull<NumericTag>()?.asDouble }, { this.set(it?.let { a -> DoubleTag.valueOf(a) }) })
val NbtProperty<Tag?, Tag?>.number get() = NbtProperty<Number?, Number?>({ this.get()?.castOrNull<NumericTag>()?.asNumber }, { this.set(it?.let { a -> DoubleTag.valueOf(a.toDouble()) }) })
val NbtProperty<Tag?, Tag?>.string get() = NbtProperty<String?, String?>({ this.get()?.castOrNull<StringTag>()?.asString }, { this.set(it?.toNbtString()) })
val NbtProperty<Tag?, Tag?>.boolean get() = NbtProperty<Boolean?, Boolean?>({ this.get()?.castOrNull<NumericTag>()?.asByte?.let { it != 0.toByte() } }, { this.set(it?.let { a -> ByteTag.valueOf(a) }) })

@get:JvmName("listGetter")
val NbtProperty<Tag, Nothing>.list get() = NbtProperty<ListTag?, Nothing>({ this.get().castOrNull() }, {})

@get:JvmName("compoundGetter")
val NbtProperty<Tag, Nothing>.compound get() = NbtProperty<CompoundTag?, Nothing>({ this.get().castOrNull() }, {})

@get:JvmName("byteGetter")
val NbtProperty<Tag, Nothing>.byte get() = NbtProperty<Byte?, Nothing>({ this.get().castOrNull<NumericTag>()?.asByte }, {})

@get:JvmName("shortGetter")
val NbtProperty<Tag, Nothing>.short get() = NbtProperty<Short?, Nothing>({ this.get().castOrNull<NumericTag>()?.asShort }, {})

@get:JvmName("intGetter")
val NbtProperty<Tag, Nothing>.int get() = NbtProperty<Int?, Nothing>({ this.get().castOrNull<NumericTag>()?.asInt }, {})

@get:JvmName("longGetter")
val NbtProperty<Tag, Nothing>.long get() = NbtProperty<Long?, Nothing>({ this.get().castOrNull<NumericTag>()?.asLong }, {})

@get:JvmName("floatGetter")
val NbtProperty<Tag, Nothing>.float get() = NbtProperty<Float?, Nothing>({ this.get().castOrNull<NumericTag>()?.asFloat }, {})

@get:JvmName("doubleGetter")
val NbtProperty<Tag, Nothing>.double get() = NbtProperty<Double?, Nothing>({ this.get().castOrNull<NumericTag>()?.asDouble }, {})

@get:JvmName("numberGetter")
val NbtProperty<Tag, Nothing>.number get() = NbtProperty<Number?, Nothing>({ this.get().castOrNull<NumericTag>()?.asNumber }, {})

@get:JvmName("stringGetter")
val NbtProperty<Tag, Nothing>.string get() = NbtProperty<String?, Nothing>({ this.get().castOrNull<StringTag>()?.asString }, {})

@get:JvmName("booleanGetter")
val NbtProperty<Tag, Nothing>.boolean get() = NbtProperty<Boolean?, Nothing>({ this.get().castOrNull<NumericTag>()?.asByte?.let { it != 0.toByte() } }, {})

val NbtProperty<Tag?, Tag?>.map
    get() = NbtProperty<Map<String, Tag>?, Map<String, Tag>?>({
        val nbt = this.get()?.castOrNull<CompoundTag>() ?: return@NbtProperty null
        nbt.allKeys.associate { key -> key!! to nbt[key]!! }
    }, {
        this.set(it?.let { a ->
            CompoundTag().also { nbt ->
                a.forEach { entry ->
                    nbt.put(entry.key, entry.value)
                }
            }
        })
    })

fun <T> NbtProperty<T?, T?>.orDefault(getter: () -> T) = object : NbtProperty<T, T> {
    override fun get() = this@orDefault.get() ?: getter()
    override fun set(value: T) = this@orDefault.set(value)
}
