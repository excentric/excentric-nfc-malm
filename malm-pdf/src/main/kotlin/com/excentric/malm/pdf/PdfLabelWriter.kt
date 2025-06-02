package com.excentric.malm.pdf

import com.itextpdf.io.font.constants.StandardFonts.HELVETICA_BOLD
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment.CENTER
import java.io.File
import java.lang.Math.PI

class PdfLabelWriter {
    companion object {
        const val LABEL_WIDTH = 132f
        const val ROTATE_90_ANTI_CLOCKWISE = PI / 2
        const val DEFAULT_FONT_SIZE = 17f
    }

    var paragraphBorder = false

    private val reader = PdfReader(javaClass.classLoader.getResourceAsStream("blank-avery.pdf"))
    private val writer = PdfWriter(File("output-hello-world.pdf"))

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

    fun runSafely() {
        try {
            run()
        } catch (e: Exception) {
            println("Error modifying PDF: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun run() {
        val pdfDoc = PdfDocument(reader, writer)
        document = Document(pdfDoc)

        slotPositionDetailsMap.keys.forEach {
            addLabelForSlot(it)
        }

        document.close()

        println("PDF successfully modified. Output saved to: ${File("output-hello-world.pdf").absolutePath}")
    }

    private fun addLabelForSlot(slot: Int) {
        val slotPosition = slotPositionDetailsMap[slot]!!
        document.add(createParagraph(slotPosition, "Pink Floyd\nAnimals\n(1971)"))
        document.add(createImage(slotPosition, "1/1.jpg"))
    }

    private fun createImage(positionDetails: SlotPositionDetails, imagePath: String): Image {
        return Image(ImageDataFactory.create(getAlbumArtPath(imagePath))).apply {
            setWidth(LABEL_WIDTH)
            setHeight(LABEL_WIDTH)
            setRotationAngle(ROTATE_90_ANTI_CLOCKWISE)
            setFixedPosition(positionDetails.imageLeft, positionDetails.imageBottom)
        }
    }

    private fun createParagraph(positionDetails: SlotPositionDetails, paragraphText: String): Paragraph {
        return Paragraph(paragraphText).apply {
            setFont(PdfFontFactory.createFont(HELVETICA_BOLD))
            setFontSize(DEFAULT_FONT_SIZE)
            setTextAlignment(CENTER)
            setRotationAngle(ROTATE_90_ANTI_CLOCKWISE)
            if (paragraphBorder) {
                setBorderTop(SolidBorder(1f))
                setBorderBottom(SolidBorder(1f))
            }
            setFixedPosition(positionDetails.paragraphLeft, positionDetails.paragraphBottom, LABEL_WIDTH) // x, y, width (increased width for vertical text)
        }
    }

    private fun getAlbumArtPath(albumArtPath: String): String {
        return File(System.getProperty("user.home") + "/.album-metadata/$albumArtPath").absolutePath
    }
}
