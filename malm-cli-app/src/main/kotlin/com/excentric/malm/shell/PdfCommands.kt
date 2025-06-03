package com.excentric.malm.shell

import com.excentric.malm.metadata.LabelMetadata
import com.excentric.malm.pdf.PdfLabelWriter
import com.excentric.malm.storage.MetadataStorage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import java.io.File
import java.lang.System.currentTimeMillis

@ShellComponent
class PdfCommands(
    private val metadataStorage: MetadataStorage,
    @Value("\${music-album-label-maker.pdf-directory}")
    private val pdfDirPath: String,
) : AbstractShellCommands() {
    override val logger: Logger = LoggerFactory.getLogger(PdfCommands::class.java)

    @ShellMethod(key = ["pdf"], value = "Create and open a pdf with slots 1-10")
    fun quit() {
        val slotsMap = metadataStorage.getSlotsMap()
        val pdfSlotsMap = slotsMap.filter { it.key in (1..10) }

        val labels = pdfSlotsMap.map { (slot, albumMetadata) ->
            LabelMetadata(slot, albumMetadata.title, albumMetadata.artist, albumMetadata.year, metadataStorage.getCoverArtFile(slot))
        }

        val pdfDir = File(pdfDirPath).apply {
            mkdirs()
        }

        val outputFile = File(pdfDir, "Avery80x50-R-RectangleLabels-${currentTimeMillis()}.pdf")
        PdfLabelWriter(labels, outputFile = outputFile).createPdf()
        logger.info("Created pdf: ${outputFile.absolutePath}")
        openUriOnOperatingSystem(outputFile.toURI().toString())
    }
}
