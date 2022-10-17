plugins {
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    buildToolsVersion = "33.0.0"
    compileSdk = 33

    defaultConfig {
        namespace = "app"
        applicationId = "com.github.crow_misia.zxing_android"
        minSdk = 23
        targetSdk = 33
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    lint {
        textReport = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    kotlin {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
            apiVersion = "1.7"
            languageVersion = "1.7"
        }
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(project(":zxing-android"))

    implementation(Kotlin.stdlib)
    implementation(KotlinX.coroutines.android)

    implementation(AndroidX.core.ktx)

    // App compat and UI things
    implementation(AndroidX.activity.ktx)
    implementation(AndroidX.appCompat)
    implementation(AndroidX.fragment.ktx)
    implementation(AndroidX.lifecycle.runtime.ktx)
    implementation(AndroidX.constraintLayout)
    implementation(AndroidX.window)

    // Navigation library
    implementation(AndroidX.navigation.fragmentKtx)
    implementation(AndroidX.navigation.uiKtx)

    // CameraX
    implementation(AndroidX.camera.core)
    implementation(AndroidX.camera.camera2)
    implementation(AndroidX.camera.lifecycle)
    implementation(AndroidX.camera.view)

    // Unit testing
    testImplementation(AndroidX.test.ext.junit)
    testImplementation(AndroidX.test.rules)
    testImplementation(AndroidX.test.runner)
    testImplementation(AndroidX.test.espresso.core)
    testImplementation(Testing.junit4)
    testImplementation(Testing.robolectric)

    // Instrumented testing
    androidTestImplementation(AndroidX.test.ext.junit)
    androidTestImplementation(AndroidX.test.core)
    androidTestImplementation(AndroidX.test.rules)
    androidTestImplementation(AndroidX.test.runner)
    androidTestImplementation(AndroidX.test.espresso.core)
    androidTestImplementation(Testing.junit4)
}