package com.excentric.malm.storage

import com.excentric.malm.errors.MalmException
import com.excentric.malm.metadata.AlbumMetadata
import com.excentric.malm.util.ConsoleColors
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

    fun getSlotsMap(): Map<Int, AlbumMetadata> {
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
        return slots.toSortedMap()
    }

    private fun getMetadataFiles() = metadataDir.listFiles { file ->
        file.isFile && file.name.matches(Regex("\\d+\\.json"))
    }.orEmpty()

    private fun validate(slot: Int, metadata: AlbumMetadata?) {
        if (slot < 1 || slot > 99) {
            throw MalmException("Invalid slot number: $slot. Must be between 1 and 99.")
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
        metadataDir.listFiles()?.forEach { it.deleteRecursively() }
        val newFileCount = metadataDir.listFiles()?.size ?: 0
        logger.info("Removed ${originalFileCount - newFileCount} metadata files. $newFileCount files remain.")
    }

    fun removeSlots(slotNumbers: List<Int>) {
        validateMetadataDir()
        val originalFileCount = metadataDir.listFiles()?.size ?: 0
        slotNumbers.forEach { slot ->
            File(metadataDirPath, "$slot.json").delete()
            File(metadataDirPath, "$slot.jpg").delete()
            File(metadataDirPath, "$slot").deleteRecursively()
        }

        val newFileCount = metadataDir.listFiles()?.size ?: 0
        logger.info("Removed ${originalFileCount - newFileCount} metadata files. $newFileCount files remain.")
    }

    fun saveCoverArt(slot: Int, index: Int, mbid: String, coverArtResource: Resource) {
        val slotDir = File(metadataDirPath, "$slot").also { it.mkdirs() }
        val imageFile = File(slotDir, "${index.toString().padStart(2, '0')}.jpg")

        coverArtResource.inputStream.use { inputStream ->
            imageFile.writeBytes(inputStream.readAllBytes())
            logger.info("Cover art [${ConsoleColors.greenOrRed(mbid)}] downloaded successfully to: ${imageFile.toURI()}")
        }
    }

    fun getCoverArtsDir(slot: Int): File {
        return File(metadataDirPath, "$slot")
    }

    fun getPotentialCoverArtsFiles(slot: Int): List<File> {
        val slotDir = getCoverArtsDir(slot)

        if (slotDir.exists() && slotDir.isDirectory) {
            return slotDir.listFiles()?.filter { it.isFile && !it.name.startsWith(".") } ?: emptyList()
        }

        return emptyList()
    }

    fun getCoverArtFile(slot: Int): File {
        return File(metadataDirPath, "$slot.jpg")
    }

    fun selectCoverArt(slot: Int, selectedFile: File) {
        // SC: we will keep the potential cover art for now i think
        selectedFile.copyTo(File(metadataDirPath, "$slot.jpg"), overwrite = true)
        selectedFile.delete()
    }

    fun moveSlot(sourceSlot: Int, targetSlot: Int) {
        if ((sourceSlot < 1 || sourceSlot > 99) || (targetSlot < 1 || targetSlot > 99) || sourceSlot == targetSlot) {
            throw MalmException("Invalid slot numbers: $sourceSlot : $targetSlot")
        }

        validateMetadataDir()

        val sourceJsonFile = File(metadataDirPath, "$sourceSlot.json")
        sourceJsonFile.copyTo(File(metadataDirPath, "$targetSlot.json"), true)
        sourceJsonFile.delete()

        val sourceJpgFile = File(metadataDirPath, "$sourceSlot.jpg")
        if (sourceJpgFile.exists()) {
            sourceJpgFile.copyTo(File(metadataDirPath, "$targetSlot.jpg"), true)
            sourceJpgFile.delete()
        }

        val sourceCoverArtDir = File(metadataDirPath, "$sourceSlot")
        if (sourceCoverArtDir.exists() && sourceCoverArtDir.isDirectory) {
            val targetAlbumDir = File(metadataDirPath, "$targetSlot")
            targetAlbumDir.deleteRecursively()
            sourceCoverArtDir.copyRecursively(targetAlbumDir)
            sourceCoverArtDir.deleteRecursively()
        }

        logger.info("Successfully moved slot $sourceSlot to slot $targetSlot")
    }
}
