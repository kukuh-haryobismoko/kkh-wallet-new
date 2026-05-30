# Default proguard rules
-keepattributes *Annotation*, InnerClasses
-keep class com.kkh.wallet.data.local.entity.** { *; }
-keep class com.kkh.wallet.domain.model.** { *; }
-keepclassmembers,allowobfuscation class * {
  @kotlinx.serialization.Serializable <fields>;
}
