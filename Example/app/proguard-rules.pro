# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Bagisto Native SDK classes
-keep class com.mobikul.bagisto.** { *; }

# Keep Hotwire classes
-keep class dev.hotwire.** { *; }

# Keep ML Kit classes
-keep class com.google.mlkit.** { *; }

# Keep Firebase classes
-keep class com.google.firebase.** { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
