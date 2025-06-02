package com.excentric.malm

import com.excentric.malm.pdf.PdfLabelWriter

fun main() {
    PdfLabelWriter().apply {
        paragraphBorder = false
    }.runSafely()
}
