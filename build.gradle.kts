import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.plugin.KaptExtension

plugins {
    kotlin("multiplatform")
    kotlin("kapt")
    kotlin("plugin.serialization")
    id("org.jetbrains.compose")
}

group = "me.ghostbear"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://m2.dv8tion.net/releases")
    maven("https://jitpack.io")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

                val daggerVersion = "2.42"

                implementation("com.google.dagger:dagger:$daggerVersion")
                configurations.getByName("kapt").dependencies.add(
                    org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency(
                        "com.google.dagger",
                        "dagger-compiler",
                        daggerVersion
                    )
                )

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

                val ktorVersion = "2.0.1"

                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")

                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

                implementation("io.ktor:ktor-client-websockets:$ktorVersion")

                implementation("com.sedmelluq:lavaplayer:1.3.78")

                implementation("com.github.Vatuu:discord-rpc:1.6.2")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Scialytic"
            packageVersion = "1.0.0"
        }
    }
}
