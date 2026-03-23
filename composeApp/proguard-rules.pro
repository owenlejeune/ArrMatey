# Prevent renaming of classes and members (Required for IzzyOnDroid/F-Droid)
-dontobfuscate

# Keeps the attributes that help keep the build deterministic
-keepattributes SourceFile,LineNumberTable

# Keep the Moko Resources generated classes exactly as they are
-keep class com.dnfapps.arrmatey.shared.MR** { *; }
-keep class dev.icerock.moko.resources.** { *; }

# Prevent R8 from reordering instructions in the static initializers
-keepclassmembers class com.dnfapps.arrmatey.shared.MR** {
    <clinit>();
}

# WebView
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebChromeClient {
    public void *(android.webkit.WebView, java.lang.String);
}

# Keep WebView JavaScript interfaces
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}