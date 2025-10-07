@file:Suppress("FunctionName")

package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.IngredientStack
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import kotlin.math.roundToInt


abstract class ContainerView<P> : View {
    protected val children = mutableListOf<Child<P, *>>()

    protected inner class ChildWithMinSize(val position: P, val viewWithMinSize: ViewWithMinSize)
    protected inner class ChildWithSize(val position: P, val viewWithSize: ViewWithSize)

    protected fun Child<P, *>.withMinSize(rendererProxy: RendererProxy) = ChildWithMinSize(this.position, this.view.calculateMinSize(rendererProxy))
    protected val ContainerView<P>.ChildWithMinSize.minSize get() = this.viewWithMinSize.minSize
    protected fun ContainerView<P>.ChildWithMinSize.withSize(maxSize: IntPoint) = ChildWithSize(this.position, this.viewWithMinSize.calculateSize(maxSize))
    protected val ContainerView<P>.ChildWithSize.size get() = this.viewWithSize.size
    protected fun ContainerView<P>.ChildWithSize.assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) = this.viewWithSize.assemble(position, viewPlacer)

    abstract fun createDefaultPosition(): P

    fun add(child: Child<P, *>) {
        children += child
    }
}

operator fun <P> ContainerView<P>.plusAssign(view: View) = this.add(Child(this.createDefaultPosition(), view))
operator fun <P> ContainerView<P>.plusAssign(child: Child<P, *>) = this.add(child)

class Child<P, V : View>(var position: P, val view: V)

context(Child<*, out ContainerView<P>>)
fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }


class SingleView : ContainerView<Unit>() {
    override fun createDefaultPosition() = Unit
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childWithMinSize = children.single().withMinSize(rendererProxy)
        return object : ViewWithMinSize {
            override val minSize = childWithMinSize.minSize
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childWithSize = childWithMinSize.withSize(maxSize)
                return object : ViewWithSize {
                    override val size = childWithSize.size
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        childWithSize.assemble(position, viewPlacer)
                    }
                }
            }
        }
    }

    val childView get() = children.single().view
}


class MarginView(private val xMin: Int, private val xMax: Int, private val yMin: Int, private val yMax: Int) : ContainerView<Unit>() {
    override fun createDefaultPosition() = Unit
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childWithMinSize = children.single().withMinSize(rendererProxy)
        return object : ViewWithMinSize {
            override val minSize = childWithMinSize.minSize.plus(xMin + xMax, yMin + yMax)
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childWithSize = childWithMinSize.withSize(maxSize.minus(xMin + xMax, yMin + yMax))
                return object : ViewWithSize {
                    override val size = childWithSize.size.plus(xMin + xMax, yMin + yMax)
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        childWithSize.assemble(position.offset(xMin, yMin), viewPlacer)
                    }
                }
            }
        }
    }
}

fun MarginView(x: Int, y: Int) = MarginView(x, x, y, y)
fun MarginView(margin: Int) = MarginView(margin, margin)


class AbsoluteView(private val size: IntPoint) : ContainerView<AbsoluteView.Position>() {
    sealed class Position {
        abstract fun getMaxSize(childMinSize: IntPoint, maxSize: IntPoint): IntPoint
        abstract fun getOffset(): IntPoint
    }

    data object Fill : Position() {
        override fun getMaxSize(childMinSize: IntPoint, maxSize: IntPoint) = maxSize
        override fun getOffset() = IntPoint.ZERO
    }

    data class Offset(@JvmField val offset: IntPoint) : Position() {
        override fun getMaxSize(childMinSize: IntPoint, maxSize: IntPoint) = childMinSize
        override fun getOffset() = offset
    }

    data class Bounds(val bounds: IntRectangle) : Position() {
        override fun getMaxSize(childMinSize: IntPoint, maxSize: IntPoint) = bounds.size
        override fun getOffset() = bounds.offset
    }

    override fun createDefaultPosition() = Fill
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = size
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childrenWithSize = childrenWithMinSize.map { it.withSize(it.position.getMaxSize(it.minSize, maxSize)) }
                return object : ViewWithSize {
                    override val size = this@AbsoluteView.size
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        childrenWithSize.forEach {
                            it.assemble(position + it.position.getOffset(), viewPlacer)
                        }
                    }
                }
            }
        }
    }
}


class StackView : ContainerView<Unit>() {
    override fun createDefaultPosition() = Unit
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = IntPoint(
                childrenWithMinSize.maxOfOrNull { it.minSize.x } ?: 0,
                childrenWithMinSize.maxOfOrNull { it.minSize.y } ?: 0,
            )

            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childrenWithSize = childrenWithMinSize.map { it.withSize(maxSize) }
                return object : ViewWithSize {
                    override val size = IntPoint(
                        childrenWithSize.maxOfOrNull { it.size.x } ?: 0,
                        childrenWithSize.maxOfOrNull { it.size.y } ?: 0,
                    )

                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        childrenWithSize.forEach {
                            it.assemble(position, viewPlacer)
                        }
                    }
                }
            }
        }
    }
}


abstract class ListView : ContainerView<ListView.Position>() {
    class Position(var alignment: Alignment, var weight: Double)

    override fun createDefaultPosition() = Position(Alignment.START, 0.0)
}

class XListView : ListView() {
    var minHeight = 0
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = IntPoint(
                childrenWithMinSize.sumOf { it.minSize.x },
                (childrenWithMinSize.maxOfOrNull { it.minSize.y } ?: 0) atLeast minHeight,
            )

            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val remainingX = maxSize.x - minSize.x
                val totalWeight = childrenWithMinSize.sumOf { it.position.weight }
                val childrenWithSize = run {
                    var childX = 0.0
                    childrenWithMinSize.map {
                        val nextChildX = childX + it.minSize.x + if (totalWeight > 0.0) remainingX * (it.position.weight / totalWeight) else 0.0
                        val childWithSize = it.withSize(IntPoint(nextChildX.roundToInt() - childX.roundToInt(), maxSize.y))
                        childX = nextChildX
                        childWithSize
                    }
                }
                return object : ViewWithSize {
                    override val size = IntPoint(
                        childrenWithSize.sumOf { it.size.x },
                        (childrenWithSize.maxOfOrNull { it.size.y } ?: 0) atLeast minHeight,
                    )

                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        var childX = 0
                        childrenWithSize.forEach {
                            val childY = when (it.position.alignment) {
                                Alignment.START -> 0
                                Alignment.CENTER -> (size.y - it.size.y) / 2
                                Alignment.END -> size.y - it.size.y
                            }
                            it.assemble(position.offset(childX, childY), viewPlacer)
                            childX += it.size.x
                        }
                    }
                }
            }
        }
    }
}

class YListView : ListView() {
    var minWidth = 0
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = IntPoint(
                (childrenWithMinSize.maxOfOrNull { it.minSize.x } ?: 0) atLeast minWidth,
                childrenWithMinSize.sumOf { it.minSize.y },
            )

            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val remainingY = maxSize.y - minSize.y
                val totalWeight = childrenWithMinSize.sumOf { it.position.weight }
                val childrenWithSize = run {
                    var childY = 0.0
                    childrenWithMinSize.map {
                        val nextChildY = childY + it.minSize.y + if (totalWeight > 0.0) remainingY * (it.position.weight / totalWeight) else 0.0
                        val childWithSize = it.withSize(IntPoint(maxSize.x, nextChildY.roundToInt() - childY.roundToInt()))
                        childY = nextChildY
                        childWithSize
                    }
                }
                return object : ViewWithSize {
                    override val size = IntPoint(
                        (childrenWithSize.maxOfOrNull { it.size.x } ?: 0) atLeast minWidth,
                        childrenWithSize.sumOf { it.size.y },
                    )

                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        var childY = 0
                        childrenWithSize.forEach {
                            val childX = when (it.position.alignment) {
                                Alignment.START -> 0
                                Alignment.CENTER -> (size.x - it.size.x) / 2
                                Alignment.END -> size.x - it.size.x
                            }
                            it.assemble(position.offset(childX, childY), viewPlacer)
                            childY += it.size.y
                        }
                    }
                }
            }
        }
    }
}


abstract class SolidView(private val size: IntPoint) : View {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = size
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = this@SolidView.size
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        viewPlacer.place(this@SolidView, position.sized(size))
                    }
                }
            }
        }
    }
}


abstract class SpaceView(private val minSize: IntPoint, private val maxSize: IntPoint) : View {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = this@SpaceView.minSize
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = maxSize min this@SpaceView.maxSize
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {

                    }
                }
            }
        }
    }
}

class XSpaceView(minWidth: Int) : SpaceView(IntPoint(minWidth, 0), IntPoint(Int.MAX_VALUE, 0))
class YSpaceView(minHeight: Int) : SpaceView(IntPoint(0, minHeight), IntPoint(0, Int.MAX_VALUE))


abstract class SlotView : View {
    var drawBackground = true
    var margin = 1
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint(16 + margin * 2, 16 + margin * 2)
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = minSize
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        viewPlacer.place(this@SlotView, position.sized(size))
                    }
                }
            }
        }
    }
}

fun <V : SlotView> V.noBackground() = this.apply { this.drawBackground = false }
fun <V : SlotView> V.margin(margin: Int) = this.apply { this.margin = margin }
fun <V : SlotView> V.noMargin() = this.margin(0)

class InputSlotView(val ingredientStack: IngredientStack) : SlotView()
class CatalystSlotView(val ingredientStack: IngredientStack) : SlotView()
class OutputSlotView(val itemStack: ItemStack) : SlotView()


class TextView(val text: Component) : View {
    var minWidth = 0
    var color: ColorPair? = null
    var shadow = true
    var xAlignment: Alignment? = null
    var tooltip: List<Component>? = null
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint(minWidth, rendererProxy.getTextHeight())
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = IntPoint(rendererProxy.calculateTextWidth(text) atMost maxSize.x atLeast minSize.x, minSize.y)
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        viewPlacer.place(this@TextView, position.sized(size))
                    }
                }
            }
        }
    }
}


class ImageView(val textureId: ResourceLocation, val bound: IntRectangle, val textureSize: IntPoint) : SolidView(bound.size)

class NinePatchImageView(
    val textureId: ResourceLocation,
    val xStartSize: Int,
    val xMiddleSize: Int,
    val xEndSize: Int,
    val yStartSize: Int,
    val yMiddleSize: Int,
    val yEndSize: Int,
) : View {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint.ZERO
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = maxSize
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        viewPlacer.place(this@NinePatchImageView, position.sized(size))
                    }
                }
            }
        }
    }
}


class ArrowView() : SolidView(IntPoint(24, 17)) {
    var durationMilliSeconds: Int? = null
}
