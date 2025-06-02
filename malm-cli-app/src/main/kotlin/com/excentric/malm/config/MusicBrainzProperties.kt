package com.excentric.malm.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "music-album-label-maker.musicbrainz")
data class MusicBrainzProperties(
    val api: MusicBrainzApiProperties = MusicBrainzApiProperties(),
    var releaseYearCoversOnly: Boolean = true
) {
    data class MusicBrainzApiProperties(
        val url: String = "https://musicbrainz.org/ws/2/"
    )
}
