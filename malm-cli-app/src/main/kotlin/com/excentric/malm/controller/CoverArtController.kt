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
    
    @GetMapping("/ca/{slot}")
    @ResponseBody
    fun getCoverArtThumbnails(@PathVariable slot: Int): String {
        val coverArtFiles = metadataStorage.getPotentialCoverArtsFiles(slot)
        
        if (coverArtFiles.isEmpty()) {
            return "<html><body><h1>No cover art found for slot $slot</h1></body></html>"
        }
        
        val htmlBuilder = StringBuilder()
        htmlBuilder.append("<html><head><style>")
        htmlBuilder.append("body { font-family: Arial, sans-serif; margin: 20px; }")
        htmlBuilder.append(".thumbnail-container { display: flex; flex-wrap: wrap; gap: 10px; }")
        htmlBuilder.append(".thumbnail { width: 250px; height: 250px; object-fit: contain; border: 1px solid #ddd; }")
        htmlBuilder.append("</style></head><body>")
        htmlBuilder.append("<h1>Cover Art for Slot $slot</h1>")
        htmlBuilder.append("<div class=\"thumbnail-container\">")
        
        coverArtFiles.forEach { file ->
            val base64Image = Base64.getEncoder().encodeToString(file.readBytes())
            htmlBuilder.append("<img class=\"thumbnail\" src=\"data:image/jpeg;base64,$base64Image\" />")
        }
        
        htmlBuilder.append("</div></body></html>")
        
        return htmlBuilder.toString()
    }
}
