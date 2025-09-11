package miragefairy2024

import mirrg.kotlin.helium.join
import java.io.File

object MapColorViewerGeneratorMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val colorPairs = listOf(
            Pair("NONE", 0),
            Pair("GRASS", 8368696),
            Pair("SAND", 16247203),
            Pair("WOOL", 13092807),
            Pair("FIRE", 16711680),
            Pair("ICE", 10526975),
            Pair("METAL", 10987431),
            Pair("PLANT", 31744),
            Pair("SNOW", 16777215),
            Pair("CLAY", 10791096),
            Pair("DIRT", 9923917),
            Pair("STONE", 7368816),
            Pair("WATER", 4210943),
            Pair("WOOD", 9402184),
            Pair("QUARTZ", 16776437),
            Pair("COLOR_ORANGE", 14188339),
            Pair("COLOR_MAGENTA", 11685080),
            Pair("COLOR_LIGHT_BLUE", 6724056),
            Pair("COLOR_YELLOW", 15066419),
            Pair("COLOR_LIGHT_GREEN", 8375321),
            Pair("COLOR_PINK", 15892389),
            Pair("COLOR_GRAY", 5000268),
            Pair("COLOR_LIGHT_GRAY", 10066329),
            Pair("COLOR_CYAN", 5013401),
            Pair("COLOR_PURPLE", 8339378),
            Pair("COLOR_BLUE", 3361970),
            Pair("COLOR_BROWN", 6704179),
            Pair("COLOR_GREEN", 6717235),
            Pair("COLOR_RED", 10040115),
            Pair("COLOR_BLACK", 1644825),
            Pair("GOLD", 16445005),
            Pair("DIAMOND", 6085589),
            Pair("LAPIS", 4882687),
            Pair("EMERALD", 55610),
            Pair("PODZOL", 8476209),
            Pair("NETHER", 7340544),
            Pair("TERRACOTTA_WHITE", 13742497),
            Pair("TERRACOTTA_ORANGE", 10441252),
            Pair("TERRACOTTA_MAGENTA", 9787244),
            Pair("TERRACOTTA_LIGHT_BLUE", 7367818),
            Pair("TERRACOTTA_YELLOW", 12223780),
            Pair("TERRACOTTA_LIGHT_GREEN", 6780213),
            Pair("TERRACOTTA_PINK", 10505550),
            Pair("TERRACOTTA_GRAY", 3746083),
            Pair("TERRACOTTA_LIGHT_GRAY", 8874850),
            Pair("TERRACOTTA_CYAN", 5725276),
            Pair("TERRACOTTA_PURPLE", 8014168),
            Pair("TERRACOTTA_BLUE", 4996700),
            Pair("TERRACOTTA_BROWN", 4993571),
            Pair("TERRACOTTA_GREEN", 5001770),
            Pair("TERRACOTTA_RED", 9321518),
            Pair("TERRACOTTA_BLACK", 2430480),
            Pair("CRIMSON_NYLIUM", 12398641),
            Pair("CRIMSON_STEM", 9715553),
            Pair("CRIMSON_HYPHAE", 6035741),
            Pair("WARPED_NYLIUM", 1474182),
            Pair("WARPED_STEM", 3837580),
            Pair("WARPED_HYPHAE", 5647422),
            Pair("WARPED_WART_BLOCK", 1356933),
            Pair("DEEPSLATE", 6579300),
            Pair("RAW_IRON", 14200723),
            Pair("GLOW_LICHEN", 8365974),
        )
        f(colorPairs)
    }

    @JvmStatic
    fun main_1_20_1_yarn(args: Array<String>) {
        val colorPairs = listOf(
            Pair("PALE_GREEN", 8368696),
            Pair("PALE_YELLOW", 16247203),
            Pair("WHITE_GRAY", 0xC7C7C7),
            Pair("BRIGHT_RED", 0xFF0000),
            Pair("PALE_PURPLE", 0xA0A0FF),
            Pair("IRON_GRAY", 0xA7A7A7),
            Pair("DARK_GREEN", 31744),
            Pair("WHITE", 0xFFFFFF),
            Pair("LIGHT_BLUE_GRAY", 10791096),
            Pair("DIRT_BROWN", 9923917),
            Pair("STONE_GRAY", 0x707070),
            Pair("WATER_BLUE", 0x4040FF),
            Pair("OAK_TAN", 9402184),
            Pair("OFF_WHITE", 0xFFFCF5),
            Pair("ORANGE", 14188339),
            Pair("MAGENTA", 11685080),
            Pair("LIGHT_BLUE", 6724056),
            Pair("YELLOW", 0xE5E533),
            Pair("LIME", 8375321),
            Pair("PINK", 15892389),
            Pair("GRAY", 0x4C4C4C),
            Pair("LIGHT_GRAY", 0x999999),
            Pair("CYAN", 5013401),
            Pair("PURPLE", 8339378),
            Pair("BLUE", 3361970),
            Pair("BROWN", 6704179),
            Pair("GREEN", 6717235),
            Pair("RED", 0x993333),
            Pair("BLACK", 0x191919),
            Pair("GOLD", 16445005),
            Pair("DIAMOND_BLUE", 6085589),
            Pair("LAPIS_BLUE", 4882687),
            Pair("EMERALD_GREEN", 55610),
            Pair("SPRUCE_BROWN", 8476209),
            Pair("DARK_RED", 0x700200),
            Pair("TERRACOTTA_WHITE", 13742497),
            Pair("TERRACOTTA_ORANGE", 10441252),
            Pair("TERRACOTTA_MAGENTA", 9787244),
            Pair("TERRACOTTA_LIGHT_BLUE", 7367818),
            Pair("TERRACOTTA_YELLOW", 12223780),
            Pair("TERRACOTTA_LIME", 6780213),
            Pair("TERRACOTTA_PINK", 10505550),
            Pair("TERRACOTTA_GRAY", 0x392923),
            Pair("TERRACOTTA_LIGHT_GRAY", 8874850),
            Pair("TERRACOTTA_CYAN", 0x575C5C),
            Pair("TERRACOTTA_PURPLE", 8014168),
            Pair("TERRACOTTA_BLUE", 4996700),
            Pair("TERRACOTTA_BROWN", 4993571),
            Pair("TERRACOTTA_GREEN", 5001770),
            Pair("TERRACOTTA_RED", 9321518),
            Pair("TERRACOTTA_BLACK", 2430480),
            Pair("DULL_RED", 12398641),
            Pair("DULL_PINK", 9715553),
            Pair("DARK_CRIMSON", 6035741),
            Pair("TEAL", 1474182),
            Pair("DARK_AQUA", 3837580),
            Pair("DARK_DULL_PINK", 5647422),
            Pair("BRIGHT_TEAL", 1356933),
            Pair("DEEPSLATE_GRAY", 0x646464),
            Pair("RAW_IRON_PINK", 14200723),
            Pair("LICHEN_GREEN", 8365974),
        )
        f(colorPairs)
    }

    private fun f(colorPairs: List<Pair<String, Int>>) {
        val html = html {
            "html" {
                "head" {
                    "meta"("charset" to "utf-8")
                    "style" {
                        !"td {"
                        !"  font-family: \"FOT-ハミング Std D\";"
                        !"}"
                    }
                }
                "body" {
                    "table" {
                        colorPairs.chunked(8) { chunk ->
                            "tr" {
                                chunk.forEach { (name, rgb) ->
                                    "td"(
                                        "style" to inlineCss {
                                            "background-color" to "#" + String.format("%06X", rgb)
                                            if ((rgb and 0xFF) + (rgb shr 8 and 0xFF) + (rgb shr 16 and 0xFF) < 128 * 3) "color" to "#FFFFFF"
                                            "word-break" to "break-all"
                                            "width" to "100px"
                                            "height" to "100px"
                                            "font-family" to "\"FOT-ハミング Std D\""
                                            "font-size" to "15px"
                                            "line-height" to "25px"
                                            "text-align" to "center"
                                        },
                                    ) {
                                        !name.replace("_", "<br/>")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        File("./build/MapColorViewer.html").writeText(html)
    }

    class HtmlScope(private val output: (String) -> Unit) {
        operator fun String.not() {
            output(this)
        }

        operator fun String.invoke(vararg attributes: Pair<String, String>, block: HtmlScope.() -> Unit = {}) {
            val head = listOf(
                this,
                *attributes.map { "${it.first}=\"${it.second.replace("&", "&amp;").replace("\"", "&quot;")}\"" }.toTypedArray(),
            ).join(" ")
            val strings = mutableListOf<String>()
            block(HtmlScope {
                strings += it
            })
            if (strings.isNotEmpty()) {
                !"<$head>"
                strings.forEach {
                    !"  $it"
                }
                !"</$this>"
            } else {
                !"<$head/>"
            }
        }
    }

    fun html(block: HtmlScope.() -> Unit): String {
        val strings = mutableListOf<String>()
        strings += "<!DOCTYPE html>"
        block(HtmlScope {
            strings += it
        })
        return strings.map { "$it\n" }.join("")
    }

    class CssScope(private val output: (Pair<String, String>) -> Unit) {
        infix fun String.to(value: String) {
            output(Pair(this, value))
        }
    }

    fun inlineCss(block: CssScope.() -> Unit): String {
        val properties = mutableListOf<Pair<String, String>>()
        block(CssScope {
            properties += it
        })
        return properties.map { "${it.first}: ${it.second}" }.join("; ")
    }
}
