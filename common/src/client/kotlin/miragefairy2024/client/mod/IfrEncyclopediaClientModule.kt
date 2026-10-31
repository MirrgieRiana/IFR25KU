package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.clientProxy
import miragefairy2024.mod.IfrEncyclopediaPageScreen
import miragefairy2024.mod.onOpenIfrEncyclopediaPageScreen
import miragefairy2024.util.register

context(ModContext)
fun initIfrEncyclopediaClientModule() {
    onOpenIfrEncyclopediaPageScreen.register {
        clientProxy!!.openViewScreen(IfrEncyclopediaPageScreen(it))
        true
    }
}
