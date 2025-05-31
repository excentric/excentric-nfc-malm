package com.excentric

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object MusicBrainzAlbumArtDownloader {
    private const val USER_AGENT = "MyMusicApp/1.0 (nfc-sonos@excentric.com)"
    private const val CAA_API_URL = "https://coverartarchive.org/"

    @JvmStatic
    fun main(args: Array<String>) {
        downloadAlbumArt("mbid", "album_cover.jpg")
    }

    fun downloadAlbumArt(mbid: String, outputPath: String) {
        try {
            // Construct the URL for the front cover image
            val url = URL(CAA_API_URL + "release/" + mbid + "/front")

            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", USER_AGENT)

            // Follow redirects (Cover Art Archive often redirects to the actual image)
            connection.instanceFollowRedirects = true

            connection.inputStream.use { `in` ->
                Files.copy(`in`, Path.of(outputPath), StandardCopyOption.REPLACE_EXISTING)
                println("Album art downloaded successfully to: $outputPath")
            }
        } catch (e: IOException) {
            System.err.println("Failed to download album art: " + e.message)
            throw e
        }
    }
}
