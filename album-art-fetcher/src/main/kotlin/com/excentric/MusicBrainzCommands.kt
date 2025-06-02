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

    @ShellMethod(key = ["mb-search-artist-id", "mbsaid"], value = "Search MusicBrainz by artist id")
    fun musicBrainzSearchByArtistId(
        @ShellOption(help = "Search for albums by artist") artistId: String,
        @ShellOption(help = "Filter to show only albums", defaultValue = "false") includeSingles: Boolean
    ) {
        doSafely {
            val releaseGroups = musicBrainzService.searchReleaseGroupsByArtistId(artistId, includeSingles)

            if (releaseGroups.isEmpty()) {
                logger.info("Nothing found for artist ID: $artistId")
                return@doSafely
            }

            logger.info("Found ${releaseGroups.size} release groups by artist ID $artistId:")

            releaseGroups.forEachIndexed { index, releaseGroup ->
                val releaseDate = releaseGroup.firstReleaseDate ?: "Unknown date"
                val releaseTitle = releaseGroup.title
                val artistName = releaseGroup.getFirstArtistName() ?: "Unknown artist"
                val secondaryTypes = releaseGroup.secondaryTypes?.joinToString(", ").orEmpty()
                val primaryType = releaseGroup.primaryType
                logger.info("${index + 1}. $releaseTitle by $artistName ($primaryType:$secondaryTypes}) ($releaseDate)")
            }
        }
    }
}
