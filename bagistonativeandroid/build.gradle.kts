plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("org.jetbrains.kotlin.plugin.serialization")
    id("maven-publish")
}

android {
    namespace = "com.mobikul.bagisto"
    compileSdk = 35

    defaultConfig {
        minSdk = 28

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        
        // Only include arm64-v8a for better 16KB page size compatibility
        ndk {
            abiFilters += "arm64-v8a"
        }
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

    packaging {
        // Enable legacy packaging to extract native libraries to filesystem
        // This is required for 16KB page size compatibility when dependencies
        // (like ML Kit) require native library extraction
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    api(libs.hotwire.core)
    api(libs.hotwire.navigation.fragments)
    api("dev.hotwire:activity:1.2.3")
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.material3)
    implementation(libs.review.ktx)
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.lifecycle)
    debugImplementation(libs.androidx.ui.tooling)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("com.google.mlkit:barcode-scanning:17.2.0")

    implementation("androidx.compose.material:material-icons-extended:1.6.1")

    implementation("androidx.navigation:navigation-compose:2.7.6")

    implementation("com.google.mlkit:object-detection:17.0.0")
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    implementation(platform("com.google.firebase:firebase-bom:33.12.0"))

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    implementation("androidx.compose.runtime:runtime:1.6.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.6.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.0")

    implementation("com.google.android.gms:play-services-location:21.0.1")

    implementation("com.google.mlkit:text-recognition:16.0.0")

    implementation("androidx.startup:startup-runtime:1.1.1")


}

publishing {
    publications {
        register<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])

            }
        }
    }
}
