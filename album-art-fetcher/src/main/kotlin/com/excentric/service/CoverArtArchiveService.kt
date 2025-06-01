package com.excentric.service

import com.excentric.client.CoverArtArchiveClient
import com.excentric.errors.MalmException
import com.excentric.storage.MetadataStorage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CoverArtArchiveService(
    private val storage: MetadataStorage,
    private val coverArtArchiveClient: CoverArtArchiveClient,
) {
    private val logger = LoggerFactory.getLogger(CoverArtArchiveService::class.java)

    fun downloadAlbumArt() {
        validateMetadata()
        coverArtArchiveClient.downloadAlbumArt(storage.albumMetadata!!.mbid)
    }

    private fun validateMetadata() {
        if (storage.albumMetadata?.mbid == null) {
            throw MalmException("No album metadata found")
        }
    }
}
