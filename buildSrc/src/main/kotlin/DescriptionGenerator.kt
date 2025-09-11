import kotlin.math.abs

private class MarkdownScope {
    val strings = mutableListOf<String>()
}

private val (MarkdownScope.() -> Unit).strings
    get(): List<String> {
        val scope = MarkdownScope()
        this(scope)
        return scope.strings
    }

context(MarkdownScope)
private operator fun String.not() {
    this@MarkdownScope.strings += this
}

fun <T> Iterable<T>.sandwich(vararg separator: T) = this.flatMapIndexed { i, it -> if (i != 0) listOf(*separator, it) else listOf(it) }
fun Iterable<String>.join(separator: String) = this.joinToString(separator)
fun <T> Iterable<T>.join(separator: String, transform: (T) -> String) = this.joinToString(separator, transform = transform)
fun Iterable<String>.multiLine() = this.join("\n")

fun String.escapeHtml() = this
    .replace("&", "&amp;")
    .replace("'", "&apos;")
    .replace("\"", "&quot;")
    .replace("<", "&lt;")
    .replace(">", "&gt;")

private fun markdown(block: MarkdownScope.() -> Unit) = block.strings.sandwich("").multiLine() + "\n"
context(MarkdownScope) private fun h1(string: String, block: MarkdownScope.() -> Unit = {}) = listOf("# $string", *block.strings.toTypedArray()).sandwich("").multiLine()
context(MarkdownScope) private fun h2(string: String, block: MarkdownScope.() -> Unit = {}) = listOf("## $string", *block.strings.toTypedArray()).sandwich("").multiLine()
context(MarkdownScope) private fun h3(string: String, block: MarkdownScope.() -> Unit = {}) = listOf("### $string", *block.strings.toTypedArray()).sandwich("").multiLine()
context(MarkdownScope) private val br get() = "br"()
context(MarkdownScope) private fun br(count: Int) = (1..count).map { br }.multiLine()
context(MarkdownScope) private operator fun Int.not() = !br(this)
context(MarkdownScope) private val hr get() = "---"
context(MarkdownScope) private fun li(block: MarkdownScope.() -> Unit) = block.strings.map { "- $it" }.multiLine()
context(MarkdownScope) private fun singleLine(block: MarkdownScope.() -> Unit = {}) = block.strings.join("")
context(MarkdownScope) private fun multiLine(block: MarkdownScope.() -> Unit = {}) = block.strings.multiLine()
context(MarkdownScope) private fun String.p() = "p" { !this@p }
context(MarkdownScope) private fun String.center() = "center" { !this@center }
context(MarkdownScope) private fun String.serif() = "font"("face" to "serif") { !this@serif }
context(MarkdownScope) private fun String.size(size: Int) = "font"("size" to String.format("%+d", size)) { !this@size }
context(MarkdownScope) private fun String.b() = "b" { !this@b }
context(MarkdownScope) private fun String.i() = "i" { !this@i }

context(MarkdownScope)
private operator fun String.invoke(vararg attributes: Pair<String, String>?, block: (MarkdownScope.() -> Unit)? = null): String {
    return if (block != null) {
        val content = block.strings.multiLine()
        if ("\n" in content) {
            "<${(listOf(this) + attributes.filterNotNull().map { """${it.first}="${it.second.escapeHtml()}"""" }).join(" ")}>\n  ${content.replace("\n", "\n  ")}\n</$this>"
        } else {
            "<${(listOf(this) + attributes.filterNotNull().map { """${it.first}="${it.second.escapeHtml()}"""" }).join(" ")}>$content</$this>"
        }
    } else {
        "<${(listOf(this) + attributes.filterNotNull().map { """${it.first}="${it.second.escapeHtml()}"""" }).join(" ")}>"
    }
}

context(MarkdownScope)
private fun style(vararg entries: Pair<String, String>?): Pair<String, String>? {
    val nonnullEntries = entries.filterNotNull()
    return if (nonnullEntries.isNotEmpty()) "style" to nonnullEntries.join(" ") { "${it.first}: ${it.second};" } else null
}

context(MarkdownScope)
private fun img(alt: String, src: String, width: Int? = null, float: String? = null, pixelated: Boolean = false): String {
    return "img"(
        "alt" to alt,
        style(
            if (float != null) "float" to float else null,
            if (pixelated) "image-rendering" to "pixelated" else null,
        ),
        if (width != null) "width" to "$width" else null,
        "src" to src,
    )
}

context(MarkdownScope)
private fun catchPhrase(string: String) = string.size(3).serif().center()

context(MarkdownScope)
private fun poem(indent: Int, width: Int, src: String, poem1: String, poem2: String): String { // TODO
    return multiLine {
        !img("Vertical Filler", "https://cdn.modrinth.com/data/cached_images/d4e90f750011606c078ec608f87019f9ad960f6a_0.webp", width = abs(indent), float = if (indent < 0) "right" else "left")
        !"table" {
            !"tr" {
                !"td"("width" to "$width") {
                    !img("TODO", src, width = 48, float = "left", pixelated = true)
                    !singleLine {
                        !singleLine {
                            !"&nbsp;".repeat(4)
                            !poem1
                        }.b()
                        !br
                        !singleLine {
                            !"&nbsp;".repeat(16)
                            !"“$poem2”"
                        }.size(-1).i()
                    }.serif()
                }
            }
        }
    }.center()
}

fun getModrinthBody(): String {
    return markdown {
        run {
            !3
            !img("Fairy Quest Card Top Frame", "https://cdn.modrinth.com/data/cached_images/89547d4a2a78505dc864d9b5e3cb212861aa81a5.png").center().p()
            !catchPhrase("Fatal Accident")
            !3
            !img("A city eroded by Local Vacuum Decay", "https://cdn.modrinth.com/data/cached_images/46e762d464fd36db2f58d8f2f7aaee6aa25b1202_0.webp").center().p()
            !1
            !listOf(
                "………".center(),
                "“Damn it, the vacuum decay reactor was never something humans should have messed with!”".center(),
                "“If someone is reading this, please understand.”".center(),
                "“The vacuum decay reactor wasn't a safe, environmentally friendly source of energy.”".center(),
                "“One wrong move, and it's a terrifying thing that could wipe out an entire planet.”".center(),
                "“If anyone is in control of the vacuum decay reactor, please stop it now!”".center(),
                "“Before your world ceases to exist!!!”".center(),
            ).sandwich(br).multiLine()
            !1
            !img("Fairy Quest Card Bottom Frame", "https://cdn.modrinth.com/data/cached_images/a9bba084db1b7e2cd2513e509fbf26bd2250c36d.png").center().p()
            !3
            !catchPhrase("Why is humanity here now?")
            !4
            !"︙".center()
            !4
            //!catchPhrase("There were “fairies” on that planet.")
            !"p" {
                !img("Toast Top Frame", "https://cdn.modrinth.com/data/cached_images/52f554abf896a453d52f012313801247b7cd77e7.png", width = 400).center()
                !"${img("Fairy icon", "https://cdn.modrinth.com/data/cached_images/1f24ada58c4d32f2b88443878d9650ae81a46579.png", width = 32, pixelated = true)}&nbsp;&nbsp;Dreamed of a new fairy!".size(2).center()
                !img("Toast Bottom Frame", "https://cdn.modrinth.com/data/cached_images/cd79cf31789501fa8c616784e9eb756813f39f1e.png", width = 400).center()
            }
            !4
            !multiLine {
                !"table" {
                    !"tr" {
                        !"td"("width" to "700") {
                            !img("Portrait of Mirage fairy", "https://cdn.modrinth.com/data/cached_images/00fd8432abd76e76bf952bc13ae0490a0d265468_0.webp", float = "left")
                            !"p" {
                                !singleLine {
                                    !"Monocots ― Order Miragales ― Family Miragaceae".b().size(-1).serif()
                                    !br
                                }
                                !"Mirage".b().size(3).serif()
                            }
                            !"p" {
                                !"A palm-sized fairy in the form of a little girl with butterfly-like wings. Extremely timid, it rarely shows itself to people. When one tries to catch it, it disguises itself as a will-o'-the-wisp and flees; no matter how long you pursue it, you cannot seize it. For this elusive behavior it is known as the “Mirage.”".serif()
                            }
                            !"p" {
                                !"Once regarded as a kind of divine spirit, later research clarified that it is in fact the pollen of Mirage plants, possessing an autonomous structure.".serif()
                            }
                        }
                    }
                }
            }.center()
            !2
            !catchPhrase("What, exactly, is the true nature of fairies?")
            !4
            !"︙".center()
            !4
            // フェアリークリスタル
            !poem(
                -300, 430,
                "https://github.com/MirrgieRiana/IFR25KU/blob/main/common/src/main/resources/assets/miragefairy2024/textures/item/fairy_crystal.png?raw=true",
                "Crystallized soul",
                "That which makes a creature a creature.",
            )
            // ファントムの葉
            !poem(
                300, 340,
                "https://github.com/MirrgieRiana/IFR25KU/blob/main/common/src/main/resources/assets/miragefairy2024/textures/item/phantom_leaves.png?raw=true",
                "The eroding reality",
                "The precipitating fantasy.",
            )
            !1
            // ハイメヴィスカ
            !poem(
                0, 410,
                "https://cdn.modrinth.com/data/cached_images/4789326b379836f317635052dcac361ff3a07b9e_0.webp",
                "Do fairy trees have qualia of pain?",
                "What it means to protect animals.",
            )
            !1
            // サラセニア
            !poem(
                200, 380,
                "https://github.com/MirrgieRiana/IFR25KU/blob/main/common/src/main/resources/assets/miragefairy2024/textures/block/magic_plant/sarracenia_age3.png?raw=true",
                "Waiting for a flying creature...",
                "Fairies’ resting place.",
            )
            !2
            // ミラジディアン
            !poem(
                -100, 460,
                "https://github.com/MirrgieRiana/IFR25KU/blob/main/common/src/main/resources/assets/miragefairy2024/textures/item/miragidian.png?raw=true",
                "The great collapse 30,000 years ago",
                "The dream Miragium saw 30,000 years ago.",
            )
            !2
            // 紅天石
            !poem(
                0, 580,
                "https://github.com/MirrgieRiana/IFR25KU/blob/main/common/src/main/resources/assets/miragefairy2024/textures/item/xarpite.png?raw=true",
                "Binds astral flux with magnetic force",
                "A blood-reeking cage of souls bound by chains of black iron.",
            )
            !3
            // 理天石
            !poem(
                200, 440,
                "https://cdn.modrinth.com/data/cached_images/9f481e640f797ca8665bd21e7d39cfcd34ac9ee8.gif",
                "Class 4 time evolution rule.",
                "A stone that etches the patterns of time.",
            )
            !3
            // オーラ反射炉
            !poem(
                50, 490,
                "https://cdn.modrinth.com/data/cached_images/ce5ecf74a49ca60c318da7dbccef60bddde3e7a8.png",
                "Life is essentially inorganic.",
                "The boundary between life and inorganic matter.",
            )
            !4
            // 蒼天石
            !poem(
                -50, 650,
                "https://cdn.modrinth.com/data/cached_images/2edab3f8a66c4c27505aa35c0aeb1c79393098ea.png",
                "A Turing-complete crystal lattice",
                "A world where everything has been prophesied since the dawn of creation.",
            )
            !5
            // 局所真空崩壊
            !poem(
                0, 490,
                "https://cdn.modrinth.com/data/cached_images/acb14f57121d7f180077eba96b87edcd957e82f4_0.webp",
                "Stable instability due to anti-entropy.",
                "Could this be the ultimate form of anti-entropy?",
            )
            !6
            // ノイズブロック
            !poem(
                0, 370,
                "https://cdn.modrinth.com/data/cached_images/dbe6a42399b5a56332e2f96ebd89891b2a95f425.gif",
                "No one can block that noise.",
                "No one can block that noise.",
            )
            !8
            !catchPhrase("A World Ruled by Plants.")
            !8
            !"The Institute of Fairy Research 2025 Kakera Unofficial".serif().center()
            !1
            !img("IFR25KU Logo", "https://cdn.modrinth.com/data/cached_images/146f7b7ba56f7314f818ef00a991d22f12dfc97b_0.webp", width = 400).center().p()
            !8
        }
        !h2("Overview") {
            !"IFR25KU is a Minecraft mod that depicts a possible world based on the MirageFairy universe."
            !"IFR25KU is an unofficial fork of [MF24KU](https://modrinth.com/mod/miragefairy2024-kakera-unofficial) by Yoruno Kakera."
            !"MF24KU was an unofficial fork of [MirageFairy2024](https://modrinth.com/mod/miragefairy2024)."
            !"MirageFairy2024 is a mod that represents the worldview of the project ${"MirageFairy".i()}."
            !hr
            !"This mod adds items, blocks, worldgens, biomes, enchantments, game mechanics, and many other kinds of content to Minecraft."
            !"Through this mod, you can experience one possible world grounded in the MirageFairy universe."
        }
        !h2("Documents") {
            !"[CHANGELOG.md](https://github.com/MirrgieRiana/IFR25KU/blob/main/CHANGELOG.md) is the only official document in Japanese that comprehensively describes IFR25KU's specifications."
            !"There are no other official documents, but some unofficial resources exist."
            !"Known examples include:"
            !li {
                !"[MFKU非公式Wiki (Japanese)](https://wikiwiki.jp/mifai2024/)"
            }
        }
        !h2("Adventure Guide") {
            !"What should you do next? Press L to open the Advancements screen."
            !img("Advancements screen", "https://cdn.modrinth.com/data/cached_images/9d4b145be73d124a862dc5fadb65ccb6e187cbd5.png").center().p()
            !1
            !"It will guide you toward your next objectives."
            !img("Advancement description", "https://cdn.modrinth.com/data/cached_images/30f0425c308ccc1ae482775fddd8ee7959046d1d.png").center().p()
        }
        !h2("Biomes") {
            !"As you roam the world, you'll come upon biomes remade by fairies."
            !h3("Fairy Forest") {
                !"Fairy forests dot the land far and wide."
                !img("Fairy Forest", "https://cdn.modrinth.com/data/cached_images/1952646971c206beff58fa3791a177a2bbc533bd_0.webp").center().p()
                !"Here, many Mirage flowers bloom in profusion."
                !1
                !"The Phantom flower found here is difficult to cultivate, yet it serves as a medium for higher-tier fairy summoning."
                !img("Phantom flower", "https://cdn.modrinth.com/data/cached_images/351c3d683c0ff26eba7d7034011c81f7ba25aaeb_0.webp").center().p()
            }
            !h3("Deep Fairy Forest") {
                !"A deep, overgrown forest where Haimeviska rise like pillars."
                !img("Deep Fairy Forest", "https://cdn.modrinth.com/data/cached_images/a3dc02ec6167526592cc7cc124cb5b94fa65acda_0.webp").center().p()
                !"Venturing in unprepared is extremely dangerous."
            }
        }
        !h2("Fairies") {
            !"As you journey across the world, you'll encounter many kinds of fairies along the way."
            !img("Fairies", "https://cdn.modrinth.com/data/cached_images/307ff49a23763570f0c5070e5de25f574e68aaad.png").center().p()
            !1
            !"Fairies possess a wide range of abilities."
            !img("Light fairy", "https://cdn.modrinth.com/data/cached_images/25c57e881ae19dd5a84754a38fcce627e95244bc.png").center().p()
            !img("Arrow fairy", "https://cdn.modrinth.com/data/cached_images/e4dc387c8958f81cbe3c0495d11d64d508be1d60.png").center().p()
            !"A light fairy increases your walking speed in bright areas, and an arrow fairy passively increases your bow damage."
            !h3("Fairy Dreams") {
                !"This world is brimming with fairies."
                !img("Fairy dream toast", "https://cdn.modrinth.com/data/cached_images/40f8d08f89553eebaa3e70022824a233f0b4b128.png").center().p()
                !"Search among natural and artificial objects alike. When you obtain a low-rarity Fairy Dream, you can immediately receive one fairy matching its motif."
            }
            !h3("The Soul Stream") {
                !"Press K to open the Soul Stream and place fairies in the top slots."
                !img("Soul Stream", "https://cdn.modrinth.com/data/cached_images/ebe833acd596054213b7f89081701788fd61f780.png").center().p()
                !"They confer powerful enhancements."
            }
            !h3("Fairy Condensation") {
                !"Need more fairy power? Having multiples of the same fairy strengthens its effects."
                !img("Empowered sugar fairy", "https://cdn.modrinth.com/data/cached_images/41c353e38c47a2cde38e6adcd3689499dd385c6a.png").center().p()
                !1
                !"Cultivate Mirage flowers to obtain Mirage flour."
                !img("Mirage flour", "https://cdn.modrinth.com/data/cached_images/e75c326da1b6479677f559f6ed4dbe824d4409e0.png").center().p()
                !"Mirage flour spawns fairies at random according to the Fairy Dreams you have obtained so far."
                !1
                !"Fairies can be condensed indefinitely."
                !img("Fairy condensation", "https://cdn.modrinth.com/data/cached_images/e9a521f0229af711da664abfef1606029d03cc8a.png").center().p()
            }
        }
        !h2("Magic Plants") {
            !"Mysterious plants grow all over this world."
            !img("Fairy Ring", "https://cdn.modrinth.com/data/cached_images/94f160e46960c4414e032ba26f7e6202f7a9b370_0.webp").center().p()
            !"Many can be harvested as crops with a right-click."
            !h3("Crossing") {
                !"Naturally occurring magic plants are born with random trait bits."
                !img("Magic plant traits", "https://cdn.modrinth.com/data/cached_images/afcf48c58e957e5c4220f24cbb04fa9f51fa5666.png").center().p()
                !"Planting them adjacent to one another triggers crossing."
                !img("Adjacent magic plants", "https://cdn.modrinth.com/data/cached_images/80dacc16b262d5f8330c9f98c2ed25c4627005f8.png").center().p()
                !"You have a 25% chance to obtain seeds that inherit both parents’ trait bits."
                !img("Improved seeds", "https://cdn.modrinth.com/data/cached_images/370006cc1896973853bf59445d1033236299d273.png").center().p()
            }
            !h3("Crossbreeding") {
                !"There are many kinds of magic plants."
                !img("Many kinds of magic plants", "https://cdn.modrinth.com/data/cached_images/c9532c1ed9c69499d650754522ebe8b65ac94a7f.png").center().p()
                !"Using the “Crossbreeding” trait, you can cross two different species within the same family."
            }
            !h3("Plant Bag") {
                !"When your inventory overflows with seeds on your journey, the Plant Bag will help you."
                !img("Plant Bag", "https://cdn.modrinth.com/data/cached_images/09f4d27b1266da03d002b7de7f3c6fbf19f821e8.png").center().p()
            }
        }
        !h2("World Generation") {
            !h3("Weathered Ancient Remnants") {
                !"As you travel the Overworld, you'll discover Weathered Ancient Remnants."
                !img("Weathered Ancient Remnants", "https://cdn.modrinth.com/data/cached_images/ba4ea56b35cac23f703ae12639ae4c5e755f2bf2_0.webp").center().p()
                !"Try brushing Suspicious Gravel."
                !"You may unearth a map that leads to the Dripstone Cave Ruin, along with useful materials."
                !img("Dripstone Cave Ruin", "https://cdn.modrinth.com/data/cached_images/a0b179287f9ac8beded0e4037cc4788dc3d836ba_0.webp").center().p()
            }
            !h3("Debris") {
                !"Debris can now be found throughout the world."
                !img("Debris", "https://cdn.modrinth.com/data/cached_images/504ce1940464d214a2f3e725bb02ce88758d8974.png").center().p()
                !"These piles can contain early-game vanilla materials as well as materials added by this mod."
                !1
                !"You can place items on the ground by pressing the Z key."
            }
        }
        !h2("Materials & Processing") {
            !h3("Haimeviska") {
                !"This is a Haimeviska tree. In the fairy language, its name means “great tree of fairies.”"
                !img("Haimeviska", "https://cdn.modrinth.com/data/cached_images/2b8db6bc14815d912c96c6e7a441db77f1d0a6cc_0.webp").center().p()
                !"To collect sap for fuel or as a drink, stack Haimeviska Logs to form a wall and score them with a sword."
                !img("Sap farm", "https://cdn.modrinth.com/data/cached_images/ea98315555098fb52d52cff608aeffa95c9eae80_0.webp").center().p()
            }
            !h3("Basic Materials") {
                !"Felling a Haimeviska is extremely hard, but a Xarpite Axe—which can break connected logs in one swing—makes the job easy."
                !"As Xarpite’s counterpart, Miranagite is a gem found near the surface. Tools made from it have Silk Touch by default."
                !img("Xarpite and Miranagite tools", "https://cdn.modrinth.com/data/cached_images/30d662dd3fa505cc7ceac312ce74fcc4a64f735b.png").center().p()
            }
            !h3("Brewing Barrel") {
                !"Using Haimeviska Logs, you can craft a Brewing Barrel and make liquor that grants both benefits and drawbacks."
                !multiLine {
                    !img("Brewing Barrel recipe", "https://cdn.modrinth.com/data/cached_images/9ccab95ed4b3ffc9e5134b1b0a95e17cdecc8b06.png")
                    !img("Fairy Liqueur", "https://cdn.modrinth.com/data/cached_images/b6fcb829216f0fa98a2402e9e93f60d984634316.png")
                }.center().p()
                !img("Brewing Barrel placed", "https://cdn.modrinth.com/data/cached_images/8c56debebae75dcb5398235bc6ebefa5b2841053.png").center().p()
            }
            !h3("Aura Reflector Furnace") {
                !"Once you've gathered enough Xarpite, craft an Aura Reflector Furnace."
                !img("Aura Reflector Furnace recipe", "https://cdn.modrinth.com/data/cached_images/d418b63551cf892ef5c6c118504392aae0881ecd.png").center().p()
                !"Miragium produced in the furnace is used to craft powerful tools and also serves as an intermediate for further materials."
                !multiLine {
                    !img("Miragium Nugget recipe", "https://cdn.modrinth.com/data/cached_images/c2f1d3b9f4c820f97dacfa258c2a17d75c8b0065.png")
                    !img("Miragium recipe", "https://cdn.modrinth.com/data/cached_images/92ed27deeeec8facbdffb7b73380669f88761212.png")
                }.center().p()
                !multiLine {
                    !img("Lilagium recipe", "https://cdn.modrinth.com/data/cached_images/86a38d1218cafe5ca55baebd20db0d9447f6d729.png")
                    !img("Resonite recipe", "https://cdn.modrinth.com/data/cached_images/baa1a654af9891783d670bd9dd678f8da4462c31.png")
                }.center().p()
                !"To run it, you'll need Soul Sand or Soul Soil as fuel."
            }
            !h3("Fairy House") {
                !"You can build a Fairy House from Haimeviska Logs and several other materials."
                !img("Fairy House recipe", "https://cdn.modrinth.com/data/cached_images/81dd2f3401638fa4ffb473ea7679b37d2db93b4b.png").center().p()
                !"Fairies can live and work there."
                !img("Inside the Fairy House", "https://cdn.modrinth.com/data/cached_images/b37e0797d786d123f74d53cc2bc454c4ca6ae43a.png").center().p()
            }
        }
        !h2("ツール") {
            !"IFR25KUには多様なツールが実装されています。"
            !img("多様な素材のツール", "https://cdn.modrinth.com/data/cached_images/f008e4f81cf2ee45181cc113b2496fefdec51c65.png")
            !"バニラの素材や汎用素材には基本的な5種のツールがあります。"
            !"MOD固有の素材には、特殊なタイプのツールがあることがあります。"
            !hr
            !"様々な素材のツールが特殊な効果を持ちます。"
            !img("シャルパイトの斧のツールチップ", "https://cdn.modrinth.com/data/cached_images/70a39cbd649328297ee833f14d0b1ff092910b40.png")
            !"例えば、シャルパイトが持つ一括伐採効果は、採掘した原木に接続する樹木を一撃で伐採します。"
            !hr
            !"これはみらなぎの杖という魔法の武器です。"
            !img("みらなぎの杖", "https://cdn.modrinth.com/data/cached_images/d12d5212d8bc71f23b6e94d47a2ab478f5d7e5e4.png")
            !"序盤から制作をスタートでき、魔法弾を撃ちます。"
        }
        !h2("フェアリージュエル") {
            !"ある程度進めたら、ミーニャの望遠鏡を作りましょう！"
            !img("ミーニャの望遠鏡のレシピ", "https://cdn.modrinth.com/data/cached_images/14a1bd9b37a5416dc0caba9fd033668679fd86e7.png")
            !"それを毎日叩けば、フェアリージュエルが手に入ります。"
            !"フェアリージュエルは妖精の像の泉に捧げましょう。"
            !img("妖精の像の泉のレシピ", "https://cdn.modrinth.com/data/cached_images/0d69db0ca5cd5f25d8d3cc415802c827c214ed4b.png")
            !"妖精の夢のキャリアである妖精の像がランダムにもらえます。"
        }
        !h2("世界観") {
            !"ほとんどの固有のアイテムはポエムと呼ばれるフレーバーテキストを持ちます。"
            !img("ミラナガイトのタイルのブロックのポエム", "https://cdn.modrinth.com/data/cached_images/1788893ec4b494a8184e56b9131eb26c55fb97eb.png")
            !"魔法植物の特性は、長文の説明文を持ちます。"
            !img("Xp Productionの特性図鑑ポエム", "https://cdn.modrinth.com/data/cached_images/4d39b35c4c7269f97fe44c90ede21b54415bea3a.png")
            !"フェアリークエストカードには、いくつかのストーリーが表現されています。"
            !img("即席ファンタスティックカーニバルのフェアリークエストカードメッセージ", "https://cdn.modrinth.com/data/cached_images/41d3dbe429c052fdf00a22ccba3d81335ee5f4ce.png")
            !"MODの外には、MFKU公式WebサイトにMFAと呼ばれる日本語の読み物が存在します。"
            !"[G1-MFA　ミラージュ妖精の生態](https://miragefairy-kakera-unofficial.notion.site/G1-MFA-2f122378b3de4ba39cc5492e3af27b50)"
            !hr
            !"これらは様々に相互に情報を提供しあって、IFRの世界観を形作ります。"
        }
        !h2("互換性") {
            !"IFR25KUはMF24KUと同一のmodidを共有しており、ワールドデータに互換性があります。"
            !"IFR25KU v23.23.0はMF24KU v22.22.0とほとんど同等のコンテンツを持っており、MF24KUのワールドをIFR25KUで読み込むことができます。"
            !"技術的には同一のMODとして扱われており、競合を引き起こすため、IFR25KUとMF24KUを同時にインストールしてはいけません。"
        }
        !h2("MF24KUとの関係は？") {
            !"""
```
■
┣ MirageFairy Official
┃　┣ Project: MirageFairy
┃　┣ MOD: MirageFairy2019
┃　┣ MOD: MirageFairy2023
┃　┗ MOD: MirageFairy2024
┗ ★MirageFairy Unofficial Fork★
　　┣ MFKU Official
　　┃　┣ Project: MFKU
　　┃　┗ MOD: MF24KU
　　┗ ★MFKU Unofficial Fork★
　　　　┗ ★IFRKU Official★
　　　　　　┣ Project: IFRKU
　　　　　　┗ ★MOD: IFR25KU★
```
        """.trim()
            !"IFR25KUはMFKU公式でもMirageFairy公式でもありません。"
        }
        !h2("サポート") {
            !"MODのサポートはDiscord上で受け付けています。"
            !"IFR25KUはそのすべてが日本人によって開発されており、サポートは主に日本語で受け付けています。"
            !"日本語以外の言語によるサポートは、間にAIによる翻訳が存在するため、我々の対応は流暢でないかもしれません。"
        }
        !hr
        !"Note: This description is automatically updated from [GitHub Actions](https://github.com/MirrgieRiana/IFR25KU/blob/main/MODRINTH-BODY.md) and cannot be changed manually."
    }
}
