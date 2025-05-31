package com.excentric

import com.excentric.service.MusicBrainzService
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component

@ShellComponent
@Component
class MusicBrainzMetadataFetcher(
    private val musicBrainzService: MusicBrainzService
) {
    private val logger = LoggerFactory.getLogger(MusicBrainzMetadataFetcher::class.java)

    @ShellMethod(key = ["f"], value = "Find MusicBrainz Metadata for an album")
    fun findMusicBrainzAlbum(
        @ShellOption(help = "Album name") album: String,
        @ShellOption(help = "Artist name", defaultValue = "") artist: String,
    ) {
        logger.info("Searching for album: $album by artist: $artist")

        musicBrainzService.searchMusicBrainz(artist, album)
    }
}
