# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keep class kotlinx.datetime.** { *; }

# Keep generated serializers
-keepclassmembers class **$serializer { *; }

# Prevent stripping of @Serializable
-keepattributes *Annotation*

# Compose (recommended set)
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }

# Koin
-keep class org.koin.** { *; }

# MOKO permissions
-keep class dev.icerock.moko.permissions.** { *; }
