package com.excentric.storage

import com.excentric.MusicBrainzApplication
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(classes = [MusicBrainzApplication::class])
class MetadataStorageSpringBootTest {
    private val logger = LoggerFactory.getLogger(MetadataStorageSpringBootTest::class.java)

    @Autowired
    private lateinit var metadataStorage: MetadataStorage

    @Test
    fun `print current value of MetadataStorage getSlotsMap`() {
        // Get the current slots map
        val slotsMap = metadataStorage.getSlotsMap()

        // Print the size of the map
        System.out.println("[DEBUG_LOG] MetadataStorage.getSlotsMap() contains ${slotsMap.size} entries")

        // Print each entry in the map
        slotsMap.forEach { (slot, metadata) ->
            System.out.println("[DEBUG_LOG] Slot $slot: ${metadata.artist} - ${metadata.album} (${metadata.year})")
        }

        // If the map is empty, print a message
        if (slotsMap.isEmpty()) {
            System.out.println("[DEBUG_LOG] MetadataStorage.getSlotsMap() is empty")
        }
    }
}
