import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import io.github.themrmilchmann.gradle.publish.curseforge.GameVersion
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class CurseforgeClient(private val token: String) {

    fun callApi(url: String): String {
        val client = HttpClient.newHttpClient()
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("X-Api-Token", token)
            .header("Accept", "application/json")
            .GET()
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    data class VersionType(val id: Int, val name: String, val slug: String)

    val versionTypes by lazy {
        val json = callApi("https://minecraft.curseforge.com/api/game/version-types")
        val root = GsonBuilder().create().fromJson(json, JsonElement::class.java)
        root.asJsonArray.map {
            val item = it.asJsonObject
            VersionType(
                id = item["id"].asInt,
                name = item["name"].asString,
                slug = item["slug"].asString
            )
        }
    }

    data class Version(val id: Int, val gameVersionTypeID: Int, val name: String, val slug: String, val apiVersion: String?)

    val versions by lazy {
        val json = callApi("https://minecraft.curseforge.com/api/game/versions")
        val root = GsonBuilder().create().fromJson(json, JsonElement::class.java)
        root.asJsonArray.map {
            val item = it.asJsonObject
            Version(
                id = item["id"].asInt,
                gameVersionTypeID = item["gameVersionTypeID"].asInt,
                name = item["name"].asString,
                slug = item["slug"].asString,
                apiVersion = item["apiVersion"].takeIf { !it.isJsonNull }?.asString
            )
        }
    }

    fun createGameVersion(typeSlug: String, versionSlug: String): GameVersion {
        val versionType = versionTypes.find { it.slug == typeSlug }
        if (versionType == null) error("Unknown version type: $typeSlug")
        val version = versions.find { it.slug == versionSlug }
        if (version == null) error("Unknown version: $versionSlug")
        if (version.gameVersionTypeID != versionType.id) error("Version $versionSlug is not of type $typeSlug")
        return GameVersion(typeSlug, versionSlug)
    }

    fun createMinecraftGameVersion(minecraftVersion: String): GameVersion {
        val result = """(\d+)\.(\d+)\.(\d+)""".toRegex().matchEntire(minecraftVersion)!!
        val typeSlug = "minecraft-${result.groups[1]!!.value}-${result.groups[2]!!.value}"
        val versionSlug = minecraftVersion.replace(".", "-")
        return createGameVersion(typeSlug, versionSlug)
    }

}
