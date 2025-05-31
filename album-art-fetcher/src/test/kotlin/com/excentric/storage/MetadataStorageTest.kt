package com.excentric.storage

import com.excentric.model.storage.AlbumMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
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
            mbid = "test-mbid",
            album = "Test Album",
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
    fun `saveToSlot should return false when slot is less than 1`() {
        // When
        val result = metadataStorage.saveToSlot(0)

        // Then
        assertFalse(result)
    }

    @Test
    fun `saveToSlot should return false when slot is greater than 10`() {
        // When
        val result = metadataStorage.saveToSlot(11)

        // Then
        assertFalse(result)
    }

    @Test
    fun `saveToSlot should return false when albumMetadata is null`() {
        // Given
        metadataStorage.albumMetadata = null

        // When
        val result = metadataStorage.saveToSlot(1)

        // Then
        assertFalse(result)
    }

    @Test
    fun `saveToSlot should save albumMetadata to correct file`() {
        // When
        val result = metadataStorage.saveToSlot(5)

        // Then
        assertTrue(result)

        // Verify file was created
        val savedFile = File(testDirectoryPath, "5.json")
        assertTrue(savedFile.exists())

        // Verify file content
        val savedMetadata: AlbumMetadata = objectMapper.readValue(savedFile)
        assertEquals("test-mbid", savedMetadata.mbid)
        assertEquals("Test Album", savedMetadata.album)
        assertEquals("Test Artist", savedMetadata.artist)
        assertEquals(2023, savedMetadata.year)
    }

    @Test
    fun `listSlots should return all saved album metadata`() {
        // Given
        // Save first album
        metadataStorage.saveToSlot(1)

        // Save second album with different metadata
        val secondAlbum = AlbumMetadata(
            mbid = "second-mbid",
            album = "Second Album",
            artist = "Second Artist",
            year = 2022
        )
        metadataStorage.albumMetadata = secondAlbum
        metadataStorage.saveToSlot(2)

        // When
        val result = metadataStorage.listSlots()

        // Then
        assertEquals(2, result.size)

        // Verify first album
        val firstAlbum = result.find { it.mbid == "test-mbid" }
        assertNotNull(firstAlbum)
        assertEquals("Test Album", firstAlbum?.album)
        assertEquals("Test Artist", firstAlbum?.artist)
        assertEquals(2023, firstAlbum?.year)

        // Verify second album
        val foundSecondAlbum = result.find { it.mbid == "second-mbid" }
        assertNotNull(foundSecondAlbum)
        assertEquals("Second Album", foundSecondAlbum?.album)
        assertEquals("Second Artist", foundSecondAlbum?.artist)
        assertEquals(2022, foundSecondAlbum?.year)
    }
}
