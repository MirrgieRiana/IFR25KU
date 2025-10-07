package miragefairy2024.client.util

import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.StackLayout
import io.wispforest.owo.ui.container.WrappingParentComponent
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.OwoUIDrawContext
import io.wispforest.owo.ui.core.Size
import io.wispforest.owo.ui.core.Sizing

fun topBorderLayout(horizontalSizing: Sizing, verticalSizing: Sizing) = BorderLayout(BorderLayout.Direction.TOP, horizontalSizing, verticalSizing)
fun bottomBorderLayout(horizontalSizing: Sizing, verticalSizing: Sizing) = BorderLayout(BorderLayout.Direction.BOTTOM, horizontalSizing, verticalSizing)
fun leftBorderLayout(horizontalSizing: Sizing, verticalSizing: Sizing) = BorderLayout(BorderLayout.Direction.LEFT, horizontalSizing, verticalSizing)
fun rightBorderLayout(horizontalSizing: Sizing, verticalSizing: Sizing) = BorderLayout(BorderLayout.Direction.RIGHT, horizontalSizing, verticalSizing)

class BorderLayout(val direction: Direction, horizontalSizing: Sizing, verticalSizing: Sizing) : WrappingParentComponent<StackLayout>(horizontalSizing, verticalSizing, Containers.stack(horizontalSizing, verticalSizing)) {

    enum class Direction {
        TOP,
        BOTTOM,
        LEFT,
        RIGHT,
    }

    private val child1 = when (direction) {
        Direction.TOP -> Containers.verticalFlow(horizontalSizing, Sizing.content())
        Direction.BOTTOM -> Containers.verticalFlow(horizontalSizing, Sizing.fill(100))
        Direction.LEFT -> Containers.horizontalFlow(Sizing.content(), verticalSizing)
        Direction.RIGHT -> Containers.horizontalFlow(Sizing.fill(100), verticalSizing)
    }
    private val child2 = when (direction) {
        Direction.TOP -> Containers.verticalFlow(horizontalSizing, Sizing.fill(100))
        Direction.BOTTOM -> Containers.verticalFlow(horizontalSizing, Sizing.content())
        Direction.LEFT -> Containers.horizontalFlow(Sizing.fill(100), verticalSizing)
        Direction.RIGHT -> Containers.horizontalFlow(Sizing.content(), verticalSizing)
    }

    init {
        child.child(child1)
        child.child(child2)
    }

    fun child1(component: OwoComponent?) {
        child1.clearChildren()
        if (component != null) child1.child(component)
    }

    fun child2(component: OwoComponent?) {
        child2.clearChildren()
        if (component != null) child2.child(component)
    }

    var gap = 0

    override fun layout(space: Size) {
        when (direction) {
            Direction.TOP -> {
                child1.inflate(calculateChildSpace(space))
                child2.margins(Insets.of(child1.height() + gap, 0, 0, 0))
            }

            Direction.BOTTOM -> {
                child2.inflate(calculateChildSpace(space))
                child1.margins(Insets.of(0, child2.height() + gap, 0, 0))
            }

            Direction.LEFT -> {
                child1.inflate(calculateChildSpace(space))
                child2.margins(Insets.of(0, 0, child1.width() + gap, 0))
            }

            Direction.RIGHT -> {
                child2.inflate(calculateChildSpace(space))
                child1.margins(Insets.of(0, 0, 0, child2.width() + gap))
            }
        }
        super.layout(space)
    }

    override fun draw(context: OwoUIDrawContext, mouseX: Int, mouseY: Int, partialTicks: Float, delta: Float) {
        super.draw(context, mouseX, mouseY, partialTicks, delta)
        drawChildren(context, mouseX, mouseY, partialTicks, delta, childView)
    }

}
