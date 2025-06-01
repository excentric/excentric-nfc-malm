package com.excentric.storage

import com.excentric.errors.MalmException
import com.excentric.model.storage.AlbumMetadata
import com.excentric.util.ConsoleColors.greenOrRed
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.io.File

@Component
class MetadataStorage(
    @Value("\${music-album-label-maker.metadata-directory}")
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

    fun saveToSlot(slot: Int) {
        val metadata = albumMetadata

        validate(slot, metadata)

        val metadataFile = File(metadataDirPath, "$slot.json")
        return try {
            objectMapper.writeValue(metadataFile, metadata)
            logger.info("Successfully saved album metadata to slot $slot")
        } catch (e: Exception) {
            throw MalmException("Failed to save album metadata to slot $slot: ${e.message}")
        }
    }

    fun listSlots(): List<AlbumMetadata> {
        validateMetadataDir()

        return getMetadataFiles().mapNotNull { metadataFile ->
            try {
                val metadata = objectMapper.readValue(metadataFile, AlbumMetadata::class.java)
                logger.info("Slot ${metadataFile.nameWithoutExtension}: Album: ${greenOrRed(metadata.album)}, Artist: ${greenOrRed(metadata.artist)}, Year: ${greenOrRed(metadata.year)}")
                metadata
            } catch (e: Exception) {
                logger.error("Failed to read album metadata from file ${metadataFile.name}: ${e.message}")
                null
            }
        }
    }

    private fun getMetadataFiles() = metadataDir.listFiles { file ->
        file.isFile && file.name.matches(Regex("\\d+\\.json"))
    }.orEmpty()

    private fun validate(slot: Int, metadata: AlbumMetadata?) {
        if (slot < 1 || slot > 10) {
            throw MalmException("Invalid slot number: $slot. Must be between 1 and 10.")
        }

        if (metadata == null) {
            throw MalmException("No album metadata available to save")
        }

        validateMetadataDir()
    }

    private fun validateMetadataDir() {
        if (!metadataDir.exists() || !metadataDir.isDirectory) {
            throw MalmException("Metadata directory does not exist: $metadataDirPath")
        }
    }

    fun removeAllSlots() {
        validateMetadataDir()

        val files = getMetadataFiles()
        if (files.isEmpty()) {
            logger.info("No metadata slots found to remove")
            return
        }

        var successCount = 0
        var failCount = 0

        files.forEach { file ->
            try {
                if (file.delete()) {
                    successCount++
                    logger.info("Deleted metadata slot: ${file.nameWithoutExtension}")
                } else {
                    failCount++
                    logger.error("Failed to delete metadata slot: ${file.nameWithoutExtension}")
                }
            } catch (e: Exception) {
                failCount++
                logger.error("Error deleting metadata slot ${file.nameWithoutExtension}: ${e.message}")
            }
        }

        logger.info("Removed $successCount metadata slots" + if (failCount > 0) ", failed to remove $failCount slots" else "")
    }
}
