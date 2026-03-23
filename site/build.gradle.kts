import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

tasks.register<Sync>("syncPages") {
    group = "pages"
    //dependsOn(project("fabric").tasks.named("runDatagen")) // CI上でrunDatagenが実行済みであることを強制しているので実行しないことにする

    val en by lazy { GsonBuilder().create().fromJson(rootProject.file("common/src/generated/resources/assets/miragefairy2024/lang/en_us.json").readText(), JsonElement::class.java).asJsonObject }
    val ja by lazy { GsonBuilder().create().fromJson(rootProject.file("common/src/generated/resources/assets/miragefairy2024/lang/ja_jp.json").readText(), JsonElement::class.java).asJsonObject }
    val keys by lazy { (en.keySet() + ja.keySet()).sorted() }

    from("pages") {
        include("**/*")
    }

    into(layout.buildDirectory.dir("pages"))

    // bundle installで生成されるファイルをSyncの削除対象から除外する
    preserve {
        include("vendor/**")
        include(".bundle/**")
    }

    fun write(path: String, content: String) {
        val outFile = layout.buildDirectory.file(path).get().asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(content)
        println("Wrote to ${outFile.absolutePath}")
    }

    doLast {
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
            write("pages/lang_table.html", html)
        }
        run {
            val table = keys.associateWith { key ->
                mapOf(
                    "en_us" to (en.get(key) as JsonPrimitive?)?.asString,
                    "ja_jp" to (ja.get(key) as JsonPrimitive?)?.asString,
                )
            }
            val json = GsonBuilder().setPrettyPrinting().create().toJson(table)
            write("pages/lang_table.json", json)
        }
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
            write("pages/lang_table.csv", csv)
        }
    }
}

tasks.register<Exec>("buildPages") {
    group = "pages"
    dependsOn("syncPages")
    commandLine("bash", "scripts/build-pages.sh")
}

tasks.register<Exec>("servePages") {
    group = "pages"
    dependsOn("syncPages")
    commandLine("bash", "scripts/serve-pages.sh")
}
