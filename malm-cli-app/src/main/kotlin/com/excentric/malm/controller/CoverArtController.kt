package com.excentric.malm.controller

import com.excentric.malm.storage.ImageMetadata
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
        val selectedCoverArtIndex = metadataStorage.getSelectedCoverArtIndex(slot)

        // Create a map of image index to image metadata
        val imageMetadataMap = coverArtFiles.associate { file ->
            val index = file.nameWithoutExtension.toInt()
            val metadata = metadataStorage.getImageMetadata(file)
            index to metadata
        }

        model.addAttribute("slot", slot)
        model.addAttribute("album", albumMetadata)
        model.addAttribute("imageIndexes", coverArtFiles.map { it.nameWithoutExtension.toInt() })
        model.addAttribute("imageMetadataMap", imageMetadataMap)
        model.addAttribute("selectedCoverArtIndex", selectedCoverArtIndex)

        return "coverArtThumbnails"
    }

    @GetMapping("/ca/{slot}/image/{index}", produces = [MediaType.IMAGE_JPEG_VALUE])
    fun getCoverArtImage(@PathVariable slot: Int, @PathVariable index: Int): ResponseEntity<ByteArray> {
        val coverArtFile = metadataStorage.getPotentialCoverArtFile(slot, index)

        try {
            val imageBytes = coverArtFile.readBytes()
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(imageBytes)
        } catch (e: FileNotFoundException) {
            return ResponseEntity.notFound().build()
        } catch (e: Exception) {
            return ResponseEntity.internalServerError().build()
        }
    }

    @GetMapping("/ca/{slot}/select/{index}")
    fun selectCoverArt(@PathVariable slot: Int, @PathVariable index: Int, model: Model): String {
        metadataStorage.selectCoverArt(slot, index)
        model.addAttribute("selectedCoverArtIndex", index)
        return "redirect:/ca/$slot"
    }
}
