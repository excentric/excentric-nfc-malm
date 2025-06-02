package com.excentric.malm.metadata

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AlbumMetadata(
    val mbids: List<String>,
    val album: String,
    val artist: String?,
    val year: Int?,
)
