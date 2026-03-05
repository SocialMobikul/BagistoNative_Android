# Keep Hotwire classes and annotated fragments
-keep class dev.hotwire.** { *; }
-keep @dev.hotwire.navigation.destinations.HotwireDestinationDeepLink class * { *; }

# Keep our SDK Components, Fragments, and Initializers
-keep class com.mobikul.bagisto.components.** { *; }
-keep class com.mobikul.bagisto.fragments.** { *; }
-keep class com.mobikul.bagisto.utils.CustomBridgeComponents { *; }
-keep class com.mobikul.bagisto.utils.BagistoSdkInitializer { *; }
-keep class com.mobikul.bagisto.utils.ThemeStateHolder { *; }
-keep class com.mobikul.bagisto.utils.AppSharedPreference { *; }

# Needed for Serialization and JSON mapping via Hotwire
-keepclassmembers class com.mobikul.bagisto.components.** {
    <fields>;
    <init>();
}

# Ignore missing nullness annotations from external library (ml kit / play core)
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite
