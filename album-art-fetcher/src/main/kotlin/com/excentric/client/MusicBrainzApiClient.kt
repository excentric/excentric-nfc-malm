package com.excentric.client

import com.excentric.model.MusicBrainzResponseModel
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets.UTF_8

@Component
class MusicBrainzApiClient(
    private val restTemplate: RestTemplate
) {
    private val logger = LoggerFactory.getLogger(MusicBrainzApiClient::class.java)

    companion object {
        private const val USER_AGENT = "MyMusicApp/1.0 (nfc-sonos@excentric.com)"
        private const val MB_API_URL = "https://musicbrainz.org/ws/2/"
    }

    fun searchAlbums(artist: String, album: String): MusicBrainzResponseModel {
        // Encode the album name for URL
        val encodedAlbum = URLEncoder.encode(album, UTF_8.toString())

        // Construct the search URL based on whether artist is provided
        val searchUrl = if (artist.isEmpty()) {
            "${MB_API_URL}release?query=release:$encodedAlbum&fmt=json"
        } else {
            // Encode the artist name for URL
            val encodedArtist = URLEncoder.encode(artist, UTF_8.toString())
            "${MB_API_URL}release?query=artist:$encodedArtist%20AND%20release:$encodedAlbum&fmt=json"
        }

        logger.info("Querying MusicBrainz API: $searchUrl")

        val headers = HttpHeaders().apply {
            set("User-Agent", USER_AGENT)
            accept = listOf(APPLICATION_JSON)
        }

        // Make the request
        val entity = HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            searchUrl,
            HttpMethod.GET,
            entity,
            MusicBrainzResponseModel::class.java
        )

        // Process the response
        val musicBrainzResponse = response.body ?: throw IOException("Empty response from MusicBrainz API")

        if (musicBrainzResponse.releases.isEmpty()) {
            throw IOException("No releases found in the response")
        }
        return musicBrainzResponse
    }
}
