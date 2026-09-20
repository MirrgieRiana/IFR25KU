package miragefairy2024.mod

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.fairy.MotifCard
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.motifRegistry
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.IntPoint
import miragefairy2024.mod.recipeviewer.view.IntRectangle
import miragefairy2024.mod.recipeviewer.view.Sizing
import miragefairy2024.mod.recipeviewer.view.ViewTexture
import miragefairy2024.mod.recipeviewer.views.CatalystSlotView
import miragefairy2024.mod.recipeviewer.views.Child
import miragefairy2024.mod.recipeviewer.views.ImageButtonView
import miragefairy2024.mod.recipeviewer.views.MultiLineTextChildrenGenerator
import miragefairy2024.mod.recipeviewer.views.NinePatchImageView
import miragefairy2024.mod.recipeviewer.views.PagingView
import miragefairy2024.mod.recipeviewer.views.StackView
import miragefairy2024.mod.recipeviewer.views.TextView
import miragefairy2024.mod.recipeviewer.views.View
import miragefairy2024.mod.recipeviewer.views.XListView
import miragefairy2024.mod.recipeviewer.views.YListView
import miragefairy2024.mod.recipeviewer.views.YSpaceView
import miragefairy2024.mod.recipeviewer.views.configure
import miragefairy2024.mod.recipeviewer.views.margin
import miragefairy2024.mod.recipeviewer.views.minContentSizeX
import miragefairy2024.mod.recipeviewer.views.onClick
import miragefairy2024.mod.recipeviewer.views.plusAssign
import miragefairy2024.mod.recipeviewer.views.tooltip
import miragefairy2024.util.EnJa
import miragefairy2024.util.EventRegistry
import miragefairy2024.util.ObservableValue
import miragefairy2024.util.Translation
import miragefairy2024.util.enJa
import miragefairy2024.util.fire
import miragefairy2024.util.invoke
import miragefairy2024.util.register
import miragefairy2024.util.sortedEntrySet
import miragefairy2024.util.text
import miragefairy2024.util.toIngredientStack
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack

class IfrEncyclopediaEntryCard(
    path: String,
    val itemStacksGetter: () -> List<ItemStack>,
    en: List<String>,
    ja: List<String>,
) {
    companion object {
        val MIRAGE = IfrEncyclopediaEntryCard(
            "mirage",
            { motifRegistry.sortedEntrySet.map { it.value.createFairyItemStack() } },
            listOf(
                "Monocots, order Miragales, family Miragaceae: Mirage",
                "",
                "A fairy in the form of a palm-sized girl with butterfly-like wings. It is extremely timid and rarely appears before people. When one tries to catch it, it takes on a wisp-like shape and escapes, and no pursuit ever succeeds. This is the origin of the name Mirage.",
                "",
                "It was long regarded as a kind of divine spirit, but recent research has revealed it to be the pollen of Mirage plants that has acquired an autonomous structure.",
            ),
            listOf(
                "単子葉類妖花目ミラージュ科　ミラージュ",
                "",
                "　蝶のような翅の生えた手のひら大の少女の姿をした妖精。非常に憶病で、滅多に人前に姿を現さない。捕まえようとしても人魂（ウィスプ）のような姿に化けて逃げ、いくら追いかけても捕まえることができないさまから、ミラージュ（蜃気楼）の名で知られている。",
                "",
                "　古くは神霊の一種と考えられていたが、近年の研究により、ミラージュ植物の花粉が自律構造を持ったものであると解明された。",
            ),
        )

        val entries = listOf(MIRAGE)
    }

    val identifier = MirageFairy2024.identifier(path)
    val textTranslation = Translation({ identifier.toLanguageKey("ifr_encyclopedia", "text") }, EnJa(en.joinToString("\n"), ja.joinToString("\n")))

    /** 本文を、空行を区切りとする段落に分けるのだ～🌱 */
    fun getParagraphs(): List<Component> {
        return text { textTranslation() }.string
            .split("\n\n")
            .filter { it.isNotBlank() }
            .map { text { it() } }
    }
}

/**
 * 段落の間に挟む縦の隙間なのだ～🌱
 * [miragefairy2024.mod.recipeviewer.views.PagingView] はページ分割のために [miragefairy2024.mod.recipeviewer.view.ChildrenGenerator] しか受け取らないのだ～🌱
 */
private val PARAGRAPH_SPACE_CHILDREN_GENERATOR = ChildrenGenerator<Alignment> { _, _ ->
    listOf(Child(Alignment.START, YSpaceView(4)))
}

context(ModContext)
fun initIfrEncyclopedia() {
    IfrEncyclopediaRecipeViewerCategoryCard.init()

    IfrEncyclopediaEntryCard.entries.forEach { card ->
        card.textTranslation.enJa()
    }
}

val onOpenIfrEncyclopediaPageScreen = EventRegistry<(IfrEncyclopediaEntryCard) -> Boolean>()

object IfrEncyclopediaRecipeViewerCategoryCard : RecipeViewerCategoryCard<IfrEncyclopediaEntryCard>() {
    override fun getId() = MirageFairy2024.identifier("ifr_encyclopedia")
    override fun getName() = EnJa("IFR Encyclopedia", "IFR図鑑")
    override fun getIcon() = MotifCard.MIRAGE.createFairyItemStack()

    override fun getRecipeCodec(registryAccess: RegistryAccess): Codec<IfrEncyclopediaEntryCard> = ResourceLocation.CODEC.xmap(
        { identifier -> IfrEncyclopediaEntryCard.entries.first { it.identifier == identifier } },
        { it.identifier },
    )

    override fun getInputs(recipeEntry: RecipeEntry<IfrEncyclopediaEntryCard>) = listOf(Input(recipeEntry.recipe.itemStacksGetter().toIngredientStack(), true))

    override fun createRecipeEntries(registryAccess: RegistryAccess): Iterable<RecipeEntry<IfrEncyclopediaEntryCard>> {
        return IfrEncyclopediaEntryCard.entries.map { card ->
            RecipeEntry(registryAccess, card.identifier, card, true)
        }
    }

    override fun createView(recipeEntry: RecipeEntry<IfrEncyclopediaEntryCard>) = View {
        val pageIndex = ObservableValue(0)
        val pageCount = ObservableValue(0)

        view += StackView().configure {
            view.sizingX = Sizing.FILL
            view.sizingY = Sizing.FILL

            // 背景
            view += NinePatchImageView(NinePatchTextureCard.TRAIT_BACKGROUND.texture, 22, 22, 22, 22, 22, 22)

            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // 掲げられたスロットなのだ～🌱
                view += CatalystSlotView(recipeEntry.recipe.itemStacksGetter().toIngredientStack()).configure {
                    position.alignmentX = Alignment.CENTER
                    view.drawBackground = false
                    view.margin = 0
                }

                view += YSpaceView(5)

                // 本文なのだ～🌱
                view += PagingView().configure {
                    position.weight = 1.0
                    recipeEntry.recipe.getParagraphs().forEachIndexed { index, paragraph ->
                        if (index > 0) view += PARAGRAPH_SPACE_CHILDREN_GENERATOR
                        view += MultiLineTextChildrenGenerator(paragraph) { Alignment.START }
                    }

                    view.pageCount.register { _, it ->
                        pageCount.value = it
                    }
                    view.pageIndex.register { _, it ->
                        pageIndex.value = it
                    }
                    pageIndex.register { _, it ->
                        view.pageIndex.value = it
                    }
                }.onClick {
                    onOpenIfrEncyclopediaPageScreen.fire {
                        if (it(recipeEntry.recipe)) return@onClick true
                    }
                    true
                }.tooltip(text { guiFullScreenTranslation() })

                view += YSpaceView(5)

                // ページ操作ボタンなのだ～🌱
                view += XListView().configure {
                    view.sizingX = Sizing.FILL

                    // 左ボタン
                    view += ImageButtonView(IntPoint(12, 12)).configure {
                        position.alignmentY = Alignment.CENTER
                        view.texture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_left.png"), IntPoint(12, 36), IntRectangle(0, 0, 12, 12))
                        view.hoveredTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_left.png"), IntPoint(12, 36), IntRectangle(0, 12, 12, 12))
                        view.disabledTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_left.png"), IntPoint(12, 36), IntRectangle(0, 24, 12, 12))

                        fun update() {
                            view.enabled.value = pageIndex.value > 0
                        }
                        pageIndex.register { _, _ -> update() }
                        update()

                        view.onClick.register {
                            pageIndex.value -= 1
                            true
                        }
                    }

                    // ページ番号
                    view += TextView().configure {
                        position.alignmentY = Alignment.CENTER
                        view.sizingX = Sizing.FILL
                        view.alignmentX = Alignment.CENTER

                        fun update() {
                            view.text.value = text { "${pageIndex.value + 1}"() }.visualOrderText
                        }
                        pageIndex.register { _, _ -> update() }
                        update()
                    }.minContentSizeX(32)

                    // 右ボタン
                    view += ImageButtonView(IntPoint(12, 12)).configure {
                        position.alignmentY = Alignment.CENTER
                        view.texture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_right.png"), IntPoint(12, 36), IntRectangle(0, 0, 12, 12))
                        view.hoveredTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_right.png"), IntPoint(12, 36), IntRectangle(0, 12, 12, 12))
                        view.disabledTexture = ViewTexture(MirageFairy2024.identifier("textures/gui/sprites/button_14_right.png"), IntPoint(12, 36), IntRectangle(0, 24, 12, 12))

                        fun update() {
                            view.enabled.value = pageIndex.value < pageCount.value - 1
                        }
                        pageIndex.register { _, _ -> update() }
                        pageCount.register { _, _ -> update() }
                        update()

                        view.onClick.register {
                            pageIndex.value += 1
                            true
                        }
                    }

                }

            }.margin(5)

        }
    }
}
