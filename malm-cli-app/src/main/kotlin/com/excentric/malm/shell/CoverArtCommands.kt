package com.excentric.malm.shell

import com.excentric.malm.client.CoverArtArchiveClient
import com.excentric.malm.config.CoverArtProperties
import com.excentric.malm.config.ServerProperties
import com.excentric.malm.errors.MalmException
import com.excentric.malm.storage.MetadataStorage
import com.excentric.malm.util.SlotArgumentParser.parseSlotNumbers
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.shell.component.context.ComponentContext
import org.springframework.shell.component.support.SelectorItem
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import java.io.File
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

@ShellComponent
class CoverArtCommands(
    private val coverArtArchiveClient: CoverArtArchiveClient,
    private val metadataStorage: MetadataStorage,
    private val serverProperties: ServerProperties,
    private val coverArtProperties: CoverArtProperties,
) : AbstractShellCommands() {
    override val logger: Logger = LoggerFactory.getLogger(CoverArtCommands::class.java)

    @ShellMethod(key = ["ca-download", "cad"], value = "Download cover art from Cover Art Archive")
    fun downloadArt(
        @ShellOption(help = "Slot numbers", defaultValue = "") slots: String
    ) {
        doSafely {
            val slotNumbers = if (slots.isEmpty())
                metadataStorage.getSlotsWithoutPotentialCoverArt()
            else
                parseSlotNumbers(slots)

            val slotsMap = metadataStorage.getSlotsMap().filter { it.key in slotNumbers }

            val totalDownloads = slotsMap.flatMap { it.value.mbids }.size
            var completedAttempts = 0

            val startTime = System.currentTimeMillis()
            startProgressBar(totalDownloads)

            // Create a list of all download tasks
            val downloadTasks = mutableListOf<Triple<Int, Int, String>>()
            slotsMap.forEach { (slot, albumMetadata) ->
                albumMetadata.mbids.forEachIndexed { index, mbid ->
                    downloadTasks.add(Triple(slot, index, mbid))
                }
            }

            // Process downloads in parallel with a configurable thread count
            val executor = Executors.newFixedThreadPool(coverArtProperties.threadCount)
            try {
                // Submit all tasks to the executor
                val futures = downloadTasks.map { (slot, index, mbid) ->
                    executor.submit(Callable {
                        val coverArtFile = coverArtArchiveClient.downloadCoverArt(mbid)
                        Triple(slot, index, coverArtFile)
                    })
                }

                // Process results as they complete
                for (future in futures) {
                    val (slot, index, coverArtFile) = future.get()
                    coverArtFile?.let { file ->
                        metadataStorage.saveCoverArt(slot, index, file)
                    }
                    completedAttempts++
                    updateProgressBar(completedAttempts, totalDownloads)
                }
            } finally {
                executor.shutdown()
            }

            finishProgressBar()

            val endTime = System.currentTimeMillis()
            val elapsedTimeMs = endTime - startTime
            val elapsedTimeSec = elapsedTimeMs / 1000.0

            val totalDownloaded = slotsMap.keys.flatMap { metadataStorage.getPotentialCoverArtsFiles(it) }.size
            logger.info("Downloaded $totalDownloaded cover art images for ${slotsMap.keys.count()} slot(s) in ${String.format("%.2f", elapsedTimeSec)} seconds")
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
        val coverArtSelectorUri = "http://localhost:${serverProperties.port}/ca/${slot}"
        openUriOnOperatingSystem(coverArtSelectorUri)
    }
}
