package com.excentric.malm.metadata

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlbumMetadata(
    val mbids: List<String>,
    val title: String,
    val artist: String?,
    val year: Int?,
) {
    var appleMusicAlbumId: String? = null
}
