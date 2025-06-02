package com.excentric.storage

import com.excentric.malm.MusicBrainzApplication
import com.excentric.malm.metadata.AlbumLabelMetadata
import com.excentric.malm.pdf.PdfLabelWriter
import com.excentric.malm.storage.MetadataStorage
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
        val slotsMap = metadataStorage.getSlotsMap()
        val pdfSlotsMap = slotsMap.filter { it.key in (1..10) }

        val labels = pdfSlotsMap.map { (slot, albumMetadata) ->
            AlbumLabelMetadata(slot, albumMetadata.album, albumMetadata.artist, albumMetadata.year, metadataStorage.getAlbumArtFile(slot))
        }

        PdfLabelWriter(labels).createPdf()
    }
}
