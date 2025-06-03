package com.excentric.malm.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "music-album-label-maker.cover-art-archive")
data class CoverArtProperties(
    val url: String = "https://coverartarchive.org/",
    val threadCount: Int = 3
)
