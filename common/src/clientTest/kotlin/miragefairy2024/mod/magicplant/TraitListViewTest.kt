package miragefairy2024.mod.magicplant

import com.mojang.serialization.Lifecycle
import kotlinx.coroutines.runBlocking
import miragefairy2024.MirageFairy2024
import miragefairy2024.client.mod.recipeviewer.common.ClickableViewRenderer
import miragefairy2024.mod.magicplant.contents.TraitEffectKeyCard
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.PlaceableView
import miragefairy2024.mod.recipeviewer.view.RenderingProxy
import miragefairy2024.mod.recipeviewer.view.ViewController
import miragefairy2024.mod.recipeviewer.view.evaluateActualSize
import miragefairy2024.mod.recipeviewer.view.offset
import miragefairy2024.mod.recipeviewer.views.ClickableView
import miragefairy2024.mod.recipeviewer.views.ImageView
import miragefairy2024.mod.recipeviewer.views.YFlatScrollbarView
import miragefairy2024.mod.recipeviewer.views.YScrollContainerView
import miragefairy2024.util.invoke
import miragefairy2024.util.text
import mirrg.kotlin.helium.Slot
import mirrg.kotlin.helium.atLeast
import net.fabricmc.fabric.mixin.registry.sync.RegistriesAccessor
import net.minecraft.SharedConstants
import net.minecraft.client.Minecraft
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceKey
import net.minecraft.server.Bootstrap
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.ContainerLevelAccess
import net.minecraft.world.level.Level
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.Mockito

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TraitListViewTest {

    private lateinit var traitStacks: TraitStacks

    @BeforeAll
    fun initMinecraft() {
        SharedConstants.tryDetectVersion()
        Bootstrap.bootStrap()
        val rootRegistry: MappedRegistry<WritableRegistry<*>> = MappedRegistry(ResourceKey.createRegistryKey(Registries.ROOT_REGISTRY_NAME), Lifecycle.stable(), false)
        // テスト JVM では Fabric の Mixin が適用されないので、アクセサーがテスト用のルートレジストリを返すようにするのだ～🌱
        Mockito.mockStatic(RegistriesAccessor::class.java).use { registries ->
            registries.`when`<WritableRegistry<WritableRegistry<*>>> { RegistriesAccessor.getROOT() }.thenReturn(rootRegistry)
            val menuType = runBlocking { traitListScreenHandlerType.creator() }
            traitListScreenHandlerType.complete(menuType, Holder.direct(menuType))
            traitStacks = TraitStacks.of((1..8).map { index ->
                val trait = object : Trait(Style.EMPTY, text { "message ".repeat(60)() }) {
                    override val conditions = emptyList<TraitCondition>()
                    override val primaryEffect = TraitEffectKeyCard.TEMPERATURE.traitEffectKey
                    override val traitEffectKeyEntries = emptyList<TraitEffectKeyEntry>()
                    override fun getTraitEffects(level: Level, blockPos: BlockPos, blockEntity: MagicPlantBlockEntity?, traitLevel: Int): MutableTraitEffects? = null
                }
                Registry.register(traitRegistry, MirageFairy2024.identifier("test_trait_list_$index"), trait)
                TraitStack(trait, index)
            })
        }
    }

    @Test
    fun `左クリックだけでカードを切り替え一覧のスクロール位置を保つのだ`() {
        val player: Player = Mockito.mock(Player::class.java)
        val level: Level = Mockito.mock(Level::class.java)
        Mockito.`when`(player.level()).thenReturn(level)
        val menu = TraitListScreenHandler(0, Inventory(player), ContainerLevelAccess.NULL, traitStacks, BlockPos.ZERO)
        val rootView = menu.createRootView(text { "Traits"() }, text { "Inventory"() })
        val proxy: RenderingProxy = object : RenderingProxy {
            override fun calculateTextWidth(text: FormattedText) = text.string.length * 6
            override fun calculateTextWidth(text: FormattedCharSequence) = throw UnsupportedOperationException()
            override fun getTextHeight() = 9
            override fun wrapText(text: FormattedText, maxWidth: Int) = text.string.chunked(maxWidth / 6 atLeast 1).map { FormattedCharSequence.forward(it, Style.EMPTY) }
        }
        rootView.initSizeCalculation(proxy, { 400 }, { 240 })

        val placements = mutableMapOf<PlaceableView, IntRectangle>()
        fun place(view: PlaceableView, bounds: IntRectangle): ViewController {
            placements[view] = bounds
            val childController = if (view is YScrollContainerView) view.childView.attachTo(bounds.offset, ::place) else ViewController.NONE
            return object : ViewController by childController {
                override fun detach() {
                    childController.detach()
                    placements.remove(view)
                }
            }
        }

        val controller = rootView.attachTo(IntPoint.ZERO, ::place)
        try {
            Assertions.assertEquals(IntPoint(176, 210), rootView.evaluateActualSize())
            val scrollViews = placements.keys.filterIsInstance<YScrollContainerView>()
            val scrollbarViews = placements.keys.filterIsInstance<YFlatScrollbarView>()
            val (bodyScrollView, listScrollView) = scrollViews
            val (bodyScrollbarView, listScrollbarView) = scrollbarViews
            val bodyBounds = placements.getValue(bodyScrollView)
            val bodyBarBounds = placements.getValue(bodyScrollbarView)
            val listBounds = placements.getValue(listScrollView)
            val listBarBounds = placements.getValue(listScrollbarView)
            Assertions.assertEquals(152, bodyBounds.sizeX)
            Assertions.assertEquals(bodyBounds.x + bodyBounds.sizeX, bodyBarBounds.x + bodyBarBounds.sizeX)
            Assertions.assertEquals(IntRectangle(11, 135, 149, 64), listBounds)
            Assertions.assertEquals(listBounds.x + listBounds.sizeX, listBarBounds.x)
            val traits = traitStacks.traitStackList.map { it.trait }
            Assertions.assertEquals(traits[0].texture, placements.keys.filterIsInstance<ImageView>().single().texture.id)

            listScrollbarView.scrollOffset.value = 30.0
            val row = placements.keys.filterIsInstance<ClickableView>()[1]
            val rowBounds = placements.getValue(row)
            val minecraft: Minecraft = Mockito.mock(Minecraft::class.java)
            val soundManager: SoundManager = Mockito.mock(SoundManager::class.java)
            Mockito.`when`(minecraft.soundManager).thenReturn(soundManager)
            Mockito.mockStatic(Minecraft::class.java).use { mock ->
                mock.`when`<Minecraft> { Minecraft.getInstance() }.thenReturn(minecraft)
                Assertions.assertFalse(ClickableViewRenderer.mouseClicked(row, Slot(Unit), rowBounds, rowBounds.x, rowBounds.y, 1))
                Assertions.assertFalse(ClickableViewRenderer.mouseClicked(row, Slot(Unit), rowBounds, rowBounds.x, rowBounds.y, 2))
                Assertions.assertEquals(traits[0].texture, placements.keys.filterIsInstance<ImageView>().single().texture.id)
                Mockito.verifyNoInteractions(soundManager)

                Assertions.assertTrue(ClickableViewRenderer.mouseClicked(row, Slot(Unit), rowBounds, rowBounds.x, rowBounds.y, 0))
                Mockito.verify(soundManager).play(Mockito.any(SoundInstance::class.java))
            }

            Assertions.assertEquals(traits[1].texture, placements.keys.filterIsInstance<ImageView>().single().texture.id)
            Assertions.assertFalse(placements.containsKey(bodyScrollView))
            Assertions.assertSame(listScrollView, placements.keys.filterIsInstance<YScrollContainerView>().first())
            Assertions.assertEquals(30.0, listScrollView.scrollOffsetY.value)
            Assertions.assertEquals(30.0, listScrollbarView.scrollOffset.value)
            Mockito.verify(level, Mockito.times(3)).getBlockEntity(BlockPos.ZERO)
        } finally {
            controller.dispose()
            controller.detach()
        }
    }
}
