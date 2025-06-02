package com.excentric.malm.shell

import com.excentric.malm.config.MusicBrainzProperties
import org.slf4j.LoggerFactory
import org.springframework.shell.standard.ShellComponent
import org.springframework.shell.standard.ShellMethod
import kotlin.system.exitProcess

@ShellComponent
class ConfigurationCommands(
    private val musicBrainzProperties: MusicBrainzProperties,
) {
    private val logger = LoggerFactory.getLogger(ConfigurationCommands::class.java)

    @ShellMethod(key = ["q"], value = "Exit the application immediately")
    fun quit() {
        exitProcess(0)
    }

    @ShellMethod(key = ["mb-find-more-ca"], value = "Set to only show album covers from release year")
    fun moreCoverArt() {
        musicBrainzProperties.releaseYearCoversOnly = false
        logger.info("Set releaseYearCoversOnly to true - only showing album covers from release year")
    }

    @ShellMethod(key = ["mb-find-less-ca"], value = "Set to show all album covers, not just from release year")
    fun lessCoverArt() {
        musicBrainzProperties.releaseYearCoversOnly = true
        logger.info("Set releaseYearCoversOnly to false - showing all album covers")
    }
}
