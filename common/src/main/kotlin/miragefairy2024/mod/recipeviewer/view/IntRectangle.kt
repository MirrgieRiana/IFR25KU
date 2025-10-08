package miragefairy2024.mod.recipeviewer.view

data class IntRectangle(val x: Int, val y: Int, val sizeX: Int, val sizeY: Int) {
    companion object {
        val ZERO = IntRectangle(0, 0, 0, 0)
    }
}

val IntRectangle.offset get() = IntPoint(x, y)
val IntRectangle.size get() = IntPoint(sizeX, sizeY)
fun IntRectangle.offset(dx: Int, dy: Int) = IntRectangle(x + dx, y + dy, sizeX, sizeY)
fun IntRectangle.grow(d: Int) = this.grow(d, d)
fun IntRectangle.grow(dx: Int, dy: Int) = this.grow(dx, dx, dy, dy)
fun IntRectangle.grow(dxMin: Int, dxMax: Int, dyMin: Int, dyMax: Int) = IntRectangle(x - dxMin, y - dyMin, sizeX + dxMin + dxMax, sizeY + dyMin + dyMax)
operator fun IntRectangle.plus(point: IntPoint) = IntRectangle(x + point.x, y + point.y, sizeX, sizeY)
operator fun IntRectangle.minus(point: IntPoint) = IntRectangle(x - point.x, y - point.y, sizeX, sizeY)
