package miragefairy2024.client.util

import miragefairy2024.ModContext
import miragefairy2024.ModEvents
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.MenuScreens.ScreenConstructor
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.MenuAccess
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.MenuType

context(_: ModContext)
fun <M : AbstractContainerMenu, U> (() -> MenuType<M>).registerHandledScreen(screenConstructor: ScreenConstructor<M, U>) where U : Screen, U : MenuAccess<M> {
    ModEvents.onClientInit {
        MenuScreens.register(this(), screenConstructor)
    }
}
