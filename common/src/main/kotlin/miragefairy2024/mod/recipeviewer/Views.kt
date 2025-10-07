@file:Suppress("FunctionName")

package miragefairy2024.mod.recipeviewer

import miragefairy2024.util.EventRegistry
import miragefairy2024.util.IngredientStack
import mirrg.kotlin.helium.atLeast
import mirrg.kotlin.helium.atMost
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import kotlin.math.roundToInt


abstract class ParentView<P> : View {

    protected inner class ChildWithMinSize(val position: P, val viewWithMinSize: ViewWithMinSize)
    protected inner class ChildWithSize(val position: P, val viewWithSize: ViewWithSize)

    @JvmName("withMinSizeOfChild")
    protected fun Child<P, *>.withMinSize(rendererProxy: RendererProxy) = ChildWithMinSize(this.position, this.view.calculateMinSize(rendererProxy))

    @get:JvmName("getMinSizeOfChildWithMinSize")
    protected val ParentView<P>.ChildWithMinSize.minSize get() = this.viewWithMinSize.minSize

    @JvmName("withSizeOfChildWithMinSize")
    protected fun ParentView<P>.ChildWithMinSize.withSize(maxSize: IntPoint) = ChildWithSize(this.position, this.viewWithMinSize.calculateSize(maxSize))

    @get:JvmName("getSizeOfChildWithSize")
    protected val ParentView<P>.ChildWithSize.size get() = this.viewWithSize.size

    @JvmName("assembleOfChildWithSize")
    protected fun ParentView<P>.ChildWithSize.assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = this.viewWithSize.assemble(offset, viewPlacer)

    abstract fun createDefaultPosition(): P

}

class Child<P, V : View>(var position: P, val view: V)

context(Child<*, out ParentView<P>>)
fun <P, V : View> V.configure(block: Child<P, V>.() -> Unit) = Child(this@Child.view.createDefaultPosition(), this).apply { block() }


abstract class ContainerView<P> : ParentView<P>() {
    protected val children = mutableListOf<Child<P, *>>()

    fun add(child: Child<P, *>) {
        children += child
    }
}

operator fun <P> ContainerView<P>.plusAssign(view: View) = this.add(Child(this.createDefaultPosition(), view))
operator fun <P> ContainerView<P>.plusAssign(child: Child<P, *>) = this.add(child)


open class SingleView : ContainerView<Unit>() {
    override fun createDefaultPosition() = Unit
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        val childWithMinSize = children.single().withMinSize(rendererProxy)
        return object : ViewWithMinSize {
            override val minSize = getMinSize(childWithMinSize)
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val childWithSize = childWithMinSize.withSize(maxSize)
                return object : ViewWithSize {
                    override val size = getSize(childWithSize)
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = assemble(childWithSize, offset, viewPlacer)
                }
            }
        }
    }

    protected open fun getMinSize(childWithMinSize: ChildWithMinSize) = childWithMinSize.minSize
    protected open fun getChildWithSize(childWithMinSize: ChildWithMinSize, maxSize: IntPoint) = childWithMinSize.withSize(maxSize)
    protected open fun getSize(childWithSize: ChildWithSize) = childWithSize.size
    protected open fun assemble(childWithSize: ChildWithSize, offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = childWithSize.assemble(offset, viewPlacer)

    val childView get() = children.single().view
}


class MarginView(private val xMin: Int, private val xMax: Int, private val yMin: Int, private val yMax: Int) : SingleView() {
    override fun getMinSize(childWithMinSize: ChildWithMinSize) = childWithMinSize.minSize.plus(xMin + xMax, yMin + yMax)
    override fun getChildWithSize(childWithMinSize: ChildWithMinSize, maxSize: IntPoint) = childWithMinSize.withSize(maxSize.minus(xMin + xMax, yMin + yMax))
    override fun getSize(childWithSize: ChildWithSize) = childWithSize.size.plus(xMin + xMax, yMin + yMax)
    override fun assemble(childWithSize: ParentView<Unit>.ChildWithSize, offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = childWithSize.assemble(offset.offset(xMin, yMin), viewPlacer)
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
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        return childrenWithSize.map {
                            it.assemble(offset + it.position.getOffset(), viewPlacer)
                        }.flatten()
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

                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        return childrenWithSize.map {
                            it.assemble(offset, viewPlacer)
                        }.flatten()
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

                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        var childX = 0
                        return childrenWithSize.map {
                            val childY = when (it.position.alignment) {
                                Alignment.START -> 0
                                Alignment.CENTER -> (size.y - it.size.y) / 2
                                Alignment.END -> size.y - it.size.y
                            }
                            val remover = it.assemble(offset.offset(childX, childY), viewPlacer)
                            childX += it.size.x
                            remover
                        }.flatten()
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

                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        var childY = 0
                        return childrenWithSize.map {
                            val childX = when (it.position.alignment) {
                                Alignment.START -> 0
                                Alignment.CENTER -> (size.x - it.size.x) / 2
                                Alignment.END -> size.x - it.size.x
                            }
                            val remover = it.assemble(offset.offset(childX, childY), viewPlacer)
                            childY += it.size.y
                            remover
                        }.flatten()
                    }
                }
            }
        }
    }
}


class PagingView : ParentView<Alignment>() {
    val childrenGenerators = mutableListOf<ChildrenGenerator<Alignment>>()

    override fun createDefaultPosition() = Alignment.START
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint.ZERO
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                val children = childrenGenerators.flatMap { it.generateChildren(rendererProxy, maxSize) }
                val childrenWithMinSize = children.map { it.withMinSize(rendererProxy) }
                val childrenWithSize = childrenWithMinSize.map { it.withSize(it.minSize) }

                val pages = mutableListOf<List<ParentView<Alignment>.ChildWithSize>>().also { pages ->
                    var page = mutableListOf<ParentView<Alignment>.ChildWithSize>()
                    var y = 0
                    childrenWithSize.forEach {
                        if (page.isNotEmpty() && y + it.size.y > maxSize.y) {
                            // ページが空でなく、これを追加するとはみ出す場合、新しいページを作る
                            pages += page
                            page = mutableListOf()
                            y = 0
                        }
                        page += it
                        y += it.size.y
                    }
                    if (page.isNotEmpty()) pages += page
                    if (pages.isEmpty()) pages += listOf<ParentView<Alignment>.ChildWithSize>()
                }

                return object : ViewWithSize {
                    override val size = maxSize
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>): Remover {
                        var pageIndex = 0

                        val removers = mutableListOf<Remover>()

                        fun clear() {
                            removers.forEach {
                                it.remove()
                            }
                            removers.clear()
                        }

                        fun load() {
                            clear()

                            var childY = 0
                            pages[pageIndex].forEach {
                                removers += it.assemble(offset.offset(0, childY), viewPlacer)
                                childY += it.size.y
                            }
                        }

                        load()

                        return Remover { clear() }
                    }
                }
            }
        }
    }

    fun add(childrenGenerator: ChildrenGenerator<Alignment>) {
        childrenGenerators += childrenGenerator
    }
}

operator fun PagingView.plusAssign(childrenGenerator: ChildrenGenerator<Alignment>) = this.add(childrenGenerator)

fun interface ChildrenGenerator<P> {
    fun generateChildren(rendererProxy: RendererProxy, maxSize: IntPoint): List<Child<P, *>>
}


abstract class SolidView(private val size: IntPoint) : View, PlaceableView {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = size
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = this@SolidView.size
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = this@SolidView.assemble(offset, viewPlacer)
                }
            }
        }
    }

    protected open fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this, offset.sized(size))
}


abstract class SpaceView(private val minSize: IntPoint, private val maxSize: IntPoint) : View {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = this@SpaceView.minSize
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = maxSize min this@SpaceView.maxSize
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = Remover.NONE
                }
            }
        }
    }
}

class XSpaceView(minWidth: Int) : SpaceView(IntPoint(minWidth, 0), IntPoint(Int.MAX_VALUE, 0))
class YSpaceView(minHeight: Int) : SpaceView(IntPoint(0, minHeight), IntPoint(0, Int.MAX_VALUE))


abstract class SlotView : View, PlaceableView {
    var drawBackground = true
    var margin = 1
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint(16 + margin * 2, 16 + margin * 2)
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = minSize
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this@SlotView, offset.sized(size))
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


class TextView(val text: Component) : View, PlaceableView {
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
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this@TextView, offset.sized(size))
                }
            }
        }
    }
}


class ImageView(val texture: ViewTexture) : SolidView(texture.bounds.size)

class NinePatchImageView(
    val textureId: ResourceLocation,
    val xStartSize: Int,
    val xMiddleSize: Int,
    val xEndSize: Int,
    val yStartSize: Int,
    val yMiddleSize: Int,
    val yEndSize: Int,
) : View, PlaceableView {
    override fun calculateMinSize(rendererProxy: RendererProxy): ViewWithMinSize {
        return object : ViewWithMinSize {
            override val minSize = IntPoint.ZERO
            override fun calculateSize(maxSize: IntPoint): ViewWithSize {
                return object : ViewWithSize {
                    override val size = maxSize
                    override fun assemble(offset: IntPoint, viewPlacer: ViewPlacer<PlaceableView>) = viewPlacer.place(this@NinePatchImageView, offset.sized(size))
                }
            }
        }
    }
}


class ImageButtonView(size: IntPoint) : SolidView(size) {
    val texture: ViewTexture? = null
    val hoverTexture: ViewTexture? = null
    val onClick = EventRegistry<() -> Unit>()
}


class ArrowView() : SolidView(IntPoint(24, 17)) {
    var durationMilliSeconds: Int? = null
}
