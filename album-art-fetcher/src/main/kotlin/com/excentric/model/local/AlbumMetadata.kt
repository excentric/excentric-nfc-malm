package com.excentric.model.local

data class AlbumMetadata(
    val mbid: String,
    val album: String,
    val artist: String?,
    val year: Int?,
) {
    var coverArt: String? = null
}
