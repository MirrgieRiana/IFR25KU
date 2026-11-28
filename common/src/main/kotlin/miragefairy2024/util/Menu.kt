package miragefairy2024.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import miragefairy2024.clientProxy
import miragefairy2024.mod.recipeviewer.vanilla.ViewProviderMenu
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType

/** 汎用の View 画面をクライアント初期化時に登録するのだ～🌱 */
context(_: ModContext)
fun <M, T> (() -> MenuType<M>).registerViewScreen() where M : AbstractContainerMenu, M : ViewProviderMenu<T> = ModEvents.onClientInit {
    clientProxy!!.registerViewScreen(this())
}
