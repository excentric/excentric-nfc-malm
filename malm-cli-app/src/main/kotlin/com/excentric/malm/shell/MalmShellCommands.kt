package com.excentric.malm.shell

import com.excentric.malm.metadata.AlbumMetadata
import com.excentric.malm.storage.MetadataStorage
import com.excentric.malm.util.ConsoleColors.green
import com.excentric.malm.util.ConsoleColors.greenOrRed
import com.excentric.malm.util.ConsoleColors.red
import com.excentric.malm.util.ConsoleColors.yellow
import com.excentric.malm.util.SlotArgumentParser.parseSlotNumbers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class MalmShellCommands(
    private val metadataStorage: MetadataStorage,
) : AbstractShellCommands() {
    override val logger: Logger = LoggerFactory.getLogger(MalmShellCommands::class.java)

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
                val coverArtStatus = getCoverArtStatus(index)
                val appleMusicIdStatus = if (metadata.appleMusicAlbumId.isNullOrEmpty()) red("No") else green("Yes")
                logger.info("Slot $index: Album: ${greenOrRed(metadata.title)}, Artist: ${greenOrRed(metadata.artist)}, Year: ${greenOrRed(metadata.year)}, Cover: $coverArtStatus, Apple Id: $appleMusicIdStatus")
            }
        }
    }

    @ShellMethod(key = ["create-slot", "c"], value = "Create new slot with custom metadata")
    fun createSlot(
        @ShellOption(help = "Slot number (1-99)") slot: Int,
        @ShellOption(help = "Artist") artist: String,
        @ShellOption(help = "Album") album: String,
        @ShellOption(help = "Year") year: Int?,
    ) {
        logger.info("Creating artist/album metadata to slot: $slot")
        doSafely {
            metadataStorage.albumMetadata = AlbumMetadata(emptyList(), album, artist, year)
            metadataStorage.saveToSlot(slot)
        }
    }

    @ShellMethod(key = ["save-to-slot", "s"], value = "Save current album metadata to a numbered slot (1-99)")
    fun saveToSlot(
        @ShellOption(help = "Slot number (1-99)") slot: Int
    ) {
        logger.info("Saving album metadata to slot: $slot")
        doSafely {
            metadataStorage.saveToSlot(slot)
        }
    }

    private fun getCoverArtStatus(index: Int): String {
        return if (metadataStorage.getCoverArtFile(index).exists()) {
            green("Yes")
        } else if (metadataStorage.getPotentialCoverArtsFiles(index).isNotEmpty()) {
            yellow("Not Selected")
        } else {
            red("No")
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
}
