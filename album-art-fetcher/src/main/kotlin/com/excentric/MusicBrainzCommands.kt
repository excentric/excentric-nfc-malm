package com.excentric

import com.excentric.service.MusicBrainzService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class MusicBrainzCommands(
    private val musicBrainzService: MusicBrainzService,
) : AbstractShellCommands() {

    override val logger: Logger = LoggerFactory.getLogger(MusicBrainzCommands::class.java)

    @ShellMethod(key = ["mb-search-artist-id", "mbsaid"], value = "Search MusicBrainz by artist id")
    fun musicBrainzSearchByArtistId(
        @ShellOption(help = "Search for albums by artist") artistId: String,
        @ShellOption(help = "Albums only", defaultValue = "true") albumsOnly: Boolean
    ) {
        doSafely {
            val releaseGroups = musicBrainzService.searchReleaseGroupsByArtistId(artistId).toMutableList()

            if (albumsOnly)
                releaseGroups.removeAll { it.primaryType != "Album" || !it.secondaryTypes.isNullOrEmpty() }

            if (releaseGroups.isEmpty()) {
                logger.info("Nothing found for artist ID: $artistId")
                return@doSafely
            }

            val artistName = releaseGroups.firstOrNull()?.getFirstArtistName()

            logger.info("Found ${releaseGroups.size} release groups by artist $artistName ID $artistId:")

            releaseGroups.forEachIndexed { index, releaseGroup ->
                val releaseDate = releaseGroup.firstReleaseDate ?: "Unknown date"
                val releaseTitle = releaseGroup.title
                val type = if (albumsOnly) "" else "[${releaseGroup.primaryType} ${releaseGroup.secondaryTypes?.joinToString(", ").orEmpty()}]"

                logger.info("${index + 1}. $releaseTitle $type($releaseDate)")
            }
        }
    }
}
