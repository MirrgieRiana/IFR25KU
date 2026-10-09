package miragefairy2024.mod

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.motifRegistry
import miragefairy2024.mod.magicplant.contents.magicplants.VeropedaCard
import miragefairy2024.mod.materials.MaterialCard
import miragefairy2024.mod.recipeviewer.RecipeViewerCategoryCard
import miragefairy2024.mod.recipeviewer.view.Alignment
import miragefairy2024.mod.recipeviewer.view.ChildrenGenerator
import miragefairy2024.mod.recipeviewer.view.ColorPair
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
import miragefairy2024.mod.recipeviewer.views.SpaceView
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
import miragefairy2024.util.configure
import miragefairy2024.util.createItemStack
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
    title: EnJa,
    val itemStacksGetter: () -> List<ItemStack>,
    en: List<String>,
    ja: List<String>,
) {
    companion object {
        val MIRAGE_FAIRY = IfrEncyclopediaEntryCard(
            "mirage_fairy",
            EnJa("Mirage Fairy", "妖精"),
            { motifRegistry.sortedEntrySet.map { it.value.createFairyItemStack() } },
            listOf(
                "Monocots, order Miragales, family Miragaceae",
                "Mirage",
                "",
                "A palm-sized fairy in the form of a little girl with butterfly-like wings. Extremely timid, it rarely shows itself to people. When one tries to catch it, it disguises itself as a will-o'-the-wisp and flees; no matter how long you pursue it, you cannot seize it. For this elusive behavior it is known as the “Mirage.”",
                "",
                "Once regarded as a kind of divine spirit, later research clarified that it is in fact the pollen of Mirage plants, possessing an autonomous structure.",
            ),
            listOf(
                "単子葉類妖花目ミラージュ科",
                "ミラージュ",
                "",
                "蝶のような翅の生えた手のひら大の少女の姿をした妖精。非常に憶病で、滅多に人前に姿を現さない。捕まえようとしても人魂（ウィスプ）のような姿に化けて逃げ、いくら追いかけても捕まえることができないさまから、ミラージュ（蜃気楼）の名で知られている。",
                "",
                "古くは神霊の一種と考えられていたが、近年の研究により、ミラージュ植物の花粉が自律構造を持ったものであると解明された。",
            ),
        )

        val VEROPEDA = IfrEncyclopediaEntryCard(
            "veropeda",
            EnJa("Veropeda", "呪草ヴェロペダ"),
            { listOf(VeropedaCard.item().createItemStack()) },
            listOf(
                "Monocots, order Miragales, family Veropedaceae",
                "Veropeda",
                "",
                "This magical plant, synthesized from the carnivorous plant Sarracenia and curse magic, has long been identified by the alchemists of old with Veropeda, the demon that lives by devouring fairies. The existence of such a demon has never been confirmed, yet the fibers of this plant do carry a genuine curse. When the Institute of Fairy Research analyzed the genes of this plant, they proved, astonishingly, to be a perfect match for those of the Mirage.",
                "",
                "The Institute of Fairy Research",
            ),
            listOf(
                "単子葉類妖花目ヴェロペダ科",
                "ヴェロペダ",
                "",
                "サラセニアという食虫植物と呪いの魔法を合成して作られたこの魔法植物は、いにしえの錬金術師の間で、妖精を喰らって生きる悪魔ヴェロペーダと同一視されてきました。そのような悪魔の実在は確認されていませんが、この植物の繊維には実際に呪いの効果があります。妖精研究所がこの植物の遺伝子を解析したところ、驚くべきことにミラージュの遺伝子と完全に一致しました。",
                "",
                "妖精研究所",
            ),
        )

        val FAIRY_RUBBER = IfrEncyclopediaEntryCard(
            "fairy_rubber",
            EnJa("Fairy Rubber", "夜のかけら"),
            { listOf(MaterialCard.FAIRY_RUBBER.item().createItemStack()) },
            listOf(
                "The night ends. When morning comes, the darkness that gave our bodies their shape returns to the sky, every last thread of it. The hour when the night grows thin is the hour we fear most.",
                "",
                "One night, a scrap of darkness small enough to fit in a palm came falling down. Held tight, that one spot stayed night. It was soft, it was warm, and when morning came, it did not return to the sky.",
                "",
                "So long as we hold it, we lose nothing.",
                "",
                "Nightia",
            ),
            listOf(
                "夜は明ける。朝が来れば、わたしたちの体をかたちづくっていた闇は、残らず空へ返っていく。夜が薄くなっていく時間が、いちばん、こわい。",
                "",
                "ある夜、手のひらに収まるくらいの闇が、ひとつ、落ちてきた。握ると、そこだけが、ずっと夜のままだった。やわらかくて、あたたかくて、朝が来ても、空へ返らなかった。",
                "",
                "これを握っている間、わたしたちは、何も失くさない。",
                "",
                "夜精ニグチャ",
            ),
        )

        val entries = listOf(MIRAGE_FAIRY, VEROPEDA, FAIRY_RUBBER)
    }

    val identifier = MirageFairy2024.identifier(path)
    val titleTranslation = Translation({ identifier.toLanguageKey("ifr_encyclopedia", "title") }, title)
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
 * タイトルと本文の文字色なのだ～🌱
 * 背景のテクスチャはダークモードでも替わらないから、暗い側も同じ色にするのだ～🌱
 */
private val TEXT_COLOR = ColorPair(0xFF493208.toInt(), 0xFF493208.toInt())

/** 段落の間に挟む縦の隙間なのだ～🌱 */
private val PARAGRAPH_SPACE_CHILDREN_GENERATOR = ChildrenGenerator { _, _ ->
    listOf(Child(Alignment.START, YSpaceView(4)))
}

context(ModContext)
fun initIfrEncyclopedia() {
    IfrEncyclopediaEntryCard.entries.forEach { card ->
        card.titleTranslation.enJa()
        card.textTranslation.enJa()
    }

    IfrEncyclopediaRecipeViewerCategoryCard.init()
}

val onOpenIfrEncyclopediaPageScreen = EventRegistry<(IfrEncyclopediaEntryCard) -> Boolean>()

object IfrEncyclopediaRecipeViewerCategoryCard : RecipeViewerCategoryCard<IfrEncyclopediaEntryCard>() {
    override fun getId() = MirageFairy2024.identifier("ifr_encyclopedia")
    override fun getName() = EnJa("IFR Encyclopedia", "IFR図鑑")
    override fun getIcon() = MaterialCard.FAIRY_PLASTIC.item().createItemStack()

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
            view += NinePatchImageView(NinePatchTextureCard.IFR_ENCYCLOPEDIA_BACKGROUND.texture, 22, 22, 22, 22, 22, 22)

            view += YListView().configure {
                view.sizingX = Sizing.FILL
                view.sizingY = Sizing.FILL

                // タイトルなのだ～🌱
                view += TextView(text { recipeEntry.recipe.titleTranslation() }).configure {
                    view.sizingX = Sizing.FILL
                    view.alignmentX = Alignment.CENTER
                    view.color = TEXT_COLOR
                    view.shadow = false
                    view.scroll = true
                    view.tooltip = listOf(text { recipeEntry.recipe.titleTranslation() })
                }

                view += YSpaceView(5)

                // 掲げられたスロットなのだ～🌱
                view += CatalystSlotView(recipeEntry.recipe.itemStacksGetter().toIngredientStack()).configure {
                    position.alignmentX = Alignment.CENTER
                }

                view += YSpaceView(5)

                // 本文なのだ～🌱
                view += PagingView().configure {
                    position.weight = 1.0
                    recipeEntry.recipe.getParagraphs().forEachIndexed { index, paragraph ->
                        if (index > 0) view += PARAGRAPH_SPACE_CHILDREN_GENERATOR
                        view += MultiLineTextChildrenGenerator(paragraph) { Alignment.START }.configure {
                            onTextViewCreated.register {
                                it.color = TEXT_COLOR
                                it.shadow = false
                            }
                        }
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

                    // 左右の余白を均等に分けて、ボタンの並びを中央に寄せるのだ～🌱
                    view += SpaceView().configure {
                        position.weight = 1.0
                    }

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
                        view.color = TEXT_COLOR
                        view.shadow = false

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

                    view += SpaceView().configure {
                        position.weight = 1.0
                    }

                }

            }.margin(5)

        }
    }
}
