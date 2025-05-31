package com.excentric.storage

import com.excentric.model.local.AlbumMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@Component
class MetadataStorage {
    private val logger = LoggerFactory.getLogger(MetadataStorage::class.java)
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    var albumMetadata: AlbumMetadata? = null

    /**
     * Saves the current albumMetadata to a JSON file in the specified slot
     * @param slot An integer between 1 and 10 representing the storage slot
     * @return true if save was successful, false otherwise
     */
    fun saveToSlot(slot: Int): Boolean {
        if (slot < 1 || slot > 10) {
            logger.error("Invalid slot number: $slot. Must be between 1 and 10.")
            return false
        }

        val metadata = albumMetadata
        if (metadata == null) {
            logger.error("No album metadata available to save")
            return false
        }

        val directory = File("album-metadata")
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val file = File(directory, "$slot.json")
        return try {
            objectMapper.writeValue(file, metadata)
            logger.info("Successfully saved album metadata to slot $slot")
            true
        } catch (e: Exception) {
            logger.error("Failed to save album metadata to slot $slot", e)
            false
        }
    }
}
