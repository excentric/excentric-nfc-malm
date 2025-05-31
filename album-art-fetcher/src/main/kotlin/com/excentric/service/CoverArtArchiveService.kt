package com.excentric.service

import com.excentric.client.CoverArtArchiveClient
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Component

@ShellComponent
@Component
class CoverArtArchiveService(
    private val coverArtArchiveClient: CoverArtArchiveClient,
) {
    private val logger = LoggerFactory.getLogger(CoverArtArchiveService::class.java)

    @ShellMethod(key = ["download-art"], value = "Download album art using MusicBrainz ID")
    fun downloadArt(
        @ShellOption(help = "MusicBrainz ID") mbid: String,
        @ShellOption(help = "Output file path", defaultValue = "album_cover.jpg") outputPath: String
    ): String {
        coverArtArchiveClient.downloadAlbumArt(mbid, outputPath)
        return "Album art downloaded to $outputPath"
    }
}
