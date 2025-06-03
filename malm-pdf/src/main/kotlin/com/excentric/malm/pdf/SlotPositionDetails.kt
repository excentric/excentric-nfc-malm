package com.excentric.malm.pdf

class SlotPositionDetails(row: Int, column: Int) {

    val imageLeft: Float = ORIGIN_IMAGE_LEFT + column * COLUMN_OFFSET
    val imageBottom: Float = ORIGIN_IMAGE_BOTTOM - row * ROW_OFFSET
    val artistLeft: Float = ORIGIN_ARTIST_LEFT + column * COLUMN_OFFSET
    val artistBottom: Float = ORIGIN_PARAGRAPH_BOTTOM - row * ROW_OFFSET
    val titleLeft: Float = ORIGIN_TITLE_LEFT + column * COLUMN_OFFSET
    val titleBottom: Float = ORIGIN_PARAGRAPH_BOTTOM - row * ROW_OFFSET
    val yearLeft: Float = ORIGIN_YEAR_LEFT + column * COLUMN_OFFSET
    val yearBottom: Float = ORIGIN_PARAGRAPH_BOTTOM - row * ROW_OFFSET

    companion object {
        const val ROW_OFFSET = 156.8f
        const val COLUMN_OFFSET = 270f
        const val PARAGRAPH_OFFSET = 26f

        const val ORIGIN_IMAGE_LEFT = 58f
        const val ORIGIN_IMAGE_BOTTOM = 667f
        const val ORIGIN_ARTIST_LEFT = 225f
        const val ORIGIN_PARAGRAPH_BOTTOM = 670f
        const val ORIGIN_TITLE_LEFT = ORIGIN_ARTIST_LEFT + PARAGRAPH_OFFSET
        const val ORIGIN_YEAR_LEFT = ORIGIN_TITLE_LEFT + PARAGRAPH_OFFSET
    }
}
