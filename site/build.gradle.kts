import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

val makeLangTable = tasks.register("makeLangTable") {
    group = "generate"
    //dependsOn(project("fabric").tasks.named("runDatagen")) // CI上でrunDatagenが実行済みであることを強制しているので実行しないことにする

    val enFile = rootProject.file("common/src/generated/resources/assets/miragefairy2024/lang/en_us.json")
    val jaFile = rootProject.file("common/src/generated/resources/assets/miragefairy2024/lang/ja_jp.json")
    val templateFile = file("src/langTable/html/lang_table.html")

    inputs.file(enFile)
    inputs.file(jaFile)
    inputs.file(templateFile)
    outputs.dir(layout.buildDirectory.dir("langTable"))

    fun write(path: String, content: String) {
        val outFile = layout.buildDirectory.file(path).get().asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(content)
        println("Wrote to ${outFile.absolutePath}")
    }

    doLast {
        val en by lazy { GsonBuilder().create().fromJson(enFile.readText(), JsonElement::class.java).asJsonObject }
        val ja by lazy { GsonBuilder().create().fromJson(jaFile.readText(), JsonElement::class.java).asJsonObject }
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
            val html = templateFile.readText().replace("<%= trs %>", trs)
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

val installJekyllBundle = tasks.register<Exec>("installJekyllBundle") {
    group = "other"

    inputs.file("src/main/bundle/Gemfile")
    inputs.file("src/main/bundle/Gemfile.lock")
    outputs.dir("src/main/bundle/vendor")
    outputs.dir("src/main/bundle/.bundle")

    commandLine("bash", "scripts/bundle-install.sh")
}

val syncJekyllSource = tasks.register<Sync>("syncJekyllSource") {
    group = "other"
    dependsOn(installJekyllBundle)
    from("src/main/resources")
    from("src/main/bundle")
    into(layout.buildDirectory.dir("jekyllSource"))
}

val jekyllBuild = tasks.register<Exec>("jekyllBuild") {
    group = "build"
    inputs.files(syncJekyllSource)
    outputs.dir(layout.buildDirectory.dir("jekyllBuild"))
    commandLine("bash", "scripts/build-site.sh")
}

val buildSite = tasks.register<Sync>("buildSite") {
    group = "build"
    from(jekyllBuild)
    from(makeLangTable)
    from("src/main/resources") {
        include("**/*.md")
    }
    into(layout.buildDirectory.dir("site"))
}

val serveSite = tasks.register<Exec>("serveSite") {
    group = "application"
    inputs.files(buildSite)
    inputs.files(syncJekyllSource) // bundle exec のために必要
    commandLine("bash", "scripts/serve-site.sh")
}
