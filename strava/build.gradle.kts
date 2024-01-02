plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    id("maven-publish")
}

android {
    namespace = "xyz.marcb.strava"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
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
    api(libs.retrofit2.converter.moshi)

    api(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    // Testing
    testImplementation(libs.kotlin.test.junit)
    testImplementation(libs.mockito.kotlin)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["release"])
                groupId = "com.github.marcbaldwin"
                artifactId = "Strava-Kotlin"
            }
        }
    }
}