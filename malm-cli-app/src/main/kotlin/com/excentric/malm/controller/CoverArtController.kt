package com.excentric.malm.controller

import com.excentric.malm.storage.MetadataStorage
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import java.io.File
import java.util.Base64
import org.springframework.http.MediaType

@Controller
class CoverArtController(
    private val metadataStorage: MetadataStorage
) {

    data class EncodedImage(val base64Data: String)

    @GetMapping("/ca/{slot}")
    fun getCoverArtThumbnails(@PathVariable slot: Int, model: Model): String {
        val coverArtFiles = metadataStorage.getPotentialCoverArtsFiles(slot)

        // Pre-encode the images to Base64
        val encodedImages = coverArtFiles.map { file ->
            try {
                EncodedImage(Base64.getEncoder().encodeToString(file.readBytes()))
            } catch (e: Exception) {
                // Return an empty string if there's an error reading the file
                EncodedImage("")
            }
        }

        model.addAttribute("slot", slot)
        model.addAttribute("encodedImages", encodedImages)

        return "coverArtThumbnails"
    }
}
