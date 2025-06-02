package com.excentric.malm.pdf

fun main() {
    PdfLabelWriter().apply {
        paragraphBorder = false
    }.runSafely()
}
