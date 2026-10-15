package miragefairy2024.client.ui

import kotlin.math.roundToInt

/**
 * コンポーネントの縦または横の片方の長さの決め方なのだ～🌱
 *
 * バニラのウィジェットは長さを数値で直接受け取るため、親が与えた空間から長さを導く機構を持たないのだ～🌱
 * それが無いと、画面ごとに座標を手計算することになるのだ～🌱
 */
sealed interface Sizing {
    /**
     * [space] の中に収まる実際の長さを求めるのだ～🌱
     *
     * @param space 親が与えた空間から、親の内側の余白を引いた長さなのだ～🌱
     * @param contentLength 中身を並べるのに要る長さを返す関数なのだ～🌱
     */
    fun inflate(space: Int, contentLength: () -> Int): Int

    /** 中身の長さに依存するなら true なのだ～🌱 */
    val isContent: Boolean

    /** 兄弟を配置した後の残りの空間を要求するなら true なのだ～🌱 */
    val isExpand: Boolean

    /** 長さを直接指定するのだ～🌱 */
    data class Fixed(val length: Int) : Sizing {
        override fun inflate(space: Int, contentLength: () -> Int) = length
        override val isContent get() = false
        override val isExpand get() = false
    }

    /** 中身の長さに、[padding] を両端それぞれへ足した長さにするのだ～🌱 */
    data class Content(val padding: Int) : Sizing {
        override fun inflate(space: Int, contentLength: () -> Int) = contentLength() + padding * 2
        override val isContent get() = true
        override val isExpand get() = false
    }

    /** 親が与えた空間の [percent] パーセントにするのだ～🌱 */
    data class Fill(val percent: Int) : Sizing {
        override fun inflate(space: Int, contentLength: () -> Int) = (percent / 100.0 * space).roundToInt()
        override val isContent get() = false
        override val isExpand get() = false
    }

    /**
     * 他の兄弟を配置した後の残りの空間の [percent] パーセントにするのだ～🌱
     *
     * 計算式は [Fill] と同じで、[inflate] へ残りの空間を渡す側が [Expand] を後回しにするのだ～🌱
     */
    data class Expand(val percent: Int) : Sizing {
        override fun inflate(space: Int, contentLength: () -> Int) = (percent / 100.0 * space).roundToInt()
        override val isContent get() = false
        override val isExpand get() = true
    }

    companion object {
        fun fixed(length: Int): Sizing = Fixed(length)
        fun content(): Sizing = Content(0)
        fun content(padding: Int): Sizing = Content(padding)
        fun fill(): Sizing = Fill(100)
        fun fill(percent: Int): Sizing = Fill(percent)
        fun expand(): Sizing = Expand(100)
        fun expand(percent: Int): Sizing = Expand(percent)
    }
}
