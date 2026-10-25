package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.clientProxy
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType

/** 汎用の View 画面をクライアント初期化時に登録するのだ～🌱 */
context(ModContext)
fun <T> (() -> MenuType<T>).registerViewScreen() where T : AbstractContainerMenu, T : ViewProviderMenu = ModEvents.onClientInit {
    clientProxy!!.registerViewScreen(this())
}
