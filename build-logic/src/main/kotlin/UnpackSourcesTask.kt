import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.component.ProjectComponentIdentifier
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.artifacts.result.ComponentArtifactsResult
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jvm.JvmLibrary
import org.gradle.language.base.artifact.SourcesArtifact

abstract class UnpackSourcesTask : DefaultTask() {

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    init {
        group = "help"
        outputDirectory.convention(project.layout.buildDirectory.dir("unpackedSources"))
    }

    @TaskAction
    fun run() {
        project.logger.info("Processing project: ${project.name}")

        data class Coordinate(val group: String, val name: String, val version: String) {
            val string = "$group:$name:$version"
        }

        // プロジェクトのリポジトリを表示
        project.repositories.also {
            if (it.isEmpty()) logger.info("No repositories defined")
        }.forEach { repository ->
            logger.info("Repository: ${repository.name} - ${(repository as? MavenArtifactRepository)?.url}")
        }

        // プロジェクトにconfigurationがない場合はスキップ
        if (project.configurations.isEmpty()) {
            logger.info("[Skipped] Project '${project.name}' has no configurations.")
            return
        }

        // プロジェクトのconfigurationを解決可能かどうかで分別
        val (resolvableConfigurations, nonResolvableConfigurations) = project.configurations.partition { it.isCanBeResolved }

        // 依存性解決のためのクエリ
        val query = project.dependencies.createArtifactResolutionQuery()

        // 解決可能なconfigurationから、プロジェクト依存性でない依存性を抽出
        // プロジェクト依存性はスキップ
        val resolvableNonProjectDependencies = resolvableConfigurations
            .flatMap { it.incoming.resolutionResult.allComponents }
            .filter { it.id !is ProjectComponentIdentifier }
            .map { it.id }
            .toSet()

        // 解決可能な依存性はそのまま解決可能
        resolvableNonProjectDependencies.forEach {
            logger.info("Adding resolvable non-project dependency: ${it.displayName}")
            query.forComponents(it)
        }

        // 解決可能な外部モジュール依存性の座標
        val resolvableCoordinates = resolvableNonProjectDependencies
            .mapNotNull { it as? ModuleComponentIdentifier }
            .map { Coordinate(it.group, it.module, it.version) }
            .toSet()

        // 解決不可能なconfigurationから、外部モジュール依存性を抽出
        // 外部モジュール依存性でないものはスキップ
        val nonResolvableCoordinates = nonResolvableConfigurations
            .flatMap { configuration ->
                configuration.dependencies.mapNotNull { dependency ->
                    if (dependency is ExternalModuleDependency) {
                        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
                        Coordinate(dependency.group!!, dependency.name, dependency.version ?: "")
                    } else {
                        logger.info("[Skipped] Configuration '${configuration.name}' Dependency '${dependency.name}' is not an ExternalModuleDependency.");
                        null
                    }
                }
            }
            .toSet()

        // 解決不可能な外部モジュール依存性は個別に座標を指定して解決
        (nonResolvableCoordinates - resolvableCoordinates).forEach { coordinate ->
            logger.info("Adding non-resolvable coordinate: ${coordinate.string}")
            query.forModule(coordinate.group, coordinate.name, coordinate.version)
        }

        // 解決
        query.withArtifacts(JvmLibrary::class.java, SourcesArtifact::class.java)
        val result = query.execute()

        // 出力先を準備
        val outputDirectoryFile = outputDirectory.get().asFile
        if (outputDirectoryFile.exists()) outputDirectoryFile.deleteRecursively()

        // すべてのソースアーティファクトを展開
        result.components.forEach component@{ componentResult ->

            // アーティファクトが解決できなかった場合はスキップ
            if (componentResult !is ComponentArtifactsResult) {
                logger.error("[Failed] Could not resolve component: ${componentResult.id.displayName}")
                return@component
            }

            // ソースアーティファクトのみを抽出
            val sourceArtifactResults = componentResult.getArtifacts(SourcesArtifact::class.java)

            // ソースアーティファクトが無かった場合はスキップ
            if (sourceArtifactResults.isEmpty()) {
                logger.info("[Skipped] No sources found for ${componentResult.id.displayName}")
                return@component
            }

            // すべてのソースアーティファクトを展開
            sourceArtifactResults.forEach { sourceArtifactResult ->

                // ソースアーティファクトが解決できなかった場合はスキップ
                if (sourceArtifactResult !is ResolvedArtifactResult) {
                    logger.error("[Failed] Could not resolve source for ${sourceArtifactResult.id.displayName}")
                    return@forEach
                }

                // 展開
                val dest = outputDirectoryFile.resolve(sourceArtifactResult.file.nameWithoutExtension)
                dest.mkdirs()
                project.copy {
                    from(project.zipTree(sourceArtifactResult.file))
                    into(dest)
                }

            }

        }

    }

}
