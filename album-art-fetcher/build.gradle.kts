plugins {
    id("com.excentric.kotlin-jvm")
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    alias(libs.plugins.kotlinPluginSerialization)
    application
}

dependencies {
    implementation(libs.springBootStarter)
    implementation(libs.springBootWeb)
    implementation(libs.springShell)
    implementation(libs.kotlinxSerialization)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Testing dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.1")
}

application {
    mainClass = "com.excentric.MusicBrainzApplicationKt"
}
