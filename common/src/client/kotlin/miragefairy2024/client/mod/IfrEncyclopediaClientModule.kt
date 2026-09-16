package miragefairy2024.client.mod

import miragefairy2024.ModContext
import miragefairy2024.mod.onOpenIfrEncyclopediaPageScreen
import miragefairy2024.util.register
import net.minecraft.client.Minecraft

context(ModContext)
fun initIfrEncyclopediaClientModule() {
    onOpenIfrEncyclopediaPageScreen.register {
        Minecraft.getInstance().setScreen(IfrEncyclopediaPageScreen(Minecraft.getInstance().screen, it))
        true
    }
}
