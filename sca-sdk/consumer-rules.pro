-keep class tg.sdk.sca.domain.enrollment.** { *; }
-keep class com.miracl.trust.network.** { *; }

-keepclasseswithmembernames class * { native <methods>; }

-keep,includedescriptorclasses class net.sqlcipher.** { *; }
-keep,includedescriptorclasses interface net.sqlcipher.** { *; }