import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.yaml.snakeyaml.Yaml
import java.io.ByteArrayInputStream
import java.util.Base64.getEncoder
import javax.imageio.ImageIO

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.yaml:snakeyaml:2.2")
        classpath("com.microsoft.playwright:playwright:1.58.0")
        classpath("com.twelvemonkeys.imageio:imageio-webp:3.12.0")
    }
}

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
    outputs.dir(layout.buildDirectory.dir("bundleVendor"))
    outputs.dir(layout.buildDirectory.dir("bundleConfig"))

    commandLine("bash", "scripts/bundle-install.sh")
}

val syncJekyllSource = tasks.register<Sync>("syncJekyllSource") {
    group = "other"
    from("src/main/resources")
    from("src/main/bundle")
    into(layout.buildDirectory.dir("jekyllSource"))
}

val jekyllBuild = tasks.register<Exec>("jekyllBuild") {
    group = "build"
    dependsOn(installJekyllBundle) // UP-TO-DATE の判定にかかるコストの削減のために敢えて inputs にしない
    inputs.files(syncJekyllSource)
    outputs.dir(layout.buildDirectory.dir("jekyllBuild"))
    commandLine("bash", "scripts/build-site.sh")
}

private fun parseFrontMatter(file: File): Map<String, Any>? {
    val content = file.readText()
    val match = Regex("\\A---\\r?\\n(.*?)\\r?\\n---(?:\\r?\\n|\\Z)", RegexOption.DOT_MATCHES_ALL).find(content) ?: return null
    @Suppress("UNCHECKED_CAST")
    return Yaml().load<Map<String, Any>>(match.groupValues[1]) as? Map<String, Any>
}

class OgImageRenderer(private val defaultBackgroundFile: File) : AutoCloseable {
    private var playwright: Playwright? = null
    private var browser: Browser? = null
    private var page: Page? = null

    private fun ensureInitialized() {
        if (playwright == null) {
            playwright = Playwright.create()
            browser = playwright!!.chromium().launch()
            page = browser!!.newPage().apply { setViewportSize(1200, 630) }
        }
    }

    private fun toDataUri(file: File): String {
        val base64 = getEncoder().encodeToString(file.readBytes())
        val mimeType = when (file.extension.lowercase()) {
            "webp" -> "image/webp"
            "svg" -> "image/svg+xml"
            "png" -> "image/png"
            "jpg", "jpeg" -> "image/jpeg"
            else -> "application/octet-stream"
        }
        return "data:$mimeType;base64,$base64"
    }

    fun render(title: String, backgroundImageFile: File?, outputFile: File) {
        ensureInitialized()
        val effectiveFile = if (backgroundImageFile != null && backgroundImageFile.exists()) backgroundImageFile else defaultBackgroundFile
        val backgroundCss = "background: url('${toDataUri(effectiveFile)}') center/cover no-repeat"
        val escapedTitle = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
        page!!.setContent(
            """
            <!DOCTYPE html>
            <html>
            <head><meta charset="utf-8"></head>
            <body style="margin: 0; width: 1200px; height: 630px; $backgroundCss; display: flex; align-items: flex-end;">
                <div style="width: 100%; padding: 24px 40px; background: rgba(0, 0, 0, 0.5); color: white; font-family: 'Noto Sans CJK JP', sans-serif; font-size: 36px; line-height: 1.4; word-break: auto-phrase;">
                    $escapedTitle
                </div>
            </body>
            </html>
        """.trimIndent()
        )
        val pngBytes = page!!.screenshot()
        val image = ImageIO.read(ByteArrayInputStream(pngBytes))
        require(ImageIO.write(image, "webp", outputFile)) { "Failed to write WebP image: $outputFile" }
    }

    override fun close() {
        page?.close()
        browser?.close()
        playwright?.close()
    }
}

val generateOgImages = tasks.register("generateOgImages") {
    group = "generate"

    val resourcesDir = file("src/main/resources")
    val outputDir = resourcesDir.resolve("assets/images")
    val regenerate = project.hasProperty("regenerate")

    inputs.files(fileTree(resourcesDir) { include("*.md", "_posts/*.md") })
    inputs.dir(resourcesDir.resolve("assets/images"))

    doLast {
        val outputDirFile = outputDir
        outputDirFile.mkdirs()

        // .mdファイルを収集（ルート + _posts）
        val mdFiles = resourcesDir.listFiles { f -> f.isFile && f.extension == "md" }?.toList().orEmpty() +
            (resourcesDir.resolve("_posts").listFiles { f -> f.isFile && f.extension == "md" }?.toList().orEmpty())

        val defaultBg = resourcesDir.resolve("assets/images/og-default-background.svg")
        OgImageRenderer(defaultBg).use { renderer ->
            mdFiles.forEach { mdFile ->
                val frontMatter = parseFrontMatter(mdFile) ?: return@forEach

                // titleを取得
                val title = frontMatter["title"] as? String ?: return@forEach

                // header画像パスを取得（優先順位: overlay_image > image > teaser）
                @Suppress("UNCHECKED_CAST")
                val header = frontMatter["header"] as? Map<String, Any>
                val imagePath = (header?.get("overlay_image") ?: header?.get("image") ?: header?.get("teaser")) as? String

                // page.url準拠の出力パスを決定
                val isPost = mdFile.parentFile.name == "_posts"
                val outputFile = if (isPost) {
                    // _posts/YYYY-MM-DD-title.md → YYYY/MM/DD/title.og.webp
                    val name = mdFile.nameWithoutExtension
                    val year = name.substring(0, 4)
                    val month = name.substring(5, 7)
                    val day = name.substring(8, 10)
                    val slug = name.substring(11)
                    outputDirFile.resolve("$year/$month/$day/$slug.og.webp")
                } else {
                    // page.md → page.og.webp
                    outputDirFile.resolve("${mdFile.nameWithoutExtension}.og.webp")
                }

                // 既に存在する場合はスキップ（-Pregenerateで強制再生成）
                if (!regenerate && outputFile.exists()) {
                    logger.lifecycle("OG image already exists, skipping: ${outputFile.absolutePath}")
                    return@forEach
                }

                outputFile.parentFile.mkdirs()

                // ベース画像を解決
                val baseImageFile = if (imagePath != null) resourcesDir.resolve(imagePath.removePrefix("/")) else null

                renderer.render(title, baseImageFile, outputFile)
                logger.lifecycle("Generated OG image: ${outputFile.absolutePath}")
            }
        }
    }
}

val build = tasks.register("build") {
    group = "build"
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
build.configure { dependsOn(buildSite) }

val serveSite = tasks.register<Exec>("serveSite") {
    group = "application"
    inputs.files(buildSite)
    commandLine("bash", "scripts/serve-site.sh")
}
