package com.excentric.storage

import com.excentric.malm.metadata.AlbumMetadata
import com.excentric.malm.storage.MetadataStorage
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class MetadataStorageTest {

    private lateinit var metadataStorage: MetadataStorage
    private lateinit var objectMapper: ObjectMapper
    private lateinit var testDirectory: File
    private lateinit var testDirectoryPath: String

    @BeforeEach
    fun setUp() {
        testDirectoryPath = "album-metadata-test"
        objectMapper = jacksonObjectMapper()
        metadataStorage = MetadataStorage(testDirectoryPath, objectMapper)

        // Create a temporary directory for testing
        testDirectory = File(testDirectoryPath)
        if (!testDirectory.exists()) {
            testDirectory.mkdirs()
        }

        // Set up a test album metadata
        metadataStorage.albumMetadata = AlbumMetadata(
            mbids = listOf("test-mbid"),
            title = "Test Album",
            artist = "Test Artist",
            year = 2023
        )
    }

    @AfterEach
    fun tearDown() {
        // Clean up test directory
        testDirectory.deleteRecursively()
    }


    @Test
    fun `listSlots should return all saved album metadata`() {
        // Given
        // Save first album
        metadataStorage.saveToSlot(1)

        // Save second album with different metadata
        val secondAlbum = AlbumMetadata(
            mbids = listOf("second-mbid"),
            title = "Second Album",
            artist = "Second Artist",
            year = 2022
        )
        metadataStorage.albumMetadata = secondAlbum
        metadataStorage.saveToSlot(2)

        // When
        val slots = metadataStorage.getSlotsMap()

        // Then
        assertEquals(2, slots.size)

        // Verify first album
        val firstAlbum = slots.values.find { it.mbids[0] == "test-mbid" }
        assertNotNull(firstAlbum)
        assertEquals("Test Album", firstAlbum?.title)
        assertEquals("Test Artist", firstAlbum?.artist)
        assertEquals(2023, firstAlbum?.year)

        // Verify second album
        val foundSecondAlbum = slots.values.find { it.mbids[0] == "second-mbid" }
        assertNotNull(foundSecondAlbum)
        assertEquals("Second Album", foundSecondAlbum?.title)
        assertEquals("Second Artist", foundSecondAlbum?.artist)
        assertEquals(2022, foundSecondAlbum?.year)
    }

    @Test
    fun `findNextAvailableSlot should return the first available slot`() {
        // Given no slots are occupied
        // When
        val nextSlot = metadataStorage.findNextAvailableSlot()
        // Then
        assertEquals(1, nextSlot)

        // Given slot 1 is occupied
        metadataStorage.saveToSlot(1)
        // When
        val nextSlotAfterOne = metadataStorage.findNextAvailableSlot()
        // Then
        assertEquals(2, nextSlotAfterOne)

        // Given slots 1 and 3 are occupied (non-sequential)
        metadataStorage.saveToSlot(3)
        // When
        val nextSlotAfterOneAndThree = metadataStorage.findNextAvailableSlot()
        // Then
        assertEquals(2, nextSlotAfterOneAndThree)

        // Given slots 1, 2, and 3 are occupied
        metadataStorage.saveToSlot(2)
        // When
        val nextSlotAfterOneToThree = metadataStorage.findNextAvailableSlot()
        // Then
        assertEquals(4, nextSlotAfterOneToThree)
    }
}
