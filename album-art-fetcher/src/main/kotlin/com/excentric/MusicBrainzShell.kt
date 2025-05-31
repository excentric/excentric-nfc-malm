package com.excentric

import com.excentric.errors.MusicBrainzException
import com.excentric.service.MusicBrainzService
import com.excentric.storage.MetadataStorage
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@ShellComponent
@Component
class MusicBrainzShell(
    private val musicBrainzService: MusicBrainzService,
    private val metadataStorage: MetadataStorage
) {
    private val logger = LoggerFactory.getLogger(MusicBrainzShell::class.java)

    @ShellMethod(key = ["q"], value = "Exit the application immediately")
    fun quit() {
        exitProcess(0)
    }

    @ShellMethod(key = ["find-metadata", "f"], value = "Find MusicBrainz Metadata for an album")
    fun findMusicBrainzAlbum(
        @ShellOption(help = "Album name") album: String,
        @ShellOption(help = "Artist name", defaultValue = "") artist: String,
    ) {
        logger.info("Searching for album: $album by artist: $artist")
        val albumMetadata = musicBrainzService.searchMusicBrainz(artist, album)
        metadataStorage.albumMetadata = albumMetadata
    }

    @ShellMethod(key = ["save-to-slot", "s"], value = "Save current album metadata to a numbered slot (1-10)")
    fun saveToSlot(
        @ShellOption(help = "Slot number (1-10)") slot: Int
    ) {
        logger.info("Saving album metadata to slot: $slot")
        doSafely { metadataStorage.saveToSlot(slot) }
    }

    @ShellMethod(key = ["list-slots", "ls"], value = "List all saved album metadata slots")
    fun listSlots() {
        doSafely { metadataStorage.listSlots() }
    }

    private fun doSafely(command: () -> Unit) {
        try {
            command()
        } catch (e: MusicBrainzException) {
            logger.error(e.message)
        }
    }
}
