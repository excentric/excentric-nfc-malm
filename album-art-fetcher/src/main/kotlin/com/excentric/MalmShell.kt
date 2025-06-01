package com.excentric

import com.excentric.config.MusicBrainzProperties
import com.excentric.errors.MalmException
import com.excentric.service.CoverArtArchiveService
import com.excentric.service.MusicBrainzService
import com.excentric.storage.MetadataStorage
import com.excentric.util.ConsoleColors.greenOrRed
import org.jline.terminal.Terminal
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Lazy
import org.springframework.shell.command.CommandContext
import org.springframework.shell.component.SingleItemSelector
import org.springframework.shell.component.context.ComponentContext
import org.springframework.shell.component.support.SelectorItem
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component
import java.io.File
import kotlin.system.exitProcess

@ShellComponent
@Component
class MalmShell(
    private val musicBrainzService: MusicBrainzService,
    private val coverArtArchiveService: CoverArtArchiveService,
    private val metadataStorage: MetadataStorage,
    private val musicBrainzProperties: MusicBrainzProperties,
    @Value("\${music-album-label-maker.metadata-directory}")
    private val metadataDirPath: String,
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

    @ShellMethod(key = ["more-aa"], value = "Set to only show album covers from release year")
    fun moreAlbumArt() {
        musicBrainzProperties.releaseYearCoversOnly = true
        logger.info("Set releaseYearCoversOnly to true - only showing album covers from release year")
    }

    @ShellMethod(key = ["less-aa"], value = "Set to show all album covers, not just from release year")
    fun lessAlbumArt() {
        musicBrainzProperties.releaseYearCoversOnly = false
        logger.info("Set releaseYearCoversOnly to false - showing all album covers")
    }

    @ShellMethod(key = ["select-aa"], value = "List album art files in a slot and select one")
    fun selectAlbumArt(
        @ShellOption(help = "Slot number (1-10)") slot: Int,
        ctx: CommandContext
    ): String {
        return doSafelyWithResult {
            val slotDir = File(metadataDirPath, "$slot")

            if (!slotDir.exists() || !slotDir.isDirectory) {
                throw MalmException("No album art directory found for slot $slot")
            }

            val files = slotDir.listFiles()?.filter { it.isFile } ?: emptyList()

            if (files.isEmpty()) {
                throw MalmException("No album art files found in slot $slot")
            }

            // Create selector items for each file with their index as the key
            val items = files.mapIndexed { index, file ->
                val sizeInKB = file.length() / 1024
                // Store the file in a map with its index as the key
                SelectorItem.of(index.toString(), "${file.name} (${sizeInKB} KB)")
            }

            // Create a map to look up files by their index
            val fileMap = files.mapIndexed { index, file -> index.toString() to file }.toMap()

            // Create and configure the selector
            val selector = SingleItemSelector(
                ctx.terminal,
                items,
                "Select album art file from slot $slot:",
                null // No comparator needed for our use case
            )

            // Execute the selector and get the result
            val componentContext = ComponentContext.empty()
            val context = selector.run(componentContext)

            // Check if a selection was made
            if (!context.resultItem.isPresent) {
                return@doSafelyWithResult "No file selected"
            }

            // Get the selected item
            val selectedItem = context.resultItem.get()

            // Debug the structure of the selected item
            logger.info("Selected item: $selectedItem")

            // Extract the key from the selected item's toString() representation
            // The toString() format is expected to be something like "SelectorItem[key=0, ...]"
            val key = selectedItem.toString().substringAfter("key=").substringBefore(",")
            logger.info("Extracted key: $key")

            // Get the selected file using the extracted key
            val selectedFile = fileMap[key]
                ?: throw MalmException("Failed to find selected file with key: $key")

            // Return just the filename
            selectedFile.name
        }
    }

    private fun doSafely(command: () -> Unit) {
        try {
            command()
        } catch (e: MalmException) {
            logger.error(e.message)
        }
    }

    private fun <T> doSafelyWithResult(command: () -> T): T {
        try {
            return command()
        } catch (e: MalmException) {
            logger.error(e.message)
            throw e
        }
    }
}
