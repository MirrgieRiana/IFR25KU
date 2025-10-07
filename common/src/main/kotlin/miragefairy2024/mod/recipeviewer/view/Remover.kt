package miragefairy2024.mod.recipeviewer.view

fun interface Remover {
    companion object {
        val NONE = Remover { }
    }

    fun remove()
}

fun Iterable<Remover>.flatten() = Remover { this.forEach { it.remove() } }

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
