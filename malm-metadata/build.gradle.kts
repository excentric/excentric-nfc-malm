plugins {
    id("com.excentric.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
}

dependencies {
    implementation(libs.bundles.kotlinxEcosystem)
    implementation(libs.jacksonKotlin)

    testImplementation(libs.kotlinTest)
}
