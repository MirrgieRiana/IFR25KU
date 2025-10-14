package miragefairy2024.mod.recipeviewer.view

data class IntPoint(val x: Int, val y: Int) {
    companion object {
        val ZERO = IntPoint(0, 0)
    }
}

operator fun IntPoint.unaryMinus() = IntPoint(-x, -y)
fun IntPoint.plus(dx: Int, dy: Int) = IntPoint(x + dx, y + dy)
fun IntPoint.offset(dx: Int, dy: Int) = this.plus(dx, dy)
fun IntPoint.minus(dx: Int, dy: Int) = this.plus(-dx, -dy)
operator fun IntPoint.plus(other: IntPoint) = this.plus(other.x, other.y)
operator fun IntPoint.minus(other: IntPoint) = this.minus(other.x, other.y)
