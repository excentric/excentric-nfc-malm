package com.excentric.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Model classes for Cover Art Archive API responses
 */

@Serializable
data class CoverArtResponse(
    val images: List<CoverArtImage> = emptyList(),
    val release: String? = null
)

@Serializable
data class CoverArtImage(
    val id: String,
    val front: Boolean = false,
    val back: Boolean = false,
    val approved: Boolean = false,
    val image: String, // URL to the full-size image
    val thumbnails: Thumbnails? = null
)

@Serializable
data class Thumbnails(
    val small: String? = null,
    val large: String? = null,
    @SerialName("250")
    val size250: String? = null,
    @SerialName("500")
    val size500: String? = null,
    @SerialName("1200")
    val size1200: String? = null
)
