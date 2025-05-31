package com.excentric.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AlbumReleaseModelTest {

    @Test
    fun `getYear returns correct year from yyyy format`() {
        val model = AlbumReleaseModel(
            id = "test-id",
            title = "Test Album",
            date = "2020"
        )

        assertEquals(2020, model.getYear())
    }

    @Test
    fun `getYear returns correct year from yyyy-mm-dd format`() {
        val model = AlbumReleaseModel(
            id = "test-id",
            title = "Test Album",
            date = "2020-05-15"
        )

        assertEquals(2020, model.getYear())
    }

    @Test
    fun `getYear returns null for invalid date format`() {
        val model = AlbumReleaseModel(
            id = "test-id",
            title = "Test Album",
            date = "invalid-date"
        )

        assertNull(model.getYear())
    }

    @Test
    fun `getYear returns null for null date`() {
        val model = AlbumReleaseModel(
            id = "test-id",
            title = "Test Album",
            date = null
        )

        assertNull(model.getYear())
    }
}
