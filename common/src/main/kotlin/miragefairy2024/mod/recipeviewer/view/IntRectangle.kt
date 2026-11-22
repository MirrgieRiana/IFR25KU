package miragefairy2024.mod.recipeviewer.view

import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

data class IntRectangle(val x: Int, val y: Int, val sizeX: Int, val sizeY: Int) {
    init {
        require(sizeX >= 0 && sizeY >= 0) { "Rectangle dimensions must be non-negative" }
    }

    companion object {
        val ZERO = IntRectangle(0, 0, 0, 0)
    }
}

val IntRectangle.x1 get() = x
val IntRectangle.y1 get() = y
val IntRectangle.x2 get() = x + sizeX
val IntRectangle.y2 get() = y + sizeY
val IntRectangle.offset get() = IntPoint(x, y)
val IntRectangle.size get() = IntPoint(sizeX, sizeY)
fun IntRectangle.offset(dx: Int, dy: Int) = IntRectangle(x + dx, y + dy, sizeX, sizeY)
fun IntRectangle.grow(d: Int) = this.grow(d, d)
fun IntRectangle.grow(dx: Int, dy: Int) = this.grow(dx, dx, dy, dy)
fun IntRectangle.grow(dxMin: Int, dxMax: Int, dyMin: Int, dyMax: Int) = IntRectangle(
    Math.toIntExact(this.x.toLong() - dxMin),
    Math.toIntExact(this.y.toLong() - dyMin),
    Math.toIntExact((this.sizeX.toLong() + dxMin + dxMax).coerceAtLeast(0L)),
    Math.toIntExact((this.sizeY.toLong() + dyMin + dyMax).coerceAtLeast(0L)),
)
operator fun IntRectangle.plus(point: IntPoint) = IntRectangle(x + point.x, y + point.y, sizeX, sizeY)
operator fun IntRectangle.minus(point: IntPoint) = IntRectangle(x - point.x, y - point.y, sizeX, sizeY)
fun IntRectangle.contains(x: Int, y: Int) = x >= this.x1 && x < this.x2 && y >= this.y1 && y < this.y2
fun IntRectangle.contains(x: Double, y: Double) = x >= this.x1 && x < this.x2 && y >= this.y1 && y < this.y2
operator fun IntRectangle.contains(point: IntPoint) = this.contains(point.x, point.y)

/** 交わらない矩形を与えた場合は大きさ 0 の矩形を返す。 */
fun IntRectangle.intersect(other: IntRectangle): IntRectangle {
    val x1 = this.x1 max other.x1
    val y1 = this.y1 max other.y1
    val x2 = minOf(this.x.toLong() + this.sizeX, other.x.toLong() + other.sizeX)
    val y2 = minOf(this.y.toLong() + this.sizeY, other.y.toLong() + other.sizeY)
    return IntRectangle(x1, y1, Math.toIntExact((x2 - x1).coerceAtLeast(0L)), Math.toIntExact((y2 - y1).coerceAtLeast(0L)))
}

/** 大きさが 0 の矩形もカバーされる。 */
fun IntRectangle.union(other: IntRectangle): IntRectangle {
    val x1 = this.x1 min other.x1
    val y1 = this.y1 min other.y1
    val x2 = maxOf(this.x.toLong() + this.sizeX, other.x.toLong() + other.sizeX)
    val y2 = maxOf(this.y.toLong() + this.sizeY, other.y.toLong() + other.sizeY)
    return IntRectangle(x1, y1, Math.toIntExact(x2 - x1), Math.toIntExact(y2 - y1))
}
