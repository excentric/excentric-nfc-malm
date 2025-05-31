package com.excentric

import com.excentric.model.MusicBrainzResponse
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@ShellComponent
@Component
class MusicBrainzMetadataFetcher(private val restTemplate: RestTemplate) {
    private val USER_AGENT = "MyMusicApp/1.0 (nfc-sonos@excentric.com)"
    private val MB_API_URL = "https://musicbrainz.org/ws/2/"

    @ShellMethod(key = ["find-mbid"], value = "Find MusicBrainz ID for an album")
    fun findMbid(
        @ShellOption(help = "Artist name") artist: String,
        @ShellOption(help = "Album name") album: String
    ): String {
        println("Searching for album: $album by artist: $artist")
        val mbid = findMbidByAlbumInfo(artist, album)
        println("Found MBID: $mbid")
        return mbid
    }

    fun findMbidByAlbumInfo(artist: String, album: String): String {
        // Encode the artist and album names for URL
        val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
        val encodedAlbum = URLEncoder.encode(album, StandardCharsets.UTF_8.toString())

        // Construct the search URL
        val searchUrl = "${MB_API_URL}release?query=artist:$encodedArtist%20AND%20release:$encodedAlbum&fmt=json"

        println("Querying MusicBrainz API: $searchUrl")

        // Set up headers
        val headers = HttpHeaders()
        headers.set("User-Agent", USER_AGENT)
        headers.accept = listOf(MediaType.APPLICATION_JSON)

        // Make the request
        val entity = HttpEntity<String>(headers)
        val response = restTemplate.exchange(
            searchUrl,
            HttpMethod.GET,
            entity,
            MusicBrainzResponse::class.java
        )

        println("Received response from MusicBrainz API")

        // Process the response
        val musicBrainzResponse = response.body
            ?: throw IOException("Empty response from MusicBrainz API")

        if (musicBrainzResponse.releases.isEmpty()) {
            throw IOException("No releases found in the response")
        }

        val mbid = musicBrainzResponse.releases.first().id

        // Validate that we have a proper UUID format
        if (mbid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex())) {
            return mbid
        } else {
            throw IOException("Invalid MBID format: $mbid")
        }
    }
}
