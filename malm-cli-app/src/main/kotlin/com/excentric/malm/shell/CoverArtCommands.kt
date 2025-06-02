package com.excentric.malm.shell

import com.excentric.malm.errors.MalmException
import com.excentric.malm.service.CoverArtArchiveService
import com.excentric.malm.storage.MetadataStorage
import com.excentric.malm.util.SlotArgumentParser.parseSlotNumbers
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
class CoverArtCommands(
    private val coverArtArchiveService: CoverArtArchiveService,
    private val metadataStorage: MetadataStorage,
) : AbstractShellCommands() {
    override val logger: Logger = LoggerFactory.getLogger(CoverArtCommands::class.java)

    @ShellMethod(key = ["ca-download", "cad"], value = "Download cover art from Cover Art Archive")
    fun downloadArt(
        @ShellOption(help = "Slot numbers") slots: String
    ) {
        doSafely {
            parseSlotNumbers(slots).forEach { slot ->
                coverArtArchiveService.downloadCoverArt(slot)
            }
        }
    }

    @ShellMethod(key = ["ca-select", "cas"], value = "List cover art files in a slot and select one")
    fun selectCoverArt(
        @ShellOption(help = "Slot number (1-99)") slot: Int
    ) {
        doSafely {
            val coverArtFiles = metadataStorage.getPotentialCoverArtsFiles(slot)
            if (coverArtFiles.isEmpty())
                throw MalmException("No cover art directory found for slot $slot")

            val selectorItems = coverArtFiles.mapIndexed { index, file ->
                val sizeInKB = file.length() / 1024
                SelectorItem.of("${file.name} (${sizeInKB} KB)", file.name)
            }.toMutableList()

            selectorItems.add(SelectorItem.of("None", "-1", true, true))

            val singleItemSelector = createSingleItemSelector(selectorItems, "Select cover art file from slot $slot:")
            val context = singleItemSelector.run(ComponentContext.empty())

            if (!context.resultItem.isPresent) {
                return@doSafely
            }

            coverArtFiles.find { it.name == context.resultItem.get().item }?.let { selectedFile ->
                metadataStorage.selectCoverArt(slot, selectedFile)
            }
        }
    }

    @ShellMethod(key = ["ca-open-folder", "cao"], value = "Open folder with potential cover art")
    fun openCoverArt(
        @ShellOption(help = "Slot number (1-99)") slot: Int
    ) {
        val coverArtsUri = metadataStorage.getCoverArtsDir(slot).toURI()

        val os = System.getProperty("os.name").lowercase(getDefault())

        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(BROWSE)) {
            Desktop.getDesktop().browse(coverArtsUri);
        } else if (os.indexOf("mac") >= 0) {
            val rt = Runtime.getRuntime()
            rt.exec("open $coverArtsUri")
        } else {
            logger.warn("Could not open cover art folder, browse here: $coverArtsUri")
        }
    }
}
