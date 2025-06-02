package com.excentric.utils.com.excentric.malm.pdf

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Image
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.io.image.ImageDataFactory
import java.io.File
import java.io.FileOutputStream

/**
 * A simple Kotlin main class that opens a PDF file from resources,
 * adds fixed position text "hello world", and saves it as a new PDF file.
 */
fun main() {
    PdfModifier().run()
}

class PdfModifier {
    fun run() {
        try {
            // Get the input PDF file from resources
            val inputStream = javaClass.classLoader.getResourceAsStream("blank-avery.pdf")
                ?: throw IllegalArgumentException("Could not find blank-avery.pdf in resources")

            // Create a temporary file to read from
            val tempInputFile = File.createTempFile("blank-avery", ".pdf")
            tempInputFile.deleteOnExit()
            inputStream.use { input ->
                FileOutputStream(tempInputFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Define the output file
            val outputFile = File("output-hello-world.pdf")

            // Initialize PDF reader and writer
            val reader = PdfReader(tempInputFile)
            val writer = PdfWriter(outputFile)

            // Initialize PDF document
            val pdfDoc = PdfDocument(reader, writer)
            val document = Document(pdfDoc)

            // Add text at a fixed position with vertical orientation and specific font
            // Create Sans-serif font with 62px size
            // Using Helvetica as it's a standard sans-serif font
            val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)
            val fontSize = 17f

            // Create paragraph with the text
            val paragraph = Paragraph("Pink Floyd\nAnimals\n(1971)")

            // Apply font and size
            paragraph.setFont(font)
            paragraph.setFontSize(fontSize)

            // Center align the text
            paragraph.setTextAlignment(TextAlignment.CENTER)

            // Add a 1px border around the paragraph
//            paragraph.setBorder(SolidBorder(1f))

            // Set to vertical orientation by rotating 90 degrees
            paragraph.setRotationAngle(Math.PI / 2)

            // Position the text
            paragraph.setFixedPosition(275f, 675f, 130f) // x, y, width (increased width for vertical text)

            document.add(paragraph)

            // Load the image from the specified path
            val imagePath = System.getProperty("user.home") + "/.album-metadata/1/1.jpg"
            val imageFile = File(imagePath)

            if (imageFile.exists()) {
                // Create an image object
                val image = Image(ImageDataFactory.create(imagePath))

                // Resize the image to 300x300
                image.setWidth(130f)
                image.setHeight(130f)

                // Rotate the image (90 degrees = PI/2 radians)
                image.setRotationAngle(Math.PI / 2)

                // Position the image at (20, 20)
                image.setFixedPosition(58f, 670f)

                // Add the image to the document
                document.add(image)

                println("Image added from: $imagePath")
            } else {
                println("Warning: Image file not found at: $imagePath")
            }

            // Close the document
            document.close()

            println("PDF successfully modified. Output saved to: ${outputFile.absolutePath}")
        } catch (e: Exception) {
            println("Error modifying PDF: ${e.message}")
            e.printStackTrace()
        }
    }
}
