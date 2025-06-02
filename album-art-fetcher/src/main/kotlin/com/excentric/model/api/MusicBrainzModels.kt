package com.excentric.model.api

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class MusicBrainzReleasesModel(
    val releases: List<AlbumReleaseModel> = emptyList()
) : MusicBrainzResultsModel<AlbumReleaseModel> {
    override val results: List<AlbumReleaseModel>
        get() = releases

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

interface MusicBrainzResultsModel<T> {
    val results: List<T>
}

data class MusicBrainzReleaseGroupsModel(
    @JsonProperty("release-groups")
    val releaseGroups: List<AlbumReleaseGroupModel> = emptyList()
) : MusicBrainzResultsModel<AlbumReleaseGroupModel> {
    override val results: List<AlbumReleaseGroupModel>
        get() = releaseGroups
}

@Serializable
data class AlbumReleaseGroupModel(
    val status: String? = null,
    @JsonProperty("artist-credit")
    val artistCredit: List<ArtistCreditModel> = emptyList(),
    val releases: List<AlbumReleaseModel>? = null,
    val id: String,
    val title: String,
    val score: Int? = null,
    @JsonProperty("first-release-date")
    val firstReleaseDate: String? = null,
    @JsonProperty("primary-type")
    val primaryType: String? = null,
    @JsonProperty("secondary-types")
    val secondaryTypes: List<String>? = null,
) {
    fun getYear(): Int? {
        if (firstReleaseDate.isNullOrBlank() || firstReleaseDate.length < 4) return null
        return firstReleaseDate.substring(0, 4).toIntOrNull()
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
    @JsonProperty("release-group")
    val releaseGroupModel: AlbumReleaseGroupModel? = null,
    val date: String? = null
) {
    fun getFirstArtistName() = artistCredit.firstOrNull()?.name

    fun getYear(): Int? {
        if (date.isNullOrBlank() || date.length < 4) return null
        return date.substring(0, 4).toIntOrNull()
    }
}

enum class ReleaseGroupPrimaryType(val value: String) {
    ALBUM("Album"),
    SINGLE("Single"),
    EP("EP"),
    BROADCAST("Broadcast"),
    OTHER("Other"),
    ;

    override fun toString(): String {
        return value
    }
}

enum class ReleaseGroupSecondaryType(val value: String) {
    COMPILATION("Compilation"),
    SOUNDTRACK("Soundtrack"),
    SPOKENWORD("Spokenword"),
    INTERVIEW("Interview"),
    AUDIO_DRAMA("Audio drama"),
    LIVE("Live"),
    REMIX("Remix"),
    DJ_MIX("DJ-mix"),
    MIXTAPE_STREET("Mixtape/Street"),
    DEMO("Demo"),
    FIELD_RECORDING("Field recording")
    ;

    override fun toString(): String {
        return value
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
    val name: String,
    val score: Int? = null
)

data class MusicBrainzArtistsModel(
    val artists: List<ArtistModel> = emptyList()
) : MusicBrainzResultsModel<ArtistModel> {
    override val results: List<ArtistModel>
        get() = artists
}
