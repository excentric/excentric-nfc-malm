package com.excentric.storage

import com.excentric.malm.MalmApplication
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
}
