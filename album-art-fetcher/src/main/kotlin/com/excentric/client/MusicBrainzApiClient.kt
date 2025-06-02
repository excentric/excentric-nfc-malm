package com.excentric.client

import com.excentric.config.MusicBrainzProperties
import com.excentric.errors.MalmException
import com.excentric.model.api.MusicBrainzResponseModel
import org.slf4j.LoggerFactory
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class MusicBrainzApiClient(
    private val musicBrainzProperties: MusicBrainzProperties
) : AbstractClient() {
    private val logger = LoggerFactory.getLogger(MusicBrainzApiClient::class.java)

    fun searchAlbums(artist: String, album: String): MusicBrainzResponseModel {
        val searchUrl = buildSearchUrl(artist, album)

        logger.info("Querying MusicBrainz API: $searchUrl")

        val response = restTemplate.exchange(
            searchUrl,
            GET,
            HttpEntity<MusicBrainzResponseModel>(buildRestHttpHeaders()),
            MusicBrainzResponseModel::class.java
        )

        return validateModel(response.body)
    }

    fun searchArtistAlbums(artist: String): MusicBrainzResponseModel {
        val searchUrl = buildArtistSearchUrl(artist)

        logger.info("Querying MusicBrainz API for artist albums: $searchUrl")

        val response = restTemplate.exchange(
            searchUrl,
            GET,
            HttpEntity<MusicBrainzResponseModel>(buildRestHttpHeaders()),
            MusicBrainzResponseModel::class.java,
            emptyMap<String, String>()
        )

        return validateModel(response.body)
    }

    fun searchReleasesByArtistId(artistId: String): MusicBrainzResponseModel {
        val searchUrl = buildArtistIdSearchUrl(artistId)

        logger.info("Querying MusicBrainz API for releases by artist ID: $searchUrl")

        val response = restTemplate.exchange(
            searchUrl,
            GET,
            HttpEntity<MusicBrainzResponseModel>(buildRestHttpHeaders()),
            MusicBrainzResponseModel::class.java,
            emptyMap<String, String>()
        )

        return validateModel(response.body)
    }

    private fun validateModel(musicBrainzResponseModel: MusicBrainzResponseModel?): MusicBrainzResponseModel {
        val musicBrainzResponse = musicBrainzResponseModel ?: throw MalmException("Empty response from MusicBrainz API")

        if (musicBrainzResponse.releases.isEmpty()) {
            throw MalmException("No releases found in the response")
        }
        return musicBrainzResponse
    }

    private fun buildSearchUrl(artist: String, album: String): String {
        val uriBuilder = UriComponentsBuilder.fromUriString(musicBrainzProperties.api.url)
            .path("release")
            .queryParam("fmt", "json")

        if (artist.isEmpty()) {
            uriBuilder.queryParam("query", "release:$album")
        } else {
            uriBuilder.queryParam("query", "artist:$artist AND release:$album")
        }

        return uriBuilder.build().toUriString()
    }

    private fun buildArtistSearchUrl(artist: String): String {
        val uriBuilder = UriComponentsBuilder.fromUriString(musicBrainzProperties.api.url)
            .path("release")
            .queryParam("fmt", "json")
            .queryParam("query", "artist:$artist")

        return uriBuilder.build().toUriString()
    }

    private fun buildArtistIdSearchUrl(artistId: String): String {
        val uriBuilder = UriComponentsBuilder.fromUriString(musicBrainzProperties.api.url)
            .path("release")
            .queryParam("fmt", "json")
            .queryParam("query", "arid:$artistId")
            .queryParam("limit", 100)

        return uriBuilder.build().toUriString()
    }
}
