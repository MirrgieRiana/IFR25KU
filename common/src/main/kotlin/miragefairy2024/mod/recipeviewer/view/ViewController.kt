package miragefairy2024.mod.recipeviewer.view

/** アタッチ済みの View を制御するためのインターフェースなのだ～🌱 */
interface ViewController {
    companion object {
        val NONE: ViewController = object : ViewController {
            override fun setOffset(offset: IntPoint) = Unit
            override fun remove() = Unit
        }
    }

    /**
     * アタッチ時に指定した配置オフセットを [offset] で置き換えるのだ～🌱
     * サイズと UI の実体は維持するのだ～🌱
     */
    fun setOffset(offset: IntPoint)

    /** この配置を解除するのだ～🌱 */
    fun remove()
}

operator fun ViewController.plus(other: ViewController): ViewController {
    return object : ViewController {
        override fun setOffset(offset: IntPoint) {
            this@plus.setOffset(offset)
            other.setOffset(offset)
        }

        override fun remove() {
            this@plus.remove()
            other.remove()
        }
    }
}
