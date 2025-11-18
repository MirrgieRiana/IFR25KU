import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun Project.getAllPngFilesInAllProjects(): List<File> {
    return this.allprojects.flatMap { project ->
        val sourceSets = project.extensions.findByName("sourceSets") as? SourceSetContainer ?: return@flatMap emptyList()
        sourceSets.flatMap { sourceSet ->
            sourceSet.resources.srcDirs
                .filter { it.exists() }
                .flatMap { it.walkTopDown().filter { file -> file.isFile && file.extension.equals("png", true) }.toList() }
        }
    }.distinct()
}

fun resizeImage(srcImage: BufferedImage, scale: Int): Triple<BufferedImage, Int, Int> {
    val destWidth = srcImage.width * scale
    val destHeight = srcImage.height * scale
    val destImage = BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_ARGB)
    val graphics = destImage.createGraphics()
    try {
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR)
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        graphics.drawImage(srcImage, 0, 0, destWidth, destHeight, null)
    } finally {
        graphics.dispose()
    }
    return Triple(destImage, destWidth, destHeight)
}

fun generateResizedImagesOfAllProjects(inputFiles: List<File>, baseDir: File, destDir: File, scale: Int) {
    val actualBaseDir = baseDir.absoluteFile.normalize()
    val actualDestDir = destDir.absoluteFile.normalize()

    if (actualDestDir.exists() && !actualDestDir.deleteRecursively()) error("Failed to delete directory: ${actualDestDir.path}")
    inputFiles.forEach { srcFile ->
        val actualSrcFile = srcFile.absoluteFile.normalize()

        val srcImage = ImageIO.read(actualSrcFile) ?: error("Invalid image format: ${actualSrcFile.path}")
        val (destImage, destWidth, destHeight) = resizeImage(srcImage, scale)
        if (!actualSrcFile.startsWith(actualBaseDir)) error("Source file is not under base directory: ${actualSrcFile.path}")
        val destFileName = "${actualSrcFile.nameWithoutExtension}_${destWidth}x${destHeight}.png"
        val destFile = actualDestDir.resolve(actualSrcFile.parentFile.relativeTo(actualBaseDir)).resolve(destFileName)
        if (!destFile.parentFile.exists() && !destFile.parentFile.mkdirs()) error("Failed to create directory: ${destFile.parentFile.path}")
        ImageIO.write(destImage, "png", destFile)
    }
}
