package miragefairy2024.util

import miragefairy2024.ModContext
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup as FabricCreativeModeTab // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents as CreativeModeTabEvents // MIGRATION2612 ReplaceImport net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation as Identifier // MIGRATION2612 ReplaceImport net.minecraft.resources.Identifier
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

context(_: ModContext)
fun (() -> Item).registerItemGroup(itemGroup: ResourceKey<CreativeModeTab>) {
    CreativeModeTabEvents.modifyEntriesEvent(itemGroup).register {
        it.accept(this())
    }
}

context(_: ModContext)
@Suppress("UnusedReceiverParameter")
fun (() -> Item).registerItemGroup(itemGroup: ResourceKey<CreativeModeTab>, supplier: () -> List<ItemStack>) {
    CreativeModeTabEvents.modifyEntriesEvent(itemGroup).register {
        supplier().forEach { itemStack ->
            it.accept(itemStack)
        }
    }
}


class ItemGroupCard(
    val identifier: Identifier,
    val enName: String,
    val jaName: String,
    icon: () -> ItemStack,
) {
    val translation = Translation({ "itemGroup.${identifier.toLanguageKey()}" }, enName, jaName)
    val itemGroupKey = Registries.CREATIVE_MODE_TAB with identifier
    val itemGroup: CreativeModeTab = FabricCreativeModeTab.builder()
        .icon(icon)
        .title(text { translation() })
        .build()

    context(_: ModContext)
    fun init() {
        Registration(BuiltInRegistries.CREATIVE_MODE_TAB, identifier) { itemGroup }.register()
        translation.enJa()
    }
}
