package com.excentric

import com.excentric.malm.metadata.AlbumMetadata
import com.excentric.service.MusicBrainzService
import com.excentric.storage.MetadataStorage
import com.excentric.util.ConsoleColors.green
import com.excentric.util.ConsoleColors.greenOrRed
import com.excentric.util.ConsoleColors.red
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.component.context.ComponentContext
import org.springframework.shell.component.support.SelectorItem
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class MusicBrainzCommands(
    private val musicBrainzService: MusicBrainzService,
    private val metadataStorage: MetadataStorage,
) : AbstractShellCommands() {

    override val logger: Logger = LoggerFactory.getLogger(MusicBrainzCommands::class.java)

    @ShellMethod(key = ["mb-search", "mbs"], value = "Search MusicBrainz for album and artist")
    fun findMusicBrainzAlbum(
        @ShellOption(help = "Album name") album: String,
        @ShellOption(help = "Artist name", defaultValue = "") artist: String,
    ) {
        logger.info("Searching for album: $album by artist: $artist")
        val albumMetadata = musicBrainzService.searchMusicBrainz(artist, album)
        logAlbumResult(albumMetadata)
        metadataStorage.albumMetadata = albumMetadata
    }

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

            val artist = artists.first()
            logger.info("Found artist: ${green(artist.name)} (ID: ${green(artist.id)})")

            // Call the existing method to search by artist ID
            val releaseGroups = musicBrainzService.searchReleaseGroupsByArtistId(artist.id).toMutableList()

            if (albumsOnly)
                releaseGroups.removeAll { it.primaryType != "Album" || !it.secondaryTypes.isNullOrEmpty() || it.firstReleaseDate.isNullOrEmpty() }

            if (releaseGroups.isEmpty()) {
                logger.info("Nothing found for artist ID: ${red(artist.id)}")
                return@doSafely
            }

            logger.info("Found ${releaseGroups.size} release groups by artist $artist ID ${artist.id}:")

            val selectorItems = releaseGroups.mapIndexed { index, releaseGroup ->
                val releaseDate = releaseGroup.firstReleaseDate ?: "Unknown date"
                SelectorItem.of("${releaseGroup.title} ($releaseDate)", index.toString())
            }.toMutableList()

            selectorItems.add(SelectorItem.of("None", "-1", true, true))

            val singleItemSelector = createSingleItemSelector(selectorItems, "Select release:")
            val context = singleItemSelector.run(ComponentContext.empty())

            if (!context.resultItem.isPresent || context.resultItem.get().item == "-1") {
                return@doSafely
            }

            val release = releaseGroups[context.resultItem.get().item.toInt()]

            val albumMetadata = AlbumMetadata(release.releases?.map { it.id }.orEmpty(), release.title, artist.name, release.getYear())
            metadataStorage.albumMetadata = albumMetadata
            logAlbumResult(albumMetadata)

//            releaseGroups.forEachIndexed { index, releaseGroup ->
//                val releaseDate = releaseGroup.firstReleaseDate ?: "Unknown date"
//                val releaseTitle = releaseGroup.title
//                val type = if (albumsOnly) "" else "[${releaseGroup.primaryType} ${releaseGroup.secondaryTypes?.joinToString(", ").orEmpty()}]"
//
//                logger.info("${index + 1}. $releaseTitle $type($releaseDate)")
//            }
        }
    }

    private fun logAlbumResult(album: AlbumMetadata) {
        logger.info("IDs: ${greenOrRed(album.mbids)}")
        logger.info("Album: ${greenOrRed(album.album)}")
        logger.info("Artist: ${greenOrRed(album.artist)}")
        logger.info("Year: ${greenOrRed(album.year)}")
    }
}
