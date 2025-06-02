package com.excentric

import com.excentric.service.MusicBrainzService
import com.excentric.util.ConsoleColors.green
import com.excentric.util.ConsoleColors.red
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

    @ShellMethod(key = ["mb-search-artist", "mbsa"], value = "Search MusicBrainz by artist name")
    fun musicBrainzSearchByArtistName(
        @ShellOption(help = "Artist name to search for") artistNameQuery: String,
        @ShellOption(help = "Albums only", defaultValue = "true") albumsOnly: Boolean
    ) {
        doSafely {
            val artists = musicBrainzService.searchArtistsByName(artistNameQuery)

            if (artists.isEmpty()) {
                logger.info("No artists found with name: ${red(artistNameQuery)}")
                return@doSafely
            }

            val artistName = artists.first()
            logger.info("Found artist: ${green(artistName.name)} (ID: ${green(artistName.id)})")

            // Call the existing method to search by artist ID
            doSafely {
                val releaseGroups = musicBrainzService.searchReleaseGroupsByArtistId(artistName.id).toMutableList()

                if (albumsOnly)
                    releaseGroups.removeAll { it.primaryType != "Album" || !it.secondaryTypes.isNullOrEmpty() || it.firstReleaseDate.isNullOrEmpty() }

                if (releaseGroups.isEmpty()) {
                    logger.info("Nothing found for artist ID: ${red(artistName.id)}")
                    return@doSafely
                }

                logger.info("Found ${releaseGroups.size} release groups by artist $artistName ID ${artistName.id}:")

                releaseGroups.forEachIndexed { index, releaseGroup ->
                    val releaseDate = releaseGroup.firstReleaseDate ?: "Unknown date"
                    val releaseTitle = releaseGroup.title
                    val type = if (albumsOnly) "" else "[${releaseGroup.primaryType} ${releaseGroup.secondaryTypes?.joinToString(", ").orEmpty()}]"

                    logger.info("${index + 1}. $releaseTitle $type($releaseDate)")
                }
            }
        }
    }
}
