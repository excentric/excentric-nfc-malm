package com.excentric.malm.service

import com.excentric.malm.client.CoverArtArchiveClient
import com.excentric.malm.storage.MetadataStorage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CoverArtArchiveService(
    private val metadataStorage: MetadataStorage,
    private val coverArtArchiveClient: CoverArtArchiveClient,
) {
    private val logger = LoggerFactory.getLogger(CoverArtArchiveService::class.java)

    fun downloadAlbumArt(slot: Int) {
        val slots = metadataStorage.getSlotsMap()
        val albumMetadata = slots[slot]
        if (albumMetadata == null) {
            logger.error("No metadata found for slot: $slot")
        } else {
            coverArtArchiveClient.downloadCoverArt(slot, albumMetadata.mbids)
        }
    }
}
