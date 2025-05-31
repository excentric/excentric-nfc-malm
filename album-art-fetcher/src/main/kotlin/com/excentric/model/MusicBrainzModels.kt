package com.excentric.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model classes for MusicBrainz API responses
 */

@Serializable
data class MusicBrainzResponse(
    val releases: List<Release> = emptyList()
)

@Serializable
data class Release(
    val id: String,
    val title: String,
    val status: String? = null,
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit> = emptyList(),
    val date: String? = null
)

@Serializable
data class ArtistCredit(
    val artist: Artist,
    val name: String
)

@Serializable
data class Artist(
    val id: String,
    val name: String
)
