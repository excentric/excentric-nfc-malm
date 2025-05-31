package com.excentric.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class MusicBrainzResponseModel(
    val releases: List<AlbumReleaseModel> = emptyList()
)

@Serializable
data class AlbumReleaseModel(
    val id: String,
    val title: String,
    val status: String? = null,
    @JsonProperty("artist-credit")
    val artistCredit: List<ArtistCreditModel> = emptyList(),
    val date: String? = null
)

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
