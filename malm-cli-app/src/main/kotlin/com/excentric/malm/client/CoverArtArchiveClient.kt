package com.excentric.malm.client

import com.excentric.malm.config.CoverArtProperties
import com.excentric.malm.errors.MalmException
import com.excentric.malm.model.CoverArtResponseModel
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import java.io.File
import java.util.Locale.getDefault

@Component
class CoverArtArchiveClient(
    private val coverArtProperties: CoverArtProperties
) : AbstractClient() {
    fun downloadCoverArt(mbid: String): File? {
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

            val coverUrl = frontCover.thumbnails?.size1200 ?: frontCover.imageUrl

            val imageResponse = restTemplate.exchange(
                coverUrl,
                HttpMethod.GET,
                HttpEntity<String>(buildImageHttpHeaders()),
                Resource::class.java
            )

            val imageResource = imageResponse.body
                ?: throw MalmException("Not found")

            if (imageResponse.headers.contentType?.type?.lowercase(getDefault()) != "image") {
                throw MalmException("Not an image")
            }

            val tempImageFile = File.createTempFile("malm-cover-art", ".jpg").apply {
                deleteOnExit()
            }

            imageResource.inputStream.use { inputStream ->
                tempImageFile.writeBytes(inputStream.readAllBytes())
            }

            return tempImageFile
        } catch (e: Exception) {
            return null
        }
    }

    private fun getImageMetadataApiUrl(mbid: String) = "${coverArtProperties.url}release/$mbid"
}
