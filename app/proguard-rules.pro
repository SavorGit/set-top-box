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

#不混淆资源类
-keepclassmembers class **.R$* {
    public static <fields>;
}
#保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


#保持自定义控件类不被混淆
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

#保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
#表示不混淆Parcelable实现类中的CREATOR字段，毫无疑问，CREATOR字段是绝对不能改变的，包括大小写都不能变，不然整个Parcelable工作机制都会失败。
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
#保持 Serializable 不被混淆
-keepnames class * implements java.io.Serializable
#保持 Serializable 不被混淆并且enum 类也不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    !private <fields>;
    !private <methods>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#keep entity for gson
-keep class com.savor.ads.bean.** { *; }
-keep class com.jar.savor.box.** { *; }
-keep class cn.savor.small.netty.** { *; }

#Proguard for netty begin
-keepattributes Signature,InnerClasses
-keepclasseswithmembers class io.netty.*.* {
    *;
}
-keepnames class io.netty.*.* {
    *;
}
-dontwarn io.netty.**
-dontwarn sun.**
#Proguard for netty end

#Proguard for Glide begin
-keep public class * implements com.bumptech.glide.module.AppGlideModule
-keep public class * implements com.bumptech.glide.module.LibraryGlideModule
-keep class com.bumptech.glide.*.* { *; }
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#Proguard for Glide end

#Proguard for okhttp3 begin
-keepattributes Signature
-keepattributes Annotation
-keep class okhttp3.*.* { *; }
-keep interface okhttp3.* { *; }
-dontwarn okhttp3.*
-dontwarn okio.**
#Proguard for okhttp3 end

#友盟混淆开始
-keep class com.umeng.commonsdk.*.* {*;}

-dontwarn com.taobao.**
-dontwarn anet.channel.**
-dontwarn anetwork.channel.**
-dontwarn org.android.**
-dontwarn org.apache.thrift.**
-dontwarn com.xiaomi.**
-dontwarn com.huawei.**

-keepattributes *Annotation*

-keep class com.taobao.*.* {*;}
-keep class org.android.*.* {*;}
-keep class anet.channel.*.* {*;}
-keep class com.umeng.*.* {*;}
-keep class com.xiaomi.*.* {*;}
-keep class com.huawei.*.* {*;}
-keep class org.apache.thrift.*.* {*;}

-keep class com.alibaba.sdk.android.*.*{*;}
-keep class com.ut.*.*{*;}
-keep class com.ta.*.*{*;}

-keep public class **.R$*{
   public static final int *;
}
#友盟混淆结束

#admaster混淆开始
-dontwarn com.admaster.**
-keep class com.admaster.*.* {
*;
}
#admaster混淆结束

#aliyun混淆开始
-dontwarn com.alibaba.sdk.**
-keep class com.alibaba.sdk.*.* {
*;
}
#aliyun混淆结束


-dontwarn org.apache.commons.**
-keep class org.apache.commons.*.* {
*;
}

-dontwarn org.eclipse.jetty.**
-keep class org.eclipse.jetty.*.* {
*;
}

-dontwarn com.amlogic.update.**
-keep class com.amlogic.update.*.* {
*;
}

-dontwarn javax.servlet.**
-keep class javax.servlet.*.* {
*;
}

-dontwarn com.mstar.tv.service.**
-keep class com.mstar.tv.service.*.* {
*;
}

-dontwarn com.droidlogic.app.**
-keep class com.droidlogic.app.*.* {
*;
}

#播放器混淆开始

-keep class com.shuyu.gsyvideoplayer.video.*.* { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.*.* { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.*.* { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class tv.danmaku.ijk.*.* { *; }
-dontwarn tv.danmaku.ijk.**

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#播放器混淆结束