plugins {
    id("com.excentric.kotlin-jvm")
    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
    alias(libs.plugins.kotlinPluginSerialization)
    application
    id("org.graalvm.buildtools.native") version "0.9.28"
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

graalvmNative {
    binaries {
        named("main") {
            imageName = "malm-cli-app"
            mainClass = "com.excentric.malm.MalmApplicationKt"
            buildArgs.add("--verbose")
            buildArgs.add("--no-fallback")
            buildArgs.add("-H:+ReportExceptionStackTraces")
            // Enable reflection for Spring Boot
            buildArgs.add("-H:+AddAllCharsets")
            buildArgs.add("-H:ReflectionConfigurationFiles=${projectDir}/src/main/resources/META-INF/native-image/reflect-config.json")
            buildArgs.add("-H:ResourceConfigurationFiles=${projectDir}/src/main/resources/META-INF/native-image/resource-config.json")
        }
    }
}
