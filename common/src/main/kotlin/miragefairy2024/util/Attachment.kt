package miragefairy2024.util

import miragefairy2024.ModContext
import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents as ServerEntityLevelChangeEvents // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents

operator fun <T> AttachmentTarget.get(type: AttachmentType<T>) = AttachmentDelegate(this, type)

class AttachmentDelegate<T>(val value: AttachmentTarget, val type: AttachmentType<T>)

fun <T> AttachmentDelegate<T>.get(): T? {
    return this.value.getAttached(this.type)
}

fun <T> AttachmentDelegate<T>.getOrDefault(): T {
    return this.value.getAttached(this.type) ?: this.type.initializer()!!.get()
}

fun <T> AttachmentDelegate<T>.getOrCreate(): T {
    return this.value.getAttachedOrCreate(this.type)
}

fun <T> AttachmentDelegate<T>.set(data: T?) {
    if (data != null) {
        this.value.setAttached(this.type, data) // ドキュメントに反して data == null で呼び出すと爆死する
    } else {
        this.value.removeAttached(this.type)
    }
}

fun <T> AttachmentDelegate<T>.modify(block: (T?) -> T?) {
    val data = this.get()
    val data2 = block(data)
    this.set(data2)
}

fun <T> AttachmentDelegate<T>.mutate(block: (T) -> Unit) {
    val data = this.get() ?: this.type.initializer()!!.get()
    block(data)
    this.set(data)
}

fun <T> AttachmentDelegate<T>.sync() {
    this.set(this.get())
}


context(_: ModContext)
fun AttachmentType<*>.register() {
    // AttachmentTypeはインスタンス化した時点で登録成立

    if (this.isSynced) {
        ServerPlayConnectionEvents.JOIN.register { handler, _, server ->
            server.execute {
                AttachmentDelegate(handler.player, this).sync()
            }
        }
        ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register { player, _, _ ->
            AttachmentDelegate(player, this).sync()
        }
    }
}
