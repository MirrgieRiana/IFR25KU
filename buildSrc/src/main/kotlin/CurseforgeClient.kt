import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
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

    fun getVersionTypes(): List<VersionType> {
        val json = callApi("https://minecraft.curseforge.com/api/game/version-types")
        val root = GsonBuilder().create().fromJson(json, JsonElement::class.java)
        return root.asJsonArray.map {
            val item = it.asJsonObject
            VersionType(
                id = item["id"].asInt,
                name = item["name"].asString,
                slug = item["slug"].asString
            )
        }
    }

    data class Version(val id: Int, val gameVersionTypeID: Int, val name: String, val slug: String, val apiVersion: String?)

    fun getVersions(): List<Version> {
        val json = callApi("https://minecraft.curseforge.com/api/game/versions")
        val root = GsonBuilder().create().fromJson(json, JsonElement::class.java)
        return root.asJsonArray.map {
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

}
