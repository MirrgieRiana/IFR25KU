package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.Remover

class RemoverList : Remover {
    private val list = mutableListOf<Remover>()

    fun add(remover: Remover) {
        list += remover
    }

    override fun remove() {
        list.forEach {
            it.remove()
        }
        list.clear()
    }
}

operator fun RemoverList.plusAssign(other: Remover) = this.add(other)
