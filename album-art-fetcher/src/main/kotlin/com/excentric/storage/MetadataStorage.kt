package com.excentric.storage

import com.excentric.model.local.AlbumMetadata
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File

@Component
class MetadataStorage(
    @Value("\${application.metadata-directory}")
    private val metadataDirPath: String,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(MetadataStorage::class.java)

    private var metadataDir: File = File(metadataDirPath).also {
        if (!it.exists())
            if (!it.mkdirs()) {
                logger.error("Failed to create metadata directory: $metadataDirPath")
            }
    }

    var albumMetadata: AlbumMetadata? = null

    fun saveToSlot(slot: Int): Boolean {
        val metadata = albumMetadata

        if (slot < 1 || slot > 10) {
            logger.error("Invalid slot number: $slot. Must be between 1 and 10.")
            return false
        }

        if (metadata == null) {
            logger.error("No album metadata available to save")
            return false
        }

        if (!metadataDir.exists()) {
            logger.error("Metadata directory does not exist: $metadataDirPath")
            return false
        }

        val metadataFile = File(metadataDirPath, "$slot.json")
        return try {
            objectMapper.writeValue(metadataFile, metadata)
            logger.info("Successfully saved album metadata to slot $slot")
            true
        } catch (e: Exception) {
            logger.error("Failed to save album metadata to slot $slot: ${e.message}")
            false
        }
    }
}
