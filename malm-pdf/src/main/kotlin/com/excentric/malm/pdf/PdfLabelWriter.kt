package com.excentric.malm.pdf

import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.properties.TextAlignment.CENTER
import java.io.File
import java.lang.Math.PI

fun main() {
    PdfLabelWriter().runSafely()
}

class PdfLabelWriter {

    private val reader = PdfReader(javaClass.classLoader.getResourceAsStream("blank-avery.pdf"))
    private val writer = PdfWriter(File("output-hello-world.pdf"))

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
        val document = Document(pdfDoc)

        document.add(createParagraph("Pink Floyd\nAnimals\n(1971)"))
        document.add(createImage("1/1.jpg"))

        document.close()

        println("PDF successfully modified. Output saved to: ${File("output-hello-world.pdf").absolutePath}")
    }

    private fun createImage(imagePath: String): Image {
        // Create an image object
        val image = Image(ImageDataFactory.create(getAlbumArtPath(imagePath))).apply {
            setWidth(130f)
            setHeight(130f)
            setRotationAngle(PI / 2)
            setFixedPosition(58f, 670f)
        }
        return image
    }

    private fun getAlbumArtPath(albumArtPath: String): String {
        return File(System.getProperty("user.home") + "/.album-metadata/$albumArtPath").absolutePath
    }

    private fun createParagraph(paragraphText: String): Paragraph {
        val font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val fontSize = 17f

        return Paragraph(paragraphText).apply {
            setFont(font)
            setFontSize(fontSize)
            setTextAlignment(CENTER)
            setRotationAngle(PI / 2)
//            setBorder(SolidBorder(1f))
            setFixedPosition(275f, 675f, 130f) // x, y, width (increased width for vertical text)
        }
    }
}
