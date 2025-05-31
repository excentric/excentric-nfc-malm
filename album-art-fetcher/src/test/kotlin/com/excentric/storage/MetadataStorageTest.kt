package com.excentric.storage

import com.excentric.model.local.AlbumMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class MetadataStorageTest {

    private lateinit var metadataStorage: MetadataStorage
    private lateinit var objectMapper: ObjectMapper
    private lateinit var testDirectory: File
    private lateinit var testDirectoryPath: String

    @BeforeEach
    fun setUp() {
        testDirectoryPath = "album-metadata-test"
        metadataStorage = MetadataStorage(testDirectoryPath)
        objectMapper = jacksonObjectMapper()

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
}
