import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

val makeLangTable = tasks.register("makeLangTable") {
    group = "pages"
    //dependsOn(project("fabric").tasks.named("runDatagen")) // CI上でrunDatagenが実行済みであることを強制しているので実行しないことにする

    outputs.dir(layout.buildDirectory.dir("langTable"))

    fun write(path: String, content: String) {
        val outFile = layout.buildDirectory.file(path).get().asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(content)
        println("Wrote to ${outFile.absolutePath}")
    }

    doLast {
        val en by lazy { GsonBuilder().create().fromJson(rootProject.file("common/src/generated/resources/assets/miragefairy2024/lang/en_us.json").readText(), JsonElement::class.java).asJsonObject }
        val ja by lazy { GsonBuilder().create().fromJson(rootProject.file("common/src/generated/resources/assets/miragefairy2024/lang/ja_jp.json").readText(), JsonElement::class.java).asJsonObject }
        val keys by lazy { (en.keySet() + ja.keySet()).sorted() }

        // lang_table.html: テンプレート展開
        run {
            val trs = keys.joinToString("") { key ->
                listOf(
                    """<tr>""",
                    """<td class="key">$key</td>""",
                    """<td class="value">${(en.get(key) as JsonPrimitive?)?.asString ?: "-"}</td>""",
                    """<td class="value">${(ja.get(key) as JsonPrimitive?)?.asString ?: "-"}</td>""",
                    """</tr>""",
                ).joinToString("\n") { it }
            }
            val html = file("src/langTable/html/lang_table.html").readText().replace("<%= trs %>", trs)
            write("langTable/lang_table.html", html)
        }

        // lang_table.json
        run {
            val table = keys.associateWith { key ->
                mapOf(
                    "en_us" to (en.get(key) as JsonPrimitive?)?.asString,
                    "ja_jp" to (ja.get(key) as JsonPrimitive?)?.asString,
                )
            }
            val json = GsonBuilder().setPrettyPrinting().create().toJson(table)
            write("langTable/lang_table.json", json)
        }

        // lang_table.csv
        run {
            val table = listOf(
                listOf("key", "en_us", "ja_jp"),
                *keys.map { key ->
                    listOf(
                        key,
                        (en.get(key) as JsonPrimitive?)?.asString ?: "-",
                        (ja.get(key) as JsonPrimitive?)?.asString ?: "-",
                    )
                }.toTypedArray(),
            )
            val csv = table.joinToString("") { row ->
                row.joinToString(",") {
                    if (',' in it || '"' in it || '\r' in it || '\n' in it) {
                        "\"" + it.replace("\"", "\"\"") + "\""
                    } else {
                        it
                    }
                } + "\n"
            }
            write("langTable/lang_table.csv", csv)
        }
    }
}

val syncPages = tasks.register<Sync>("syncPages") {
    group = "pages"

    from("pages") {
        include("**/*")
    }

    from(makeLangTable)

    into(layout.buildDirectory.dir("pages"))

    // bundle installで生成されるファイルをSyncの削除対象から除外する
    preserve {
        include("vendor/**")
        include(".bundle/**")
    }
}

val buildPages = tasks.register<Exec>("buildPages") {
    group = "pages"
    dependsOn(syncPages)
    commandLine("bash", "scripts/build-pages.sh")
}

val servePages = tasks.register<Exec>("servePages") {
    group = "pages"
    dependsOn(syncPages)
    commandLine("bash", "scripts/serve-pages.sh")
}
