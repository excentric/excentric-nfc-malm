package com.excentric.malm.pdf

class SlotPositionDetails(row: Int, column: Int) {

    val imageLeft: Float = ORIGIN_IMAGE_LEFT + column * COLUMN_OFFSET
    val imageBottom: Float = ORIGIN_IMAGE_BOTTOM - row * ROW_OFFSET
    val paragraphLeft: Float = ORIGIN_PARAGRAPH_LEFT + column * COLUMN_OFFSET
    val paragraphBottom: Float = ORIGIN_PARAGRAPH_BOTTOM - row * ROW_OFFSET

    companion object {
        const val ORIGIN_IMAGE_LEFT = 58f
        const val ORIGIN_IMAGE_BOTTOM = 670f
        const val ORIGIN_PARAGRAPH_LEFT = 275f
        const val ORIGIN_PARAGRAPH_BOTTOM = 675f

        const val ROW_OFFSET = 156f
        const val COLUMN_OFFSET = 270f
    }
}
