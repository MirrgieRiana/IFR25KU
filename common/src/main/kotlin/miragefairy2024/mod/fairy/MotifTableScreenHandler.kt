package miragefairy2024.mod.fairy

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Chance
import miragefairy2024.util.CondensedItem
import miragefairy2024.util.EMPTY_ITEM_STACK
import miragefairy2024.util.Registration
import miragefairy2024.util.register
import miragefairy2024.util.string
import miragefairy2024.util.toIdentifier
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack

val MOTIF_TABLE_STREAM_CODEC = object : StreamCodec<RegistryFriendlyByteBuf, List<CondensedMotifChance>> {
    override fun encode(`object`: RegistryFriendlyByteBuf, object2: List<CondensedMotifChance>) {
        `object`.writeInt(object2.size)
        object2.forEach {
            ItemStack.STREAM_CODEC.encode(`object`, it.showingItemStack)
            `object`.writeUtf(it.item.item.item.getIdentifier()!!.string)
            `object`.writeDouble(it.item.weight)
            `object`.writeDouble(it.item.item.count)
        }
    }

    override fun decode(`object`: RegistryFriendlyByteBuf): List<CondensedMotifChance> {
        val length = `object`.readInt()
        val chanceTable = mutableListOf<CondensedMotifChance>()
        repeat(length) {
            val showingItemStack = ItemStack.STREAM_CODEC.decode(`object`)
            val motifId = `object`.readUtf()
            val rate = `object`.readDouble()
            val count = `object`.readDouble()
            chanceTable += CondensedMotifChance(showingItemStack, Chance(rate, CondensedItem(count, motifRegistry.getValue(motifId.toIdentifier())!!)))
        }
        return chanceTable
    }
}

val motifTableScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("motif_table")) {
    ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
        MotifTableScreenHandler(syncId, buf)
    }, MOTIF_TABLE_STREAM_CODEC)
}

context(ModContext)
fun initMotifTableScreenHandler() {
    motifTableScreenHandlerType.register()
}

class MotifTableScreenHandler(syncId: Int, val chanceTable: List<CondensedMotifChance>) : AbstractContainerMenu(motifTableScreenHandlerType(), syncId) {
    override fun stillValid(player: Player) = true
    override fun quickMoveStack(player: Player, slot: Int) = EMPTY_ITEM_STACK
}
