package com.excentric

import com.excentric.errors.MalmException
import com.excentric.service.CoverArtArchiveService
import com.excentric.service.MusicBrainzService
import com.excentric.storage.MetadataStorage
import com.excentric.util.ConsoleColors.greenOrRed
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@ShellComponent
@Component
class MalmShell(
    private val musicBrainzService: MusicBrainzService,
    private val coverArtArchiveService: CoverArtArchiveService,
    private val metadataStorage: MetadataStorage
) {
    private val logger = LoggerFactory.getLogger(MalmShell::class.java)

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
        doSafely {
            val slots = metadataStorage.getSlots()
            slots.forEach { (index, metadata) ->
                logger.info("Slot $index: Album: ${greenOrRed(metadata.album)}, Artist: ${greenOrRed(metadata.artist)}, Year: ${greenOrRed(metadata.year)}")
            }
        }
    }

    @ShellMethod(key = ["download-album-art", "aa"], value = "Download album art from Cover Art Archive")
    fun downloadArt(
        @ShellOption(help = "Slot number (1-10)") slot: Int
    ) {
        doSafely { coverArtArchiveService.downloadAlbumArt(slot) }
    }

    @ShellMethod(key = ["remove-slots", "rm"], value = "Delete all metadata slots from the metadata directory")
    fun removeSlots() {
        doSafely { metadataStorage.removeAllSlots() }
    }

    private fun doSafely(command: () -> Unit) {
        try {
            command()
        } catch (e: MalmException) {
            logger.error(e.message)
        }
    }
}
