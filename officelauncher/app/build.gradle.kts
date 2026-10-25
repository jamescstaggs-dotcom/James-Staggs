plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.stanfordremodeling.officelauncher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.stanfordremodeling.officelauncher"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Deliberately no AppCompat/Leanback — plain framework views keep the APK tiny
    // and avoid any theme conflicts on TCL's Google TV build.
}
