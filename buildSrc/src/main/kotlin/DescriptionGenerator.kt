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
fun Iterable<String>.multiLine() = this.joinToString("\n")

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
context(MarkdownScope) private fun br(count: Int) = (1..count).map { "<br>" }.multiLine()
context(MarkdownScope) private operator fun Int.not() = !br(this)
context(MarkdownScope) private val hr get() = "---"
context(MarkdownScope) private fun li(block: MarkdownScope.() -> Unit) = block.strings.map { "- $it" }.multiLine()
context(MarkdownScope) private fun center(string: String) = if ("\n" in string) "<center>\n  ${string.replace("\n", "\n  ")}\n</center>" else "<center>$string</center>"
context(MarkdownScope) private fun serif(string: String) = """<font face="serif">$string</font>"""
context(MarkdownScope) private fun size(size: Int, string: String) = """<font size="${String.format("%+d", size)}">$string</font>"""

context(MarkdownScope)
private fun img(alt: String, src: String, width: Int? = null, float: String? = null, pixelated: Boolean = false): String {
    return listOf(
        "img",
        *listOfNotNull(
            "alt" to alt,
            listOfNotNull(
                if (float != null) "float" to float else null,
                if (pixelated) "image-rendering" to "pixelated" else null,
            ).let { entries -> if (entries.isNotEmpty()) "style" to entries.joinToString(" ") { "${it.first}: ${it.second};" } else null },
            if (width != null) "width" to "$width" else null,
            "src" to src,
        ).map { """${it.first}="${it.second.escapeHtml()}"""" }.toTypedArray(),
    ).joinToString(" ").let { "<$it>" }
}

context(MarkdownScope)
private fun catchPhrase(string: String) = center(serif(size(3, string)))

context(MarkdownScope)
private fun poem(indent: Int, width: Int, src: String, poem1: String, poem2: String): String { // TODO
    return center(
        """
${img("Vertical Filler", "https://cdn.modrinth.com/data/cached_images/d4e90f750011606c078ec608f87019f9ad960f6a_0.webp", width = abs(indent), float = if (indent < 0) "right" else "left")}
<table><tr><td width="$width">
  ${img("TODO", src, width = 48, float = "left", pixelated = true)}${serif("<b>${"&nbsp;".repeat(4)}$poem1</b><br><i>${size(-1, "${"&nbsp;".repeat(16)}“$poem2”")}</i>")}
</td></tr></table>
    """.trim()
    )
}

fun getModrinthBody(): String {
    return markdown {
        !h2("Prologue") {
            !3
            //!catchPhrase("There were “fairies” on that planet.")
            !center(img("Toast Top Frame", "https://cdn.modrinth.com/data/cached_images/52f554abf896a453d52f012313801247b7cd77e7.png", width = 400))
            !center(size(2, "${img("Fairy icon", "https://cdn.modrinth.com/data/cached_images/1f24ada58c4d32f2b88443878d9650ae81a46579.png", width = 32, pixelated = true)}&nbsp;&nbsp;Dreamed of a new fairy!"))
            !center(img("Toast Bottom Frame", "https://cdn.modrinth.com/data/cached_images/cd79cf31789501fa8c616784e9eb756813f39f1e.png", width = 400))
            !4
            !center(
                """
<table><tr><td width="700">
  ${img("Portrait of Mirage fairy", "https://cdn.modrinth.com/data/cached_images/00fd8432abd76e76bf952bc13ae0490a0d265468_0.webp", float = "left")}
  <p>
    ${serif(size(-1, "<b>Monocots ― Order Miragales ― Family Miragaceae</b>"))}<br>
    ${serif(size(3, "<b>Mirage</b>"))}
  </p>
  <p>${serif("A palm-sized fairy in the form of a little girl with butterfly-like wings. Extremely timid, it rarely shows itself to people. When one tries to catch it, it disguises itself as a will-o'-the-wisp and flees; no matter how long you pursue it, you cannot seize it. For this elusive behavior it is known as the “Mirage.”")}</p>
  <p>${serif("Once regarded as a kind of divine spirit, later research clarified that it is in fact the pollen of Mirage plants, possessing an autonomous structure.")}</p>
</td></tr></table>
        """.trim()
            )
            !2
            !catchPhrase("What, exactly, is the true nature of fairies?")
            !8
            !img("Fairy Quest Card Top Frame", "https://cdn.modrinth.com/data/cached_images/89547d4a2a78505dc864d9b5e3cb212861aa81a5.png")
            !1
            !catchPhrase("Fatal Accident")
            !3
            !center(img("A city eroded by Local Vacuum Decay", "https://cdn.modrinth.com/data/cached_images/46e762d464fd36db2f58d8f2f7aaee6aa25b1202_0.webp"))
            !1
            !listOf(
                center("………"),
                center("“Damn it, the vacuum decay reactor was never something humans should have messed with!”"),
                center("“If someone is reading this, please understand.”"),
                center("“The vacuum decay reactor wasn't a safe, environmentally friendly source of energy.”"),
                center("“One wrong move, and it's a terrifying thing that could wipe out an entire planet.”"),
                center("“If anyone is in control of the vacuum decay reactor, please stop it now!”"),
                center("“Before your world ceases to exist!!!”"),
            ).sandwich("<br>").multiLine()
            !1
            !img("Fairy Quest Card Bottom Frame", "https://cdn.modrinth.com/data/cached_images/a9bba084db1b7e2cd2513e509fbf26bd2250c36d.png")
            !2
            !catchPhrase("Why is humanity here now?")
            !8
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
                0, 400,
                "https://cdn.modrinth.com/data/cached_images/4789326b379836f317635052dcac361ff3a07b9e_0.webp",
                "Do fairy trees have qualia of pain?",
                "On protecting animals.",
            )
            !1
            // サラセニア
            !poem(
                200, 380,
                "https://github.com/MirrgieRiana/IFR25KU/blob/main/common/src/main/resources/assets/miragefairy2024/textures/block/magic_plant/sarracenia_age3.png?raw=true",
                "Waiting for a flying creature...",
                "A place of repose for fairies.",
            )
            !2
            // ミラジディアン
            !poem(
                -100, 510,
                "https://github.com/MirrgieRiana/IFR25KU/blob/main/common/src/main/resources/assets/miragefairy2024/textures/item/miragidian.png?raw=true",
                "The great collapse 30,000 years ago",
                "The dream Miragium saw thirty thousand years ago.",
            )
            !2
            // 紅天石
            !poem(
                0, 610,
                "https://github.com/MirrgieRiana/IFR25KU/blob/main/common/src/main/resources/assets/miragefairy2024/textures/item/xarpite.png?raw=true",
                "Binds astral flux with magnetic force",
                "The black iron chain is fastened into a blood reeking cage for souls.",
            )
            !3
            // 理天石
            !poem(
                200, 430,
                "https://cdn.modrinth.com/data/cached_images/9f481e640f797ca8665bd21e7d39cfcd34ac9ee8.gif",
                "Class 4 time evolution rule.",
                "A stone that etches the patterns of time.",
            )
            !3
            // オーラ反射炉
            !poem(
                50, 460,
                "https://cdn.modrinth.com/data/cached_images/ce5ecf74a49ca60c318da7dbccef60bddde3e7a8.png",
                "Life is essentially inorganic.",
                "The boundary between life and the inorganic.",
            )
            !4
            // 蒼天石
            !poem(
                -50, 590,
                "https://cdn.modrinth.com/data/cached_images/2edab3f8a66c4c27505aa35c0aeb1c79393098ea.png",
                "A Turing-complete crystal lattice",
                "A world where all has been prophesied since the dawn of creation.",
            )
            !5
            // 局所真空崩壊
            !poem(
                0, 440,
                "https://cdn.modrinth.com/data/cached_images/acb14f57121d7f180077eba96b87edcd957e82f4_0.webp",
                "Stable instability due to anti-entropy.",
                "Is this the ultimate form of order?",
            )
            !6
            // ノイズブロック
            !poem(
                0, 360,
                "https://cdn.modrinth.com/data/cached_images/dbe6a42399b5a56332e2f96ebd89891b2a95f425.gif",
                "No one can block that noise.",
                "No one can block that noise.",
            )
            !8
            !catchPhrase("A World Ruled by Plants.")
            !8
            !center(serif("The Institute of Fairy Research 2025 Kakera Unofficial"))
            !1
            !center(img("IFR25KU Logo", "https://cdn.modrinth.com/data/cached_images/146f7b7ba56f7314f818ef00a991d22f12dfc97b_0.webp", width = 400))
            !8
        }
        !h2("概要") {
            !"IFR25KUはMirageFairyの世界観に基づいた可能な世界の一つを表現するMODです。"
            !"IFR25KUはYoruno Kakeraによる“[MF24KU](https://modrinth.com/mod/miragefairy2024-kakera-unofficial)”の非公式フォークプロジェクトです。"
            !"MF24KUは“[MirageFairy2024](https://modrinth.com/mod/miragefairy2024)”の非公式フォークでした。"
            !"MirageFairy2024は“MirageFairy”という創作プロジェクトの世界観を表現するMODです。"
            !hr
            !"このMODは、アイテム、ブロック、地形生成物、バイオーム、エンチャント、ゲームメカニズム、その他様々な種類のコンテンツをゲーム内に追加します。"
            !"プレイヤーは、このMODを通してMirageFairyの世界観に基づく世界の一つを体験することができます。"
        }
        !h2("ドキュメント") {
            !"[CHANGELOG.md](https://github.com/MirrgieRiana/IFR25KU/blob/main/CHANGELOG.md)はIFR25KU公式による唯一のすべての仕様が網羅的に記述されたドキュメントです。"
            !"最新版における仕様が記述されたドキュメントは公式には存在しませんが、非公式には存在します。"
            !"そのようなWebサイトには、以下のものが知られています。"
            !li {
                !"[MFKU非公式Wiki](https://wikiwiki.jp/mifai2024/)"
            }
        }
        !h2("冒険の手引き") {
            !"あなたはこのMODで次に何をしますか？"
            !"Lキーで進捗のGUIを開いてください！"
            !img("進捗", "https://cdn.modrinth.com/data/cached_images/9d4b145be73d124a862dc5fadb65ccb6e187cbd5.png")
            !"それはあなたにあなたが次にできることを案内する！"
            !img("進捗説明文", "https://cdn.modrinth.com/data/cached_images/30f0425c308ccc1ae482775fddd8ee7959046d1d.png")
        }
        !h2("バイオーム") {
            !"あなたは世界各地を冒険すると妖精のバイオームに遭遇できます。"
            !h3("妖精の森") {
                !"世界のそこかしこにある妖精の森。"
                !"そこには多くのMirage flowerが咲き誇る。"
                !img("妖精の森", "https://cdn.modrinth.com/data/cached_images/1952646971c206beff58fa3791a177a2bbc533bd_0.webp")
                !"ここで見つかるファントムフラワーは、栽培は困難であるが、より高度な妖精の召喚の媒体となる。"
                !img("ファントムフラワー", "https://cdn.modrinth.com/data/cached_images/351c3d683c0ff26eba7d7034011c81f7ba25aaeb_0.webp")
            }
            !h3("妖精の樹海") {
                !"ハイメヴィスカが立ち並ぶ鬱蒼とした森。"
                !img("妖精の樹海", "https://cdn.modrinth.com/data/cached_images/a3dc02ec6167526592cc7cc124cb5b94fa65acda_0.webp")
                !"装備の整わないうちにここを歩くのは非常に危険である。"
            }
        }
        !h2("妖精") {
            !"あなたは世界の各地で妖精を見つけることができる。"
            !img("妖精", "https://cdn.modrinth.com/data/cached_images/307ff49a23763570f0c5070e5de25f574e68aaad.png")
            !"妖精は様々な能力を持っている。"
            !img("光の妖精", "https://cdn.modrinth.com/data/cached_images/25c57e881ae19dd5a84754a38fcce627e95244bc.png")
            !img("矢の妖精", "https://cdn.modrinth.com/data/cached_images/e4dc387c8958f81cbe3c0495d11d64d508be1d60.png")
            !"光の妖精は明るい場所であなたの歩行の速度を上げ、矢の妖精は無条件であなたに弓矢のダメージを増加する効果を与える。"
            !hr
            !"この世界は妖精で満ち溢れています。"
            !"様々な自然物や人工物に触れ合いましょう！"
            !img("妖精の夢のトースト", "https://cdn.modrinth.com/data/cached_images/40f8d08f89553eebaa3e70022824a233f0b4b128.png")
            !"あなたはレアリティーの低い妖精を見つけてすぐに受け取ることができる！"
            !hr
            !"Kキーでソウルストリームを開き、トップのスロットに妖精を配置してください！"
            !img("ソウルストリーム", "https://cdn.modrinth.com/data/cached_images/ebe833acd596054213b7f89081701788fd61f780.png")
            !"それらはあなたに猛烈な恩恵を与える！"
            !hr
            !"妖精の能力が足りないですか？"
            !"同じ妖精を複数所持すると、妖精の能力が強化されます！"
            !img("強化された砂糖の妖精", "https://cdn.modrinth.com/data/cached_images/41c353e38c47a2cde38e6adcd3689499dd385c6a.png")
            !"ミラージュフラワーを栽培し、花粉を手に入れてください！"
            !img("ミラージュの花粉", "https://cdn.modrinth.com/data/cached_images/e75c326da1b6479677f559f6ed4dbe824d4409e0.png")
            !"ミラージュの花粉はあなたが手に入れた「妖精の夢」に従ってランダムに妖精をポップします。"
            !"妖精はいくらでも圧縮することができます。"
            !img("妖精の凝縮", "https://cdn.modrinth.com/data/cached_images/e9a521f0229af711da664abfef1606029d03cc8a.png")
        }
        !h2("魔法植物") {
            !"この惑星にはミステリアスな植物が生えています。"
            !img("Fairy Ring", "https://cdn.modrinth.com/data/cached_images/94f160e46960c4414e032ba26f7e6202f7a9b370_0.webp")
            !"それらは右クリックでいくつかの種類の農作物が収穫可能です。"
            !hr
            !"天然の魔法植物はランダムな特性ビットを持っています。"
            !img("魔法植物の特性", "https://cdn.modrinth.com/data/cached_images/afcf48c58e957e5c4220f24cbb04fa9f51fa5666.png")
            !"それらを隣接して植えると、交配が発生します。"
            !img("隣接して生えている魔法植物", "https://cdn.modrinth.com/data/cached_images/80dacc16b262d5f8330c9f98c2ed25c4627005f8.png")
            !"あなたは25%の確率で両親から両方の特性ビットを貰った種子を得るだろう！"
            !img("品種改良済みの種子", "https://cdn.modrinth.com/data/cached_images/370006cc1896973853bf59445d1033236299d273.png")
            !hr
            !"あなたが冒険の過程であなたの持ち物があふれたとき、植物カバンはあなたを助けるだろう。"
            !img("Replace this with a description", "https://cdn.modrinth.com/data/cached_images/09f4d27b1266da03d002b7de7f3c6fbf19f821e8.png")
            !hr
            !"魔法植物にはたくさんの種類がある。"
            !img("多くの種類の魔法植物", "https://cdn.modrinth.com/data/cached_images/c9532c1ed9c69499d650754522ebe8b65ac94a7f.png")
            !"あなたは「交雑」の特性を使って同じ科に属する異なる2種のそれらで品種改良ができる！"
        }
        !h2("地形生成物") {
            !"あなたはオーバーワールドを旅しているとき、旧世代の遺構を見つけるだろう。"
            !img("風化した旧世代の遺構", "https://cdn.modrinth.com/data/cached_images/ba4ea56b35cac23f703ae12639ae4c5e755f2bf2_0.webp")
            !"怪しい砂利をブラシで掘ってみましょう！"
            !"あなたは有用な素材とともに、鍾乳洞の遺跡の地図を見つけることができるかもしれません！"
            !img("鍾乳洞の遺跡", "https://cdn.modrinth.com/data/cached_images/a0b179287f9ac8beded0e4037cc4788dc3d836ba_0.webp")
            !hr
            !"世界にがれきが追加されています！"
            !img("がれき", "https://cdn.modrinth.com/data/cached_images/504ce1940464d214a2f3e725bb02ce88758d8974.png")
            !"それらにはバニラの序盤の素材や、MODの固有素材が含まれています。"
            !"あなたはZキーによってアイテムを地面に設置することができます。"
        }
        // TODO
        !h2("素材・加工") {
            !"これは妖精語で妖精の大樹を意味する、「ハイメヴィスカ」の木。"
            !"燃料に使ったり飲むことができる樹液を採取するにはハイメヴィスカの原木を並べて剣で傷をつけてみよう！"
            !hr
            !"ハイメヴィスカを伐採するのは非常に大変だが、繋がった原木を一度に破壊できる紅天石の斧であれば簡単に伐採できる！"
            !"そのほか、蒼天石は地表近くに出没する鉱石で、デフォルトでシルクタッチの能力を持っている！"
            !hr
            !"ハイメヴィスカからは酒を作ることができる醸造樽が作れる。"
            !"酒はあなたにメリットとデメリットを提供する！"
            !hr
            !"ある程度シャルパイトがたまったら、オーラ反射炉を作ってみよう！"
            !"これを使って作れるミラジウムは、強力なツールとなり、さらに派生素材のための中間素材でもある。"
            !"それを動かすには燃料としてソウルサンドやソウルソイルが必要だ。"
            !hr
            !"あなたはハイメヴィスカの原木から妖精の家を作ることができる。"
            !"そこには妖精が住むことができる！"
        }
        /*

ツール／エンチャント関連

ツールカテゴリ　つるはし／シャベル／斧／ナイフ／大鎌／剣／戦斧を実装。素材や特殊効果の違いで用途が分かれる。
素材バリエーション　バニラ素材とIFR25KU固有素材（ビスマス、プロミナイト、紅天石、蒼天石、磁鉄鉱、ガラス／黒曜石系など）で各カテゴリのツールが実装。素材ごとに標準効果や付与候補が異なる。
みらなぎの杖　IFR25KUに含まれる杖。ツール枠の代表的アイテムとして扱われ、使用時に固有の挙動を持つ。詳細は個別仕様に従う。
一括採掘効果　鉱石ブロックを起点に、接続した同種鉱石を最長19離れまで探索し、最大31個まで巻き込んで同時破壊。鉱石限定で土などには発動しない。
一括伐採効果　原木を壊すと周囲の原木と葉を連鎖破壊。原木は距離19、葉は距離8まで探索。スニーク中は発動せず、巻き込み分は耐久を消費する。複雑樹にも有効。
範囲採掘効果　対応ブロックを掘ると中心3×3×3を巻き込み採掘。起点より硬いブロックは巻き込めない（例：土の隣の黒曜石は壊れない）。混沌のつるはし等が対象。
耐久値自然回復効果　手に持っている間、平均1分に1回耐久が自動回復。v21kuからオフハンド所持でも発動。回復量は素材ごとに異なる。旧称「自己修繕」。
収集効果　そのツールで倒した敵のドロップや経験値を足元に引き寄せ即回収。パッシブ「収集」に近い仕様。ブロック破壊の回収はエンチャ「粘着採掘」が担当。
幸運強化（エンチャ）　v16ku追加。幸運の実効レベルを+Lvする拡張。幸運2+幸運強化1なら実効3。ドロップ増加（Looting）には乗らない。主にビスマス製に標準付与。
粘着採掘（エンチャ）　v19ku追加。壊したブロックのドロップや発生経験値、範囲採掘の巻き込み分も即時吸引し足元で回収可能。チェスト中身や既落下品は対象外。磁鉄鉱・紅天石・涙系に標準。
精錬（エンチャ）　v19ku追加。適正ツールで採掘したドロップをかまど精錬済みで入手。MOD追加ブロックもレシピ参照で対応。プロミナイト製採掘ツールが標準所持（本はサバイバル入手不可）。
妖精獲得効果　一部素材のツールが所持（例：幻想の雫）。ブロック破壊や戦闘での妖精入手を強化する固有効果。素材により倍率が設定される。

フェアリージュエル関連

ミーニャの望遠鏡　1日1回右クリックでフェアリージュエルを獲得。初回1000、日200／週500／月2000。可時は水色パーティクル表示。マルチはプレイヤーごとに管理。
像の泉　100フェアリージュエルで「妖精の像」ガチャを引ける。スニーク使用で提供割合を表示。材料は妖精の鱗粉＋フェアリークエストカードベース。
妖精の像　設置物。対応する妖精の夢を取得できる（チェスト内でも視線で取得可）。入手は像の泉のガチャから。

世界観要素関連

アイテムのポエム　多くのアイテムに短い詩的テキストが付随。入手や使用の文脈を示し、世界観や手触りを補強する。機能とは独立しつつ、遊びの方向づけに役立つ。
特性図鑑　特性GUI／REIから参照できる説明文。各ビットの条件・効果に加え、簡潔なテキストで背景を補足する。特性理解と改良計画の手がかりになる。
フェアリークエストカード　カード自体に短いテキストがあり、探索や収集の動機づけを担う。関連地点・要素とセットで読み解ける作り。
公式サイトのMFA　公式側で提供される背景資料・用語の索引。ゲーム内テキストと併読することで、設定の参照点を補える。

        * */
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
