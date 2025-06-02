plugins {
    id("com.excentric.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation("com.itextpdf:itext7-core:7.2.5")

    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(libs.kotlinTest)
}
