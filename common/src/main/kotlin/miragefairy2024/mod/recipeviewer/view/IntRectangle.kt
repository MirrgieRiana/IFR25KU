package miragefairy2024.mod.recipeviewer.view

data class IntRectangle(val x: Int, val y: Int, val width: Int, val height: Int)

val IntRectangle.topLeft get() = IntPoint(x, y)
val IntRectangle.topRight get() = IntPoint(x + width, y)
val IntRectangle.bottomLeft get() = IntPoint(x, y + height)
val IntRectangle.bottomRight get() = IntPoint(x + width, y + height)
fun IntRectangle.offset(dx: Int, dy: Int) = IntRectangle(x + dx, y + dy, width, height)
fun IntRectangle.grow(d: Int) = this.grow(d, d)
fun IntRectangle.grow(dw: Int, dh: Int) = IntRectangle(x - dw, y - dh, width + dw * 2, height + dh * 2)
operator fun IntRectangle.plus(point: IntPoint) = IntRectangle(x + point.x, y + point.y, width, height)
operator fun IntRectangle.minus(point: IntPoint) = IntRectangle(x - point.x, y - point.y, width, height)
