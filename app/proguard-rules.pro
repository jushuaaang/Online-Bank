# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Hide the original source file name
-renamesourcefileattribute SourceFile

# Preserve annotations (needed for libraries like Room and Retrofit)
-keepattributes *Annotation*

# Prevent obfuscation of Kotlin metadata
-keep class kotlin.Metadata { *; }

# Keep R8 and D8 classes to avoid runtime issues
-keep class com.android.tools.r8.** { *; }

# Keep Glide models and generated API
-keep class com.bumptech.glide.** { *; }
-keep public class * implements com.bumptech.glide.module.GlideModule

# Keep Room database entities and Dao classes
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep Retrofit and OkHttp classes
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-dontwarn okio.**

# Keep Gson classes
-keep class com.google.gson.** { *; }

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Additional rules for debugging or specific use cases can be added below:
# Uncomment to keep all public classes and methods for debugging purposes
#-keep public class * {
#    public *;
#}

# Uncomment to skip obfuscation for specific packages or classes
#-keep class com.example.myapplication.** { *; }