package com.excentric.service

import com.excentric.client.MusicBrainzApiClient
import com.excentric.config.MusicBrainzProperties
import com.excentric.model.api.AlbumReleaseModel
import com.excentric.model.api.MusicBrainzResponseModel
import com.excentric.model.storage.AlbumMetadata
import com.excentric.util.ConsoleColors.greenOrRed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MusicBrainzService(
    private val musicBrainzApiClient: MusicBrainzApiClient,
    private val musicBrainzProperties: MusicBrainzProperties
) {
    private val logger = LoggerFactory.getLogger(MusicBrainzService::class.java)

    fun searchArtistAlbums(artistQuery: String): List<AlbumReleaseModel> {
        val albumResults = musicBrainzApiClient.searchArtistAlbums(artistQuery)

        // Filter for official releases and group by album title to avoid duplicates
        val officialReleases = albumResults.releases.filter { it.status == "Official" }
        val uniqueAlbums = officialReleases.groupBy { it.title }
            .map { (_, releases) -> releases.first() }

        // Sort by release date
        return uniqueAlbums.sortedBy { it.date }
    }

    fun searchReleasesByArtistId(artistId: String): List<AlbumReleaseModel> {
        val albumResults = musicBrainzApiClient.searchReleasesByArtistId(artistId)

        // Filter for official releases and group by album title to avoid duplicates
        val officialReleases = albumResults.releases.filter { it.status == "Official" }
        val uniqueAlbums = officialReleases.groupBy { it.title }
            .map { (_, releases) -> releases.first() }

        // Sort by release date
        return uniqueAlbums.sortedBy { it.date }
    }

    fun searchMusicBrainz(artistQuery: String, albumQuery: String): AlbumMetadata {
        val albumResults = musicBrainzApiClient.searchAlbums(artistQuery, albumQuery)
        val firstResult = albumResults.releases.first()

        val album = firstResult.title
        val artist = firstResult.getFirstArtistName()
        val year = albumResults.findEarliestReleaseYear(artist, album)
        val mbids = findReleasesForAlbumArt(albumResults, artist, album, year).map { it.id }

        logger.info("IDs: ${greenOrRed(mbids)}")
        logger.info("Album: ${greenOrRed(album)}")
        logger.info("Artist: ${greenOrRed(artist)}")
        logger.info("Year: ${greenOrRed(year)}")

        return AlbumMetadata(mbids, album, artist, year)
    }

    private fun findReleasesForAlbumArt(albumResults: MusicBrainzResponseModel, artist: String?, album: String, year: Int?): List<AlbumReleaseModel> {
        val matchingReleases = albumResults.findReleasesByAlbumAndArtist(album, artist)
        val officialSortedReleases = matchingReleases.filter { it.status == "Official" }.sortedBy { it.date }

        if (musicBrainzProperties.releaseYearCoversOnly && year != null) {
            return officialSortedReleases.filter { it.date?.startsWith(year.toString()) == true }
        }

        return officialSortedReleases
    }
}
