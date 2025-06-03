plugins {
    id("com.excentric.kotlin-jvm")
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    alias(libs.plugins.kotlinPluginSerialization)
    application
}

dependencies {
    implementation(project(":malm-metadata"))
    implementation(project(":malm-pdf"))

    implementation(libs.springBootStarter)
    implementation(libs.springBootWeb) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation(libs.springBootJetty)
    implementation(libs.springBootThymeleaf)
    implementation(libs.springShell)
    implementation(libs.kotlinxSerialization)
    implementation(libs.jacksonKotlin)

    // Testing dependencies
    testImplementation(libs.springBootTest)
    testImplementation(libs.junitJupiterApi)
    testRuntimeOnly(libs.junitJupiterEngine)
}

application {
    mainClass = "com.excentric.malm.MalmApplicationKt"
}
