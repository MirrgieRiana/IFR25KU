package miragefairy2024.mod.recipeviewer.views

import miragefairy2024.mod.recipeviewer.view.View

fun View(block: Child<Unit, SingleView>.() -> Unit): View = SingleView().apply { block(Child(Unit, this)) }.childView
