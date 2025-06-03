package com.excentric.malm.shell

import com.excentric.malm.client.AppleMusicApiClient
import com.excentric.malm.storage.MetadataStorage
import com.excentric.malm.util.SlotArgumentParser.parseSlotNumbers
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import org.springframework.shell.standard.ShellOption

@ShellComponent
class AppleMusicCommands(
    private val appleMusicApiClient: AppleMusicApiClient,
    private val metadataStorage: MetadataStorage,
) {
    private val logger = LoggerFactory.getLogger(AppleMusicCommands::class.java)

    @ShellMethod(key = ["am-fetch"], value = "Fetch and print the apple music ablum id")
    fun updateAppleMusicAlbumIds(
        @ShellOption(help = "Slot numbers", defaultValue = "") slots: String
    ) {
        val slotNumbers = if (slots.isEmpty())
            metadataStorage.getSlotsWithoutAppleMusicIds()
        else
            parseSlotNumbers(slots)

        val slotsMap = metadataStorage.getSlotsMap().filter { it.key in slotNumbers }

        slotsMap.forEach { (slotNumber, album) ->
            val artist = album.artist.orEmpty()
            val title = album.title

            val appleMusicId = appleMusicApiClient.getAlbumId(artist, title)
            logger.info("Found $appleMusicId for artist: ${album.artist} and title $title [https://music.apple.com/album/$appleMusicId]")
            metadataStorage.updateAppleMusicId(slotNumber, appleMusicId)
        }
    }
}
