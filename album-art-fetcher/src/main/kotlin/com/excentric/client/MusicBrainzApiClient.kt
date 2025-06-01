package com.excentric.client

import com.excentric.errors.MalmException
import com.excentric.model.api.MusicBrainzResponseModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod.GET
import org.springframework.stereotype.Component
import org.springframework.web.util.UriComponentsBuilder

@Component
class MusicBrainzApiClient(
    @Value("\${music-album-label-maker.musicbrainz.api.url}") private val mbApiUrl: String
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

    private fun validateModel(musicBrainzResponseModel: MusicBrainzResponseModel?): MusicBrainzResponseModel {
        val musicBrainzResponse = musicBrainzResponseModel ?: throw MalmException("Empty response from MusicBrainz API")

        if (musicBrainzResponse.releases.isEmpty()) {
            throw MalmException("No releases found in the response")
        }
        return musicBrainzResponse
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
