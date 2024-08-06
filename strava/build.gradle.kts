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
    api(libs.rxjava2)
    api(libs.retrofit2)
    api(libs.retrofit2.adapter.rxjava2)
    api(libs.retrofit2.converter.serialization)
    api(libs.kotlinx.serialization)

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