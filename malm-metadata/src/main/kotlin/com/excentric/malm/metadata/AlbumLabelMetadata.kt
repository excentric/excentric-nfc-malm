package com.excentric.malm.metadata

import java.io.File

data class AlbumLabelMetadata(
    val slot: Int,
    val title: String,
    val artist: String?,
    val year: Int?,
    val albumArt: File,
)
