package com.excentric.malm.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URLEncoder.encode
import java.net.http.HttpClient.newHttpClient
import java.net.http.HttpRequest.newBuilder
import java.net.http.HttpResponse.BodyHandlers.ofString
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

@Component
class AppleMusicApiClient {
    fun getAlbumId(artist: String, title: String): String? {
        val query = encode("$artist $title", UTF_8)
        val url = "https://itunes.apple.com/search?term=$query&entity=album&limit=10"

        val client = newHttpClient()
        val request = newBuilder()
            .uri(URI.create(url))
            .build()

        val response = client.send(
            request,
            ofString()
        )

        val mapper = ObjectMapper()
        val root: JsonNode = mapper.readTree(response.body())
        val results: JsonNode = root.get("results")

        for (result in results) {
            val foundArtist: String = result.get("artistName").asText().lowercase(Locale.getDefault())
            val foundAlbum: String = result.get("collectionName").asText().lowercase(Locale.getDefault())

            if (foundArtist.contains(artist.lowercase(Locale.getDefault())) &&
                foundAlbum.contains(title.lowercase(Locale.getDefault()))
            ) {
                return result.get("collectionId").asText()
            }
        }

        return null
    }
}
