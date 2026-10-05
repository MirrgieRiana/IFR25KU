package miragefairy2024.mod

import com.mojang.serialization.Codec
import miragefairy2024.MirageFairy2024
import miragefairy2024.ModContext
import miragefairy2024.mod.common.guiFullScreenTranslation
import miragefairy2024.mod.fairy.createFairyItemStack
import miragefairy2024.mod.fairy.motifRegistry
import miragefairy2024.mod.materials.BlockMaterialCard
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

        val MIRANBERIA = IfrEncyclopediaEntryCard(
            "miranberia",
            EnJa("Miranberia", "ミランベリア"),
            { listOf(BlockMaterialCard.MIRANBERIA.item().createItemStack()) },
            listOf(
                "Monocots, order Miragales, family Miranberiaceae",
                "Miranberia",
                "",
                "A weed scattered across the floor of the fairy forest. Its leaves glow faintly after nightfall, and fairies gather above them. It has long served as a landmark for locating sites where fairies can be observed.",
                "",
                "Why it draws fairies remained unclear for many years. It has since been confirmed that the plant discharges surplus aura, and that fairies visit in order to feed on it. The glow of the leaves accompanies the disposal of erg that the plant could not put to use.",
                "",
                "The scales shed when a fairy beats its wings are spent catalysts that once converted mana into erg within its body. Fairies produce them without pause, so the ones that settle here are shed in abundance. The roots of this plant absorb them without delay, converting mana into erg for a while before breaking them down for nourishment.",
                "",
                "The aura it offers is therefore paid out of what last night's visitors left behind. Discharging surplus at the very spot where fairies are drawn is held to be an adaptation that keeps this cycle turning, and neither party need intend it for the arrangement to balance.",
            ),
            listOf(
                "単子葉類妖花目ミランベリア科",
                "ミランベリア",
                "",
                "妖精の森の地表に点々と生える雑草。日が落ちると葉が淡く発光し、その上に妖精が集まる。妖精の観測地点を探す際の目印として、古くから利用されてきた。",
                "",
                "妖精を引き寄せる仕組みは長らく不明であったが、この草が余剰のオーラを放出していることが確認され、妖精がそれを摂取しに訪れるものと判明した。葉の発光は、この草が用途を見いだせなかったエルグを捨てる際に伴うものである。",
                "",
                "妖精が羽を払う際に落とす鱗粉は、その体内でマナをエルグへ変えていた触媒が、古くなったものである。妖精は鱗粉を絶えず生成しているため、ここに降りた個体は、これを大量に落としていく。この草の根はこれを速やかに吸収し、しばらくマナをエルグへ変えさせた後、分解して栄養とする。",
                "",
                "したがって、この草が差し出すオーラは、前夜の客が置いていったものを元手としている。妖精の集まる場所でこそ余剰を捨てるという振る舞いは、この循環を回し続けるための適応と見られており、どちらの側も意図せずとも、この関係は釣り合う。",
            ),
        )

        val entries = listOf(MIRAGE_FAIRY, MIRANBERIA)
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
