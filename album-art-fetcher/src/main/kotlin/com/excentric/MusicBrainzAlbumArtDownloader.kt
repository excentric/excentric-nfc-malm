package com.excentric

import com.excentric.errors.MusicBrainzException
import com.excentric.model.CoverArtResponseModel
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

@ShellComponent
@Component
class MusicBrainzAlbumArtDownloader(private val restTemplate: RestTemplate) {
    private val logger = LoggerFactory.getLogger(MusicBrainzAlbumArtDownloader::class.java)
    private val USER_AGENT = "MyMusicApp/1.0 (nfc-sonos@excentric.com)"
    private val CAA_API_URL = "https://coverartarchive.org/"

    @ShellMethod(key = ["download-art"], value = "Download album art using MusicBrainz ID")
    fun downloadArt(
        @ShellOption(help = "MusicBrainz ID") mbid: String,
        @ShellOption(help = "Output file path", defaultValue = "album_cover.jpg") outputPath: String
    ): String {
        downloadAlbumArt(mbid, outputPath)
        return "Album art downloaded to $outputPath"
    }

    fun downloadAlbumArt(mbid: String, outputPath: String) {
        try {
            // Construct the URL for the front cover image
            val url = CAA_API_URL + "release/" + mbid

            // Set up headers
            val headers = HttpHeaders()
            headers.set("User-Agent", USER_AGENT)
            headers.accept = listOf(MediaType.APPLICATION_JSON)

            // First, get the cover art metadata
            val entity = HttpEntity<String>(headers)
            val response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                CoverArtResponseModel::class.java
            )

            val coverArtResponse = response.body
                ?: throw MusicBrainzException("Empty response from Cover Art Archive API")

            // Find the front cover image
            val frontCover = coverArtResponse.images.find { it.front }
                ?: throw MusicBrainzException("No front cover found for release $mbid")

            // Get the image URL (prefer large size if available)
            val imageUrl = frontCover.thumbnails?.large ?: frontCover.image

            // Now download the actual image
            val imageHeaders = HttpHeaders()
            imageHeaders.set("User-Agent", USER_AGENT)
            val imageEntity = HttpEntity<String>(imageHeaders)

            val imageResponse = restTemplate.exchange(
                imageUrl,
                HttpMethod.GET,
                imageEntity,
                Resource::class.java
            )

            val imageResource = imageResponse.body
                ?: throw MusicBrainzException("Failed to download image from $imageUrl")

            // Save the image to the output path
            imageResource.inputStream.use { inputStream ->
                Files.copy(inputStream, Path.of(outputPath), StandardCopyOption.REPLACE_EXISTING)
                logger.info("Album art downloaded successfully to: $outputPath")
            }
        } catch (e: IOException) {
            logger.error("Failed to download album art: {}", e.message)
            throw e
        }
    }
}
