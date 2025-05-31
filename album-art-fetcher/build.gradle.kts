plugins {
    id("com.excentric.kotlin-jvm")
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    application
}

dependencies {
    implementation(libs.springBootStarter)
    implementation(libs.springShell)
}

application {
    mainClass = "com.excentric.MusicBrainzMetadataFetcherKt"
}
