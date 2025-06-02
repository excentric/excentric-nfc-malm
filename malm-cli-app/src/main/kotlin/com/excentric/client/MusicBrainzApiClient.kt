package com.excentric.client

import com.excentric.config.MusicBrainzProperties
import com.excentric.errors.MalmException
import com.excentric.model.api.MusicBrainzArtistsModel
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
        val searchUrl = buildReleaseGroupArtistIdSearchUrl(artistId, 0)

        logger.info("Querying MusicBrainz API for release groups by artist ID: $searchUrl")

        val initialResponseModel = restTemplate.exchange(
            searchUrl,
            GET,
            HttpEntity<MusicBrainzReleaseGroupsModel>(buildRestHttpHeaders()),
            MusicBrainzReleaseGroupsModel::class.java,
            emptyMap<String, String>()
        ).body

        validateModel(initialResponseModel)

        // Initialize with the first page results
        var allReleaseGroups = initialResponseModel!!.releaseGroups.toMutableList()

        // Continue fetching pages as long as we get exactly 100 results (the max per page)
        var currentOffset = 100
        var currentPageSize = initialResponseModel.results.size
        val maxPages = 10 // Safety limit to prevent infinite loops
        var pageCount = 1

        while (currentPageSize == 100 && pageCount < maxPages) {
            logger.info("Found 100 results on page $pageCount, fetching next page with offset $currentOffset")
            val nextPageUrl = buildReleaseGroupArtistIdSearchUrl(artistId, currentOffset)

            logger.info("Querying MusicBrainz API for next page of release groups: $nextPageUrl")

            val nextPageResponseModel = restTemplate.exchange(
                nextPageUrl,
                GET,
                HttpEntity<MusicBrainzReleaseGroupsModel>(buildRestHttpHeaders()),
                MusicBrainzReleaseGroupsModel::class.java,
                emptyMap<String, String>()
            ).body

            if (nextPageResponseModel != null && nextPageResponseModel.results.isNotEmpty()) {
                // Add results from this page to our collection
                allReleaseGroups.addAll(nextPageResponseModel.releaseGroups)
                currentPageSize = nextPageResponseModel.results.size
                currentOffset += 100
                pageCount++

                logger.info("Added ${nextPageResponseModel.results.size} results from page $pageCount, total results: ${allReleaseGroups.size}")
            } else {
                // No more results or error occurred
                break
            }
        }

        if (pageCount > 1) {
            logger.info("Retrieved a total of ${allReleaseGroups.size} release groups across $pageCount pages")
            return MusicBrainzReleaseGroupsModel(allReleaseGroups)
        }

        return initialResponseModel
    }

    fun searchArtists(artistName: String): MusicBrainzArtistsModel {
        val searchUrl = buildArtistSearchUrl(artistName)

        logger.info("Querying MusicBrainz API for artists by name: $searchUrl")

        val responseModel = restTemplate.exchange(
            searchUrl,
            GET,
            HttpEntity<MusicBrainzArtistsModel>(buildRestHttpHeaders()),
            MusicBrainzArtistsModel::class.java,
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

    private fun buildReleaseGroupArtistIdSearchUrl(artistId: String, offset: Int = 0): String {
        val searchQuery = "arid:$artistId AND (primarytype:Album OR primarytype:Album)"

        val uriBuilder = UriComponentsBuilder.fromUriString(musicBrainzProperties.api.url)
            .path("release-group")
            .queryParam("fmt", "json")
            .queryParam("query", searchQuery)
            .queryParam("limit", 100)
            .queryParam("offset", offset)

        return uriBuilder.build().toUriString()
    }

    private fun buildArtistSearchUrl(artistName: String): String {
        val uriBuilder = UriComponentsBuilder.fromUriString(musicBrainzProperties.api.url)
            .path("artist")
            .queryParam("fmt", "json")
            .queryParam("query", "artist:$artistName")
            .queryParam("limit", 10)

        return uriBuilder.build().toUriString()
    }
}
