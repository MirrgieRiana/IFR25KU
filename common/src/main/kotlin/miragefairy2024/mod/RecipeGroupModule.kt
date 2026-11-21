package miragefairy2024.mod

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items

val recipeGroupRegistry = mutableMapOf<Item, String>()

context(_: ModContext)
fun initRecipeGroupModule() = ModEvents.onInitialize {
    recipeGroupRegistry[Items.STICK] = "sticks"
}
