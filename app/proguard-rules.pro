# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# --------------------------------------------
# Reglas específicas para Firebase y modelos
# --------------------------------------------

# 1. Mantener todas las clases y miembros de tus modelos de datos
-keep class com.cnunez.docufast.common.dataclass.** { *; }
-keepclassmembers class com.cnunez.docufast.common.dataclass.** {
    *;
}

# 2. Constructores sin argumentos (requeridos por Firebase)
-keepclassmembers class com.cnunez.docufast.common.dataclass.File** {
    public <init>();
}

# 3. Clases Parcelable (para Android)
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
    public <init>();
}

# 4. Reglas para Firebase Database
-keep class com.google.firebase.database.** { *; }
-keepclasseswithmembers class * {
    @com.google.firebase.database.Exclude public *;
}

# 5. Evitar que ProGuard optimice atributos críticos
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*