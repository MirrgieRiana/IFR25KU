package miragefairy2024.neoforge

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.PlatformProxy
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent

class NeoForgePlatformProxy : PlatformProxy {
    override fun registerModifyItemEnchantmentsHandler(handler: ModifyItemEnchantmentsHandler) {
        NeoForge.EVENT_BUS.addListener(GetEnchantmentLevelEvent::class.java) { e ->
            handler.modifyItemEnchantments(e.stack, e.enchantments, e.lookup)
        }
    }

    override fun registerComposterInput(item: Item, chance: Float) {
        // nop
    }

    override fun setFullBlock(blockFamilyProvider: Any, block: Identifier) = throw UnsupportedOperationException()
}
