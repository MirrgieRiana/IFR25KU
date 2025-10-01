package miragefairy2024.mod.recipeviewer

import dev.emi.emi.api.EmiRegistry
import miragefairy2024.InitializationEventRegistry
import miragefairy2024.ModContext

object EmiEvents {
    val onRegister = InitializationEventRegistry<(EmiRegistry) -> Unit>()
}

context(ModContext)
fun initEmiSupport() {

}
