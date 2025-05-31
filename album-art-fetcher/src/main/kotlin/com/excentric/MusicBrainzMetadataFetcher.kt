package com.excentric

import com.excentric.client.MusicBrainzApiClient
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component
import java.io.IOException

@ShellComponent
@Component
class MusicBrainzMetadataFetcher(
    private val musicBrainzApiClient: MusicBrainzApiClient
) {
    @ShellMethod(key = ["find-mbid"], value = "Find MusicBrainz ID for an album")
    fun findMbid(
        @ShellOption(help = "Artist name") artist: String,
        @ShellOption(help = "Album name") album: String
    ): String {
        println("Searching for album: $album by artist: $artist")
        val mbid = findMbidByAlbumInfo(artist, album)
        println("Found MBID: $mbid")
        return mbid
    }

    fun findMbidByAlbumInfo(artist: String, album: String): String {
        val albumResults = musicBrainzApiClient.searchAlbums(artist, album)

        val mbid = albumResults.releases.first().id

        // Validate that we have a proper UUID format
        if (mbid.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}".toRegex())) {
            return mbid
        } else {
            throw IOException("Invalid MBID format: $mbid")
        }
    }
}
