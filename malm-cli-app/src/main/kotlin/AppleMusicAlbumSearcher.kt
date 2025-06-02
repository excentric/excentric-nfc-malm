import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.util.*

fun main() {
    val searcher = AppleMusicSearcher()
    val albumId = searcher.getAlbumId("Underworld", "Second Toughest in the Infants")
    println("Album ID: $albumId") // Should print: 1443227490
}

class AppleMusicSearcher {
    @Throws(java.lang.Exception::class)
    fun getAlbumId(artistName: String, albumName: String): String? {
        val query = java.net.URLEncoder.encode("$artistName $albumName", java.nio.charset.StandardCharsets.UTF_8)
        val url = "https://itunes.apple.com/search?term=$query&entity=album&limit=10"

        val client = java.net.http.HttpClient.newHttpClient()
        val request = java.net.http.HttpRequest.newBuilder()
            .uri(java.net.URI.create(url))
            .build()

        val response = client.send(
            request,
            java.net.http.HttpResponse.BodyHandlers.ofString()
        )

        val mapper: ObjectMapper = ObjectMapper()
        val root: JsonNode = mapper.readTree(response.body())
        val results: JsonNode = root.get("results")

        for (result in results) {
            val foundArtist: String = result.get("artistName").asText().lowercase(Locale.getDefault())
            val foundAlbum: String = result.get("collectionName").asText().lowercase(Locale.getDefault())

            if (foundArtist.contains(artistName.lowercase(Locale.getDefault())) &&
                foundAlbum.contains(albumName.lowercase(Locale.getDefault()))
            ) {
                return result.get("collectionId").asText()
            }
        }

        return null // Not found
    }
}
