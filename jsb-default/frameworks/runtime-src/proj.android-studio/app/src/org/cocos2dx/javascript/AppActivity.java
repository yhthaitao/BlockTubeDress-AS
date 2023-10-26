/****************************************************************************
 Copyright (c) 2015-2016 Chukong Technologies Inc.
 Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

 http://www.cocos2d-x.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
package org.cocos2dx.javascript;

import org.cocos2dx.lib.Cocos2dxActivity;
import org.cocos2dx.lib.Cocos2dxGLSurfaceView;
import org.cocos2dx.lib.Cocos2dxJavascriptJavaBridge;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.FrameLayout;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import com.adjust.sdk.Adjust;
import com.adjust.sdk.AdjustEvent;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.ConsumeResponseListener;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesResponseListener;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.android.billingclient.api.SkuDetails;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.MaxRewardedAdListener;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.ads.MaxRewardedAd;
import com.applovin.sdk.AppLovinSdk;
import com.applovin.sdk.AppLovinSdkConfiguration;
import com.applovin.sdk.AppLovinSdkUtils;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.games.GamesSignInClient;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.games.SnapshotsClient;
import com.google.android.gms.games.snapshot.Snapshot;
import com.google.android.gms.games.snapshot.SnapshotMetadataChange;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.firebase.analytics.FirebaseAnalytics;
//applovin
//push
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import com.universal.notification.NotificationController;
import com.universal.notification.NotifyObject;

import android.view.View;
import android.widget.ImageView;
import android.view.Gravity;

import androidx.annotation.NonNull;

import static android.net.Uri.encode;
//banner
import com.sort.puzzle.dress.R;
import com.google.android.gms.games.PlayGamesSdk;

public class AppActivity extends Cocos2dxActivity implements MaxAdListener, MaxRewardedAdListener, MaxAdViewAdListener {
    private static AppActivity app = null;
    private static final String TAG = AppActivity.class.getSimpleName();

    private static Cocos2dxActivity sCocos2dxActivity;
    private static ImageView sSplashBgImageView = null;

    private String installCheckFile = "installed.txt";
    private String fbLogPrefix = "V1_";
    private String logAdid = null;
    private boolean mVedioEventSent = false;
    private static boolean VideoCompleted = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    private static BillingClient billingClient = null;
    private static List<SkuDetails> mySkuDetailsList = new ArrayList<SkuDetails>();
    private static List<ProductDetails> myProductDetailsList = new ArrayList<ProductDetails>();
    private double adRevenue = 0;
    private String logAdPlace = null;
    private String logAdType = null;
    private String logVersion = null;
    private String debugMode = "1";
    private String mCurrentSaveName = "snapshotWB";
    private static final int MAX_SNAPSHOT_RESOLVE_RETRIES = 10;
    private SnapshotsClient mSnapshotsClient = null;

    private static long exitTime = 0;//两次返回键退出

    // 已经通过的关卡数
    private int currentLevel = 0;

    private static Vibrator myVibrator;//震动
    private double adPrice = 0.01;//每10次价格
    private MaxAdView adView;

    Boolean appLovinOK = false;
    Boolean showInterAD = true;
    Locale locale;

    MyTask mTask;
    InitTask initTask;

    private ReviewManager manager;//应用内评价
    private ReviewInfo reviewInfo;//应用内评价

    private class MyTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            File file = app.getFileStreamPath(installCheckFile);
            if (file.exists()) {
                try {
                    File checkFile = new File(app.getFilesDir(), installCheckFile);
                    FileReader reader = new FileReader(checkFile);
                    BufferedReader br = new BufferedReader(reader);
                    String s;
                    if ((s = br.readLine()) != null) {
                        app.logAdid = s;
                        javaLog(" java method: MyTask.doInBackground 广告id：" + app.logAdid);
                    }
                    reader.close();
                    br.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return "installed";
            } else {
                AdvertisingIdClient.Info idInfo = null;
                try {
                    idInfo = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String advertId = null;
                try {
                    advertId = idInfo.getId();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                return advertId;
            }
        }

        @Override
        protected void onPostExecute(String advertId) {
            if (!"installed".equals(advertId)) {
                app.logAdid = advertId;
            }
            javaLog(" java method: MyTask.onPostExecute 广告id：" + app.logAdid);
        }
    }

    private class InitTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            //*******applovin***********
            AppLovinSdk.getInstance(app).setMediationProvider("max");
            AppLovinSdk.initializeSdk(app, new AppLovinSdk.SdkInitializationListener() {
                @Override
                public void onSdkInitialized(final AppLovinSdkConfiguration configuration) {
                    // AppLovin SDK is initialized, start loading ads
                    createRewardedAd();
                    createInterstitialAd();
                    createAdViewAd(MaxAdFormat.BANNER);
                    app.appLovinOK = true;
                    javaLog(" java method: InitTask.doInBackground appLovinOK：" + app.appLovinOK);
                }
            });
//        AppLovinSdk.getInstance( this ).showMediationDebugger();

            app.myVibrator = (Vibrator) app.getSystemService(Service.VIBRATOR_SERVICE);
            initNoticeMsg();
            adPriceCountry();
            purchasesInit();
            return "initOK";
        }

        @Override
        protected void onPostExecute(String ret) {
            javaLog(" java method: InitTask.onPostExecute initOK：" + ret);
        }
    }

    public static void reportInstall2(String reqNum) {
        SharedPreferences sharedPreferences = app.getSharedPreferences("data", Context.MODE_PRIVATE);
        String reportState = sharedPreferences.getString("reportState", "");
        javaLog(" java method: reportInstall2 isNetWorkGlobal：" + GlobalApplication.isNetWorkGlobal);
        if (!"".equals(app.logAdid) && !"done".equals(reportState) && GlobalApplication.isNetWorkGlobal == 2) {
            RequestQueue queue = Volley.newRequestQueue(app);

            // 新包 重新配置地址
            String url = "https://adjust.clicksplay.com/sortdress/install?os=android&gps_adid=" + app.logAdid + "&num=" + reqNum;

            javaLog(" java method: reportInstall2 广告url：" + url);
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            javaLog(" java method: reportInstall2.onResponse response：" + response);
                            // response
                            if (response.equals("done")) {
                                SharedPreferences sharedPreferences = app.getSharedPreferences("data", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("reportState", "done");
                                editor.commit();
                            } else {
                                try {
                                    File checkFile = new File(app.getFilesDir(), app.installCheckFile);
                                    FileWriter writer = new FileWriter(checkFile);
                                    writer.append(app.logAdid);
                                    writer.flush();
                                    writer.close();
                                } catch (IOException error) {
                                    error.printStackTrace();
                                    javaLog(" java method: reportInstall2.onResponse error：" + error.toString());
                                }
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            javaLog(" java method: reportInstall2.onErrorResponse error：" + error.toString());
                        }
                    }
            );
            queue.add(getRequest);
        }
    }

    public void myLogEvent(String adid, String name, String value) {
        // 输出错误到server
        javaLog(" java method: myLogEvent ad：" + adid + "; name: " + name + "; value: " + value);
        if (!"".equals(adid)) {
            RequestQueue queue = Volley.newRequestQueue(app);
            String url = "https://quizcelebrity.clicksplay.com/sortdress/buyItem?os=android&gps_adid=" + adid + "&event=" + name + "&value=" + value;
            javaLog(" java method: myLogEvent url：" + url);
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            javaLog(" java method: myLogEvent.onResponse response：" + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            javaLog(" java method: myLogEvent.onResponse response：" + error.toString());
                        }
                    }
            );
            queue.add(getRequest);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.initJsonDot();
        PlayGamesSdk.initialize(this);
        app = this;
        locale = getResources().getConfiguration().locale;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(app);
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//禁止自动锁屏

        if (!isTaskRoot()) {
            // Android launched another instance of the root activity into an existing task
            // so just quietly finish and go away, dropping the user back into the activity
            // at the top of the stack (ie: the last state of this task)
            // Don't need to finish it again since it's finished in super.onCreate .
            return;
        }
        SDKWrapper.getInstance().init(this);

        mTask = new MyTask();
        mTask.execute();

        initTask = new InitTask();
        initTask.execute();
    }

    // 打点对象；
    JSONObject jsonDot;
    private static final String stringDot = "{" +
            "100_Resource_load_success:'oepiqe'," +

            "onCreat_001:'xnbp8j'," +
            "onCreat_002:'xg1men'," +
            "onCreat_003:'h26bel'," +
            "onCreat_004:'payvo2'," +
            "onCreat_005:'8uka9k'," +
            "onCreat_006:'b58rwm'," +

            "101_Guide_adventure_01:'d1low4'," +
            "102_Guide_adventure_02:'thq0ka'," +
            "103_Guide_adventure_03:'n4cllb'," +
            "118_Ads_direct_request:'t82v21'," +
            "119_Debug_all_ad_impression:'oh8ho6'," +
            "120_Ads_All_direct_request:'jp3t4d'," +
            "122_Ads_All_direct_play_finished:'z0szk8'," +
            "123_Ads_Interstitial_direct_request:'hapnj4'," +
            "125_Debug_Interstitial_show:'amoe9g'," +
            "161_pass_level_1:'j3qv0q'," +
            "162_pass_level_2:'tylrvn'," +
            "163_pass_level_3:'nyathx'," +
            "164_pass_level_4:'aeijx6'," +
            "165_pass_level_5:'b1ilba'," +
            "166_pass_level_6:'eup194'," +
            "167_pass_level_7:'xafkse'," +
            "168_pass_level_8:'yqdaw9'," +
            "169_pass_level_9:'uu8g2t'," +
            "170_pass_level_10:'vtm411'," +
            "190_pass_level_30:'dhb3cj'," +
            "205_Stage_pass_all:'iqr3ws'," +
            "210_pass_level_50:'uwfwyv'," +
            "230_pass_level_70:'e08wz2'," +
            "260_pass_level_100:'9uz2yd'," +
            "227_Ad_revenue_track_flag:'a4n0y4'," +
            "228_Ad_revenue_track_flag_30:'kjgypt'," +
            "229_Ad_revenue_track_flag_40:'gugudo'," +
            "230_Ad_revenue_track_flag_50:'2iqul7'," +
            "231_Ad_revenue_track_flag_60:'gauzd0'," +
            "232_Ad_revenue_track_flag_70:'eyy1em'," +
            "233_Ad_revenue_track_flag_80:'ogn28x'," +
            "234_Ad_revenue_track_flag_90:'nsuk2p'," +
            "235_Ad_revenue_track_flag_100:'7fkz0m'," +
            "AdDone:'560dsk'," +
            "AdReq:'rib0a1'," +
            "Ads_addSortReturn_succ:'r97dfu'," +
            "Ads_addTube_succ:'2zi1v1'," +
            "Ads_beckground_Interstitial_succ:'1nq67e'," +
            "Ads_help_click:'mn1tzz'," +
            "Ads_help_succ:'2mf0k2'," +
            "Ads_Interstitial_nextlevel_Succeed:'ify0nf'," +
            "Ads_Interstitial_restart_Succeed:'tcsjwv'," +
            "Ads_Interstitial_videoNull_Succeed:'pa53fr'," +
            "applovin_cpe:'4ofnrq'," +
            "buy_back_click:'m8p3gx'," +
            "buy_back_show:'29ja0k'," +
            "buy_back_succ:'twqmdh'," +
            "buy_bottle_click:'vsjyv5'," +
            "buy_bottle_show:'tkmn3m'," +
            "buy_bottle_succ:'botclg'," +
            "cloudCover:'md3xkw'," +
            "cloudDataShow:'5gfbcf'," +
            "excludeLowEcpm:'j3oq5b'," +
            "levelPass:'qm62pf'," +

            "JigLevelStart:'vevwk8'," +
            "JigReStart:'cq7odd'," +
            "JigHelpSucc:'mwwzup'," +
            "JigLevelPass:'xwq06b'," +
            "JigReturn:'memlw5'," +
            "JigLevelOver:'dfnyey'," +

            "hammer_click:'gs7cxu'," +
            "hammer_consume:'ex0v2u'," +
            "levelStart:'p12dfs'," +
            "loadok_to_all:'ucrifo'," +
            "noads_buySucc:'2481h8'," +
            "OnApplicationResume:'z39hj3'," +
            "sortPass:'drj0c9'," +
            "sortReStart:'owbjwa'," +
            "sortStart:'67yxyj'," +
            "share_level_click:‘6lix01’" +
            "}";
    private void initJsonDot() {
        try {
            this.jsonDot = new JSONObject(this.stringDot);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void setAdRevenue(float adRevenue) {
        if (app.adRevenue == 0) {
            app.adRevenue = adRevenue;
        }
        javaLog(" java method: setAdRevenue adRevenue：" + app.adRevenue);
    }

    private static void showSplash() {
//        sSplashBgImageView = new ImageView(sCocos2dxActivity);
//        sSplashBgImageView.setImageResource(R.drawable.splash);
//        sSplashBgImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//        sCocos2dxActivity.addContentView(sSplashBgImageView,
//                new WindowManager.LayoutParams(
//                        FrameLayout.LayoutParams.MATCH_PARENT,
//                        FrameLayout.LayoutParams.MATCH_PARENT
//                )
//        );
    }

    public static void hideSplash() {
        javaLog(" java method: hideSplash ");
//        sCocos2dxActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (sSplashBgImageView != null) {
//                    sSplashBgImageView.setVisibility(View.GONE);
//                }
//            }
//        });
    }

    void initNoticeMsg() {
        /**
         * 推送
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Map<Integer, NotifyObject> map = new HashMap<Integer, NotifyObject>();
            long date = System.currentTimeMillis() / 1000;
            Long currentTimestamps = System.currentTimeMillis();
            Long oneDayTimestamps = Long.valueOf(60 * 60 * 24 * 1000);
            Calendar cal = Calendar.getInstance();
            int offset = cal.get(Calendar.ZONE_OFFSET);
            Long timestamp = (Long.valueOf(60 * 60 * 14 * 1000) - currentTimestamps % oneDayTimestamps - offset) + currentTimestamps;

            if (app.getCountryNew().equals("CN")) {
//            app.map.put(0, app.notifyObject1)
//            map.put(0, new NotifyObject(1, R.drawable.notification_icon, "你累了吗？", "玩Ball Sort 2020，休息一下吧", 5000L + currentTimestamps));
                map.put(1, new NotifyObject(1, R.drawable.notification_icon, "主人", "快来玩啊，我们新加了很多新鱼哟~", 7200000L + currentTimestamps));
                map.put(2, new NotifyObject(2, R.drawable.notification_icon, "主人", "有时间？你的小鱼儿想你了~", 79200000L + currentTimestamps));
                map.put(3, new NotifyObject(3, R.drawable.notification_icon, "主人", "今天有空吗？小鱼儿有点寂寞...", 172800000L + currentTimestamps));
                map.put(4, new NotifyObject(4, R.drawable.notification_icon, "主人", "来休息一下，和小鱼儿一起放松放松哦~", 432000000L + currentTimestamps));
                map.put(5, new NotifyObject(5, R.drawable.notification_icon, "主人", "我很久没见到您了，回来看看我们吧！", 597600000L + currentTimestamps));
                if (timestamp > currentTimestamps) {
                    map.put(6, new NotifyObject(6, R.drawable.notification_icon, "主人，你累了吗？", "和小鱼儿一起玩会儿吧！", timestamp));
                }
            } else if (app.getCountryNew().equals("TW")) {
//            app.map.put(0, app.notifyObject1);
//            map.put(0, new NotifyObject(0, R.drawable.notification_icon, "你累了吗？", "玩Ball Sort 2020，休息一下吧", 5000L + currentTimestamps));
                map.put(1, new NotifyObject(1, R.drawable.notification_icon, "主人", "快來玩啊，我們新加了很多新魚喲~", 7200000L + currentTimestamps));
                map.put(2, new NotifyObject(2, R.drawable.notification_icon, "主人", "有時間？你的小魚兒想你了~", 79200000L + currentTimestamps));
                map.put(3, new NotifyObject(3, R.drawable.notification_icon, "主人", "今天有空嗎？小魚兒有點寂寞...", 172800000L + currentTimestamps));
                map.put(4, new NotifyObject(4, R.drawable.notification_icon, "主人", "來休息一下，和小魚兒一起放鬆放鬆哦~", 432000000L + currentTimestamps));
                map.put(5, new NotifyObject(5, R.drawable.notification_icon, "主人", "我很久沒見到您了，回來看看我們吧！", 597600000L + currentTimestamps));
                if (timestamp > currentTimestamps) {
                    map.put(6, new NotifyObject(6, R.drawable.notification_icon, "主人，你累了嗎吗？", "和小魚兒一起玩會兒吧！", timestamp));
                }
            } else if (app.getCountryNew().equals("JP")) {
//            app.map.put(0, app.notifyObject1);
//            map.put(0, new NotifyObject(0, R.drawable.notification_icon, "你累了吗？", "玩Ball Sort 2020，休息一下吧", 5000L + currentTimestamps));
                map.put(1, new NotifyObject(1, R.drawable.notification_icon, "ザ・ホスト", "遊びに来て、たくさんの新しい魚を追加しました〜", 7200000L + currentTimestamps));
                map.put(2, new NotifyObject(2, R.drawable.notification_icon, "ザ・ホスト", "あなたの小さな魚はあなたがいなくて寂しいです〜", 79200000L + currentTimestamps));
                map.put(3, new NotifyObject(3, R.drawable.notification_icon, "ザ・ホスト", "あなたは今日利用できますか？私は少し寂しいです...", 172800000L + currentTimestamps));
                map.put(4, new NotifyObject(4, R.drawable.notification_icon, "ザ・ホスト", "しばらく休む，私と一緒にリラックスしてください〜", 432000000L + currentTimestamps));
                map.put(5, new NotifyObject(5, R.drawable.notification_icon, "ザ・ホスト", "私は長い間あなたに会っていません，戻ってきて私たちに会いましょう", 597600000L + currentTimestamps));
                if (timestamp > currentTimestamps) {
                    map.put(6, new NotifyObject(6, R.drawable.notification_icon, "疲れましたか？", "あなたの小さな魚はあなたがいなくて寂しいです〜", timestamp));
                }
            } else {
//            app.map.put(0, app.notifyObject1);
//            map.put(0, new NotifyObject(1, R.drawable.notification_icon, "Tired?", "Play Ball Sort 2020, have a relax.", 5000L + currentTimestamps));
                map.put(1, new NotifyObject(1, R.drawable.notification_icon, "Master", "come on, we have added a lot of new fish ~", 7200000L + currentTimestamps));
                map.put(2, new NotifyObject(2, R.drawable.notification_icon, "Master", " are you free? Your little fish miss you~", 79200000L + currentTimestamps));
                map.put(3, new NotifyObject(3, R.drawable.notification_icon, "Master", " are you free today? Little fish have a little lonely", 172800000L + currentTimestamps));
                map.put(4, new NotifyObject(4, R.drawable.notification_icon, "Master", " come to have a relax time with the little fish", 432000000L + currentTimestamps));
                map.put(5, new NotifyObject(5, R.drawable.notification_icon, "Master", "I haven't seen you for a long time. Come back and see us!", 597600000L + currentTimestamps));
                if (timestamp > currentTimestamps) {
                    map.put(6, new NotifyObject(6, R.drawable.notification_icon, "Master", "are you tired ? Let's relax with the little fish", timestamp));
                }
            }

            NotificationController.getInstance().initAllNotifyMsg(map, app, app.getClass());
        }
    }

    void adPriceCountry() {
        /**
         * 各个国家每10次广告定价
         */
        app.adPrice = 0.01;
        if (app.getCountryNew().equals("US") || app.getCountryNew().equals("AU")) {
            app.adPrice = 0.15;
        } else if (app.getCountryNew().equals("CA") || app.getCountryNew().equals("JP")) {
            app.adPrice = 0.1;
        } else if (app.getCountryNew().equals("DE") || app.getCountryNew().equals("NO") || app.getCountryNew().equals("CH") || app.getCountryNew().equals("GB") || app.getCountryNew().equals("TW") || app.getCountryNew().equals("SG")) {
            app.adPrice = 0.08;
        } else if (app.getCountryNew().equals("FR") || app.getCountryNew().equals("HK")) {
            app.adPrice = 0.05;
        } else if (app.getCountryNew().equals("BR") || app.getCountryNew().equals("AT") || app.getCountryNew().equals("ES") || app.getCountryNew().equals("NL") || app.getCountryNew().equals("IT") || app.getCountryNew().equals("RU") || app.getCountryNew().equals("TH") || app.getCountryNew().equals("ID") || app.getCountryNew().equals("MY")) {
            app.adPrice = 0.02;
        }
    }

    void purchasesInit() {
        // ---------IAP--------------
        PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {
            @Override
            public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
                // To be implemented in a later section.
                javaLog(" java method: purchasesInit.onPurchasesUpdated msg: " + billingResult.getDebugMessage()
                + "; code: " + billingResult.getResponseCode() + "; purchases: " + purchases);
                //打点
                String value = "ResponseCode=" + billingResult.getResponseCode();
                app.myLogEvent(app.logAdid, "finished_1", value);
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                        && purchases != null) {
                    for (Purchase purchase : purchases) {
                        javaLog(" java method: purchasesInit.onPurchasesUpdated purchase: " + purchase.getOriginalJson());
                        handlePurchase(purchase);
                    }
                } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    app.runOnGLThread(new Runnable() {
                        @Override
                        public void run() {
                            toCocos("buyFail('CANCELED');");
                        }
                    });
                } else {
                    // Handle any other error codes.
                    app.runOnGLThread(new Runnable() {
                        @Override
                        public void run() {
                            toCocos("buyFail('error');");
                        }
                    });
                }
            }
        };
        billingClient = BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                String value = "ResponseCode=" + billingResult.getResponseCode();
                javaLog(" java method: purchasesInit.billingClient.startConnection.onBillingSetupFinished code: " + value);
                //打点
                app.myLogEvent(app.logAdid, "onBillingSetupFinished_1", value);
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    List productList = new ArrayList<QueryProductDetailsParams>();
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("daily0.99")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("daily1.99")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("daily2.99")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("goldpiggy")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlyspin60")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlyspin39")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlyspin84")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlyspin150")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlyspin316")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlycoin399")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlycoin599")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlycoin1299")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlycoin2499")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("onlycoin5099")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("spin10")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("coin7999")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("coin499")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("coin999")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("coin1999")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("coin099")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("coin299")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("coin3999")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    productList.add(QueryProductDetailsParams.Product.newBuilder()
                            .setProductId("noads")
                            .setProductType(BillingClient.ProductType.INAPP)
                            .build());
                    QueryProductDetailsParams queryProductDetailsParams =
                            QueryProductDetailsParams.newBuilder()
                                    .setProductList(productList)
                                    .build();
                    billingClient.queryProductDetailsAsync(
                            queryProductDetailsParams,
                            new ProductDetailsResponseListener() {
                                public void onProductDetailsResponse(BillingResult billingResult,
                                                                     List<ProductDetails> productDetailsList) {
                                    // check billingResult
                                    // process returned productDetailsList
                                    javaLog(" java method: purchasesInit.billingClient.queryProductDetailsAsync.onProductDetailsResponse " +
                                            "msg: " + billingResult.getDebugMessage());
                                    app.myProductDetailsList = productDetailsList;
                                    if (productDetailsList != null) {
                                        //打点
                                        app.myLogEvent(app.logAdid, "onBillingSetupFinished_2", "skuDetailsList_notNull");
                                        for (int i = 0; i < productDetailsList.size(); i++) {
                                            javaLog(" java method: purchasesInit.billingClient.queryProductDetailsAsync.onProductDetailsResponse " +
                                                    "productId: " + productDetailsList.get(i).getProductId() +
                                                    "title: " + productDetailsList.get(i).getTitle());
                                        }
                                    } else {
                                        app.myLogEvent(app.logAdid, "onBillingSetupFinished", "skuDetailsList_isNull");
                                    }
                                }
                            }
                    );

                    billingClient.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                    .setProductType(BillingClient.ProductType.INAPP)
                                    .build(),
                            new PurchasesResponseListener() {
                                public void onQueryPurchasesResponse(BillingResult billingResult, List<Purchase> purchases) {
                                    // check billingResult
                                    // process returned purchase list, e.g. display the plans user owns
                                    javaLog(" java method: purchasesInit.billingClient.queryPurchasesAsync.onQueryPurchasesResponse " +
                                            "code: " + billingResult.getResponseCode());
                                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                        if (purchases != null && purchases.size() > 0) {
                                            for (Purchase purchase : purchases) {
                                                javaLog(" java method: purchasesInit.billingClient.queryPurchasesAsync.onQueryPurchasesResponse " +
                                                        "purchase: " + purchase.getOriginalJson());
                                                handlePurchase(purchase);
                                            }
                                        }
                                    }

                                }
                            }
                    );
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                javaLog(" java method: purchasesInit.billingClient.startConnection.onBillingServiceDisconnected ");
            }
        });
        // ---------IAP--------------
    }

    public void collectErr(String errInfo) {
        // 输出错误到server
        RequestQueue queue = Volley.newRequestQueue(app);
        javaLog(" java method: collectErr errInfo: " + errInfo);
        if (!errInfo.equals("")) {
            String url = "https://quizcelebrity.clicksplay.com?game=sortdress&err=" + encode(errInfo);
            javaLog(" java method: collectErr url: " + url);
            StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            // response
                            javaLog(" java method: collectErr.StringRequest.onResponse response: " + response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // error
                            javaLog(" java method: collectErr.StringRequest.onErrorResponse error: " + error.toString());
                        }
                    }
            );
            queue.add(getRequest);
        }
    }

    public static String getPackageVersion() {
        String verName = "";
        try {
            verName = app.getPackageManager().
                    getPackageInfo(app.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    public static String getADID() {
        if (app.logAdid == null) {

        }
        return app.logAdid;
    }

    private void handlePurchase(final Purchase purchase) {
//        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
//            if (!purchase.isAcknowledged()) {
//                AcknowledgePurchaseParams acknowledgePurchaseParams =
//                        AcknowledgePurchaseParams.newBuilder()
//                                .setPurchaseToken(purchase.getPurchaseToken())
//                                .build();
//                AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener = new AcknowledgePurchaseResponseListener() {
//                    @Override
//                    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
//                        int responseCode = billingResult.getResponseCode();
//                        String debugMessage = billingResult.getDebugMessage();
//                    }
//                };
//                billingClient.acknowledgePurchase(acknowledgePurchaseParams, acknowledgePurchaseResponseListener);
//            }
//        }else{
        javaLog(" java method: handlePurchase");
        ConsumeParams consumeParams =
                ConsumeParams.newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();

        ConsumeResponseListener listener = new ConsumeResponseListener() {
            @Override
            public void onConsumeResponse(BillingResult billingResult, String purchaseToken) {
                javaLog(" java method: handlePurchase.listener.onConsumeResponse responseCode: " +
                        billingResult.getResponseCode() + "; purchaseToken: " + purchaseToken);
                //打点
                String value = "ResponseCode=" + billingResult.getResponseCode();
                app.myLogEvent(app.logAdid, "finished_2", value);
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    // Handle the success of the consume operation.
                    app.runOnGLThread(new Runnable() {
                        @Override
                        public void run() {
                            toCocos("buySucc('" + purchase.getProducts().get(0).toString() + "');");
                         }
                    });
                }
            }
        };

        billingClient.consumeAsync(consumeParams, listener);
//        }
    }

    /**
     * Check whether the purchases have changed before posting changes.
     */
    private boolean isUnchangedPurchaseList(List<Purchase> purchasesList) {
        // TODO: Optimize to avoid updates with identical data.
        return false;
    }

    public static void buyItem(String sku) {
        javaLog(" java method: buyItem myProductDetailsList: " + app.myProductDetailsList.toString());
        String value = "" + sku;
        app.myLogEvent(app.logAdid, "purchase_1", value);
        Bundle params = new Bundle();
//        value = "" + app.mySkuDetailsList;
//        app.myLogEvent(app.logAdid, "purchase_2", value);
//        if (app.mySkuDetailsList == null) {
        if (app.myProductDetailsList == null) {
            javaLog(" java method: buyItem value: " + value);
            // 通知js商店未就绪
            String eventFirebase = "buyItem_mySkuDetailsList_null";
            javaLog(" java method: buyItem eventFireBase: " + eventFirebase);
            app.mFirebaseAnalytics.logEvent(eventFirebase, params);
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    toCocos("buyFail('error');");
                }
            });
        } else {
            value = "size=" + app.mySkuDetailsList.size();
            javaLog(" java method: buyItem value: " + value);
            app.myLogEvent(app.logAdid, "purchase_3", value);
//            if (app.mySkuDetailsList.size() == 0) {
            if (app.myProductDetailsList.size() == 0) {
                String eventFirebase = "buyItem_mySkuDetailsList_0";
                javaLog(" java method: buyItem eventFireBase: " + eventFirebase);
                app.mFirebaseAnalytics.logEvent(eventFirebase, params);
                app.runOnGLThread(new Runnable() {
                    @Override
                    public void run() {
                        toCocos("buyFail();");
                    }
                });
            } else {
                for (int i = 0; i < app.myProductDetailsList.size(); i++) {
                    if (sku.equals(app.myProductDetailsList.get(i).getProductId())) {
                        List productDetailsParamsList = new ArrayList<BillingFlowParams.ProductDetailsParams>();
                        productDetailsParamsList.add(
                                BillingFlowParams.ProductDetailsParams.newBuilder()
                                        // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                        .setProductDetails(app.myProductDetailsList.get(i))
                                        // to get an offer token, call ProductDetails.getSubscriptionOfferDetails()
                                        // for a list of offers that are available to the user
//                                                            .setOfferToken(productDetailsList.get(0).getSubscriptionOfferDetails())
                                        .build()
                        );
                        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(productDetailsParamsList)
                                .build();
                        BillingResult billingResult = billingClient.launchBillingFlow(app, billingFlowParams);//掉起支付界面
                        int responseCode = billingResult.getResponseCode();
                        String debugMessage = billingResult.getDebugMessage();
                        value = "responseCode=" + responseCode;
                        app.myLogEvent(app.logAdid, "purchase_5", value);
                        // firebase 打点
                        String eventFirebase = "buyItem_mySkuDetailsList_ok" + sku;
                        javaLog(" java method: buyItem eventFireBase: " + eventFirebase);
                        app.mFirebaseAnalytics.logEvent(eventFirebase, params);
                    }
                }
            }
        }
    }

    public static void vibrate(int time) {
        javaLog(" java method: vibrate time: " + time);
        if (time > 0) {
            app.myVibrator.vibrate(time);// 参数为震动时间
        }
    }

    public boolean isNetAvailable(Context context) {
        PackageManager localPackageManager = context.getPackageManager();
        if (localPackageManager.checkPermission("android.permission.ACCESS_NETWORK_STATE", context.getPackageName()) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        ConnectivityManager localConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (localConnectivityManager == null) {
            return false;
        }
        try {
            //android 5.0以后引入了多网络连接，getAllNetworkInfo将要在6.0以后弃用
            if (Build.VERSION.SDK_INT >= 21) {
                Network[] networks = localConnectivityManager.getAllNetworks();
                for (Network network : networks) {
                    NetworkCapabilities capabilities = localConnectivityManager.getNetworkCapabilities(network);
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ||
                            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                        return true;
                    }
                }
            } else {
                NetworkInfo[] info = localConnectivityManager.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i] == null)
                            continue;

                        if (info[i].isConnected()) {
                            return true;
                        }
                        // if (info[i].isConnectedOrConnecting() ) {
                        //     return true;
                        // }
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static String getLanguage() {
        return app.locale.getLanguage();
    }

    public static String getCountry() {
        javaLog(" java method: getCountry() " + app.locale.getCountry());
        return app.locale.getCountry();
    }

    public String getCountryNew() {
        javaLog(" java method: getCountryNew() " + app.locale.getCountry());
        return locale.getCountry();
    }

    public static void showInterstitial() {
        javaLog(" java method: showInterstitial() " + app.interstitialAd.isReady());
        if (app.interstitialAd.isReady()) {
            app.showInterAD = false;  //因 background问题注释
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    app.interstitialAd.showAd();
                    facebookLogEvent("119_Debug_all_ad_impression");
                    facebookLogEvent("125_Debug_Interstitial_show");
                }
            });
        } else {
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    toCocos("advertFail();");
                }
            });
        }
    }

    public static boolean interAdReady() {
        return app.interstitialAd.isReady();
    }

    public static void showBanner() {
        javaLog(" java method: showBanner() appLovinOK: " + app.appLovinOK + "; adView.Visibility: " +
                app.adView.getVisibility() + "; adView.VISIBLE: " + app.adView.VISIBLE);
//        IronSource.loadBanner(banner);
//        bannerContainer.setVisibility(View.VISIBLE);
        if (app.appLovinOK) {
            if (app.adView.getVisibility() != app.adView.VISIBLE) {
                app.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        app.adView.setVisibility(View.VISIBLE);
                        app.adView.startAutoRefresh();
                        javaLog(" java method: showBanner() adView.Visibility: " +
                                app.adView.getVisibility() + "; adView.VISIBLE: " + app.adView.VISIBLE);
                    }
                });
            }
        }
    }

    public static void closeBanner() {
//        bannerContainer.setVisibility(View.INVISIBLE);
        javaLog(" java method: closeBanner() appLovinOK: " + app.appLovinOK + "; adView.Visibility: " +
                app.adView.getVisibility() + "; adView.VISIBLE: " + app.adView.VISIBLE);
        if (app.appLovinOK) {
            if (app.adView.getVisibility() == app.adView.VISIBLE) {
                app.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        app.adView.setVisibility(View.INVISIBLE);
                        javaLog(" java method: closeBanner() aadView.Visibility: " +
                                app.adView.getVisibility() + "; adView.VISIBLE: " + app.adView.VISIBLE);
                    }
                });
            }
        }
    }

    public static boolean checkMopubRewardVideo() {
        javaLog(" java method: checkMopubRewardVideo() AdReady: " + app.rewardedAd.isReady());
        if (app.rewardedAd.isReady()) {
            return true;
        }
        return false;
    }

    public static void showMopubRewardVideo() {
        boolean isNetAvailable = app.isNetAvailable(app.getApplicationContext());
        javaLog(" java method: showMopubRewardVideo() isNetAvailable: " + isNetAvailable);
        if (!isNetAvailable) {
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    toCocos("NoNetwork();");
                }
            });
            return;
        }
        javaLog(" java method: showMopubRewardVideo() isReady: " + app.rewardedAd.isReady());
        if (app.rewardedAd.isReady()) {
            app.showInterAD = false;
            app.rewardedAd.showAd();
        } else {
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    toCocos("NoVideo();");
                }
            });
        }
    }

    public static void facebookLogEvent(String eventName) {
        if (app.jsonDot != null && app.jsonDot.has(eventName)){
            try{
                String stringEvent = app.jsonDot.getString(eventName);
                javaLog(" java method: facebookLogEvent() eventName: " + eventName + "; stringEvent: " + stringEvent);
                AdjustEvent adjustEvent = new AdjustEvent(stringEvent);
                Adjust.trackEvent(adjustEvent);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        Bundle params = new Bundle();
        String eventFirebase = "EVENT_" + eventName;
        javaLog(" java method: facebookLogEvent() eventFireBase: " + eventFirebase);
        app.mFirebaseAnalytics.logEvent(eventFirebase, params);
    }

    public static void valueLogEvent(String eventName, String value) {
        app.logVersion = app.getPackageVersion();
        if (app.jsonDot != null && app.jsonDot.has(eventName)){
            try{
                String stringEvent = app.jsonDot.getString(eventName);
                javaLog(" java method: valueLogEvent() eventName: " + eventName + "; value: " + value + "; stringEvent: " + stringEvent);
                AdjustEvent adjustEvent = new AdjustEvent(stringEvent);
                adjustEvent.addCallbackParameter("Value", value);
                adjustEvent.addCallbackParameter("Version", app.logVersion);
                adjustEvent.addCallbackParameter("PlayerUid", app.logAdid);
                Adjust.trackEvent(adjustEvent);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString("LEVEL_NAME", value);
        String eventFirebase = "EVENT_" + eventName;
        javaLog(" java method: valueLogEvent() eventFirebase: " + eventFirebase);
        app.mFirebaseAnalytics.logEvent(eventFirebase, bundle);
    }

    public static void reqLogEvent(String eventName, String adPlace, String adType) {
//        2.用户请求广告事件 （用户点击观看按钮，或者游戏主动弹出广告时，这里即便是没有填充，也要发送）
//        PlayerUid:''    //字符串  玩家id   如果有就设置,没有就不设置该字段
//        Debug:''        //字符串  0 或 1 测试数据为1 否则设置该字段
//        Version:''      //字符串  游戏版本号  1.0.1
//        ServerUid:''    //字符串  服务器uid,没有就不设置该字段
//        DatVersion: '1' //字符串  固定1
//        AdPlace:''//广告在游戏中的播放位置,自定义,能区分开就行
//        AdType:'Rewarded',//广告类别 Rewarded:视频广告 Interstital:插屏
        app.logVersion = app.getPackageVersion();
        if (app.jsonDot != null && app.jsonDot.has(eventName)){
            try{
                String stringEvent = app.jsonDot.getString(eventName);
                javaLog(" java method: reqLogEvent() eventName: " + eventName + "; adPlace: " + adPlace + "; adType: " + adType
                        + "; stringEvent: " + stringEvent);
                AdjustEvent adjustEvent = new AdjustEvent(stringEvent);
                adjustEvent.addCallbackParameter("PlayerUid", app.logAdid);
                adjustEvent.addCallbackParameter("Debug", app.debugMode);
                adjustEvent.addCallbackParameter("Version", app.logVersion);
                adjustEvent.addCallbackParameter("DateVersion", "1");
                if (eventName.equals("AdReq")) {
                    app.logAdPlace = adPlace;
                    app.logAdType = adType;
                    adjustEvent.addCallbackParameter("AdPlace", adPlace);
                    adjustEvent.addCallbackParameter("AdType", adType);
                }
                Adjust.trackEvent(adjustEvent);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        Bundle params = new Bundle();
        String eventFirebase = "EVENT_" + eventName;
        javaLog(" java method: reqLogEvent() eventFireBase: " + eventFirebase);
        app.mFirebaseAnalytics.logEvent(eventFirebase, params);
    }

    public static void passLevelLogEvent(String eventName, String value, String passTime, String passCount) {
        app.logVersion = app.getPackageVersion();
        if (app.jsonDot != null && app.jsonDot.has(eventName)){
            try{
                String stringEvent = app.jsonDot.getString(eventName);
                javaLog(" java method: passLevelLogEvent() eventName: " + eventName + "; value: " +
                        value + "; passTime: " + passTime + "; passCount: " + passCount
                        + "; stringEvent: " + stringEvent);
                AdjustEvent adjustEvent = new AdjustEvent(stringEvent);
                adjustEvent.addCallbackParameter("Value", value);
                adjustEvent.addCallbackParameter("passTime", passTime);
                adjustEvent.addCallbackParameter("passCount", passCount);
                adjustEvent.addCallbackParameter("Version", app.logVersion);
                adjustEvent.addCallbackParameter("PlayerUid", app.logAdid);
                Adjust.trackEvent(adjustEvent);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        Bundle bundle = new Bundle();
        bundle.putString("LEVEL_NAME", value);
        bundle.putString("PASS_TIME", passTime);
        bundle.putString("PASS_COUNT", passCount);
        String eventFirebase = "EVENT_" + eventName;
        javaLog(" java method: passLevelLogEvent() eventFirebase: " + eventFirebase);
        app.mFirebaseAnalytics.logEvent(eventFirebase, bundle);
    }

    public static void AdDoneLogEvent(String eventName, String adPlace, String adType, String adUnitId, String network, String networkPlacement, String adRevenue) {

//        2.用户请求广告事件 （用户点击观看按钮，或者游戏主动弹出广告时，这里即便是没有填充，也要发送）
//        PlayerUid:''    //字符串  玩家id   如果有就设置,没有就不设置该字段
//        Debug:''        //字符串  0 或 1 测试数据为1 否则设置该字段
//        Version:''      //字符串  游戏版本号  1.0.1
//        ServerUid:''    //字符串  服务器uid,没有就不设置该字段
//        DatVersion: '1' //字符串  固定1
//        AdPlace:''//广告在游戏中的播放位置,自定义,能区分开就行
//        AdType:'Rewarded',//广告类别 Rewarded:视频广告 Interstital:插屏
//        AdUnitId:'',           // MaxSdkBase.AdInfo.AdUnitIdentifier
//        Network:'',            // MaxSdkBase.AdInfo.NetworkName
//        NetworkPlacement:"",   // MaxSdkBase.AdInfo.NetworkPlacement
//        AdRevenue:'',            // MaxSdkBase.AdInfo.Revenue
        app.logVersion = app.getPackageVersion();
        if (app.jsonDot != null && app.jsonDot.has(eventName)){
            try{
                String stringEvent = app.jsonDot.getString(eventName);
                javaLog(" java method: AdDoneLogEvent() eventName: " + eventName + "; adPlace_1: " + adPlace + "; AdType_2: " + adType +
                        "; adUnitId_3: " + adUnitId +"; network_4: " + network + "; networkPlacement_5: " + networkPlacement +
                        "; adRevenue_6: " + adRevenue + "; stringEvent: " + stringEvent);
                AdjustEvent adjustEvent = new AdjustEvent(stringEvent);
                adjustEvent.addCallbackParameter("PlayerUid", app.logAdid);
                adjustEvent.addCallbackParameter("Debug", app.debugMode);
                adjustEvent.addCallbackParameter("Version", app.logVersion);
                adjustEvent.addCallbackParameter("DateVersion", "1");
                adjustEvent.addCallbackParameter("AdPlace", adPlace);
                adjustEvent.addCallbackParameter("AdType", adType);
                adjustEvent.addCallbackParameter("AdUnitId", adUnitId);
                adjustEvent.addCallbackParameter("Network", network);
                adjustEvent.addCallbackParameter("NetworkPlacement", networkPlacement);
                adjustEvent.addCallbackParameter("AdRevenue", adRevenue);
                Adjust.trackEvent(adjustEvent);
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    public static void onCreateReview() {
        javaLog(" java method: onCreateReview() 评价 ");
        app.manager = ReviewManagerFactory.create(app);
        //manager = new FakeReviewManager(mContext); //模拟调用api是否正常，需要用上面代码发布测试渠道测试
        com.google.android.play.core.tasks.Task request = app.manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                app.reviewInfo = (ReviewInfo) task.getResult();
                javaLog(" java method: onCreateReview() CompleteListener initHwSDK gp: " + Thread.currentThread().getId());
            } else {
                // There was some problem, log or handle the error code.
                //@ReviewErrorCode int reviewErrorCode = ((TaskException) task.getException()).getErrorCode();
                javaLog(" java method: onCreateReview() CompleteListener initHwSDK gp: error: ");
            }
        });
    }

    public static void showComment() {
        javaLog(" java method: showComment() 评价 ");
        if (app.manager != null && app.reviewInfo != null) {
            com.google.android.play.core.tasks.Task flow = app.manager.launchReviewFlow(app, app.reviewInfo);
            flow.addOnCompleteListener(task -> {
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
                javaLog(" java method: showComment() CompleteListener success ");
            });
        }
    }

    /**
     * 跳转到应用商店评分
     *
     * @param myAppPkg
     * @param shopPkg
     */
    public static void goAppShop(String myAppPkg, String shopPkg) {
        if (TextUtils.isEmpty(myAppPkg)) {
            return;
        }

        try {
            Uri uri = Uri.parse("market://details?id=" + myAppPkg);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (!TextUtils.isEmpty(shopPkg)) {
                intent.setPackage(shopPkg);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(intent);
        } catch (Exception e) {
            // 如果没有该应用商店，则显示系统弹出的应用商店列表供用户选择
//            goAppShop(context, myAppPkg, "");
        }
    }

    public static void goPolicy() {
        try {
            Uri uri = Uri.parse("https://www.clicksplay.com/Policy.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(intent);
        } catch (Exception e) {
            // 如果没有该应用商店，则显示系统弹出的应用商店列表供用户选择
        }
    }

    public static void goTerms() {
        try {
            Uri uri = Uri.parse("https://www.clicksplay.com/Service.html");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            app.startActivity(intent);
        } catch (Exception e) {
            // 如果没有该应用商店，则显示系统弹出的应用商店列表供用户选择
        }
    }

    @Override

    public Cocos2dxGLSurfaceView onCreateView() {
        Cocos2dxGLSurfaceView glSurfaceView = new Cocos2dxGLSurfaceView(this);
        // TestCpp should create stencil buffer
        glSurfaceView.setEGLConfigChooser(5, 6, 5, 0, 16, 8);
        SDKWrapper.getInstance().setGLSurfaceView(glSurfaceView, this);
        return glSurfaceView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        javaLog(" java method: onResume() showInterAD: " + app.showInterAD);
        SDKWrapper.getInstance().onResume();
        app.showInterAD = true;
        signInSilently();
    }

    @Override
    protected void onPause() {
        super.onPause();
        javaLog(" java method: onPause() showInterAD: " + app.showInterAD);
        SDKWrapper.getInstance().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        javaLog(" java method: onDestroy() showInterAD: " + app.showInterAD);
        if (!isTaskRoot()) {
            return;
        }
        SDKWrapper.getInstance().onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKWrapper.getInstance().onActivityResult(requestCode, resultCode, data);
//        callbackManager.onActivityResult(requestCode, resultCode, data);//分享
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKWrapper.getInstance().onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SDKWrapper.getInstance().onRestart();
        javaLog(" java method: onRestart() showInterAD: " + app.showInterAD);
        if (app.showInterAD) {
//            app.showInterstitial();
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    toCocos("adsTimeTrue();");
                }
            });
        }
        app.showInterAD = true;
        app.closeBanner();
    }

    @Override
    protected void onStop() {
        javaLog(" java method: onStop() showInterAD: " + app.showInterAD);
        NotificationController.getInstance().onStop();
        super.onStop();
        SDKWrapper.getInstance().onStop();
    }

    @Override
    public void onBackPressed() {
        javaLog(" java method: onBackPressed() showInterAD: " + app.showInterAD);
        app.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                toCocos("outGamePage();");
            }
        });
//        onBackPressedAgain(this);//两次返回，退出程序，如果自己弹窗提示，则点击
        SDKWrapper.getInstance().onBackPressed();
//        super.onBackPressed();//两次返回，退出程序，必须注释掉这个
    }

    /**
     * 两次返回，退出程序
     */
    public static void onBackPressedAgain() {
        javaLog(" java method: onBackPressedAgain() SimpleName: " + app.getClass().getSimpleName());
//        if ((System.currentTimeMillis() - exitTime) > 2000) {
//            Toast.makeText(paramActivity, "再按一次退出程序", Toast.LENGTH_SHORT).show();
//            exitTime = System.currentTimeMillis();
//        } else {
//            //退出整个应用
//            Intent intent = new Intent(Intent.ACTION_MAIN);
//            intent.addCategory(Intent.CATEGORY_HOME);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            paramActivity.startActivity(intent);
//            paramActivity.finish();
//        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        app.startActivity(intent);
        app.finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SDKWrapper.getInstance().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        SDKWrapper.getInstance().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SDKWrapper.getInstance().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        SDKWrapper.getInstance().onStart();
        javaLog(" java method: onStart() showInterAD: " + app.showInterAD);
        super.onStart();
    }


    //applovin start

    private MaxInterstitialAd interstitialAd;
    private MaxRewardedAd rewardedAd;
    private int retryAttempt;

    void createInterstitialAd() {
        interstitialAd = new MaxInterstitialAd("b6e352d5f2ef3432", this);
        interstitialAd.setListener(this);

        // Load the first ad
        interstitialAd.loadAd();
    }

    void createRewardedAd() {
        rewardedAd = MaxRewardedAd.getInstance("17324133c1fc4de0", this);
        rewardedAd.setListener(this);
        rewardedAd.loadAd();
    }

    void createAdViewAd(final MaxAdFormat adFormat) {
        adView = new MaxAdView("4cda5456b5439d37", this);
        adView.setListener(app);

        adView.setVisibility(View.INVISIBLE);
        // Stretch to the width of the screen for banners to be fully functional
        int width = ViewGroup.LayoutParams.MATCH_PARENT;

        // Banner height on phones and tablets is 50 and 90, respectively
        int heightPx = AppLovinSdkUtils.dpToPx(this, (int) getResources().getDimension(R.dimen.banner_height));

        adView.setLayoutParams(new FrameLayout.LayoutParams(width, 150, Gravity.BOTTOM));
        // Set background or background color for banners to be fully functional
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.addView(adView);
        // Load the ad
        adView.loadAd();
    }

    @Override
    public void onAdExpanded(MaxAd ad) {

    }

    @Override
    public void onAdCollapsed(MaxAd ad) {
    }

    @Override
    public void onUserRewarded(MaxAd ad, MaxReward reward) {
        app.VideoCompleted = true;//这里赋值 true Hidden里面调用，只有reward 会进入这里
        javaLog(" java method: onUserRewarded() VideoCompleted: " + app.VideoCompleted);
    }

    @Override
    public void onRewardedVideoStarted(MaxAd ad) {
        javaLog(" java method: onRewardedVideoStarted() ");
        if (!app.mVedioEventSent) {
            app.mVedioEventSent = true;
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    toCocos("videoStart();");
                }
            });
        }
    }

    @Override
    public void onRewardedVideoCompleted(MaxAd ad) { }

    @Override
    public void onAdLoaded(MaxAd ad) {
        retryAttempt = 0;
    }

    @Override
    public void onAdDisplayed(MaxAd ad) {
        interstitialAd.loadAd();
        rewardedAd.loadAd();
        double revenue = ad.getRevenue();//获取收入值
        Bundle bundle = new Bundle();
        app.adRevenue = adRevenue + revenue;
        String networkName = ad.getNetworkName(); // Display name of the network which showed the ad (e.g."AdColony")
        String adFormat = ad.getFormat().toString();
        javaLog(" java method: onAdDisplayed() app.adRevenue: " + app.adRevenue
                + "; networkName: " + networkName + "; adFormat: " + adFormat + "; currentLevel: " + app.currentLevel);
        if (app.adRevenue >= 0.01) {
            bundle.putDouble(FirebaseAnalytics.Param.VALUE, app.adRevenue);
            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
            bundle.putString("adNetwork", networkName);
            bundle.putString("adFormat", adFormat);
            app.mFirebaseAnalytics.logEvent("Ad_Impression_Revenue", bundle);

            bundle.putDouble(FirebaseAnalytics.Param.VALUE, app.adRevenue * 0.85);
            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
            bundle.putString("adNetwork", networkName);
            bundle.putString("adFormat", adFormat);
            app.mFirebaseAnalytics.logEvent("Ad_Impression_Revenue_Off", bundle);

            app.adRevenue = 0;
        }

        // 每次都传，不管多少
        if (revenue > 0) {
            bundle.putDouble(FirebaseAnalytics.Param.VALUE, revenue);
            bundle.putString(FirebaseAnalytics.Param.CURRENCY, "USD");
            bundle.putString("adNetwork", networkName);
            bundle.putString("adFormat", adFormat);
            // 关卡数大于5  大于7 处理事件
            if (app.currentLevel > 5){
                app.mFirebaseAnalytics.logEvent("level_5_Revenue", bundle);
            }
            if (app.currentLevel > 7){
                app.mFirebaseAnalytics.logEvent("level_7_Revenue", bundle);
            }
        }

        app.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                toCocos("adRevenueAdd('" + app.adRevenue + "');");
            }
        });
    }

    @Override
    public void onAdHidden(MaxAd ad) {
        // Interstitial ad is hidden. Pre-load the next ad
        interstitialAd.loadAd();
        // rewarded ad is hidden. Pre-load the next ad
        rewardedAd.loadAd();
        javaLog(" java method: onAdHidden() showInterAD: " + app.showInterAD + "; ad: " + ad.getFormat().toString());

        String eventName = "AdDone";
        if (ad.getFormat().toString().indexOf("REWARDED") == -1) {
            AdDoneLogEvent(eventName, app.logAdPlace, "Interstital", ad.getAdUnitId(), ad.getNetworkName(), ad.getNetworkPlacement(), "" + ad.getRevenue());
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    app.showInterAD = true;
                    toCocos("advertFinish();");
                }
            });
        } else if (ad.getFormat().getLabel() == "REWARDED" && app.VideoCompleted) {
            facebookLogEvent("119_Debug_all_ad_impression");
            facebookLogEvent("122_Ads_All_direct_play_finished");
            AdDoneLogEvent(eventName, app.logAdPlace, "Rewarded", ad.getAdUnitId(), ad.getNetworkName(), ad.getNetworkPlacement(), "" + ad.getRevenue());
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    toCocos("videoFinish();");
                }
            });
        }
        app.VideoCompleted = false;
    }

    @Override
    public void onAdClicked(MaxAd ad) {

    }

    @Override
    public void onAdLoadFailed(String adUnitId, MaxError error) {
        // Rewarded ad failed to load
        // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)
        // Interstitial ad failed to load
        // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)
        retryAttempt++;
        long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rewardedAd.loadAd();
                interstitialAd.loadAd();
            }
        }, delayMillis);
    }

    @Override
    public void onAdDisplayFailed(MaxAd ad, MaxError error) {
        // Rewarded ad failed to display. We recommend loading the next ad
        javaLog(" java method: onAdDisplayFailed() ad: " + ad.getFormat().toString());
        if (ad.getFormat().toString().indexOf("REWARDED") == -1) {
            app.runOnGLThread(new Runnable() {
                @Override
                public void run() {
                    toCocos("advertFail();");
                }
            });
        }
        rewardedAd.loadAd();
        interstitialAd.loadAd();
    }
    //applovin end

    public static void share(String level) {
        app.showInterAD = false;
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Block blockTube");
            String shareMessage = "Let me recommend you this application";
            shareMessage = shareMessage + "http://blockfish.gamescasual007.com/share.php?lv=" + level;
            javaLog(" java method: share() shareMessage: " + shareMessage);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            app.startActivity(Intent.createChooser(shareIntent, "share"));
        } catch (Exception e) {
            app.collectErr(e.toString());
        }
    }

    public static void cloudLoadStart() {
//        boolean isNetAvailable = app.isNetAvailable(app.getApplicationContext());
//        javaLog(" java method: cloudLoadStart() isNetAvailable: " + isNetAvailable);
//        if (isNetAvailable) {
//            app.loadSnapshot();
//        }
    }

    public static void saveGame(String json) {
//        javaLog(" java method: saveGame() json: " + json);
//        SnapshotsClient snapshotsClient =
//                PlayGames.getSnapshotsClient(app);
//        int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;
//        snapshotsClient.open(app.mCurrentSaveName, true, conflictResolutionPolicy)
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        javaLog(" java method: saveGame.snapshotsClient.open.addOnFailureListener error: " + e);
//                    }
//                }).continueWith(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, byte[]>() {
//            @Override
//            public byte[] then(@NonNull com.google.android.gms.tasks.Task<SnapshotsClient.DataOrConflict<Snapshot>> task)
//                    throws Exception {
//                try {
//                    SnapshotsClient.DataOrConflict<Snapshot> result = task.getResult();
//                    Task<Snapshot> snapshotToWrite = app.processSnapshotOpenResult(result, 0);
//                    if (snapshotToWrite == null) {
//                        // No snapshot available yet; waiting on the user to choose one.
//                        return null;
//                    }
//
//                    Snapshot snapshot = task.getResult().getData();
//                    snapshot.getSnapshotContents().writeBytes(json.getBytes());
//                    SnapshotMetadataChange metadataChange = new SnapshotMetadataChange.Builder()
////                                    .setCoverImage(app.getScreenShot())
//                            .setDescription("=========saveGame=succ=Modified data at: " + Calendar.getInstance().getTime())
//                            .build();
//                    SnapshotsClient snapshotsClient = PlayGames.getSnapshotsClient(app);
//                    snapshotsClient.commitAndClose(snapshot, metadataChange);
//                } catch (Exception e) {
//                    javaLog(" java method: saveGame.snapshotsClient.open.continueWith error: " + e);
//                }
//                return null;
//            }
//        }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
//            @Override
//            public void onComplete(@NonNull com.google.android.gms.tasks.Task<byte[]> task) {
//                // Dismiss progress dialog and reflect the changes in the UI when complete.
//                // ...
//                if (task.isSuccessful()) {
//                    javaLog(" java method: saveGame.snapshotsClient.open.addOnCompleteListener saveGame complete");
//                } else {
////                            handleException(task.getException(), getString(R.string.write_snapshot_error));
//                    javaLog(" java method: saveGame.snapshotsClient.open.addOnCompleteListener saveGame fail: " +
//                            task.getException().getMessage());
//                }
//            }
//        });
    }

    private void signInSilently() {
        GamesSignInClient gamesSignInClient = PlayGames.getGamesSignInClient(app);
        gamesSignInClient.isAuthenticated().addOnCompleteListener(isAuthenticatedTask -> {
            boolean isAuthenticated =
                    (isAuthenticatedTask.isSuccessful() &&
                            isAuthenticatedTask.getResult().isAuthenticated());
            if (isAuthenticated) {
                // Continue with Play Games Services
//                PlayGames.getPlayersClient(app).getCurrentPlayer().addOnCompleteListener(mTask -> {
//                            // Get PlayerID with mTask.getResult().getPlayerId()
//                        }
//                );
            } else {
                // Disable your integration with Play Games Services or show a
                // login button to ask  players to sign-in. Clicking it should
                // call GamesSignInClient.signIn().
            }
        });
    }

    Task<byte[]> loadSnapshot() {
        // Display a progress dialog
        // Get the SnapshotsClient from the signed in account.
        javaLog(" java method: loadSnapshot() ");
        SnapshotsClient snapshotsClient =
                PlayGames.getSnapshotsClient(app);

        // In the case of a conflict, the most recently modified version of this snapshot will be used.
        int conflictResolutionPolicy = SnapshotsClient.RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED;

        // Open the saved game using its name.
        return snapshotsClient.open(app.mCurrentSaveName, true, conflictResolutionPolicy)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        javaLog(" java method: loadSnapshot.snapshotsClient.open.addOnFailureListener e: " + e);
                    }
                }).continueWith(new Continuation<SnapshotsClient.DataOrConflict<Snapshot>, byte[]>() {
                    @Override
                    public byte[] then(@NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task) throws Exception {

                        // Opening the snapshot was a success and any conflicts have been resolved.
                        try {
                            SnapshotsClient.DataOrConflict<Snapshot> result = task.getResult();
                            Task<Snapshot> snapshotToWrite = app.processSnapshotOpenResult(result, 0);
                            if (snapshotToWrite == null) {
                                // No snapshot available yet; waiting on the user to choose one.
                                javaLog(" java method: loadSnapshot.snapshotsClient.open.continueWith snapshotToWrite: null");
                                return null;
                            }

                            Snapshot snapshot = task.getResult().getData();
                            // Extract the raw data from the snapshot.
                            byte[] dataByte = snapshot.getSnapshotContents().readFully();
                            javaLog(" java method: loadSnapshot.snapshotsClient.open.continueWith dataByte: " + dataByte.length);
                            // 解析成类似字典格式
                            app.runOnGLThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 拆成每个200字符的字符串 循环传给js端
                                    toCocos("cloudLoadSucce('" + new String(dataByte) + "');");
                                }
                            });
                            return dataByte;
                        } catch (IOException e) {
                            javaLog(" java method: loadSnapshot.snapshotsClient.open.continueWith e: " + e);
                        }
                        return null;
                    }
                }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                    @Override
                    public void onComplete(@NonNull Task<byte[]> task) {
                        // Dismiss progress dialog and reflect the changes in the UI when complete.
                        // ...
                        if (task.isSuccessful()) {
                            javaLog(" java method: loadSnapshot.snapshotsClient.open.addOnCompleteListener success ");
                        } else {
//                            handleException(task.getException(), getString(R.string.write_snapshot_error));
                            javaLog(" java method: loadSnapshot.snapshotsClient.open.addOnCompleteListener fail: " +
                                    task.getException().getMessage());
                            app.runOnGLThread(new Runnable() {
                                @Override
                                public void run() {
                                    // 拆成每个200字符的字符串 循环传给js端
                                    toCocos("cloudLoadError();");
                                }
                            });
                        }
                    }
                });
    }

    Task<Snapshot> processSnapshotOpenResult(SnapshotsClient.DataOrConflict<Snapshot> result,
                                             final int retryCount) {

        if (!result.isConflict()) {
            // There was no conflict, so return the result of the source.
            TaskCompletionSource<Snapshot> source = new TaskCompletionSource<>();
            source.setResult(result.getData());
            return source.getTask();
        }

        // There was a conflict.  Try resolving it by selecting the newest of the conflicting snapshots.
        // This is the same as using RESOLUTION_POLICY_MOST_RECENTLY_MODIFIED as a conflict resolution
        // policy, but we are implementing it as an example of a manual resolution.
        // One option is to present a UI to the user to choose which snapshot to resolve.
        SnapshotsClient.SnapshotConflict conflict = result.getConflict();

        Snapshot snapshot = conflict.getSnapshot();
        Snapshot conflictSnapshot = conflict.getConflictingSnapshot();

        // Resolve between conflicts by selecting the newest of the conflicting snapshots.
        Snapshot resolvedSnapshot = snapshot;

        if (snapshot.getMetadata().getLastModifiedTimestamp() <
                conflictSnapshot.getMetadata().getLastModifiedTimestamp()) {
            resolvedSnapshot = conflictSnapshot;
        }

        return PlayGames.getSnapshotsClient(app)
                .resolveConflict(conflict.getConflictId(), resolvedSnapshot)
                .continueWithTask(
                        new Continuation<
                                SnapshotsClient.DataOrConflict<Snapshot>,
                                Task<Snapshot>>() {
                            @Override
                            public Task<Snapshot> then(
                                    @NonNull Task<SnapshotsClient.DataOrConflict<Snapshot>> task)
                                    throws Exception {
                                // Resolving the conflict may cause another conflict,
                                // so recurse and try another resolution.
                                if (retryCount < MAX_SNAPSHOT_RESOLVE_RETRIES) {
                                    return processSnapshotOpenResult(task.getResult(), retryCount + 1);
                                } else {
                                    throw new Exception("Could not resolve snapshot conflicts");
                                }
                            }
                        });
    }

    /**
     * 发送消息给JS
     * @param data jons格式字符串
     */
    public static void postMsgToJs(final JSONObject data){
        app.runOnGLThread(new Runnable() {
            @Override
            public void run() {
                javaLog(" java method: postMsgToJs() data: " + data);
                String sData = data.toString();
                sData = sData.replace("\"", "\\\"");
                String sClass = "var mopub = __require('mopub');";
                String sFunction = "mopub.prototype.reciveAndroidMsg(\\\"\" + sData + \"\\\");";
                Cocos2dxJavascriptJavaBridge.evalString(sClass + sFunction);
            }
        });
    }

    /**
     * 接收来自JS的消息
     */
    public static String revJsMessage(String data) {
        try{
            javaLog(" java method: revJsMessage() data: " + data);
            JSONObject revObj = new JSONObject(data);
            if (revObj != null && revObj.has("currentLevel")){
                int levelFromData = revObj.getInt("currentLevel");
                if (levelFromData > app.currentLevel){
                    app.currentLevel = levelFromData;
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return "";
    }

    /**
     * java 调用 cocos
     * @param param
     */
    public static void toCocos(String param){
        String _data = "cc['NativeCall'].instance." + param;
        javaLog(" javaToCocos: " + _data);
        Cocos2dxJavascriptJavaBridge.evalString(_data);
    }

    /**
     * 统一输出格式
     * @param data
     */
    public static void javaLog(String data){
        Log.d(TAG, "--输出：" + data);
    }
}
