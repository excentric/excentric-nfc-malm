package com.excentric.malm.controller

import com.excentric.malm.storage.MetadataStorage
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.io.FileNotFoundException

@Controller
class CoverArtController(
    private val metadataStorage: MetadataStorage
) {

    @GetMapping("/ca/{slot}")
    fun getCoverArtThumbnails(@PathVariable slot: Int, model: Model): String {
        val coverArtFiles = metadataStorage.getPotentialCoverArtsFiles(slot)
        val albumMetadata = metadataStorage.getSlotsMap()[slot]

        model.addAttribute("slot", slot)
        model.addAttribute("imageCount", coverArtFiles.size)
        model.addAttribute("album", albumMetadata)

        return "coverArtThumbnails"
    }

    @GetMapping("/ca/{slot}/image/{index}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getCoverArtImage(@PathVariable slot: Int, @PathVariable index: Int): ResponseEntity<ByteArray> {
        val coverArtFiles = metadataStorage.getPotentialCoverArtsFiles(slot)

        if (index < 0 || index >= coverArtFiles.size) {
            return ResponseEntity.notFound().build()
        }

        try {
            val imageBytes = coverArtFiles[index].readBytes()
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes)
        } catch (e: FileNotFoundException) {
            return ResponseEntity.notFound().build()
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }
}
