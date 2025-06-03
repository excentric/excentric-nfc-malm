package com.excentric.storage

import com.excentric.malm.MalmApplication
import com.excentric.malm.controller.ImageMetadata
import com.excentric.malm.metadata.LabelMetadata
import com.excentric.malm.pdf.PdfLabelWriter
import com.excentric.malm.storage.MetadataStorage
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [MalmApplication::class], properties = ["spring.profiles.active=test"])
class MetadataStorageSpringBootTest {
    private val logger = LoggerFactory.getLogger(MetadataStorageSpringBootTest::class.java)

    @Autowired
    private lateinit var metadataStorage: MetadataStorage

    @Test
    fun `print current value of MetadataStorage getSlotsMap`() {
        val slotsMap = metadataStorage.getSlotsMap()
        val pdfSlotsMap = slotsMap.filter { it.key in (1..10) }

        val labels = pdfSlotsMap.map { (slot, albumMetadata) ->
            LabelMetadata(slot, albumMetadata.title, albumMetadata.artist, albumMetadata.year, metadataStorage.getCoverArtFile(slot))
        }

        PdfLabelWriter(labels).createPdf()
    }

    @Test
    fun `test getSelectedCoverArtIndex`() {
        // For each slot that has a selected cover art, check if getSelectedCoverArtIndex returns a valid index
        val slotsMap = metadataStorage.getSlotsMap()

        for (slot in slotsMap.keys) {
            val selectedIndex = metadataStorage.getSelectedCoverArtIndex(slot)
            logger.info("Slot $slot selected cover art index: $selectedIndex")

            // If there's a selected cover art file, the index should not be null
            val selectedFile = metadataStorage.getCoverArtFile(slot)
            if (selectedFile.exists()) {
                // Check if there are potential cover art files
                val potentialFiles = metadataStorage.getPotentialCoverArtsFiles(slot)
                if (potentialFiles.isNotEmpty()) {
                    // If there are potential files, we should have found a matching index
                    assert(selectedIndex != null) { "Selected index should not be null for slot $slot with existing cover art" }
                }
            }
        }
    }

    @Test
    fun `test getImageMetadata caching`() {
        // Find a slot with cover art files
        val slotsMap = metadataStorage.getSlotsMap()
        for (slot in slotsMap.keys) {
            val potentialFiles = metadataStorage.getPotentialCoverArtsFiles(slot)
            if (potentialFiles.isNotEmpty()) {
                val file = potentialFiles.first()

                // Get metadata for the file twice
                val metadata1 = metadataStorage.getImageMetadata(file)
                val metadata2 = metadataStorage.getImageMetadata(file)

                // Verify that the metadata is correct
                assert(metadata1.sizeKB > 0) { "Size should be greater than 0" }
                assert(metadata1.width > 0) { "Width should be greater than 0" }
                assert(metadata1.height > 0) { "Height should be greater than 0" }

                // Verify that the second call returns the same object (cached)
                assert(metadata1 === metadata2) { "Second call should return the cached object" }

                logger.info("[DEBUG_LOG] Image metadata for file ${file.name}: size=${metadata1.sizeKB}KB, dimensions=${metadata1.width}x${metadata1.height}")

                // Test passed, no need to check more files
                return
            }
        }

        logger.warn("No cover art files found to test getImageMetadata")
    }
}
