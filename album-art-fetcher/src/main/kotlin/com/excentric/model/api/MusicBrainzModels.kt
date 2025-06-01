package com.excentric.model.api

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class MusicBrainzResponseModel(
    val releases: List<AlbumReleaseModel> = emptyList()
) {
    fun findEarliestReleaseYear(artistName: String?, albumName: String): Int? {
        val matchingReleases = findReleasesByAlbumAndArtist(albumName, artistName)
        val sortedReleases = matchingReleases.filter { it.getYear() != null }.sortedBy { it.getYear() }
        if (sortedReleases.isEmpty()) {
            return null
        }
        return sortedReleases.first().getYear()
    }

    fun findReleasesByAlbumAndArtist(albumName: String, artistName: String?): List<AlbumReleaseModel> {
        val matchingReleases = releases.filter { it.title == albumName && it.artistCredit.firstOrNull()?.name == artistName }
        val officialReleases = matchingReleases.filter { it.status == "Official" }
        return officialReleases
    }
}

@Serializable
data class AlbumReleaseModel(
    val id: String,
    val title: String,
    val status: String? = null,
    val score: Int? = null,
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
