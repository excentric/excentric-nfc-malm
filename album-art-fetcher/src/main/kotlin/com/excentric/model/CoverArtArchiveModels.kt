package com.excentric.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable

@Serializable
data class CoverArtResponseModel(
    val images: List<CoverArtImageModel> = emptyList(),
    val release: String? = null
)

@Serializable
data class CoverArtImageModel(
    val id: String,
    val front: Boolean = false,
    val back: Boolean = false,
    val approved: Boolean = false,
    val image: String, // URL to the full-size image
    val thumbnails: ThumbnailsModel? = null
)

@Serializable
data class ThumbnailsModel(
    val small: String? = null,
    val large: String? = null,
    @JsonProperty("250")
    val size250: String? = null,
    @JsonProperty("500")
    val size500: String? = null,
    @JsonProperty("1200")
    val size1200: String? = null
)
