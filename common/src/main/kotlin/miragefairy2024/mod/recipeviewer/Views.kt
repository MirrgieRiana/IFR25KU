@file:Suppress("FunctionName")

package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.IngredientStack
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack


abstract class ContainerView<P, V : View> : View {
    protected val children = mutableListOf<Child>()

    protected inner class Child(val position: P, val view: V)
    protected inner class ChildWithMinSize(val position: P, val viewWithMinSize: ViewWithMinSize)
    protected inner class ChildWithSize(val position: P, val viewWithSize: ViewWithSize)

    protected fun getChildrenWithMinSize(
        rendererProxy: RendererProxy,
    ): List<ContainerView<P, V>.ChildWithMinSize> {
        return children.map {
            ChildWithMinSize(it.position, it.view.calculateMinSize(rendererProxy))
        }
    }

    protected fun getChildrenWithSize(
        childrenWithMinSize: List<ContainerView<P, V>.ChildWithMinSize>,
        maxSizeProvider: (ContainerView<P, V>.ChildWithMinSize) -> IntPoint,
    ): List<ContainerView<P, V>.ChildWithSize> {
        return childrenWithMinSize.map {
            ChildWithSize(it.position, it.viewWithMinSize.calculateSize(maxSizeProvider(it)))
        }
    }

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
        val childWithMinSize = children.single().view.calculateMinSize(rendererProxy)
        return object : ViewWithMinSize {
            override val minSize = childWithMinSize.minSize
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childWithSize = childWithMinSize.calculateSize(maxSize)
                return object : ViewWithSize {
                    override val size = childWithSize.size
                    override fun assemble(bounds: IntRectangle, viewPlacer: ViewPlacer<View>) {
                        childWithSize.assemble(bounds, viewPlacer)
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
        val childrenWithMinSize = getChildrenWithMinSize(rendererProxy)
        return object : ViewWithMinSize {
            override val minSize = size
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childrenWithSize = getChildrenWithSize(childrenWithMinSize) {
                    if (it.position == null) maxSize else it.position.second ?: it.viewWithMinSize.minSize
                }
                return object : ViewWithSize {
                    override val size = this@AbsoluteView.size
                    override fun assemble(bounds: IntRectangle, viewPlacer: ViewPlacer<View>) {
                        childrenWithSize.forEach {
                            it.viewWithSize.assemble(
                                if (it.position == null) {
                                    bounds
                                } else {
                                    IntRectangle(
                                        bounds.x + it.position.first.x,
                                        bounds.y + it.position.first.y,
                                        it.viewWithSize.size.x,
                                        it.viewWithSize.size.y,
                                    )
                                },
                                viewPlacer,
                            )
                        }
                    }
                }
            }
        }
    }
}

fun AbsoluteView(size: IntPoint, block: AbsoluteView<View>.() -> Unit) = AbsoluteView<View>(size).apply { block() }


abstract class ListView<V : View> : ContainerView<Alignment, V>(), DefaultedContainerView<V> {
    override fun add(view: V) = add(Alignment.START, view)
}

class XListView<V : View> : ListView<V>() {
    var minHeight = 0
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childrenWithMinSize = getChildrenWithMinSize(rendererProxy)
        return object : ViewWithMinSize {
            override val minSize = IntPoint(
                childrenWithMinSize.sumOf { it.viewWithMinSize.minSize.x },
                (childrenWithMinSize.maxOfOrNull { it.viewWithMinSize.minSize.y } ?: 0) atLeast minHeight,
            )

            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childrenWithSize = getChildrenWithSize(childrenWithMinSize) {
                    IntPoint(it.viewWithMinSize.minSize.x, maxSize.y)
                }
                return object : ViewWithSize {
                    override val size = IntPoint(
                        childrenWithSize.sumOf { it.viewWithSize.size.x },
                        (childrenWithSize.maxOfOrNull { it.viewWithSize.size.y } ?: 0) atLeast minHeight,
                    )

                    override fun assemble(bounds: IntRectangle, viewPlacer: ViewPlacer<View>) {
                        var childX = 0
                        childrenWithSize.forEach {
                            val childY = when (it.position) {
                                Alignment.START -> 0
                                Alignment.CENTER -> (size.y - it.viewWithSize.size.y) / 2
                                Alignment.END -> size.y - it.viewWithSize.size.y
                            }
                            it.viewWithSize.assemble(
                                IntRectangle(
                                    bounds.x + childX,
                                    bounds.y + childY,
                                    it.viewWithSize.size.x,
                                    it.viewWithSize.size.y,
                                ),
                                viewPlacer,
                            )
                            childX += it.viewWithSize.size.x
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
        val childrenWithMinSize = getChildrenWithMinSize(rendererProxy)
        return object : ViewWithMinSize {
            override val minSize = IntPoint(
                (childrenWithMinSize.maxOfOrNull { it.viewWithMinSize.minSize.x } ?: 0) atLeast minWidth,
                childrenWithMinSize.sumOf { it.viewWithMinSize.minSize.y },
            )

            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childrenWithSize = getChildrenWithSize(childrenWithMinSize) {
                    IntPoint(maxSize.x, it.viewWithMinSize.minSize.y)
                }
                return object : ViewWithSize {
                    override val size = IntPoint(
                        (childrenWithSize.maxOfOrNull { it.viewWithSize.size.x } ?: 0) atLeast minWidth,
                        childrenWithSize.sumOf { it.viewWithSize.size.y },
                    )

                    override fun assemble(bounds: IntRectangle, viewPlacer: ViewPlacer<View>) {
                        var childY = 0
                        childrenWithSize.forEach {
                            val childX = when (it.position) {
                                Alignment.START -> 0
                                Alignment.CENTER -> (size.x - it.viewWithSize.size.x) / 2
                                Alignment.END -> size.x - it.viewWithSize.size.x
                            }
                            it.viewWithSize.assemble(
                                IntRectangle(
                                    bounds.x + childY,
                                    bounds.y + childX,
                                    it.viewWithSize.size.x,
                                    it.viewWithSize.size.y,
                                ),
                                viewPlacer,
                            )
                            childY += it.viewWithSize.size.y
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
                    override fun assemble(bounds: IntRectangle, viewPlacer: ViewPlacer<View>) {
                        viewPlacer.place(this@SolidView, bounds)
                    }
                }
            }
        }
    }
}


abstract class SpaceView(private val size: IntPoint) : View {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = size
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = this@SpaceView.size
                    override fun assemble(bounds: IntRectangle, viewPlacer: ViewPlacer<View>) {

                    }
                }
            }
        }
    }
}

class XSpaceView(width: Int) : SpaceView(IntPoint(width, 0))
class YSpaceView(height: Int) : SpaceView(IntPoint(0, height))


abstract class SlotView : View {
    var drawBackground = true
    var margin = 1
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint(16 + margin * 2, 16 + margin * 2)
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = minSize
                    override fun assemble(bounds: IntRectangle, viewPlacer: ViewPlacer<View>) {
                        viewPlacer.place(this@SlotView, bounds)
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
                    override fun assemble(bounds: IntRectangle, viewPlacer: ViewPlacer<View>) {
                        viewPlacer.place(this@TextView, bounds)
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
