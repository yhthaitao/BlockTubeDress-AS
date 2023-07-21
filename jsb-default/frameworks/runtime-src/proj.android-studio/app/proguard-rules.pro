# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\developSoftware\Android\SDK/tools/proguard/proguard-android.txt
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


#############################################
# �������ѹ���ȣ���0~7֮�䣬Ĭ��Ϊ5��һ�㲻���޸�
-optimizationpasses 5

# ���ʱ��ʹ�ô�Сд��ϣ���Ϻ������ΪСд
-dontusemixedcaseclassnames

# ����ʱ�Ƿ��¼��־����仰�ܹ�ʹ���ǵ���Ŀ���������ӳ���ļ�
# ����������->������������ӳ���ϵ
-verbose

# ָ����ȥ���Էǹ��������
-dontskipnonpubliclibraryclasses


# ָ����ȥ���Էǹ���������Ա
-dontskipnonpubliclibraryclassmembers

# ����ԤУ�飬preverify��proguard���ĸ�����֮һ��Android����Ҫpreverify��ȥ����һ���ܹ��ӿ�����ٶȡ�
-dontpreverify

# ����Annotation������
-keepattributes *Annotation*,InnerClasses

# �����������
-keepattributes Signature
# ���Ծ���
#-ignorewarning
# �Ż����Ż���������ļ�
-dontoptimize
# �׳��쳣ʱ���������к�
-keepattributes SourceFile,LineNumberTable

# ָ�������ǲ��õ��㷨������Ĳ�����һ��������
# ����������ǹȸ��Ƽ����㷨��һ�㲻������
-optimizations !code/simplification/cast,!field/*,!class/merging/*


#############################################
#
# Android������һЩ��Ҫ�����Ĺ�������
#
#############################################

# ��������ʹ�õ��Ĵ�������Զ����Application�ȵ���Щ�಻������
# ��Ϊ��Щ���඼�п��ܱ��ⲿ����
-keep public class * extends android.app.Activity
#-keep public class * extends android.app.Appliction
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
#-keep public class com.android.vending.licensing.ILicensingService


# ����support�µ������༰���ڲ���
-keep class android.support.** {*;}

# �����̳е�
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**

# ����R�������Դ
-keep class **.R$* {*;}

# ��������native������������
-keepclasseswithmembernames class * {
    native <methods>;
}

# ������Activity�еķ���������view�ķ�����
# ��������������layout��д��onClick�Ͳ��ᱻӰ��
-keepclassmembers class * extends android.app.Activity{
    public void *(android.view.View);
}

# ����ö���಻������
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ���������Զ���ؼ����̳���View����������
-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# ����Parcelable���л��಻������
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ����Serializable���л����಻������
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

# ���ڴ��лص�������onXXEvent��**On*Listener�ģ����ܱ�����
-keepclassmembers class * {
    void *(**On*Event);
    void *(**On*Listener);
}

# webView������Ŀ��û��ʹ�õ�webView���Լ���
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
#}
#-keepclassmembers class * extends android.webkit.webViewClient {
#    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
#    public boolean *(android.webkit.WebView, java.lang.String);
#}
#-keepclassmembers class * extends android.webkit.webViewClient {
#    public void *(android.webkit.webView, jav.lang.String);
#}


# Proguard Cocos2d-x-lite for release
-keep public class org.cocos2dx.** { *; }
-dontwarn org.cocos2dx.**

# Proguard Apache HTTP for release
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

# Proguard okhttp for release
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**

-keep class okio.** { *; }
-dontwarn okio.**


# Proguard Android Webivew for release. you can comment if you are not using a webview
-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

# adjust start
-keep class com.adjust.sdk.**{ *; }
-keep class com.google.android.gms.common.ConnectionResult {
    int SUCCESS;
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient {
    com.google.android.gms.ads.identifier.AdvertisingIdClient$Info getAdvertisingIdInfo(android.content.Context);
}
-keep class com.google.android.gms.ads.identifier.AdvertisingIdClient$Info {
    java.lang.String getId();
    boolean isLimitAdTrackingEnabled();
}
-keep public class com.android.installreferrer.**{ *; }
# adjust end

#-keepclassmembers class com.ironsource.sdk.controller.IronSourceWebView$JSInterface {
#    public *;
#}
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep class com.ironsource.adapters.** { *;
}
-dontwarn com.ironsource.mediationsdk.**
-dontwarn com.ironsource.adapters.**
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-dontwarn com.moat.**
-keep class com.moat.** { public protected private *; }


-dontwarn com.facebook.ads.internal.**
-keeppackagenames com.facebook.*
-keep public class com.facebook.ads.** {*;}
-keep public class com.facebook.ads.** { public protected *; }

-keepattributes InnerClasses,Exceptions
-keep public class com.applovin.sdk.AppLovinSdk{ *; }
-keep public class com.applovin.sdk.AppLovin* { public protected *; }
-keep public class com.applovin.nativeAds.AppLovin* { public protected *; }
-keep public class com.applovin.adview.* { public protected *; }
-keep public class com.applovin.mediation.* { public protected *; }
-keep public class com.applovin.mediation.ads.* { public protected *; }
-keep public class com.applovin.impl.*.AppLovin { public protected *; }
-keep public class com.applovin.impl.**.*Impl { public protected *; }
-keepclassmembers class com.applovin.sdk.AppLovinSdkSettings { private java.util.Map localSettings; }
-keep class com.applovin.mediation.adapters.** { *; }
-keep class com.applovin.mediation.adapter.**{ *; }

# Keep filenames and line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
# Keep JavascriptInterface for WebView bridge
-keepattributes JavascriptInterface
# Sometimes keepattributes is not enough to keep annotations
-keep class android.webkit.JavascriptInterface {
   *;
}
# Keep all classes in Unity Ads package
-keep class com.unity3d.ads.** {
   *;
}
# Keep all classes in Unity Services package
-keep class com.unity3d.services.** {
   *;
}

# pangle
-keep class com.bytedance.sdk.openadsdk.*{ public *; }
# pangle

-dontwarn com.google.ar.core.**
-dontwarn com.unity3d.services.**
-dontwarn com.ironsource.adapters.unityads.**

-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient

# keep anysdk for release. you can comment if you are not using anysdk
-keep public class com.anysdk.** { *; }
-dontwarn com.anysdk.**

# Vungle
-keep class com.vungle.warren.** { *; }
#-dontwarn com.vungle.warren.error.VungleError$ErrorCode
# Moat SDK
-keep class com.moat.** { *; }
-dontwarn com.moat.**
# Okio
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
# Retrofit
-dontwarn okio.**
#-dontwarn retrofit2.Platform$Java8
# Gson
-dontwarn sun.misc.**
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
# Google Android Advertising ID
-keep class com.google.android.gms.internal.** { *; }
-dontwarn com.google.android.gms.ads.identifier.**

-keep class com.bytedance.sdk.openadsdk.** {*;}
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
#-keep class com.bytedance.sdk.openadsdk.*{ public *; }
-dontwarn com.bytedance.sdk.openadsdk.**
-keep class com.bytedance.sdk.openadsdk.*{ public *; }
#-optimizations !class/merging/*

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

# Keep ADCNative class members unobfuscated
-keep class com.adcolony.sdk.** {
    *;
}
-dontwarn com.adcolony.sdk.**
-keep class com.google.android.exoplayer.** {*;}


#-keep class module-info
-keepattributes Module*
-dontwarn module-info

-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

-keep class com.bytedance.** { *; }
-dontwarn com.bytedance.**

-keep class com.facebook.** { *; }
-dontwarn com.facebook.**

-keep class kotlinx.coroutines.**
-dontwarn kotlinx.coroutines.**

#-ignorewarnings
#-keep class module-info
#-keepattributes Module*