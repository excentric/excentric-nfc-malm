package com.excentric.malm.client

import com.excentric.malm.errors.MalmException
import com.excentric.malm.model.CoverArtResponseModel
import com.excentric.malm.storage.MetadataStorage
import com.excentric.malm.util.ConsoleColors
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import java.util.Locale.getDefault

@Component
class CoverArtArchiveClient(
    private val metadataStorage: MetadataStorage,
    @Value("\${music-album-label-maker.cover-art-archive.url}")
    private val coverArtArchiveApiUrl: String
) : AbstractClient() {
    private val logger = LoggerFactory.getLogger(CoverArtArchiveClient::class.java)

    fun downloadCoverArt(slot: Int, mbids: List<String>) {
        mbids.forEachIndexed { index, mbid ->
            downloadCoverArt(slot, mbid, index)
        }
    }

    private fun downloadCoverArt(slot: Int, mbid: String, index: Int) {
        try {
            val response = restTemplate.exchange(
                getImageMetadataApiUrl(mbid),
                HttpMethod.GET,
                HttpEntity<String>(buildRestHttpHeaders()),
                CoverArtResponseModel::class.java
            )

            val coverArtResponse = response.body
                ?: throw MalmException("Empty response from Cover Art Archive API")

            // Find the front cover image
            val frontCover = coverArtResponse.images.find { it.front }
                ?: throw MalmException("No front cover found for release $mbid")

            val imageResponse = restTemplate.exchange(
                frontCover.imageUrl,
                HttpMethod.GET,
                HttpEntity<String>(buildImageHttpHeaders()),
                Resource::class.java
            )

            val imageResource = imageResponse.body
                ?: throw MalmException("Not found")

            if (imageResponse.headers.contentType?.type?.lowercase(getDefault()) != "image") {
                throw MalmException("Not an image")
            }

            metadataStorage.saveCoverArt(slot, index, mbid, imageResource)

        } catch (e: Exception) {
            logger.warn("Cover art [${ConsoleColors.red(mbid)}] failed to download: ${e.message}")
        }
    }

    private fun getImageMetadataApiUrl(mbid: String) = "${coverArtArchiveApiUrl}release/$mbid"
}
