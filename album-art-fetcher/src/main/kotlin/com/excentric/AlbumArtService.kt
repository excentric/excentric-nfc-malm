package com.excentric

import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption
import org.springframework.stereotype.Service

@Service
class AlbumArtService(
    private val metadataFetcher: MusicBrainzMetadataFetcher,
    private val artDownloader: MusicBrainzAlbumArtDownloader
) {
    fun findAndDownloadAlbumArt(artist: String, album: String, outputPath: String): String {
        val mbid = metadataFetcher.searchMusicBrainz(artist, album)
        artDownloader.downloadAlbumArt(mbid, outputPath)
        return "Album art for '$album' by '$artist' downloaded to $outputPath"
    }
}

@ShellComponent
class AlbumArtCommands(private val albumArtService: AlbumArtService) {
    
    @ShellMethod(key = ["find-and-download"], value = "Find and download album art in one step")
    fun findAndDownload(
        @ShellOption(help = "Artist name") artist: String,
        @ShellOption(help = "Album name") album: String,
        @ShellOption(help = "Output file path", defaultValue = "album_cover.jpg") outputPath: String
    ): String {
        return albumArtService.findAndDownloadAlbumArt(artist, album, outputPath)
    }
}
