package com.excentric

import com.excentric.config.MusicBrainzProperties
import com.excentric.errors.MalmException
import com.excentric.service.CoverArtArchiveService
import com.excentric.service.MusicBrainzService
import com.excentric.storage.MetadataStorage
import com.excentric.util.ConsoleColors.green
import com.excentric.util.ConsoleColors.greenOrRed
import com.excentric.util.ConsoleColors.red
import org.jline.terminal.Terminal
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ResourceLoader
import org.springframework.shell.component.SingleItemSelector
import org.springframework.shell.component.context.ComponentContext
import org.springframework.shell.component.support.SelectorItem
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.shell.style.TemplateExecutor
import org.springframework.stereotype.Component
import java.awt.Desktop
import java.awt.Desktop.Action.BROWSE
import java.util.Locale.getDefault
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
    private val resourceLoader: ResourceLoader,
    private val terminal: Terminal,
    private val templateExecutor: TemplateExecutor,
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
                val albumArtFile = metadataStorage.getAlbumArtFile(index)
                val albumArtExists = if (albumArtFile.exists()) green("Yes") else red("No")
                logger.info("Slot $index: Album: ${greenOrRed(metadata.album)}, Artist: ${greenOrRed(metadata.artist)}, Year: ${greenOrRed(metadata.year)}, Cover: $albumArtExists")
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
    fun removeSlots(
        @ShellOption(help = "Slot number (1-10)") slots: String
    ) {
        doSafely { metadataStorage.removeAllSlots() }
    }

    @ShellMethod(key = ["print-slot-numbers"], value = "Print slot numbers")
    fun printSlotNumbers(
        @ShellOption(help = "Slot number (1-10)") slots: String
    ) {
        doSafely {


        }
    }

    @ShellMethod(key = ["more-aa"], value = "Set to only show album covers from release year")
    fun moreAlbumArt() {
        musicBrainzProperties.releaseYearCoversOnly = false
        logger.info("Set releaseYearCoversOnly to true - only showing album covers from release year")
    }

    @ShellMethod(key = ["less-aa"], value = "Set to show all album covers, not just from release year")
    fun lessAlbumArt() {
        musicBrainzProperties.releaseYearCoversOnly = true
        logger.info("Set releaseYearCoversOnly to false - showing all album covers")
    }

    @ShellMethod(key = ["open-aa"], value = "List album art files in a slot and select one")
    fun openAlbumArt(
        @ShellOption(help = "Slot number (1-10)") slot: Int
    ) {
        val albumArtsUri = metadataStorage.getAlbumArtsDir(slot).toURI()

        val os = System.getProperty("os.name").lowercase(getDefault())

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(BROWSE)) {
            Desktop.getDesktop().browse(albumArtsUri);
        } else if (os.indexOf("mac") >= 0) {
            val rt = Runtime.getRuntime()
            rt.exec("open $albumArtsUri")
        } else {
            logger.warn("Could not open album art folder, browse here: $albumArtsUri")
        }
    }

    @ShellMethod(key = ["select-aa"], value = "List album art files in a slot and select one")
    fun selectAlbumArt(
        @ShellOption(help = "Slot number (1-10)") slot: Int
    ) {
        doSafelyWithResult {
            val albumArtFiles = metadataStorage.getAlbumArtsFiles(slot)

            val selectorItems = albumArtFiles.mapIndexed { index, file ->
                val sizeInKB = file.length() / 1024
                SelectorItem.of("${file.name} (${sizeInKB} KB)", file.name)
            }.toMutableList()

            selectorItems.add(SelectorItem.of("None", "-1", true, true))

            val singleItemSelector = SingleItemSelector(terminal, selectorItems, "Select album art file from slot $slot:", null)
            singleItemSelector.setResourceLoader(this::resourceLoader.get())
            singleItemSelector.templateExecutor = templateExecutor
            singleItemSelector.setMaxItems(20)

            val context = singleItemSelector.run(ComponentContext.empty())

            if (!context.resultItem.isPresent) {
                return@doSafelyWithResult "No file selected"
            }

            val selectedItem = context.resultItem.get()
            val selectedFile = albumArtFiles.find { it.name == selectedItem.item }
            logger.info("Selected item: $selectedFile")
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
