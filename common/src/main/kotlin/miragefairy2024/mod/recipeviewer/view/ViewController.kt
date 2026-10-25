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

    /** この配置を解除し、[miragefairy2024.mod.recipeviewer.view.ViewController.dispose] に相当する後始末も行うのだ～🌱 */
    fun remove()

    /**
     * 画面側で配置が破棄されることを前提に、イベント登録などの後始末を行うのだ～🌱
     * 配置自体の解除は行わないのだ～🌱
     */
    fun dispose() = Unit
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

        override fun dispose() {
            this@plus.dispose()
            other.dispose()
        }
    }
}
