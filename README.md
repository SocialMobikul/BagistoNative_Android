# BagistoNative_Android

**BagistoNative_Android** provides production-ready **Hotwire Native Android
bridge components** that enable seamless communication between Java/Kotlin code and web views
in hybrid Android applications.

Bridge components allow your app to break out of the web view container and utilize native Android features such as scanners, ML search, download, reviews, and more—while still keeping the majority of your UI on the web.

This library contains reusable, real-world bridge components that can be easily plugged into any Hotwire Native Android app.

To find out more, visit: https://mobikul.com/

------------------------------------------------------------------------

## ✨ Features

-   Native Android bridge components for Hotwire Native
-   Designed for production use
-   Easy to extend and customize
-   Gradle support for easy integration

------------------------------------------------------------------------

## 📦 Components

The following bridge components are included:

-   Alert
-   Barcode Scanner
-   Button
-   Form
-   Haptic Feedback
-   Location
-   Review Prompt
-   Search
-   Share
-   Theme
-   Toast
-   Download
-   Image Search
-   Navigation Stack
 

------------------------------------------------------------------------

## 📋 Requirements

-  Android 9.0 (API Level 28) or higher
-  Java 17 or higher
-  Hotwire Native Android v1.2 or later

------------------------------------------------------------------------

## 🚀 How to Add BagistoNative_Android to Your Build

Step 1: Add the JitPack Repository to Your Build File
For Gradle:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }  // This line adds JitPack repository
    }
}
```


Step 2: Add the Dependency
For Gradle (build.gradle):

```gradle
dependencies {
    implementation 'com.github.SocialMobikul:BagistoNative_Android:Tag'  // Replace 'Tag' with the version or tag you want to use
}
```

------------------------------------------------------------------------

### Register bridge components

```kotlin
package com.mobikul.bagistoandroidrunner
import android.app.Application
import com.masilotti.demo.utils.CustomBridgeComponents
import dev.hotwire.core.bridge.KotlinXJsonConverter
import dev.hotwire.core.config.Hotwire
import dev.hotwire.navigation.config.registerBridgeComponents
class HotwireApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Hotwire.registerBridgeComponents(
            *CustomBridgeComponents.all
        )
        Hotwire.config.jsonConverter = KotlinXJsonConverter()
        Hotwire.config.applicationUserAgentPrefix = "HotwireApplication"
    }
}
```

### Configuration Example

```kotlin
package com.example.myapplication 
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import dev.hotwire.navigation.activities.HotwireActivity
import dev.hotwire.navigation.navigator.NavigatorConfiguration
import dev.hotwire.navigation.util.applyDefaultImeWindowInsets

class MainActivity : HotwireActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.main_nav_host).applyDefaultImeWindowInsets()
    }

    override fun navigatorConfigurations() = listOf(
        NavigatorConfiguration(
            name = "main",
            startLocation = "base_root_url",
            navigatorHostId = R.id.main_nav_host
        )
    )
}
```

------------------------------------------------------------------------

## 🧪 Examples

Check the Examples/ directory for a demo Android application showcasing the usage of the bridge components.

------------------------------------------------------------------------

## 🆘 Need Help?

Open an issue or start a discussion in the repository if you need help.

------------------------------------------------------------------------

## 📄 License

MIT License

------------------------------------------------------------------------

## 📌 About

BagistoNative_Android
Native Android bridge components for Hotwire Native application
