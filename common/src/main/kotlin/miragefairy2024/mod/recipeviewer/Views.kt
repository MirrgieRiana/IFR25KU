@file:Suppress("FunctionName")

package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.IngredientStack
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import kotlin.math.roundToInt


abstract class ContainerView<P, V : View> : View {
    protected val children = mutableListOf<Child>()

    protected inner class Child(val position: P, val view: V)
    protected inner class ChildWithMinSize(val position: P, val viewWithMinSize: ViewWithMinSize)
    protected inner class ChildWithSize(val position: P, val viewWithSize: ViewWithSize)

    protected fun ContainerView<P, V>.Child.withMinSize(rendererProxy: RendererProxy) = ChildWithMinSize(this.position, this.view.calculateMinSize(rendererProxy))
    protected val ContainerView<P, V>.ChildWithMinSize.minSize get() = this.viewWithMinSize.minSize
    protected fun ContainerView<P, V>.ChildWithMinSize.withSize(maxSize: IntPoint) = ChildWithSize(this.position, this.viewWithMinSize.calculateSize(maxSize))
    protected val ContainerView<P, V>.ChildWithSize.size get() = this.viewWithSize.size
    protected fun ContainerView<P, V>.ChildWithSize.assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) = this.viewWithSize.assemble(position, viewPlacer)

    fun add(position: P, view: V) {
        children += Child(position, view)
    }
}

interface DefaultedContainerView<V : View> {
    fun add(view: V)
}

operator fun <V : View> DefaultedContainerView<V>.plusAssign(view: V) = this.add(view)
operator fun <P, V : View> ContainerView<P, V>.plusAssign(pair: Pair<P, V>) = this.add(pair.first, pair.second)


class SingleView<V : View> : ContainerView<Unit, V>(), DefaultedContainerView<V> {
    override fun add(view: V) = add(Unit, view)
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

fun SingleView(block: SingleView<View>.() -> Unit) = SingleView<View>().apply { block() }


class AbsoluteView<V : View>(private val size: IntPoint) : ContainerView<Pair<IntPoint, IntPoint?>?, V>(), DefaultedContainerView<V> {
    override fun add(view: V) = add(null, view)
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
        return object : ViewWithMinSize {
            override val minSize = size
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childrenWithSize = childrenWithMinSize.map { it.withSize(if (it.position == null) maxSize else it.position.second ?: it.minSize) }
                return object : ViewWithSize {
                    override val size = this@AbsoluteView.size
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {
                        childrenWithSize.forEach {
                            it.assemble(if (it.position == null) position else position + it.position.first, viewPlacer)
                        }
                    }
                }
            }
        }
    }
}

fun AbsoluteView(size: IntPoint, block: AbsoluteView<View>.() -> Unit) = AbsoluteView<View>(size).apply { block() }


abstract class ListView<V : View> : ContainerView<ListView.Position, V>(), DefaultedContainerView<V> {
    class Position(val alignment: Alignment, val weight: Double)

    override fun add(view: V) = add(Position(Alignment.START, 0.0), view)
}

class XListView<V : View> : ListView<V>() {
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

fun XListView(block: XListView<View>.() -> Unit) = XListView<View>().apply { block() }

class YListView<V : View> : ListView<V>() {
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

fun YListView(block: YListView<View>.() -> Unit) = YListView<View>().apply { block() }


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


abstract class SpaceView(private val minSize: IntPoint) : View {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = this@SpaceView.minSize
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = maxSize
                    override fun assemble(position: IntPoint, viewPlacer: ViewPlacer<View>) {

                    }
                }
            }
        }
    }
}

class XSpaceView(minWidth: Int) : SpaceView(IntPoint(minWidth, 0))
class YSpaceView(minHeight: Int) : SpaceView(IntPoint(0, minHeight))


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
    var horizontalAlignment: Alignment? = null
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


class ImageView(val textureId: ResourceLocation, val bound: IntRectangle) : SolidView(bound.size)


class ArrowView() : SolidView(IntPoint(24, 17)) {
    var durationMilliSeconds: Int? = null
}
