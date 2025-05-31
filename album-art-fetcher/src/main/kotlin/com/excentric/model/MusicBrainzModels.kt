package com.excentric.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class MusicBrainzResponseModel(
    val releases: List<AlbumReleaseModel> = emptyList()
) {
    fun findEarliestReleaseYear(artistName: String?, albumName: String): Int? {
        return releases.filter { it.title == albumName && it.artistCredit.firstOrNull()?.name == artistName && it.getYear() != null }.sortedBy { it.getYear() }.first().getYear()
    }
}

@Serializable
data class AlbumReleaseModel(
    val id: String,
    val title: String,
    val status: String? = null,
    @JsonProperty("artist-credit")
    val artistCredit: List<ArtistCreditModel> = emptyList(),
    val date: String? = null
) {

    fun getFirstArtistName() = artistCredit.firstOrNull()?.name

    fun getYear(): Int? {
        if (date.isNullOrBlank() || date.length < 4) return null
        return date.substring(0, 4).toIntOrNull()
    }
}

@Serializable
data class ArtistCreditModel(
    val artist: ArtistModel,
    val name: String
)

@Serializable
data class ArtistModel(
    val id: String,
    val name: String
)
