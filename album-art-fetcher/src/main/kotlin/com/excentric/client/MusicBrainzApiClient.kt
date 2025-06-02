package com.excentric.client

import com.excentric.config.MusicBrainzProperties
import com.excentric.errors.MalmException
import com.excentric.model.api.MusicBrainzReleaseGroupsModel
import com.excentric.model.api.MusicBrainzReleasesModel
import com.excentric.model.api.MusicBrainzResultsModel
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

    fun searchAlbums(artist: String, album: String): MusicBrainzReleasesModel {
        val searchUrl = buildSearchUrl(artist, album)

        logger.info("Querying MusicBrainz API: $searchUrl")

        val responseModel = restTemplate.exchange(
            searchUrl,
            GET,
            HttpEntity<MusicBrainzReleasesModel>(buildRestHttpHeaders()),
            MusicBrainzReleasesModel::class.java
        ).body

        validateModel(responseModel)
        return responseModel!!
    }

    fun searchReleaseGroupsByArtistId(artistId: String): MusicBrainzReleaseGroupsModel {
        val searchUrl = buildReleaseGroupArtistIdSearchUrl(artistId)

        logger.info("Querying MusicBrainz API for release groups by artist ID: $searchUrl")

        val responseModel = restTemplate.exchange(
            searchUrl,
            GET,
            HttpEntity<MusicBrainzReleaseGroupsModel>(buildRestHttpHeaders()),
            MusicBrainzReleaseGroupsModel::class.java,
            emptyMap<String, String>()
        ).body

        validateModel(responseModel)
        return responseModel!!
    }

    private fun <T> validateModel(musicBrainzReleasesModel: MusicBrainzResultsModel<T>?) {
        val musicBrainzResponse = musicBrainzReleasesModel ?: throw MalmException("Empty response from MusicBrainz API")

        if (musicBrainzResponse.results.isEmpty()) {
            throw MalmException("No releases found in the response")
        }
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

    private fun buildReleaseGroupArtistIdSearchUrl(artistId: String): String {
        val searchQuery = "arid:$artistId AND (primarytype:Album OR primarytype:Album)"

        val uriBuilder = UriComponentsBuilder.fromUriString(musicBrainzProperties.api.url)
            .path("release-group")
            .queryParam("fmt", "json")
            .queryParam("query", searchQuery)
            .queryParam("limit", 100)


        return uriBuilder.build().toUriString()
    }
}
