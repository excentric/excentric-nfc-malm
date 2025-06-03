plugins {
    id("com.excentric.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(project(":malm-metadata"))
    implementation(libs.itext7Core)

    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(libs.kotlinTest)
}
