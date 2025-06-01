package com.excentric.client

import com.excentric.errors.MalmException
import com.excentric.model.api.CoverArtResponseModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import java.io.File
import java.io.IOException

@Component
class CoverArtArchiveClient(
    @Value("\${music-album-label-maker.cover-art-archive.url}")
    private val coverArtArchiveApiUrl: String
) : AbstractClient() {
    private val logger = LoggerFactory.getLogger(CoverArtArchiveClient::class.java)

    fun downloadAlbumArt(mbid: String) {
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
                ?: throw MalmException("Failed to download image from ${frontCover.imageUrl}")

            val tempImageFile = File.createTempFile("album-art", ".png")

            // Save the image to a temporary file
            imageResource.inputStream.use { inputStream ->
                tempImageFile.writeBytes(inputStream.readAllBytes())
                logger.info("Album art downloaded successfully to: ${tempImageFile.toURI()}")
            }
        } catch (e: IOException) {
            logger.error("Failed to download album art: {}", e.message)
            throw e
        }
    }

    private fun getImageMetadataApiUrl(mbid: String) = "${coverArtArchiveApiUrl}release/$mbid"
}
