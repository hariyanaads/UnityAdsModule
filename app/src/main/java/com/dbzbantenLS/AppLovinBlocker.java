package com.dbzbantenLS;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AppLovinBlocker implements IXposedHookLoadPackage {

    private static final String TAG = "AppLovinBlocker";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // AppLovin SDK classes
        String[] appLovinClasses = {
            "com.applovin.sdk.AppLovinSdk",
            "com.applovin.impl.sdk.AppLovinSdkImpl",
            "com.applovin.adview.AppLovinInterstitialAd",
            "com.applovin.adview.AppLovinRewardedVideo",
            "com.applovin.impl.adview.AdViewControllerImpl",
            "com.applovin.mediation.ads.MaxAdView",
            "com.applovin.mediation.ads.MaxInterstitialAd",
            "com.applovin.mediation.ads.MaxRewardedAd"
        };

        for (String className : appLovinClasses) {
            try {
                Class<?> clazz = XposedHelpers.findClassIfExists(className, lpparam.classLoader);
                if (clazz != null) {
                    XposedBridge.log(TAG + ": Found " + className + " in " + lpparam.packageName);
                    hookAppLovinAds(lpparam, clazz);
                }
            } catch (Exception e) {
                // Class not found, continue
            }
        }
    }

    private void hookAppLovinAds(XC_LoadPackage.LoadPackageParam lpparam, Class<?> clazz) {
        try {
            // Hook show methods
            java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName();
                if (methodName.equals("show") || methodName.equals("showAd")) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Blocked AppLovin ad show");
                            param.setResult(null);
                        }
                    });
                }
                if (methodName.equals("load") || methodName.equals("loadAd") || methodName.equals("loadAds")) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Blocked AppLovin ad load");
                            param.setResult(null);
                        }
                    });
                }
            }
        } catch (Exception e) {
            XposedBridge.log(TAG + ": Error hooking " + clazz.getName() + ": " + e.getMessage());
        }
    }
}