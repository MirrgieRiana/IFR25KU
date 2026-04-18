import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.luciad.imageio.webp.WebPWriteParam
import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import org.yaml.snakeyaml.Yaml
import tools.normalizeJson
import tools.sha256
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Base64.getEncoder
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.yaml:snakeyaml:2.2")
        classpath("com.microsoft.playwright:playwright:1.58.0")
        classpath("org.sejda.imageio:webp-imageio:0.1.6")
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

        // lang_table.jsonl
        run {
            val list = keys.map { key ->
                mapOf(
                    "key" to key,
                    "en_us" to (en.get(key) as JsonPrimitive?)?.asString,
                    "ja_jp" to (ja.get(key) as JsonPrimitive?)?.asString,
                )
            }
            val gson = GsonBuilder().create()
            val jsonl = list.joinToString("") { gson.toJson(it) + "\n" }
            write("langTable/lang_table.jsonl", jsonl)
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

val makeRecipeTable = tasks.register("makeRecipeTable") {
    group = "generate"

    val recipeDir = rootProject.file("common/src/generated/resources/data/miragefairy2024/recipe")

    inputs.dir(recipeDir)
    outputs.dir(layout.buildDirectory.dir("recipeTable"))

    fun write(path: String, content: String) {
        val outFile = layout.buildDirectory.file(path).get().asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(content)
        println("Wrote to ${outFile.absolutePath}")
    }

    doLast {
        val gson = GsonBuilder().create()
        val jsonl = recipeDir.walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .sortedBy { it.relativeTo(recipeDir).path }
            .joinToString("") { file ->
                val json = gson.fromJson(file.readText(), JsonElement::class.java).asJsonObject
                val relativePath = file.relativeTo(recipeDir).invariantSeparatorsPath.removeSuffix(".json")
                json.addProperty("id", "miragefairy2024:$relativePath")
                gson.toJson(json.normalizeJson()) + "\n"
            }
        write("recipeTable/recipe_table.jsonl", jsonl)
    }
}

private fun buildOgHtml(title: String, backgroundUrl: String, pixelated: Boolean): String {
    val escapedTitle = title.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
    val imageRendering = if (pixelated) "image-rendering: pixelated; " else ""
    return """
    <!DOCTYPE html>
    <html>
    <head><meta charset="utf-8"></head>
    <body style="margin: 0; width: 1200px; height: 630px; background: url('$backgroundUrl') center/cover no-repeat; ${imageRendering}display: flex; align-items: flex-start;">
        <div style="width: 100%; padding: 24px 40px; background: rgba(0, 0, 0, 0.5); color: white; font-family: 'Noto Sans CJK JP', sans-serif; font-size: 36px; line-height: 1.4; word-break: auto-phrase;">
            $escapedTitle
        </div>
    </body>
    </html>
""".trimIndent()
}

private fun parseFrontMatter(file: File): Map<String, Any>? {
    val content = file.readText()
    val match = Regex("\\A---\\r?\\n(.*?)\\r?\\n---(?:\\r?\\n|\\Z)", RegexOption.DOT_MATCHES_ALL).find(content) ?: return null
    @Suppress("UNCHECKED_CAST")
    return Yaml().load<Map<String, Any>>(match.groupValues[1]) as? Map<String, Any>
}

class OgImageRenderer : AutoCloseable {
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

    fun render(title: String, backgroundImageFile: File, outputFile: File, pixelated: Boolean) {
        ensureInitialized()
        page!!.setContent(buildOgHtml(title, toDataUri(backgroundImageFile), pixelated))
        val pngBytes = page!!.screenshot()
        val image = ImageIO.read(ByteArrayInputStream(pngBytes))!!
        val writer = ImageIO.getImageWritersByMIMEType("image/webp").next()
        val writeParam = writer.defaultWriteParam as WebPWriteParam
        writeParam.compressionMode = ImageWriteParam.MODE_EXPLICIT
        writeParam.compressionType = writeParam.compressionTypes[WebPWriteParam.LOSSY_COMPRESSION]
        writeParam.compressionQuality = 0.80f
        val webpBuffer = ByteArrayOutputStream()
        ImageIO.createImageOutputStream(webpBuffer).use { imageOut ->
            writer.output = imageOut
            writer.write(null, IIOImage(image, null, null), writeParam)
        }
        writer.dispose()
        outputFile.writeBytes(webpBuffer.toByteArray())
    }

    override fun close() {
        page?.close()
        browser?.close()
        playwright?.close()
    }
}

val generateOgImages = tasks.register("generateOgImages") {
    group = "generate"

    val pagesDir = file("src/pages/resources")
    val resourcesDir = file("src/main/resources")
    val ogImagesDir = file("src/ogImages/resources")
    val outputDir = ogImagesDir.resolve("assets/images")
    val regenerate = project.hasProperty("regenerate")

    inputs.dir(pagesDir)
    inputs.file(file("src/ogImages/assets/default-background.svg"))

    doLast {
        outputDir.mkdirs()
        ImageIO.scanForPlugins()

        // .mdファイルを収集
        val mdFiles = pagesDir.listFiles().orEmpty()
            .mapNotNull { dir ->
                if (!dir.isDirectory) return@mapNotNull null
                val mdFilesInDir = dir.listFiles { f -> f.isFile && f.extension == "md" }.orEmpty()
                if (mdFilesInDir.isEmpty()) error("No markdown file found in ${dir.name}")
                if (mdFilesInDir.size > 1) error("Multiple markdown files in ${dir.name}: ${mdFilesInDir.map { it.name }}")
                mdFilesInDir.single()
            }

        val defaultBg = file("src/ogImages/assets/default-background.svg")
        OgImageRenderer().use { renderer ->
            mdFiles.forEach { mdFile ->
                val frontMatter = parseFrontMatter(mdFile) ?: return@forEach

                // titleを取得
                val title = frontMatter["title"] as? String ?: return@forEach

                // header画像パスを取得（優先順位: og_background > overlay_image > image > teaser）
                @Suppress("UNCHECKED_CAST")
                val header = frontMatter["header"] as? Map<String, Any>
                val imagePath = (header?.get("og_background") ?: header?.get("overlay_image") ?: header?.get("image") ?: header?.get("teaser")) as? String

                // page.url準拠の出力パスを決定
                val pageDirName = mdFile.parentFile.name
                val postMatch = """(\d{4})-(\d{2})-(\d{2})-(.+)""".toRegex().matchEntire(pageDirName)
                val outputFile = if (postMatch != null) {
                    val (year, month, day, slug) = postMatch.destructured
                    outputDir.resolve("$year/$month/$day/$slug.og.webp")
                } else {
                    outputDir.resolve("${mdFile.nameWithoutExtension}.og.webp")
                }

                // ベース画像を解決
                val effectiveFile = if (imagePath != null) {
                    val fileName = File(imagePath).name
                    val localFile = mdFile.parentFile.resolve(fileName)
                    if (localFile.exists()) localFile
                    else error("OG background image not found: $fileName in ${mdFile.parentFile}")
                } else defaultBg

                // ピクセル化判定
                val pixelated = if (effectiveFile.extension.equals("svg", ignoreCase = true)) {
                    false
                } else {
                    val image = ImageIO.read(effectiveFile) ?: error("Failed to read image: ${effectiveFile.absolutePath}")
                    1200.0 / maxOf(image.width, image.height) >= 4.0
                }

                // キャッシュ用入力JSONを計算
                val inputsFile = outputFile.resolveSibling("${outputFile.name.removeSuffix(".og.webp")}.inputs.json")
                val inputsJson = GsonBuilder().disableHtmlEscaping().create().toJson(
                    JsonObject().also {
                        it.addProperty("background_hash", effectiveFile.sha256())
                        it.addProperty("html_hash", buildOgHtml(title, "BACKGROUND", pixelated).sha256())
                    }.normalizeJson()
                )

                // 入力に変化がない場合はスキップ（-Pregenerateで強制再生成）
                if (!regenerate && outputFile.exists() && inputsFile.exists() && inputsFile.readText() == inputsJson) {
                    logger.lifecycle("OG image is up-to-date, skipping: ${outputFile.absolutePath}")
                    return@forEach
                }

                outputFile.parentFile.mkdirs()

                renderer.render(title, effectiveFile, outputFile, pixelated)
                logger.lifecycle("Generated OG image: ${outputFile.absolutePath}")
                inputsFile.writeText(inputsJson)
            }
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
    dependsOn(generateOgImages)
    from("src/main/resources")
    from("src/ogImages/resources") {
        include("**/*.webp")
    }
    from("src/external/resources")
    from("src/pages/resources") {
        includeEmptyDirs = false
        val seenImagePaths = mutableMapOf<String, String>()
        eachFile {
            val dirName = relativePath.pathString.substringBefore("/")
            val postMatch = """(\d{4})-(\d{2})-(\d{2})-(.+)""".toRegex().matchEntire(dirName)
            if (postMatch != null) {
                val (year, month, day, _) = postMatch.destructured
                if (name.endsWith(".md")) {
                    relativePath = RelativePath(true, "_posts", "$dirName.md")
                } else {
                    val sourcePath = relativePath.pathString
                    relativePath = RelativePath(true, year, month, day, name)
                    val outputKey = relativePath.pathString
                    seenImagePaths[outputKey]?.let { existingSource ->
                        error("Image filename collision at '$outputKey': '$existingSource' and '$sourcePath'")
                    }
                    seenImagePaths[outputKey] = sourcePath
                }
            } else {
                if (name.endsWith(".md")) {
                    relativePath = RelativePath(true, name)
                } else {
                    relativePath = RelativePath(true, "assets", "images", dirName, name)
                }
            }
        }
    }
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

val build = tasks.register("build") {
    group = "build"
}

val buildSite = tasks.register<Sync>("buildSite") {
    group = "build"
    from(jekyllBuild)
    from(makeLangTable)
    from(makeRecipeTable)
    from("src/pages/resources") {
        include("**/*.md")
        includeEmptyDirs = false
        eachFile {
            val dirName = relativePath.pathString.substringBefore("/")
            val postMatch = """(\d{4})-(\d{2})-(\d{2})-(.+)""".toRegex().matchEntire(dirName)
            if (postMatch != null) {
                relativePath = RelativePath(true, "_posts", "$dirName.md")
            } else {
                relativePath = RelativePath(true, name)
            }
        }
    }
    into(layout.buildDirectory.dir("site"))
}
build.configure { dependsOn(buildSite) }

val serveSite = tasks.register<Exec>("serveSite") {
    group = "application"
    inputs.files(buildSite)
    commandLine("bash", "scripts/serve-site.sh")
}
