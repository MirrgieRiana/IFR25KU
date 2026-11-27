package miragefairy2024.fabric

import miragefairy2024.ModifyItemEnchantmentsHandler
import miragefairy2024.PlatformProxy
import miragefairy2024.fabric.mixins.api.ItemEnchantmentsCallback
import net.minecraft.core.registries.Registries
import net.minecraft.data.models.BlockModelGenerators
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.ComposterBlock

class FabricPlatformProxy : PlatformProxy {
    override fun registerModifyItemEnchantmentsHandler(handler: ModifyItemEnchantmentsHandler) {
        ItemEnchantmentsCallback.EVENT.register { itemStack, mutableItemEnchantments ->
            val server = currentServer ?: return@register
            handler.modifyItemEnchantments(itemStack, mutableItemEnchantments, server.registryAccess().lookupOrThrow(Registries.ENCHANTMENT))
        }
    }

    override fun registerComposterInput(item: Item, chance: Float) {
        ComposterBlock.COMPOSTABLES.put(item, chance)
    }

    override fun setFullBlock(blockFamilyProvider: Any, block: Identifier) {
        (blockFamilyProvider as BlockModelGenerators.BlockFamilyProvider).fullBlock = block
    }
}
