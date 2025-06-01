package com.excentric.storage

import com.excentric.errors.MalmException
import com.excentric.model.storage.AlbumMetadata
import com.excentric.util.ConsoleColors
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
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

    fun getSlots(): Map<Int, AlbumMetadata> {
        validateMetadataDir()
        val slots = mutableMapOf<Int, AlbumMetadata>()

        getMetadataFiles().forEach { metadataFile ->
            try {
                val slotIndex = metadataFile.nameWithoutExtension.toIntOrNull()
                val metadata = objectMapper.readValue(metadataFile, AlbumMetadata::class.java)

                if (slotIndex != null)
                    slots[slotIndex] = metadata
                else
                    logger.error("Failed to determine slot index: ${metadataFile.name}")
            } catch (e: Exception) {
                logger.error("Failed to read album metadata from file ${metadataFile.name}: ${e.message}")
            }
        }
        return slots
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
        val originalFileCount = metadataDir.listFiles()?.size ?: 0
        metadataDir.listFiles()?.forEach { it.delete() }
        val newFileCount = metadataDir.listFiles()?.size ?: 0
        logger.info("Removed ${originalFileCount - newFileCount} metadata files. $newFileCount files remain.")
    }

    fun saveAlbumArt(slot: Int, index: Int, mbid: String, albumArtResource: Resource) {
        val slotDir = File(metadataDirPath, "$slot").also { it.mkdirs() }
        val imageFile = File(slotDir, "$slot-$index-$mbid.jpg")

        albumArtResource.inputStream.use { inputStream ->
            imageFile.writeBytes(inputStream.readAllBytes())
            logger.info("Album art [${ConsoleColors.greenOrRed(mbid)}] downloaded successfully to: ${imageFile.toURI()}")
        }
    }
}
