package com.dbzbantenLS;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import android.content.Context;

public class UnityAdsHook {
    
    private static final String TAG = "UnityAdsHook";
    
    public void hook(XC_LoadPackage.LoadPackageParam lpparam, Context context) {
        try {
            // Hook Unity Ads Initialization
            XposedHelpers.findAndHookMethod("com.unity3d.ads.UnityAds", 
                lpparam.classLoader, "initialize", 
                Context.class, String.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(TAG + ": Unity Ads Initialize Blocked");
                        param.setResult(null);
                    }
                });
            
            // Hook Show Ads
            XposedHelpers.findAndHookMethod("com.unity3d.ads.UnityAds", 
                lpparam.classLoader, "show", 
                Context.class, String.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log(TAG + ": Unity Ads Show Blocked");
                        param.setResult(null);
                    }
                });
            
            // Hook isReady with placementId parameter
            XposedHelpers.findAndHookMethod("com.unity3d.ads.UnityAds", 
                lpparam.classLoader, "isReady", 
                String.class, 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(false);
                    }
                });
            
            // Hook isReady without parameter (overloaded method)
            try {
                XposedHelpers.findAndHookMethod("com.unity3d.ads.UnityAds", 
                    lpparam.classLoader, "isReady", 
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            param.setResult(false);
                        }
                    });
            } catch (Exception e) {
                // Method might not exist in all versions
            }
            
            // Hook load method
            try {
                XposedHelpers.findAndHookMethod("com.unity3d.ads.UnityAds", 
                    lpparam.classLoader, "load", 
                    String.class, 
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Unity Ads Load Blocked");
                            param.setResult(null);
                        }
                    });
            } catch (Exception e) {
                // Method might not exist
            }
            
            XposedBridge.log(TAG + ": Unity Ads hooks applied");
            
        } catch (Exception e) {
            XposedBridge.log(TAG + " Error: " + e.getMessage());
        }
    }
}