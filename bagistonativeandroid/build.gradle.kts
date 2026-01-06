plugins {
//    alias(libs.plugins.android.application)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("maven-publish")
}

android {
    namespace = "com.masilotti.demo"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true

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
        freeCompilerArgs += "-opt-in=androidx.camera.core.ExperimentalGetImage"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.hotwire.core)
    implementation(libs.hotwire.navigation.fragments)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom)) // Required for Button Component, Form Component, and Menu Component.
    implementation(libs.androidx.material3) // Required for Button Component, Form Component, and Menu Component.
    implementation(libs.review.ktx)
    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle) // Required for Review Prompt Component.
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ML Kit Barcode Scanning
    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    // material icons
    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    // Compose
    implementation("androidx.navigation:navigation-compose:2.7.6")

    // Object Detector
    implementation("com.google.mlkit:image-labeling:17.0.7")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))

    implementation("com.google.code.gson:gson:2.10.1")

    // Lifecycle Scope (for lifecycleScope)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.compose.runtime:runtime:1.6.0") // Use latest version
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0") // If using LiveData
    // OR for StateFlow:
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")

    // location
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // ML Kit Text Recognition
    implementation("com.google.mlkit:text-recognition:16.0.0")


}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
//                groupId = ""
//                artifactId = ""
//                version = "1.0.0"

            }
        }
    }
}

