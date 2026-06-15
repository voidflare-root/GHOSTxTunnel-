# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\tools\adt-bundle-windows-x86_64-20131030\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

-repackageclasses ''
-allowaccessmodification
-adaptclassstrings
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# Android entry points and callbacks are kept by the Android Gradle plugin, but
# these extra rules protect libraries that rely on reflection or parcelables.
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.Application

-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

-keepnames class com.trilead.ssh2.crypto.cipher.AES
-keepnames class com.trilead.ssh2.crypto.cipher.BlowFish
-keepnames class com.trilead.ssh2.crypto.cipher.DESede

-keep class net.i2p.crypto.eddsa.** { *; }
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**
-dontwarn javax.annotation.**

# Keep only the method names that can be referenced from XML onClick handlers.
-keepclassmembers class * {
    public void *(android.view.View);
}

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
