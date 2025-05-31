package com.excentric

import com.excentric.client.MusicBrainzApiClient
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component

@ShellComponent
@Component
class MusicBrainzMetadataFetcher(
    private val musicBrainzApiClient: MusicBrainzApiClient
) {
    private val logger = LoggerFactory.getLogger(MusicBrainzMetadataFetcher::class.java)

    @ShellMethod(key = ["find-mbid"], value = "Find MusicBrainz ID for an album")
    fun findMbid(
        @ShellOption(help = "Album name") album: String,
        @ShellOption(help = "Artist name", defaultValue = "") artist: String,
    ): String {
        logger.info("Searching for album: $album by artist: $artist")

        return findMbidByAlbumInfo(artist, album)
    }

    fun findMbidByAlbumInfo(artist: String, album: String): String {
        val albumResults = musicBrainzApiClient.searchAlbums(artist, album)
        val firstResult = albumResults.releases.first()

        val mbid = firstResult.id
        val albumName = firstResult.title
        val artistName = firstResult.getFirstArtistName()
        val albumYear = albumResults.findEarliestReleaseYear(artistName, albumName)

        logger.info("ID: $mbid")
        logger.info("Album: $albumName")
        logger.info("Artist: $artistName")
        logger.info("Year: $albumYear")

        return mbid
    }
}
