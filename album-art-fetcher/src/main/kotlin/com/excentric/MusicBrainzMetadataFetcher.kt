package com.excentric

import com.excentric.client.MusicBrainzApiClient
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component
import java.io.IOException

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
        if (artist.isEmpty()) {
            logger.info("Searching for album: $album")
        } else {
            logger.info("Searching for album: $album by artist: $artist")
        }
        val mbid = findMbidByAlbumInfo(artist, album)
        logger.info("Found MBID: $mbid")
        return mbid
    }

    fun findMbidByAlbumInfo(artist: String, album: String): String {
        val albumResults = musicBrainzApiClient.searchAlbums(artist, album)

        val firstResult = albumResults.releases.first()

        // Log all album details
        logger.info("Album details:")
        logger.info("ID: ${firstResult.id}")
        logger.info("Title: ${firstResult.title}")
        logger.info("Status: ${firstResult.status}")
        logger.info("Date: ${firstResult.date}")
        logger.info("Artist Credits:")
        firstResult.artistCredit.forEachIndexed { index, credit ->
            logger.info("  Artist $index:")
            logger.info("    Name: ${credit.name}")
            logger.info("    Artist ID: ${credit.artist.id}")
            logger.info("    Artist Name: ${credit.artist.name}")
        }

        val mbid = firstResult.id

        // Validate that we have a proper UUID format
        if (mbid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex())) {
            return mbid
        } else {
            throw IOException("Invalid MBID format: $mbid")
        }
    }
}
