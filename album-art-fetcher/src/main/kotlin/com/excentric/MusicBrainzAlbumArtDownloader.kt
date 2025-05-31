package com.excentric

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors

object MusicBrainzAlbumArtDownloader {
    private const val USER_AGENT = "MyMusicApp/1.0 (nfc-sonos@excentric.com)"
    private const val MB_API_URL = "https://musicbrainz.org/ws/2/"
    private const val CAA_API_URL = "https://coverartarchive.org/"

    @JvmStatic
    fun main(args: Array<String>) {
        // Search for Mezzanine by Massive Attack and download its album art
        val artist = "Massive Attack"
        val album = "Mezzanine"

        println("Searching for album: $album by artist: $artist")
        val mbid = findMbidByAlbumInfo(artist, album)
        println("Found MBID: $mbid")

//        downloadAlbumArt(mbid, "album_cover.jpg")
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

    fun findMbidByAlbumInfo(artist: String, album: String): String {
        // Encode the artist and album names for URL
        val encodedArtist = URLEncoder.encode(artist, StandardCharsets.UTF_8.toString())
        val encodedAlbum = URLEncoder.encode(album, StandardCharsets.UTF_8.toString())

        // Construct the search URL
        val searchUrl = "${MB_API_URL}release?query=artist:$encodedArtist%20AND%20release:$encodedAlbum&fmt=json"

        println("Querying MusicBrainz API: $searchUrl")

        val url = URL(searchUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.setRequestProperty("Accept", "application/json")

        // MusicBrainz API requires a delay between requests
        // For this example, we're making just one request

        // Read the response
        val responseCode = connection.responseCode
        if (responseCode != 200) {
            throw IOException("HTTP error code: $responseCode")
        }

        BufferedReader(
            InputStreamReader(connection.inputStream, StandardCharsets.UTF_8)
        ).use { reader ->
            val jsonResponse = reader.lines().collect(Collectors.joining())
            println("Received response from MusicBrainz API")

            // Simple JSON parsing to extract the first release MBID
            // Find the first "id" field after a "releases" array
            val releasesIndex = jsonResponse.indexOf("\"releases\"")
            if (releasesIndex == -1) {
                throw IOException("No releases found in the response")
            }

            val idIndex = jsonResponse.indexOf("\"id\":", releasesIndex)
            if (idIndex == -1) {
                throw IOException("No release ID found in the response")
            }

            // Extract the MBID (UUID) which is enclosed in quotes
            val startQuote = jsonResponse.indexOf("\"", idIndex + 5)
            val endQuote = jsonResponse.indexOf("\"", startQuote + 1)

            if (startQuote == -1 || endQuote == -1) {
                throw IOException("Could not parse release ID from the response")
            }

            val mbid = jsonResponse.substring(startQuote + 1, endQuote)

            // Validate that we have a proper UUID format
            if (mbid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex())) {
                return mbid
            } else {
                throw IOException("Invalid MBID format: $mbid")
            }
        }
    }
}
