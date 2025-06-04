package com.excentric.malm.pdf

import com.excentric.malm.metadata.LabelMetadata
import com.excentric.malm.pdf.SlotPositionDetails.Companion.PARAGRAPH_OFFSET
import com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFont
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment.CENTER
import com.itextpdf.layout.properties.VerticalAlignment.MIDDLE
import java.io.File
import java.lang.Math.PI

class PdfLabelWriter(
    private val labels: List<LabelMetadata>,
    private val pdfResourcePath: String = "Avery80x50-R-RectangleLabels-blank.pdf",
    private val shouldAddTestParagraphBorder: Boolean = false,
    val outputFile: File,
) {
    companion object {
        const val LABEL_WIDTH = 132f
        const val ROTATE_90_ANTI_CLOCKWISE = PI / 2
        const val DEFAULT_FONT_SIZE = 17f
    }


    private val writer = PdfWriter(outputFile)

    private lateinit var document: Document

    private val slotPositionDetailsMap = mapOf(
        1 to SlotPositionDetails(0, 0),
        2 to SlotPositionDetails(0, 1),
        3 to SlotPositionDetails(1, 0),
        4 to SlotPositionDetails(1, 1),
        5 to SlotPositionDetails(2, 0),
        6 to SlotPositionDetails(2, 1),
        7 to SlotPositionDetails(3, 0),
        8 to SlotPositionDetails(3, 1),
        9 to SlotPositionDetails(4, 0),
        10 to SlotPositionDetails(4, 1),
    )

    fun createPdf() {
        try {
            createPdfThrowingException()
        } catch (e: Exception) {
            println("Error modifying PDF: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun createPdfThrowingException() {
        // SC: we are using this blank one as it's coming straight from avery and launches with the right printer settings
        val reader = PdfReader(javaClass.classLoader.getResourceAsStream(pdfResourcePath))
        document = Document(PdfDocument(reader, writer))

        labels.forEach { label ->
            addLabel(label)
        }

        document.close()

        println("PDF successfully modified. Output saved to: ${outputFile.absolutePath}")
    }

    private fun addLabel(label: LabelMetadata) {
        val slotPosition = slotPositionDetailsMap[label.slot]!!

        val yearText = if (label.year == null) "" else "(${label.year})"
        document.add(createParagraph(label.artist.orEmpty(), slotPosition.artistLeft, slotPosition.artistBottom))
        document.add(createParagraph(label.title, slotPosition.titleLeft, slotPosition.titleBottom))
        document.add(createParagraph(yearText, slotPosition.yearLeft, slotPosition.yearBottom))
        document.add(createImage(slotPosition, label.coverArt.absolutePath))
    }

    private fun createImage(positionDetails: SlotPositionDetails, imagePath: String): Image {
        return Image(ImageDataFactory.create(imagePath)).apply {
            setWidth(LABEL_WIDTH)
            setHeight(LABEL_WIDTH)
            setRotationAngle(ROTATE_90_ANTI_CLOCKWISE)
            setFixedPosition(positionDetails.imageLeft, positionDetails.imageBottom)
        }
    }

    private fun createParagraph(paragraphText: String, leftPosition: Float, bottomPosition: Float): Paragraph {
        val font = PdfFontFactory.createFont(HELVETICA_BOLD)
        val maxWidth = LABEL_WIDTH
        val fontSize = calculateOptimalFontSize(paragraphText, font, maxWidth)

        return Paragraph(paragraphText).apply {
            setFont(font)
            setHeight(PARAGRAPH_OFFSET)
            setFontSize(fontSize)
            setTextAlignment(CENTER)
            setVerticalAlignment(MIDDLE)
            setRotationAngle(ROTATE_90_ANTI_CLOCKWISE)
            if (shouldAddTestParagraphBorder) {
//                setBorder(SolidBorder(1f))
                setBorderTop(SolidBorder(1f))
                setBorderBottom(SolidBorder(1f))
            }
            setFixedPosition(leftPosition, bottomPosition, LABEL_WIDTH) // x, y, width (increased width for vertical text)
        }
    }

    private fun calculateOptimalFontSize(text: String, font: PdfFont, maxWidth: Float): Float {
        var fontSize = DEFAULT_FONT_SIZE
        val minFontSize = 7.5f // Minimum readable size
        val maxFontSize = 17f // Maximum allowed size

        // Start with default and adjust down if needed
        while (fontSize > minFontSize) {
            val textWidth = font.getWidth(text, fontSize)
            if (textWidth <= maxWidth) {
                break
            }
            fontSize -= 0.5f
        }

        // If we can go larger without exceeding width, do so
        while (fontSize < maxFontSize) {
            val textWidth = font.getWidth(text, fontSize + 0.5f)
            if (textWidth > maxWidth) {
                break
            }
            fontSize += 0.5f
        }

        return fontSize
    }

}
