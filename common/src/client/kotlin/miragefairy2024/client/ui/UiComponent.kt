package miragefairy2024.client.ui

/**
 * 親が与えた空間から自分の寸法を決める、画面のコンポーネントの土台なのだ～🌱
 *
 * 描画と入力は、この土台を継承する側が、バニラのウィジェットへ繋ぐのだ～🌱
 */
abstract class UiComponent {
    var horizontalSizing: Sizing = Sizing.content()
        private set

    var verticalSizing: Sizing = Sizing.content()
        private set

    var margins = Insets.of(0)
        private set

    /** [inflate] が確定した横の長さなのだ～🌱 */
    var width = 0
        private set

    /** [inflate] が確定した縦の長さなのだ～🌱 */
    var height = 0
        private set

    /** 親の中身の始点から数えた、このコンポーネントの左端の位置なのだ～🌱 */
    var x = 0
        private set

    /** 親の中身の始点から数えた、このコンポーネントの上端の位置なのだ～🌱 */
    var y = 0
        private set

    fun sizing(horizontalSizing: Sizing, verticalSizing: Sizing) {
        this.horizontalSizing = horizontalSizing
        this.verticalSizing = verticalSizing
    }

    fun margins(margins: Insets) {
        this.margins = margins
    }

    /** 中身を並べるのに要る横の長さなのだ～🌱 */
    protected open fun determineContentWidth() = 0

    /** 中身を並べるのに要る縦の長さなのだ～🌱 */
    protected open fun determineContentHeight() = 0

    /**
     * 親が与えた [space] の中で、自分の寸法を確定するのだ～🌱
     *
     * マージンは自分の寸法の外側にあるため、与えられた空間から先に引くのだ～🌱
     */
    open fun inflate(space: Size) {
        width = horizontalSizing.inflate(space.width - margins.horizontal) { determineContentWidth() }
        height = verticalSizing.inflate(space.height - margins.vertical) { determineContentHeight() }
    }

    /** 親の中身の始点から数えた位置へ、このコンポーネントを置くのだ～🌱 */
    fun moveTo(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    /** マージンを含めた横の長さなのだ～🌱 */
    val fullWidth get() = width + margins.horizontal

    /** マージンを含めた縦の長さなのだ～🌱 */
    val fullHeight get() = height + margins.vertical
}
