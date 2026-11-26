package miragefairy2024.mod.recipeviewer.view

class ViewControllerList : ViewController {
    private val controllers = mutableListOf<ViewController>()

    fun add(controller: ViewController) {
        controllers += controller
    }

    fun clear() {
        controllers.clear()
    }

    override fun setOffset(offset: IntPoint) {
        controllers.forEach {
            it.setOffset(offset)
        }
    }

    override fun detach() {
        controllers.forEach {
            it.detach()
        }
    }

    override fun dispose() {
        controllers.forEach {
            it.dispose()
        }
    }
}

operator fun ViewControllerList.plusAssign(other: ViewController) = this.add(other)
