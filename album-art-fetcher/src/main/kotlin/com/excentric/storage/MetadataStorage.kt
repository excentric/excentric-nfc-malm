package com.excentric.storage

import com.excentric.model.local.AlbumMetadata
import org.springframework.stereotype.Component

@Component
class MetadataStorage {
    var albumMetadata: AlbumMetadata? = null
}
