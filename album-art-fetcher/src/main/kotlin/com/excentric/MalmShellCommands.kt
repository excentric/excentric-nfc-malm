package com.excentric

import com.excentric.errors.MalmException
import com.excentric.service.CoverArtArchiveService
import com.excentric.storage.MetadataStorage
import com.excentric.util.ConsoleColors.green
import com.excentric.util.ConsoleColors.greenOrRed
import com.excentric.util.ConsoleColors.red
import com.excentric.util.ConsoleColors.yellow
import com.excentric.util.SlotArgumentParser.parseSlotNumbers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.component.context.ComponentContext
import org.springframework.shell.component.support.SelectorItem
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.awt.Desktop
import java.awt.Desktop.Action.BROWSE
import java.util.Locale.getDefault

@ShellComponent
class MalmShellCommands(
    private val coverArtArchiveService: CoverArtArchiveService,
    private val metadataStorage: MetadataStorage,
) : AbstractShellCommands() {
    override val logger: Logger = LoggerFactory.getLogger(MalmShellCommands::class.java)

    @ShellMethod(key = ["save-to-slot", "s"], value = "Save current album metadata to a numbered slot (1-99)")
    fun saveToSlot(
        @ShellOption(help = "Slot number (1-99)") slot: Int
    ) {
        logger.info("Saving album metadata to slot: $slot")
        doSafely { metadataStorage.saveToSlot(slot) }
    }

    @ShellMethod(key = ["list-slots", "ls"], value = "List all saved album metadata slots")
    fun listSlots(
        @ShellOption(help = "Slot numbers", defaultValue = "") slots: String
    ) {
        doSafely {
            var slotsMap = metadataStorage.getSlotsMap()

            if (slots.isNotEmpty() && slots != "*") {
                val slotNumbers = parseSlotNumbers(slots)
                slotsMap = slotsMap.filter { it.key in slotNumbers }
            }

            slotsMap.forEach { (index, metadata) ->
                val albumArtFile = metadataStorage.getAlbumArtFile(index)
                val albumArtExists = if (albumArtFile.exists()) {
                    green("Yes")
                } else if (metadataStorage.getPotentialAlbumArtsFiles(index).isNotEmpty()) {
                    yellow("Not Selected")
                } else {
                    red("No")
                }
                logger.info("Slot $index: Album: ${greenOrRed(metadata.album)}, Artist: ${greenOrRed(metadata.artist)}, Year: ${greenOrRed(metadata.year)}, Cover: $albumArtExists")
            }
        }
    }

    @ShellMethod(key = ["download-album-art", "aa"], value = "Download album art from Cover Art Archive")
    fun downloadArt(
        @ShellOption(help = "Slot numbers") slots: String
    ) {
        doSafely {
            parseSlotNumbers(slots).forEach { slot ->
                coverArtArchiveService.downloadAlbumArt(slot)
            }
        }
    }

    @ShellMethod(key = ["remove-slots", "rm"], value = "Delete slots from the metadata directory")
    fun removeSlots(
        @ShellOption(help = "Slot numbers") slots: String
    ) {
        doSafely {
            if (slots == "*")
                metadataStorage.removeAllSlots()
            else
                metadataStorage.removeSlots(parseSlotNumbers(slots))
        }
    }

    @ShellMethod(key = ["open-aa"], value = "List album art files in a slot and select one")
    fun openAlbumArt(
        @ShellOption(help = "Slot number (1-99)") slot: Int
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
        @ShellOption(help = "Slot number (1-99)") slot: Int
    ) {
        doSafely {
            val albumArtFiles = metadataStorage.getPotentialAlbumArtsFiles(slot)
            if (albumArtFiles.isEmpty())
                throw MalmException("No album art directory found for slot $slot")

            val selectorItems = albumArtFiles.mapIndexed { index, file ->
                val sizeInKB = file.length() / 1024
                SelectorItem.of("${file.name} (${sizeInKB} KB)", file.name)
            }.toMutableList()

            selectorItems.add(SelectorItem.of("None", "-1", true, true))

            val singleItemSelector = createSingleItemSelector(selectorItems, "Select album art file from slot $slot:")
            val context = singleItemSelector.run(ComponentContext.empty())

            if (!context.resultItem.isPresent) {
                return@doSafely
            }

            albumArtFiles.find { it.name == context.resultItem.get().item }?.let { selectedFile ->
                metadataStorage.selectAlbumArt(slot, selectedFile)
            }
        }
    }

    @ShellMethod(key = ["move-slot", "mv"], value = "Move a slot from one position to another")
    fun moveSlot(
        @ShellOption(help = "Source slot number (1-99)") sourceSlot: Int,
        @ShellOption(help = "Target slot number (1-99)") targetSlot: Int
    ) {
        doSafely {
            metadataStorage.moveSlot(sourceSlot, targetSlot)
            logger.info("Moved slot $sourceSlot to slot $targetSlot")
        }
    }
}
