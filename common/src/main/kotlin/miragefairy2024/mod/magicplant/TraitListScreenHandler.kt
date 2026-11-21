package miragefairy2024.mod.magicplant

import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.util.Registration
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.register
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.item.ItemStack

val traitListScreenHandlerType = Registration(BuiltInRegistries.MENU, MirageFairy2024.identifier("trait_list")) {
    ExtendedScreenHandlerType({ syncId, playerInventory, buf ->
        TraitListScreenHandler(syncId, playerInventory, ContainerLevelAccess.NULL, buf.first, buf.second)
    }, TraitListScreenHandler.STREAM_CODEC)
}

val traitListScreenTranslation = Translation({ "gui.${MirageFairy2024.identifier("trait_list").toLanguageKey()}" }, "Traits", "特性")

context(_: ModContext)
fun initTraitListScreenHandler() {
    traitListScreenHandlerType.register()
    traitListScreenTranslation.enJa()
}

class TraitListScreenHandler(syncId: Int, val playerInventory: Inventory, val context: ContainerLevelAccess, val traitStacks: TraitStacks, val blockPos: BlockPos) : AbstractContainerMenu(traitListScreenHandlerType(), syncId) {
    companion object {
        val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, Pair<TraitStacks, BlockPos>> = StreamCodec.composite(
            TraitStacks.STREAM_CODEC,
            { it.first },
            BlockPos.STREAM_CODEC,
            { it.second },
            ::Pair,
        )
    }

    override fun stillValid(player: Player) = true
    override fun quickMoveStack(player: Player, slot: Int): ItemStack = slots[slot].item
}
