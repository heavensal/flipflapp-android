# Keep attributes useful for crash symbolication in Play Console.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization (models + generated serializers).
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    *** INSTANCE;
}
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Google Places / Play Services reflection entry points (consumer rules usually cover this;
# keep a light safety net for Autocomplete / PlacesClient).
-keep class com.google.android.libraries.places.** { *; }
-dontwarn com.google.android.libraries.places.**
