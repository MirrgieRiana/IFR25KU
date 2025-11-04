import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@CacheableTask
abstract class MirrorMavenTask : DefaultTask() {

    @get:Input
    abstract val repositoryUrl: Property<String>

    @get:Input
    abstract val coordinates: ListProperty<String>

    @get:Input
    abstract val licenseUrl: Property<String>

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    private data class Coordinate(val group: String, val artifact: String, val version: String, val classifier: String?, val ext: String) {
        companion object {
            private val regex = """^([^:]+):([^:]+):([^:@]+)(?::([^@]+))?(?:@([A-Za-z0-9._-]+))?$""".toRegex()
            fun parse(string: String): Coordinate {
                val m = regex.matchEntire(string) ?: error("Invalid coordinate: '$string' (expected group:artifact:version[:classifier][@ext])")
                val (g, a, v, c, e) = m.destructured
                return Coordinate(g, a, v, c.ifBlank { null }, e.ifBlank { "jar" })
            }
        }

        private val groupPath = group.replace('.', '/')
        private val parentDirectoryPath = "$groupPath/$artifact/$version"
        private val fileName = "$artifact-$version${if (classifier != null) "-$classifier" else ""}.$ext"
        fun getSrcUrl(repositoryUrl: String): URL = URI("${repositoryUrl.trimEnd('/')}/$parentDirectoryPath/$fileName").toURL()
        fun getDestFile(outputDirectory: File) = outputDirectory.resolve("$parentDirectoryPath/$fileName")
        fun getLicenseDestFile(outputDirectory: File) = outputDirectory.resolve("$parentDirectoryPath/LICENSE")
    }

    @TaskAction
    fun run() {
        val licenseFile by lazy { downloadToTemp(URI(licenseUrl.get()).toURL()) }

        coordinates.get().toSet()
            .map { Coordinate.parse(it) }
            .forEach { coordinate ->
                val dest = coordinate.getDestFile(outputDirectory.get().asFile)
                if (dest.exists()) {
                    logger.lifecycle("Already exists: ${dest.absolutePath}")
                    return@forEach
                }
                val src = downloadToTemp(coordinate.getSrcUrl(repositoryUrl.get()))
                dest.parentFile.mkdirs()
                Files.move(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }
        coordinates.get().toSet()
            .map { Coordinate.parse(it).getLicenseDestFile(outputDirectory.get().asFile) }
            .distinct()
            .reversed().withIndex().reversed()
            .forEach { (index, dest) ->
                if (dest.exists()) {
                    logger.lifecycle("Already exists: ${dest.absolutePath}")
                    return@forEach
                }
                dest.parentFile.mkdirs()
                if (index == 0) {
                    Files.move(licenseFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } else {
                    Files.copy(licenseFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            }
    }

    private fun downloadToTemp(url: URL): File {
        try {
            val connection = (url.openConnection() as HttpURLConnection).also {
                it.instanceFollowRedirects = true
                it.connectTimeout = 15_000
                it.readTimeout = 15_000
                it.requestMethod = "GET"
            }
            val tmp = Files.createTempFile("download-", ".tmp")
            connection.inputStream.use { input ->
                Files.copy(input, tmp, StandardCopyOption.REPLACE_EXISTING)
            }
            logger.lifecycle("Downloaded to temp: $url")
            return tmp.toFile()
        } catch (e: Exception) {
            logger.error("Failed to download: $url (${e.message})")
            throw e
        }
    }
}
