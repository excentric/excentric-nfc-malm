package com.excentric.service

import com.excentric.client.CoverArtArchiveClient
import com.excentric.storage.MetadataStorage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CoverArtArchiveService(
    private val metadataStorage: MetadataStorage,
    private val coverArtArchiveClient: CoverArtArchiveClient,
) {
    private val logger = LoggerFactory.getLogger(CoverArtArchiveService::class.java)

    fun downloadAlbumArt(slot: Int) {
        val slots = metadataStorage.getSlots()
        val albumMetadata = slots[slot]
        if (albumMetadata == null) {
            logger.error("No metadata found for slot: $slot")
        } else {
            coverArtArchiveClient.downloadAlbumArt(slot, albumMetadata.mbids)
        }
    }
}
