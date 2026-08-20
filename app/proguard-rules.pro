# Keep the AdhanNotificationService and alarm receivers
-keep class com.globaladhan.app.data.notifications.** { *; }
-keep class com.globaladhan.app.domain.model.** { *; }
-keep class com.globaladhan.app.data.local.db.** { *; }

# Kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.globaladhan.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.globaladhan.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
