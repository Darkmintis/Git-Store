# === Kotlin ===
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-keepclassmembers class kotlin.** { *; }
-keep class kotlin.time.** { *; }
-keepclassmembers class kotlin.time.** { *; }
-dontwarn kotlin.time.**

# === Coroutines ===
-keep class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }

# === Ktor ===
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-keepnames class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn java.lang.management.**

# === OkHttp ===
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keepclassmembers class okhttp3.** { *; }
-keepnames class okhttp3.** { *; }
-dontwarn okhttp3.**

# === Okio ===
-keep class okio.** { *; }
-keepclassmembers class okio.** { *; }
-keepnames class okio.** { *; }
-dontwarn okio.**

# === Network Stack ===
-keep class java.net.** { *; }
-keep class javax.net.** { *; }
-keep class sun.security.ssl.** { *; }
-keepclassmembers class java.net.** { *; }
-keepclassmembers class javax.net.** { *; }
-keep class java.net.InetAddress { *; }
-keep class java.net.Inet4Address { *; }
-keep class java.net.Inet6Address { *; }
-keep class java.net.InetSocketAddress { *; }

# === SSL/TLS ===
-keep class javax.net.ssl.** { *; }
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# === Kotlinx Serialization ===
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.darkmintis.gitstore.**$$serializer { *; }
-keep @kotlinx.serialization.Serializable class com.darkmintis.gitstore.** { *; }
-keepclassmembers @kotlinx.serialization.Serializable class com.darkmintis.gitstore.** {
    *** Companion;
}
-keep class kotlinx.serialization.** { *; }
-keep class **$$serializer { *; }
-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# === App Models ===
-keep class com.darkmintis.gitstore.core.domain.model.** { *; }
-keep class com.darkmintis.gitstore.core.data.remote.dto.** { *; }
-keep class com.darkmintis.gitstore.**.*DeviceStart* { *; }
-keep class com.darkmintis.gitstore.**.*DeviceToken* { *; }
-keep class com.darkmintis.gitstore.**.*AuthConfig* { *; }
-keepclassmembers class com.darkmintis.gitstore.**.DeviceStart { public static ** Companion; }
-keepclassmembers class com.darkmintis.gitstore.**.DeviceTokenSuccess { public static ** Companion; }
-keepclassmembers class com.darkmintis.gitstore.**.DeviceTokenError { public static ** Companion; }

# === AndroidX Security ===
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**
-dontwarn com.google.errorprone.annotations.**

# === BuildConfig ===
-keep class com.darkmintis.gitstore.BuildConfig { *; }

# === Room ===
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class * extends androidx.room.Dao { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
-keep class com.darkmintis.gitstore.core.data.local.db.** { *; }
-keep class com.darkmintis.gitstore.core.data.local.db.entities.** { *; }
-keep class com.darkmintis.gitstore.core.data.local.db.dao.** { *; }

# === Koin ===
-keep class org.koin.** { *; }
-keep class com.darkmintis.gitstore.app.di.** { *; }
-keepclassmembers class * {
    public <init>(...);
}

# === Coil ===
-keep class coil3.** { *; }
-dontwarn coil3.**

# === Compose ===
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# === Navigation3 ===
-keep class androidx.navigation3.** { *; }
-keep class org.jetbrains.** { *; }

# === Liquid ===
-keep class com.liquid.** { *; }
-dontwarn com.liquid.**

# === Markdown ===
-keep class com.mikepenz.** { *; }
-dontwarn com.mikepenz.**

# === DataStore ===
-keep class androidx.datastore.** { *; }

# === General ===
-dontoptimize
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
