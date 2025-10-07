package miragefairy2024.mod.recipeviewer.view

data class IntRectangle(val x: Int, val y: Int, val xSize: Int, val ySize: Int) {
    companion object {
        val ZERO = IntRectangle(0, 0, 0, 0)
    }
}

val IntRectangle.offset get() = IntPoint(x, y)
val IntRectangle.size get() = IntPoint(xSize, ySize)
fun IntRectangle.offset(dx: Int, dy: Int) = IntRectangle(x + dx, y + dy, xSize, ySize)
fun IntRectangle.grow(d: Int) = this.grow(d, d)
fun IntRectangle.grow(dx: Int, dy: Int) = this.grow(dx, dx, dy, dy)
fun IntRectangle.grow(dxMin: Int, dxMax: Int, dyMin: Int, dyMax: Int) = IntRectangle(x - dxMin, y - dyMin, xSize + dxMin + dxMax, ySize + dyMin + dyMax)
operator fun IntRectangle.plus(point: IntPoint) = IntRectangle(x + point.x, y + point.y, xSize, ySize)
operator fun IntRectangle.minus(point: IntPoint) = IntRectangle(x - point.x, y - point.y, xSize, ySize)
