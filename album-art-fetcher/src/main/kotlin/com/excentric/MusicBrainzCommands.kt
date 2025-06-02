package com.excentric

import com.excentric.service.MusicBrainzService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent("MusicBrainz commands")
class MusicBrainzCommands(
    private val musicBrainzService: MusicBrainzService,
) : AbstractShellCommands() {

    override val logger: Logger = LoggerFactory.getLogger(MusicBrainzCommands::class.java)

    @ShellMethod(key = ["mb-search", "mbs"], value = "Search MusicBrainz for artists albums")
    fun musicBrainzSearch(
        @ShellOption(help = "Search for albums by artist") artist: String
    ) {
        doSafely {
            logger.info("Searching for albums by artist: $artist")
            val albums = musicBrainzService.searchArtistAlbums(artist)

            if (albums.isEmpty()) {
                logger.info("No albums found for artist: $artist")
                return@doSafely
            }

            logger.info("Found ${albums.size} albums by $artist:")
            albums.forEachIndexed { index, album ->
                val releaseDate = album.date ?: "Unknown date"
                val albumTitle = album.title
                logger.info("${index + 1}. $albumTitle (${album.releaseGroupModel?.primaryType.orEmpty()}) ($releaseDate)")
            }
        }
    }

    @ShellMethod(key = ["mb-search-artist-id", "mbsaid"], value = "Search MusicBrainz by artist id")
    fun musicBrainzSearchByArtistId(
        @ShellOption(help = "Search for albums by artist") artistId: String
    ) {
        doSafely {
        }
    }
}
