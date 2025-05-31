package com.excentric.storage

import com.excentric.errors.MusicBrainzException
import com.excentric.model.storage.AlbumMetadata
import com.excentric.util.ConsoleColors.greenOrRed
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

    fun saveToSlot(slot: Int) {
        val metadata = albumMetadata

        validate(slot, metadata)

        val metadataFile = File(metadataDirPath, "$slot.json")
        return try {
            objectMapper.writeValue(metadataFile, metadata)
            logger.info("Successfully saved album metadata to slot $slot")
        } catch (e: Exception) {
            throw MusicBrainzException("Failed to save album metadata to slot $slot: ${e.message}")
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
            throw MusicBrainzException("Invalid slot number: $slot. Must be between 1 and 10.")
        }

        if (metadata == null) {
            throw MusicBrainzException("No album metadata available to save")
        }

        validateMetadataDir()
    }

    private fun validateMetadataDir() {
        if (!metadataDir.exists() || !metadataDir.isDirectory) {
            throw MusicBrainzException("Metadata directory does not exist: $metadataDirPath")
        }
    }
}
