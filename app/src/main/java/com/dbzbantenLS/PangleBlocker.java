package com.dbzbantenLS;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class PangleBlocker implements IXposedHookLoadPackage {

    private static final String TAG = "PangleBlocker";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Pangle/ByteDance SDK classes
        String[] pangleClasses = {
            "com.bytedance.sdk.openadsdk.TTAdSdk",
            "com.bytedance.sdk.openadsdk.TTAdManager",
            "com.bytedance.sdk.openadsdk.TTAdNative",
            "com.bytedance.sdk.openadsdk.TTFullScreenVideoAd",
            "com.bytedance.sdk.openadsdk.TTRewardVideoAd",
            "com.bytedance.sdk.openadsdk.TTNativeExpressAd",
            "com.pgl.ss.PSSDK",
            "com.pgl.ss.sp.SpManager"
        };

        for (String className : pangleClasses) {
            try {
                Class<?> clazz = XposedHelpers.findClassIfExists(className, lpparam.classLoader);
                if (clazz != null) {
                    XposedBridge.log(TAG + ": Found " + className + " in " + lpparam.packageName);
                    hookPangleAds(lpparam, clazz);
                }
            } catch (Exception e) {
                // Class not found, continue
            }
        }
    }

    private void hookPangleAds(XC_LoadPackage.LoadPackageParam lpparam, Class<?> clazz) {
        try {
            java.lang.reflect.Method[] methods = clazz.getDeclaredMethods();
            for (java.lang.reflect.Method method : methods) {
                String methodName = method.getName();
                if (methodName.equals("show") || methodName.contains("show")) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Blocked Pangle ad show");
                            param.setResult(null);
                        }
                    });
                }
                if (methodName.equals("load") || methodName.equals("loadAd") || methodName.contains("load")) {
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + ": Blocked Pangle ad load");
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