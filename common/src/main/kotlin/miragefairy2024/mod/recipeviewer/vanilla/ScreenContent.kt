package miragefairy2024.mod.recipeviewer.vanilla

import miragefairy2024.mod.recipeviewer.view.View

interface ScreenContent {
    val view: View
}

fun ScreenContent(rootView: View): ScreenContent = object : ScreenContent {
    override val view = rootView
}
