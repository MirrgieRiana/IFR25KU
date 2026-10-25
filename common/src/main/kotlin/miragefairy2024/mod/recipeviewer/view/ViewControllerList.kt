package miragefairy2024.mod.recipeviewer.view

class ViewControllerList : ViewController {
    private val controllers = mutableListOf<ViewController>()

    fun add(controller: ViewController) {
        controllers += controller
    }

    override fun setOffset(offset: IntPoint) {
        controllers.forEach {
            it.setOffset(offset)
        }
    }

    override fun remove() {
        controllers.forEach {
            it.remove()
        }
        controllers.clear()
    }
}

operator fun ViewControllerList.plusAssign(other: ViewController) = this.add(other)
