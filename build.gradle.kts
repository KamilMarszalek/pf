import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

group = "PF"
version = "1.0-SNAPSHOT"

kotlin {
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)

                // Serialization
                implementation(libs.kotlinx.serialization.json)

                // Ktor
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.ktor.client.cio)
            }
        }

        commonTest.dependencies { implementation(kotlin("test")) }
    }
}



val fmpKey: String = run {
    val props = Properties()
    val propFile = rootProject.file("local.properties")
    if (propFile.exists()) {
        propFile.inputStream().use { props.load(it) }
    }
    props.getProperty("fmp.api.key") ?: System.getenv("FMP_API_KEY") ?: ""
}

println("Loaded FMP key length = ${fmpKey.length}")

tasks.withType<JavaExec>().configureEach {
    systemProperty("fmp.api.key", fmpKey)
}

compose.desktop {
    application {
        mainClass = "MainKt"
        jvmArgs += "-Dfmp.api.key=$fmpKey"
    }
}


