import java.io.File

fun getChangeLog(file: File, version: String): String {
    val map = mutableMapOf<String?, MutableList<String>>()
    var currentVersion: String? = null
    file.readText().lines().forEach { line ->
        val result = """##\s*(\d+(:?\.\d+)*).*""".toRegex().matchEntire(line)
        if (result != null) {
            currentVersion = result.groups[1]!!.value
        } else {
            map.getOrPut(currentVersion) { mutableListOf() } += line.trim()
        }
    }
    val string = map.getOrElse(version) { listOf() }.joinToString("\n") { it }
        .replace("""\A\n+""".toRegex(), "")
        .replace("""\n+\Z""".toRegex(), "")
    return string
}
