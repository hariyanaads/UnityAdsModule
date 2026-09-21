package com.dbzbantenLS;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.File;

public class StorageBypass {
    
    private static final String TAG = "StorageBypass";
    
    public void hook(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            // Hook File.exists() to hide LSPosed/Module files
            XposedHelpers.findAndHookMethod(File.class, "exists", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    File file = (File) param.thisObject;
                    String path = file.getAbsolutePath().toLowerCase();
                    
                    // Hide LSPosed traces
                    if (path.contains("lsposed") || 
                        path.contains("xposed") ||
                        path.contains("zygisk") ||
                        path.contains("magisk") ||
                        path.contains("/su") ||
                        path.contains("busybox")) {
                        param.setResult(false);
                    }
                }
            });
            
            // Hook getAbsolutePath to mask module path
            XposedHelpers.findAndHookMethod(File.class, "getAbsolutePath", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String result = (String) param.getResult();
                    if (result != null && result.toLowerCase().contains("unityadsmodule")) {
                        param.setResult(result.replaceAll("(?i)UnityAdsModule", "SystemModule"));
                    }
                }
            });
            
            // Hook Runtime.exec to hide su commands
            XposedHelpers.findAndHookMethod(Runtime.class, "exec", String[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String[] cmd = (String[]) param.args[0];
                    if (cmd != null && cmd.length > 0) {
                        String command = cmd[0].toLowerCase();
                        if (command.contains("su") || command.contains("magisk") || command.contains("busybox")) {
                            param.setResult(null);
                        }
                    }
                }
            });
            
            XposedBridge.log(TAG + ": Storage bypass active");
            
        } catch (Exception e) {
            XposedBridge.log(TAG + " Error: " + e.getMessage());
        }
    }
}