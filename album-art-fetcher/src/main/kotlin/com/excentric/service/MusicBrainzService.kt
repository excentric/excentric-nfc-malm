package com.excentric.service

import com.excentric.client.MusicBrainzApiClient
import com.excentric.model.storage.AlbumMetadata
import com.excentric.util.ConsoleColors.greenOrRed
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MusicBrainzService(
    private val musicBrainzApiClient: MusicBrainzApiClient,
) {
    private val logger = LoggerFactory.getLogger(MusicBrainzService::class.java)

    fun searchMusicBrainz(artistQuery: String, albumQuery: String): AlbumMetadata {
        val albumResults = musicBrainzApiClient.searchAlbums(artistQuery, albumQuery)
        val firstResult = albumResults.releases.first()

        val mbid = firstResult.id
        val album = firstResult.title
        val artist = firstResult.getFirstArtistName()
        val year = albumResults.findEarliestReleaseYear(artist, album)

        logger.info("ID: ${greenOrRed(mbid)}")
        logger.info("Album: ${greenOrRed(album)}")
        logger.info("Artist: ${greenOrRed(artist)}")
        logger.info("Year: ${greenOrRed(year)}")

        return AlbumMetadata(mbid, album, artist, year)
    }
}
