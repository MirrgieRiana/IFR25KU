package miragefairy2024.mod

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.util.createItemStack
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack

class HarvestNotation(val seed: ItemStack, val crops: List<ItemStack>) {
    companion object {
        val harvestNotations = mutableListOf<HarvestNotation>()
    }
}

context(ModContext)
fun (() -> Item).registerHarvestNotation(vararg drops: () -> Item) = this.registerHarvestNotation(drops.asIterable())

context(ModContext)
fun (() -> Item).registerHarvestNotation(drops: Iterable<() -> Item>) = ModEvents.onInitialize {
    HarvestNotation.harvestNotations += HarvestNotation(this().createItemStack(), drops.map { it().createItemStack() })
}

context(ModContext)
fun initHarvestNotationModule() {

}
