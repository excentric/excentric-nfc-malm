dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
    }
}

plugins {
    // Use the Foojay Toolchains plugin to automatically download JDKs required by subprojects.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

include(":malm-cli-app")
include(":malm-metadata")
include(":malm-pdf")

include(":malm-nfc")

rootProject.name = "excentric-nfc-sonos"
