@file:Suppress("UnstableApiUsage")

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    namespace = AndroidConfig.APP_ID
    compileSdk = AndroidConfig.TARGET_SDK

    defaultConfig {
        applicationId = AndroidConfig.APP_ID
        targetSdk = AndroidConfig.TARGET_SDK
        versionCode = AndroidConfig.VERSION_CODE
        versionName = AndroidConfig.VERSION

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }

    packagingOptions {
        resources {
            excludes += "/META-INF/*"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    AndroidXDependency.run {
        implementation(CORE_KTX)
        implementation(CONSTRAINTLAYOUT)
        implementation(ACTIVITY_KTX)
        implementation(FRAGMENT_KTX)
        implementation(LIFECYCLE_LIVEDATA_KTX)
        implementation(RECYCLERVIEW)
    }

    implementation(BasicDependency.MATERIAL)

    TestDependency.run {
        testImplementation(JUNIT_JUPITER_API)
        testRuntimeOnly(JUNIT_JUPITER_ENGINE)
        testImplementation(JUNIT_JUPITER_PARAMS)
        testImplementation(ASSERTJ_CORE)
        testImplementation(MOCKK)
    }
}
