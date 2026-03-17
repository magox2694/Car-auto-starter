plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.carbeats"
    compileSdk = 35

    val youtubeApiKey = ((project.findProperty("YOUTUBE_API_KEY") as String?) ?: "")
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

    defaultConfig {
        applicationId = "com.example.carbeats"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "YOUTUBE_API_KEY", "\"$youtubeApiKey\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    val media3Version = "1.5.1"

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.activity:activity-ktx:1.10.1")

    implementation("androidx.car.app:app:1.7.0")
    implementation("androidx.car.app:app-projected:1.7.0")

    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
}
