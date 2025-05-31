package com.excentric.model.storage

data class AlbumMetadata(
    val mbid: String,
    val album: String,
    val artist: String?,
    val year: Int?,
) {
    var coverArt: String? = null
}
