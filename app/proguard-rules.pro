# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keep public class com.savor.ads.R$*{
public static final int *;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


#keep entity for gson
-keep class com.savor.ads.bean.** { *; }
-keep class com.jar.savor.box.** { *; }
-keep class cn.savor.small.** { *; }

#Proguard for netty begin
-keepattributes Signature,InnerClasses
-keepclasseswithmembers class io.netty.** {
    *;
}
-keepnames class io.netty.** {
    *;
}
-dontwarn io.netty.**
-dontwarn sun.**
#Proguard for netty end

#Proguard for Glide begin
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#Proguard for Glide end

#Proguard for okhttp3 begin
-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3.** { *; }
-keep interface okhttp3.* { *; }
-dontwarn okhttp3.*
-dontwarn okio.**
#Proguard for okhttp3 end

#友盟混淆开始
-keep class com.umeng.commonsdk.** {*;}

-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**

-keepattributes *Annotation*

-keep class com.taobao.** {*;}
-keep class org.android.** {*;}
-keep class anet.channel.** {*;}
-keep class com.umeng.** {*;}
-keep class com.xiaomi.** {*;}
-keep class com.huawei.** {*;}
-keep class org.apache.thrift.** {*;}

-keep class com.alibaba.sdk.android.**{*;}
-keep class com.ut.**{*;}
-keep class com.ta.**{*;}

-keep public class **.R$*{
   public static final int *;
}
#友盟混淆结束

#admaster混淆开始
-dontwarn com.admaster.**
-keep class com.admaster.** {
*;
}
#admaster混淆结束

#aliyun混淆开始
-dontwarn com.alibaba.sdk.**
-keep class com.alibaba.sdk.** {
*;
}
#aliyun混淆结束


-dontwarn org.apache.commons.**
-keep class org.apache.commons.** {
*;
}

-dontwarn org.eclipse.jetty.**
-keep class org.eclipse.jetty.** {
*;
}

-dontwarn com.amlogic.update.**
-keep class com.amlogic.update.** {
*;
}

-dontwarn javax.servlet.**
-keep class javax.servlet.** {
*;
}

-dontwarn com.mstar.tv.service.**
-keep class com.mstar.tv.service.** {
*;
}

-dontwarn com.droidlogic.app.**
-keep class com.droidlogic.app.** {
*;
}