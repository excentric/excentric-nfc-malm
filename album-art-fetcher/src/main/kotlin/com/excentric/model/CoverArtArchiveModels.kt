package com.excentric.model

import kotlinx.serialization.SerialName
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
    @SerialName("250")
    val size250: String? = null,
    @SerialName("500")
    val size500: String? = null,
    @SerialName("1200")
    val size1200: String? = null
)
