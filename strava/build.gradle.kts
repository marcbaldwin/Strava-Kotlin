plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    id("maven-publish")
}


group = "xyz.marcb.strava"

android {
    namespace = "xyz.marcb.strava"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
        allWarningsAsErrors = true
    }
}

dependencies {
    api(libs.kotlinx.serialization)
    api(libs.kotlinx.coroutines.core)
    api(libs.ktor.client.core)
    api(libs.ktor.client.serialization)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.json)
    api(libs.ktor.client.logging)
    api(libs.ktor.client.okhttp)

    // Testing
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito.kotlin)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.marcbaldwin"
            artifactId = "Strava-Kotlin"
            afterEvaluate {
                from(components["release"])
            }
        }
    }
}