-keep class dev.hotwire.** { *; }
-keep @dev.hotwire.navigation.destinations.HotwireDestinationDeepLink class * { *; }

-keep class com.mobikul.bagisto.components.** { *; }
-keep class com.mobikul.bagisto.fragments.** { *; }
-keep class com.mobikul.bagisto.utils.CustomBridgeComponents { *; }
-keep class com.mobikul.bagisto.utils.BagistoSdkInitializer { *; }
-keep class com.mobikul.bagisto.utils.ThemeStateHolder { *; }
-keep class com.mobikul.bagisto.utils.AppSharedPreference { *; }

-keepclassmembers class com.mobikul.bagisto.components.** {
    <fields>;
    <init>();
}

-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite
