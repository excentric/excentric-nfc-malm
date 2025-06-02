package com.excentric.malm.service

import com.excentric.malm.client.MusicBrainzApiClient
import com.excentric.malm.config.MusicBrainzProperties
import com.excentric.malm.metadata.AlbumMetadata
import com.excentric.malm.model.AlbumReleaseGroupModel
import com.excentric.malm.model.AlbumReleaseModel
import com.excentric.malm.model.ArtistModel
import com.excentric.malm.model.MusicBrainzReleasesModel
import org.springframework.stereotype.Service

@Service
class MusicBrainzService(
    private val musicBrainzApiClient: MusicBrainzApiClient,
    private val musicBrainzProperties: MusicBrainzProperties
) {
    fun searchReleaseGroupsByArtistId(artistId: String): List<AlbumReleaseGroupModel> {
        val releaseGroupResults = musicBrainzApiClient.searchReleaseGroupsByArtistId(artistId).releaseGroups
        // Sort by release date; the newest first
        return releaseGroupResults.sortedBy { it.firstReleaseDate }.reversed()
    }

    fun searchArtistsByName(artistName: String): List<ArtistModel> {
        val artistResults = musicBrainzApiClient.searchArtists(artistName).artists
        // Sort by score (relevance) descending
        return artistResults.sortedByDescending { it.score ?: 0 }
    }

    fun searchMusicBrainz(artistQuery: String, albumQuery: String): AlbumMetadata {
        val albumResults = musicBrainzApiClient.searchAlbums(artistQuery, albumQuery)
        val firstResult = albumResults.releases.first()

        val album = firstResult.title
        val artist = firstResult.getFirstArtistName()
        val year = albumResults.findEarliestReleaseYear(artist, album)
        val mbids = findReleasesForAlbum(albumResults, artist, album, year).map { it.id }

        return AlbumMetadata(mbids, album, artist, year)
    }

    private fun findReleasesForAlbum(albumResults: MusicBrainzReleasesModel, artist: String?, album: String, year: Int?): List<AlbumReleaseModel> {
        val matchingReleases = albumResults.findReleasesByAlbumAndArtist(album, artist)
        val officialSortedReleases = matchingReleases.filter { it.status == "Official" }.sortedBy { it.date }

        if (musicBrainzProperties.releaseYearCoversOnly && year != null) {
            return officialSortedReleases.filter { it.date?.startsWith(year.toString()) == true }
        }

        return officialSortedReleases
    }
}
