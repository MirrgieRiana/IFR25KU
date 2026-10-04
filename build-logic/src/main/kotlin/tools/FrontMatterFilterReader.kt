package tools

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.FilterReader
import java.io.Reader
import java.io.StringReader

// ファイルの冒頭の front matter を、区切りの行ごと捕まえるのだ～🌱
val frontMatterRegex = Regex("\\A---\\r?\\n(.*?)\\r?\\n---(?:\\r?\\n|\\Z)", RegexOption.DOT_MATCHES_ALL)

// front matter で画像を指すキーなのだ～🌱
private val frontMatterImageKeys = setOf("teaser", "image", "overlay_image", "og_background")

private val frontMatterDumperOptions = DumperOptions().also {
    it.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
    // 日本語をエスケープせずにそのまま書き出すのだ～🌱
    it.isAllowUnicode = true
    // 既定の 80 桁だと、長い値が途中で折り返されて複数行へ散るのだ～🌱
    it.width = Int.MAX_VALUE
}

fun parseFrontMatter(file: File): Map<String, Any>? {
    val match = frontMatterRegex.find(file.readText()) ?: return null
    @Suppress("UNCHECKED_CAST")
    return Yaml().load<Map<String, Any>>(match.groupValues[1]) as? Map<String, Any>
}

// 値を . から始めた画像のパスを、入れ子の奥まで辿って、絶対パスへ直すのだ～🌱
private fun resolveFrontMatterImagePaths(value: Any?, imageDir: String): Any? = when (value) {
    is Map<*, *> -> {
        value.mapValues { (key, child) ->
            if (key is String && key in frontMatterImageKeys && child is String && child.startsWith(".")) {
                File(imageDir, child).normalize().invariantSeparatorsPath
            } else {
                resolveFrontMatterImagePaths(child, imageDir)
            }
        }
    }

    is List<*> -> {
        value.map { resolveFrontMatterImagePaths(it, imageDir) }
    }

    else -> value
}

fun rewriteFrontMatter(content: String, sourcePath: String, imageDir: String): String {
    val match = frontMatterRegex.find(content) ?: return content

    @Suppress("UNCHECKED_CAST")
    // --- が 2 行並んだだけの front matter は、YAML として妥当で、snakeyaml は null を返すのだ～🌱
    // そのまま返すと source_path が落ちて、footer の source のリンクが壊れるのだ～🌱
    val frontMatter = Yaml().load<Map<String, Any>>(match.groupValues[1]) as? Map<String, Any> ?: emptyMap()

    @Suppress("UNCHECKED_CAST")
    val resolved = resolveFrontMatterImagePaths(frontMatter, imageDir) as Map<String, Any?>
    // 配置先が平らになって元のディレクトリ名が失われるから、footer の source のリンクのために、元のパスを front matter へ書き足すのだ～🌱
    val rewritten = resolved + ("source_path" to sourcePath)
    return "---\n${Yaml(frontMatterDumperOptions).dump(rewritten)}---\n${content.substring(match.range.last + 1)}"
}

// front matter を YAML として読み直して書き戻すのだ～🌱
// Sync の filter は 1 行ずつしか渡してくれないから、ファイルの全体を抱えられる FilterReader の形にするのだ～🌱
class FrontMatterFilterReader(input: Reader) : FilterReader(input) {
    // filter の第 1 引数の Map から Gradle が設定する値なのだ～🌱
    var sourcePath: String = ""
    var imageDir: String = ""

    private var rewritten = false

    private fun rewriteOnce() {
        if (rewritten) return
        rewritten = true
        // in を差し替えると、ここで元の Reader への参照が失われて、FilterReader の close が届かなくなるのだ～🌱
        val content = `in`.use { it.readText() }
        `in` = StringReader(rewriteFrontMatter(content, sourcePath, imageDir))
    }

    override fun read(): Int {
        rewriteOnce()
        return super.read()
    }

    override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        rewriteOnce()
        return super.read(cbuf, off, len)
    }
}
