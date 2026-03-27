# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /sdk/tools/proguard/proguard-android.txt

# Keep Compose classes
-keep class androidx.compose.** { *; }

# Keep model classes
-keep class com.example.xiuperlerbeads.domain.model.** { *; }
