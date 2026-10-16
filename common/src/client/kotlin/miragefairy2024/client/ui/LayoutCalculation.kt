package miragefairy2024.client.ui

/** 長さの余った空間の中で、コンポーネントをどちらへ寄せるかなのだ～🌱 */
enum class Alignment {
    /** 上または左へ寄せるのだ～🌱 */
    START,

    /** 中央へ寄せるのだ～🌱 */
    CENTER,

    /** 下または右へ寄せるのだ～🌱 */
    END,
}

/**
 * 長さ [length] のものを、長さ [space] の中へ寄せたときの、始点の位置を求めるのだ～🌱
 *
 * [miragefairy2024.client.ui.Alignment.CENTER] の式は、空間とコンポーネントをそれぞれ半分にしてから引くのだ～🌱
 * 空間とコンポーネントの長さの偶奇が異なる場合に、差を半分にする式とは 1 だけ異なるのだ～🌱
 */
fun Alignment.align(length: Int, space: Int) = when (this) {
    Alignment.START -> 0
    Alignment.CENTER -> space / 2 - length / 2
    Alignment.END -> space - length
}

/** 子を並べ終えた結果なのだ～🌱 */
data class FlowResult(
    /** 並べる方向における、親の中身の始点から数えた、それぞれの子の位置なのだ～🌱 */
    val offsets: List<Int>,
    /** 並べる方向における、子を並べ終えた長さなのだ～🌱 */
    val length: Int,
)

/**
 * 子を並べる方向へ順に置いて、それぞれの位置を求めるのだ～🌱
 *
 * @param lengths 並べる順に並んだ、子のマージンを含む長さなのだ～🌱
 * @param gap 子と子の間に空ける長さなのだ～🌱
 */
fun flowAlong(lengths: List<Int>, gap: Int): FlowResult {
    val offsets = mutableListOf<Int>()
    var cursor = 0
    lengths.forEach { length ->
        offsets += cursor
        cursor += length + gap
    }
    // 最後の子の後ろには隙間が付かないのだ～🌱
    return FlowResult(offsets, (cursor - gap).coerceAtLeast(0))
}

/**
 * 並べる方向における、子の位置の揃え方による補正の量を求めるのだ～🌱
 *
 * この方向では子がひと続きに繋がっているため、全体を 1 個の塊として寄せるのだ～🌱
 * [miragefairy2024.client.ui.Alignment.CENTER] の式が [miragefairy2024.client.ui.align] と異なり、空間と塊の長さの差を半分にするのだ～🌱
 *
 * @param contentLength [miragefairy2024.client.ui.flowAlong] が返した、子を並べ終えた長さなのだ～🌱
 * @param space 親の中身を置ける長さなのだ～🌱
 */
fun alignAlong(alignment: Alignment, contentLength: Int, space: Int) = when (alignment) {
    Alignment.START -> 0
    Alignment.CENTER -> (space - contentLength) / 2
    Alignment.END -> space - contentLength
}
