package miragefairy2024.mod.recipeviewer.view

/** アタッチ済みの View を制御するためのインターフェースなのだ～🌱 */
interface ViewController {
    companion object {
        val NONE: ViewController = object : ViewController {
            override fun remove() = Unit
        }
    }

    /** この配置を解除するのだ～🌱 */
    fun remove()
}

operator fun ViewController.plus(other: ViewController): ViewController {
    return object : ViewController {
        override fun remove() {
            this@plus.remove()
            other.remove()
        }
    }
}

fun Iterable<ViewController>.flatten(): ViewController {
    return object : ViewController {
        override fun remove() {
            this@flatten.forEach {
                it.remove()
            }
        }
    }
}
