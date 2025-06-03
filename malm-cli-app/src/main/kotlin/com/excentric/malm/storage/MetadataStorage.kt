package com.excentric.malm.storage

import com.excentric.malm.errors.MalmException
import com.excentric.malm.metadata.AlbumMetadata
import com.excentric.malm.util.ConsoleColors.green
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

    fun saveToNextAvailableSlot(albumMetadata: AlbumMetadata): Int? {
        this.albumMetadata = albumMetadata
        val slot = findNextAvailableSlot()
        if (slot == null) {
            logger.warn("No free slots!")
        } else
            saveToSlot(slot, albumMetadata)
        return slot
    }

    private fun saveToSlot(slot: Int, albumMetadata: AlbumMetadata? = this.albumMetadata) {
        validate(slot, albumMetadata)

        try {
            val metadataFile = getMetadataFile(slot)
            objectMapper.writeValue(metadataFile, albumMetadata)
            logger.info("Saved album to slot ${green(slot.toString())}")
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
            getMetadataFile(slot).delete()
            getSelectedCoverArtFile(slot).delete()
            getPotentialCoverArtFilesDir(slot).deleteRecursively()
        }

        val newFileCount = metadataDir.listFiles()?.size ?: 0
        logger.info("Removed ${originalFileCount - newFileCount} metadata files. $newFileCount files remain.")
    }

    fun saveCoverArt(slot: Int, index: Int, coverArtFile: File) {
        val imageFile = getPotentialCoverArtFile(slot, index)
        coverArtFile.copyTo(imageFile, overwrite = true)
    }

    fun getPotentialCoverArtsFiles(slot: Int): List<File> {
        val slotDir = getPotentialCoverArtFilesDir(slot)

        if (slotDir.exists() && slotDir.isDirectory) {
            return slotDir.listFiles()?.filter { it.isFile && !it.name.startsWith(".") } ?: emptyList()
        }

        return emptyList()
    }

    fun getCoverArtFile(slot: Int): File {
        return getSelectedCoverArtFile(slot)
    }

    fun selectCoverArt(slot: Int, index: Int) {
        selectCoverArt(slot, getPotentialCoverArtFile(slot, index))
    }

    fun selectCoverArt(slot: Int, selectedFile: File) {
        // SC: we will keep the potential cover art for now i think
        selectedFile.copyTo(getSelectedCoverArtFile(slot), overwrite = true)
    }

    fun moveSlot(sourceSlot: Int, targetSlot: Int) {
        if ((sourceSlot < 1 || sourceSlot > 99) || (targetSlot < 1 || targetSlot > 99) || sourceSlot == targetSlot) {
            throw MalmException("Invalid slot numbers: $sourceSlot : $targetSlot")
        }

        validateMetadataDir()

        val sourceJsonFile = getMetadataFile(sourceSlot)
        sourceJsonFile.copyTo(getMetadataFile(targetSlot), true)
        sourceJsonFile.delete()

        val sourceJpgFile = getSelectedCoverArtFile(sourceSlot)
        if (sourceJpgFile.exists()) {
            sourceJpgFile.copyTo(getSelectedCoverArtFile(targetSlot), true)
            sourceJpgFile.delete()
        }

        val sourceCoverArtDir = getPotentialCoverArtFilesDir(sourceSlot)
        if (sourceCoverArtDir.exists() && sourceCoverArtDir.isDirectory) {
            val targetAlbumDir = getPotentialCoverArtFilesDir(targetSlot)
            targetAlbumDir.deleteRecursively()
            sourceCoverArtDir.copyRecursively(targetAlbumDir)
            sourceCoverArtDir.deleteRecursively()
        }

        logger.info("Successfully moved slot $sourceSlot to slot $targetSlot")
    }

    private fun getMetadataFile(slot: Int) = File(metadataDirPath, "${padded(slot)}.json")

    private fun getSelectedCoverArtFile(slot: Int) = File(metadataDirPath, "${padded(slot)}.jpg")

    private fun getPotentialCoverArtFilesDir(slot: Int) = File(metadataDirPath, padded(slot))

    fun getPotentialCoverArtFile(slot: Int, index: Int): File {
        val slotDir = getPotentialCoverArtFilesDir(slot).also { it.mkdirs() }
        return File(slotDir, "${padded(index)}.jpg")
    }

    private fun padded(index: Int) = index.toString().padStart(2, '0')

    fun getSlotsWithoutPotentialCoverArt(): List<Int> {
        return getMetadataFiles().mapNotNull { metadataFile ->
            metadataFile.nameWithoutExtension.toIntOrNull()?.let { slot ->
                if (getPotentialCoverArtsFiles(slot).isEmpty())
                    return@mapNotNull slot
            }
            return@mapNotNull null
        }
    }

    fun getSelectedCoverArtIndex(slot: Int): Int? {
        val selectedFile = getSelectedCoverArtFile(slot)
        if (!selectedFile.exists()) {
            return null
        }

        val potentialFiles = getPotentialCoverArtsFiles(slot)
        if (potentialFiles.isEmpty()) {
            return null
        }

        val selectedBytes = selectedFile.readBytes()

        for (potentialFile in potentialFiles) {
            if (potentialFile.readBytes().contentEquals(selectedBytes)) {
                return potentialFile.nameWithoutExtension.toIntOrNull()
            }
        }

        return null
    }

    private fun findNextAvailableSlot(): Int? {
        val occupiedSlots = getSlotsMap().keys
        for (slot in 1..99) {
            if (slot !in occupiedSlots) {
                return slot
            }
        }
        return null
    }

    fun getSlotsWithoutAppleMusicIds(): Set<Int> {
        return getSlotsMap().filter { it.value.appleMusicAlbumId == null }.keys
    }

    fun updateAppleMusicId(slot: Int, appleMusicAlbumId: String?) {
        val albumMetadataFile = getMetadataFile(slot)
        if (albumMetadataFile.exists()) {
            val albumMetadata = objectMapper.readValue(albumMetadataFile, AlbumMetadata::class.java)
            albumMetadata.appleMusicAlbumId = appleMusicAlbumId
            objectMapper.writeValue(albumMetadataFile, albumMetadata)
            logger.info("Updated slot $slot with apple music album id: $appleMusicAlbumId")
        }
    }
}
