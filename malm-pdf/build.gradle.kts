plugins {
    id("com.excentric.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    application
}

application {
    mainClass.set("com.excentric.utils.com.excentric.malm.pdf.PdfModifierKt")
}

dependencies {
    implementation("com.itextpdf:itext7-core:7.2.5")

    implementation(libs.bundles.kotlinxEcosystem)
    testImplementation(libs.kotlinTest)
}
