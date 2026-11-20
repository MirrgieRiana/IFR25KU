package miragefairy2024.mod.magicplant

import com.mojang.serialization.Lifecycle
import miragefairy2024.MirageFairy2024
import miragefairy2024.mod.NinePatchTextureCard
import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextView
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.TextButtonView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.mod.recipeviewer.views.YScrollbarView
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.style
import miragefairy2024.util.text
import mirrg.kotlin.helium.atLeast
import net.fabricmc.fabric.mixin.registry.sync.RegistriesAccessor
import net.minecraft.SharedConstants
import net.minecraft.core.BlockPos
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceKey
import net.minecraft.server.Bootstrap
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.level.Level
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito

private class TraitEncyclopediaRenderingProxy : RenderingProxy {
    val widths = mutableListOf<Int>()

    override fun calculateTextWidth(text: FormattedText) = text.string.length * 6
    override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
    override fun getTextHeight() = 9

    override fun wrapText(text: FormattedText, maxWidth: Int): List<FormattedCharSequence> {
        widths += maxWidth
        return text.string.split('\n').flatMap { line ->
            line.chunked(maxWidth / 6 atLeast 1).map { FormattedCharSequence.forward(it, Style.EMPTY) }
        }
    }
}

private fun TraitEncyclopediaPageViewWrapper.place(): List<Pair<PlaceableView, IntRectangle>> {
    val placements = mutableListOf<Pair<PlaceableView, IntRectangle>>()
    fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
        placements += Pair(view, bounds)
        if (view is YScrollContainerView) return view.childView.attachTo(bounds.offset, ::place)
        return ViewController.NONE
    }
    this.view.attachTo(IntPoint.ZERO, ::place)
    return placements
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraitEncyclopediaPageViewWrapperTest {

    private lateinit var trait: Trait

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val rootRegistry: MappedRegistry<WritableRegistry<*>> = MappedRegistry(ResourceKey.createRegistryKey(Registries.ROOT_REGISTRY_NAME), Lifecycle.stable(), false)
        // テスト JVM では Fabric の Mixin が適用されないので、アクセサーがテスト用のルートレジストリを返すようにするのだ～🌱
        Mockito.mockStatic(RegistriesAccessor::class.java).use { registries ->
            registries.`when`<WritableRegistry<WritableRegistry<*>>> { RegistriesAccessor.getROOT() }.thenReturn(rootRegistry)
            trait = object : Trait(Style.EMPTY.withBold(true), text { "message".repeat(200)() }) {
                override val conditions = emptyList<TraitCondition>()
                override val primaryEffect = TraitEffectKeyCard.TEMPERATURE.traitEffectKey
                override val traitEffectKeyEntries = emptyList<TraitEffectKeyEntry>()
                override fun getTraitEffects(level: Level, blockPos: BlockPos, blockEntity: MagicPlantBlockEntity?, traitLevel: Int): MutableTraitEffects? = null
            }
            Registry.register(traitRegistry, MirageFairy2024.identifier("test_trait"), trait)
        }
    }

    @Test
    fun `名前とアイコンを固定した幅160のカード内で本文だけを折り返してスクロールするのだ`() {
        val proxy = TraitEncyclopediaRenderingProxy()
        val view = TraitEncyclopediaPageViewWrapper(trait)
        view.view.initSizeCalculation(proxy, { 400 }, { 240 })

        Assertions.assertEquals(IntPoint(400, 240), view.view.evaluateActualSize())
        val placements = view.place()
        val background = placements.single { it.first is NinePatchImageView }
        Assertions.assertEquals(IntRectangle(120, 20, 160, 186), background.second)
        Assertions.assertEquals(NinePatchTextureCard.TRAIT_BACKGROUND.texture, (background.first as NinePatchImageView).textureId)
        val icon = placements.single { it.first is ImageView }
        Assertions.assertEquals(IntRectangle(184, 50, 32, 32), icon.second)
        Assertions.assertEquals(MirageFairy2024.identifier("textures/gui/traits/test_trait.png"), (icon.first as ImageView).texture.id)
        val scroll = placements.single { it.first is YScrollContainerView }
        Assertions.assertEquals(IntRectangle(125, 87, 145, 114), scroll.second)
        val scrollView = scroll.first as YScrollContainerView
        Assertions.assertEquals(145, scrollView.childView.evaluateActualSizeX())
        Assertions.assertTrue(scrollView.childView.evaluateActualSizeY() > scroll.second.sizeY)
        val scrollbar = placements.single { it.first is YScrollbarView }
        Assertions.assertEquals(IntRectangle(270, 87, 5, 114), scrollbar.second)
        val scrollbarView = scrollbar.first as YScrollbarView
        Assertions.assertEquals(5, scrollbarView.scrollbarThickness)
        Assertions.assertEquals(0xA0FFFFFF.toInt(), scrollbarView.scrollbarColor)
        scrollbarView.scrollOffsetY.value = 125.5
        Assertions.assertEquals(125.5, scrollView.scrollOffsetY.value)
        scrollView.scrollOffsetY.value = 250.25
        Assertions.assertEquals(250.25, scrollbarView.scrollOffsetY.value)
        val texts = placements.filter { it.first is MultiLineTextView }
        Assertions.assertEquals(listOf(trait.getName().style(trait.style), trait.text), texts.map { (it.first as MultiLineTextView).text.value })
        Assertions.assertEquals(listOf(Alignment.CENTER, Alignment.START), texts.map { (it.first as MultiLineTextView).alignmentX })
        Assertions.assertTrue(texts.all { !(it.first as MultiLineTextView).shadow && (it.first as MultiLineTextView).lineSpacing == 2 })
        Assertions.assertEquals(listOf(150, 145), proxy.widths)
    }

    @Test
    fun `再初期化するとカードとスクロール領域とボタンを画面の寸法に合わせるのだ`() {
        val view = TraitEncyclopediaPageViewWrapper(trait)
        listOf(IntPoint(400, 240), IntPoint(320, 180)).forEach { size ->
            view.view.initSizeCalculation(TraitEncyclopediaRenderingProxy(), { size.x }, { size.y })
            val placements = view.place()
            Assertions.assertEquals(IntRectangle((size.x - 160) / 2, 20, 160, size.y - 54), placements.single { it.first is NinePatchImageView }.second)
            Assertions.assertEquals(IntRectangle((size.x - 150) / 2, 87, 145, size.y - 126), placements.single { it.first is YScrollContainerView }.second)
            Assertions.assertEquals(IntRectangle((size.x - 150) / 2 + 145, 87, 5, size.y - 126), placements.single { it.first is YScrollbarView }.second)
            val buttons = placements.filter { it.first is TextButtonView }.map { it.second }
            Assertions.assertEquals(listOf(IntRectangle((size.x - 180) / 2, size.y - 24, 80, 20), IntRectangle((size.x - 180) / 2 + 100, size.y - 24, 80, 20)), buttons)
        }
    }

    @Test
    fun `二つのボタンがゲーム復帰と親画面復帰を別々に通知するのだ`() {
        val view = TraitEncyclopediaPageViewWrapper(trait)
        val events = mutableListOf<String>()
        view.onBackToGame.register {
            events += "game"
            true
        }
        view.onBack.register {
            events += "parent"
            true
        }
        view.view.initSizeCalculation(TraitEncyclopediaRenderingProxy(), { 400 }, { 240 })
        val buttons = view.place().mapNotNull { it.first as? TextButtonView }

        buttons[0].onClick.fire { it() }
        Assertions.assertEquals(listOf("game"), events)
        buttons[1].onClick.fire { it() }
        Assertions.assertEquals(listOf("game", "parent"), events)
    }

}
