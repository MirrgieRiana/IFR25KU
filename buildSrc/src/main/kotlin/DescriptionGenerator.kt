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

private fun markdown(block: MarkdownScope.() -> Unit) = block.strings.joinToString("\n\n") + "\n"
context(MarkdownScope) private fun h1(string: String) = "# $string"
context(MarkdownScope) private fun h2(string: String) = "## $string"
context(MarkdownScope) private fun h3(string: String) = "### $string"
context(MarkdownScope) private val hr get() = "---"
context(MarkdownScope) private fun li(block: MarkdownScope.() -> Unit) = block.strings.joinToString("\n") { "- $it" }

fun getModrinthBody(): String {
    return markdown {
        !h1("IFR25KU")
        !"""An unofficial fork of "[MirageFairy2024](https://modrinth.com/mod/miragefairy2024)," a Minecraft mod for the MirageFairy project, created by Yoruno Kakera."""
        !"This MOD is compatible with save data from the original MirageFairy2024."
        !"Therefore, it cannot be installed simultaneously due to conflicts."
        !h2("Current Status(November 2024)")
        !li {
            !"The original MirageFairy2024 began development in Japan in November 2023."
            !"MF24KU is a MOD derived from MirageFairy2024 in November 2024."
            !"With 14 releases, the MOD's content is moderately rich."
            !"But, there are still many planned contents yet to be developed."
            !"This MOD is still in beta, with a full release planned for 2025 or later."
        }
        !h2("Experience provided by this MOD")
        !"This MOD adds various content, including items, blocks, and world generations, that express the MirageFairy world."
        !h2("MirageFairy's Story")
        !"MirageFairy unfolds in a science fiction world governed by fantastical physical laws. As humanity ventured into space, they discovered beings known as fairies on a distant planet. Through the study of their supernatural abilities, science and technology have grown dramatically. In this universe, humans and fairies interact in many places, and humans acquire advanced technology. Human civilization would eventually face an end due to a planetary-scale environmental collapse called 'local vacuum collapse.'"
        !"The storyline of MirageFairy2024 is set on a planet where plants have become wild after being modified by the ancient civilization known as the Fairy Research Institute."
        !h2("MOD Features")
        !h3("Crops and Breedings")
        !li {
            !"Adds original crops and their cultivation."
            !"Introduces a crop-crossing mechanism that allows improved crop varieties."
        }
        !h3("Fairies and Passive Skills")
        !li {
            !"""Adds "fairies," which are symbolic items of this MOD."""
            !"Fairies have motifs such as fire, stone, chicken, and red-purple glazed terracotta."
            !"Possessing fairies grants passive skills that provide buffs to players."
            !"Fairies can be obtained from original crops, specifically Mirage flowers."
        }
        !h3("Erg and Erg Craft(Unavailable)")
        !li {
            !"""Adds "Erg," a substance embodying concepts or meanings."""
            !"Allows manipulation of concepts through processing Erg."
            !"Allows transformation between fairies and Erg."
        }
        !h3("Fairy Quests")
        !li {
            !"Adds Fairy Quests that tell the story of MirageFairy."
            !"They are just card items that simply convert specific fairies into some resources as rewards."
        }
        !h3("Fairy Weapons and Sigils(Unavailable)")
        !li {
            !"Adds fairy weapons that can be enhanced by fairy abilities."
            !"Inscribing sigils on fairy weapons allows customizable enhancements."
            !"Career of summoning fairies is necessary to activate sigils."
        }
        !h2("Documentation")
        !"The specifications of this MOD are officially documented in Japanese only in [CHANGELOG.md](https://github.com/MirrgieRiana/MirageFairy2024-Kakera-Unofficial/blob/main/CHANGELOG.md)."
        !"There is currently no comprehensive official documentation explaining the specifications of this MOD at a specific point in time, and the only available resources are unofficial wikis."
        !h2("Support and Important Information")
        !li {
            !"Official support for this MOD is exclusively available in Japanese."
            !"Any discovered bugs can be reported through the official issue tracker."
            !"Proposals impacting the game balance should be discussed in Japanese through the official issue tracker."
        }
        !hr
        !"Note: This description is automatically updated from [GitHub Actions](https://github.com/MirrgieRiana/MirageFairy2024-Kakera-Unofficial/blob/main/MODRINTH-BODY.md) and cannot be changed manually."
    }
}
