# ── Kotlinx Serialization ────────────────────────────────────────────────────
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class **$$serializer {
    kotlinx.serialization.descriptors.SerialDescriptor descriptor;
}
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}

# ── Kotlinx DateTime ─────────────────────────────────────────────────────────
-keep class kotlinx.datetime.** { *; }

# ── Compose — do NOT keep all of androidx.compose (defeats R8 shrinking) ─────
# R8 handles Compose correctly without broad keep rules.
# Only add specific rules if you hit runtime crashes.

# ── Koin — keep only module declarations, not all internals ──────────────────
-keepnames class * extends org.koin.core.module.Module
-keepclassmembers class * {
    @org.koin.core.annotation.KoinInternalApi *;
}
-keepnames @org.koin.android.annotation.KoinViewModel class *
# Prevent Koin from stripping factory lambdas at runtime
-keepclassmembers class * implements org.koin.core.definition.BeanDefinition { *; }

# ── MOKO Permissions ─────────────────────────────────────────────────────────
-keep class dev.icerock.moko.permissions.** { *; }

# ── Ktor ─────────────────────────────────────────────────────────────────────
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**

# ── OkHttp (Ktor Android engine) ─────────────────────────────────────────────
-dontwarn okhttp3.**
-dontwarn okio.**

# ── Google Play Services / ML Kit ────────────────────────────────────────────
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ── AndroidX Lifecycle ───────────────────────────────────────────────────────
-keep class androidx.lifecycle.DefaultLifecycleObserver
-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <methods>;
}

# ── Kotlin Coroutines ────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── Room / DataStore ─────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── General safety ───────────────────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
