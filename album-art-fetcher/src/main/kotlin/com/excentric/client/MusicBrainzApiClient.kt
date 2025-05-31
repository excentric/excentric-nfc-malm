package com.excentric.client

import com.excentric.errors.MusicBrainzException
import com.excentric.model.api.MusicBrainzResponseModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.GET
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Component
class MusicBrainzApiClient(
    private val restTemplate: RestTemplate,
    @Value("\${musicbrainz.api.user-agent}") private val userAgent: String,
    @Value("\${musicbrainz.api.url}") private val mbApiUrl: String
) {
    private val logger = LoggerFactory.getLogger(MusicBrainzApiClient::class.java)

    fun searchAlbums(artist: String, album: String): MusicBrainzResponseModel {
        val searchUrl = buildSearchUrl(artist, album)

        logger.info("Querying MusicBrainz API: $searchUrl")

        val response = restTemplate.exchange(
            searchUrl,
            GET,
            HttpEntity<MusicBrainzResponseModel>(buildHttpHeaders()),
            MusicBrainzResponseModel::class.java
        )

        return validateModel(response.body)
    }

    private fun validateModel(musicBrainzResponseModel: MusicBrainzResponseModel?): MusicBrainzResponseModel {
        val musicBrainzResponse = musicBrainzResponseModel ?: throw MusicBrainzException("Empty response from MusicBrainz API")

        if (musicBrainzResponse.releases.isEmpty()) {
            throw MusicBrainzException("No releases found in the response")
        }
        return musicBrainzResponse
    }

    private fun buildHttpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            set("User-Agent", userAgent)
            accept = listOf(APPLICATION_JSON)
        }
    }

    private fun buildSearchUrl(artist: String, album: String): String {
        val uriBuilder = UriComponentsBuilder.fromUriString(mbApiUrl)
            .path("release")
            .queryParam("fmt", "json")

        if (artist.isEmpty()) {
            uriBuilder.queryParam("query", "release:$album")
        } else {
            uriBuilder.queryParam("query", "artist:$artist AND release:$album")
        }

        return uriBuilder.build().toUriString()
    }
}
