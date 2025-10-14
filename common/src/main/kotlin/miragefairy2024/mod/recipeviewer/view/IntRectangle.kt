package miragefairy2024.mod.recipeviewer.view

data class IntRectangle(val x: Int, val y: Int, val sizeX: Int, val sizeY: Int) {
    companion object {
        val ZERO = IntRectangle(0, 0, 0, 0)
    }
}

val IntRectangle.topLeft get() = IntPoint(x, y)
val IntRectangle.topRight get() = IntPoint(x + sizeX, y)
val IntRectangle.bottomLeft get() = IntPoint(x, y + sizeY)
val IntRectangle.bottomRight get() = IntPoint(x + sizeX, y + sizeY)
fun IntRectangle.offset(dx: Int, dy: Int) = IntRectangle(x + dx, y + dy, sizeX, sizeY)
fun IntRectangle.grow(d: Int) = this.grow(d, d)
fun IntRectangle.grow(dw: Int, dh: Int) = IntRectangle(x - dw, y - dh, sizeX + dw * 2, sizeY + dh * 2)
operator fun IntRectangle.plus(point: IntPoint) = IntRectangle(x + point.x, y + point.y, sizeX, sizeY)
operator fun IntRectangle.minus(point: IntPoint) = IntRectangle(x - point.x, y - point.y, sizeX, sizeY)
