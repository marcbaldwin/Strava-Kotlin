import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFramework

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}

group = "xyz.marcb.strava"
version = "3.0.0"

val GITHUB_USER: String by project
val GITHUB_TOKEN: String by project

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        publishLibraryVariantsGroupedByFlavor = true
    }

    jvmToolchain(17)

    val xcf = XCFramework("Strava")
    val iosTargets = listOf(iosX64(), iosArm64(), iosSimulatorArm64())

    iosTargets.forEach {
        it.binaries.framework {
            baseName = "Strava"
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinx.coroutines.core)
            api(libs.kotlinx.datetime)
            api(libs.kotlinx.serialization)
            api(libs.ktor.client.core)
            api(libs.ktor.client.content.negotiation)
            api(libs.ktor.client.serialization)
            api(libs.ktor.client.json)
            api(libs.ktor.client.logging)
        }
        androidMain.dependencies {
            api(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            api(libs.ktor.client.darwin)
        }
    }

    androidTarget {
        compilerOptions {
            allWarningsAsErrors = false
        }
    }
}

android {
    namespace = "xyz.marcb.strava"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("release") {
            groupId = "com.github.marcbaldwin"
            artifactId = "Strava-Kotlin"
            from(components["kotlin"])
        }
    }

    repositories {
        maven {
            setUrl("https://maven.pkg.github.com/marcbaldwin/Strava-Kotlin")
            credentials {
                username = GITHUB_USER
                password = GITHUB_TOKEN
            }
        }
    }
}
