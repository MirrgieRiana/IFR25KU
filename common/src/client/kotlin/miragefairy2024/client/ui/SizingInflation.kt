package miragefairy2024.client.ui

/**
 * 並べる方向における子の長さを、[Sizing.Expand] を後回しにして決めるのだ～🌱
 *
 * [Sizing.Expand] は他の兄弟が取った後の残りを要求するため、1 回の走査では決まらないのだ～🌱
 *
 * @param children 並べる順に並んだ子なのだ～🌱
 * @param space 親が子へ与える空間の、並べる方向の長さなのだ～🌱
 * @param gap 子と子の間に空ける長さなのだ～🌱
 * @param sizingOf 子の、並べる方向の [Sizing] を返す関数なのだ～🌱
 * @param inflate 子を指定の空間で膨らませて、確定した長さをマージンごと返す関数なのだ～🌱
 * @return [children] と同じ順序の、確定した長さなのだ～🌱
 */
fun <T> inflateWithExpand(children: List<T>, space: Int, gap: Int, sizingOf: (T) -> Sizing, inflate: (T, Int) -> Int): List<Int> {
    val lengths = arrayOfNulls<Int>(children.size)
    children.forEachIndexed { index, child ->
        if (!sizingOf(child).isExpand) lengths[index] = inflate(child, space)
    }
    val remainingSpace = (space - lengths.filterNotNull().sum() - gap * (children.size - 1).coerceAtLeast(0)).coerceAtLeast(0)
    children.forEachIndexed { index, child ->
        if (sizingOf(child).isExpand) lengths[index] = inflate(child, remainingSpace)
    }
    return lengths.map { it!! }
}

/**
 * 親が子へ与える空間を決めるのだ～🌱
 *
 * [Sizing.Content] の親の長さは子を並べ終えてから決まるため、その時点の [ownLength] はまだ使えないのだ～🌱
 * そこで、更に上の親から来た [parentSpace] を渡すのだ～🌱
 *
 * @param sizing 親自身の [Sizing] なのだ～🌱
 * @param ownLength 親自身の確定した長さなのだ～🌱
 * @param parentSpace 更に上の親が与えた空間の長さなのだ～🌱
 * @param padding 親の内側の余白の、両端の合計なのだ～🌱
 */
fun childSpace(sizing: Sizing, ownLength: Int, parentSpace: Int, padding: Int): Int = (if (sizing.isContent) parentSpace else ownLength) - padding
