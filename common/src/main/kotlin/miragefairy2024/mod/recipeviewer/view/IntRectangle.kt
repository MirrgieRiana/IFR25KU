package miragefairy2024.mod.recipeviewer.view

import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.max
import mirrg.kotlin.helium.min

data class IntRectangle(val x: Int, val y: Int, val sizeX: Int, val sizeY: Int) {
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
fun IntRectangle.grow(dxMin: Int, dxMax: Int, dyMin: Int, dyMax: Int) = IntRectangle(x - dxMin, y - dyMin, sizeX + dxMin + dxMax, sizeY + dyMin + dyMax)
operator fun IntRectangle.plus(point: IntPoint) = IntRectangle(x + point.x, y + point.y, sizeX, sizeY)
operator fun IntRectangle.minus(point: IntPoint) = IntRectangle(x - point.x, y - point.y, sizeX, sizeY)
fun IntRectangle.contains(x: Int, y: Int) = x >= this.x && x < this.x + this.sizeX && y >= this.y && y < this.y + this.sizeY
operator fun IntRectangle.contains(point: IntPoint) = this.contains(point.x, point.y)

/** 交わらない矩形を与えた場合は大きさ 0 の矩形を返す。 */
fun IntRectangle.intersect(other: IntRectangle): IntRectangle {
    val x1 = this.x1 max other.x1
    val y1 = this.y1 max other.y1
    val x2 = this.x2 min other.x2
    val y2 = this.y2 min other.y2
    return IntRectangle(x1, y1, x2 - x1 atLeast 0, y2 - y1 atLeast 0)
}

/** 大きさが 0 の矩形もカバーされる。 */
fun IntRectangle.union(other: IntRectangle): IntRectangle {
    val x1 = this.x1 min other.x1
    val y1 = this.y1 min other.y1
    val x2 = this.x2 max other.x2
    val y2 = this.y2 max other.y2
    return IntRectangle(x1, y1, x2 - x1, y2 - y1)
}
