package miragefairy2024.mod.recipeviewer.view

import miragefairy2024.util.EventRegistry
import miragefairy2024.util.fire

/** スクロールの範囲と現在値を保持し、変更後に通知するのだ～🌱 */
class ScrollState {
    val onChange = EventRegistry<() -> Unit>()

    var contentLength = 0
        private set
    var viewportLength = 0
        private set
    val maxValue get() = (contentLength - viewportLength).coerceAtLeast(0).toDouble()

    private var currentValue = 0.0
    var value: Double
        get() = currentValue
        set(value) {
            require(value.isFinite()) { "Scroll value must be finite" }
            val newValue = value.coerceIn(0.0, maxValue)
            if (currentValue == newValue) return
            currentValue = newValue
            onChange.fire()
        }

    fun setBounds(contentLength: Int, viewportLength: Int) {
        require(contentLength >= 0 && viewportLength >= 0) { "Scroll lengths must be non-negative" }
        if (this.contentLength == contentLength && this.viewportLength == viewportLength) return
        this.contentLength = contentLength
        this.viewportLength = viewportLength
        currentValue = currentValue.coerceIn(0.0, maxValue)
        onChange.fire()
    }
}
