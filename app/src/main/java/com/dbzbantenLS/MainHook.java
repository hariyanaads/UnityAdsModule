package com.dbzbantenLS;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import android.app.Application;
import android.content.Context;

import org.json.JSONObject;
import java.io.File;

public class MainHook implements IXposedHookLoadPackage {
    
    private static final String TAG = "DbzBantenLS";
    private static final String CONFIG_FILE = "/unityads_config.json";
    
    // Static configuration cache
    private static int rewardMultiplier = 100000;
    private static boolean antiCheatBypassEnabled = true;
    private static boolean trafficManipulationEnabled = true;
    private static boolean blockAnalytics = true;
    private static boolean debugMode = false;
    private static boolean blockAppLovin = true;
    private static boolean blockPangle = true;
    private static int gamesHooked = 0;
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Skip system packages
        if (lpparam.packageName.startsWith("android") || 
            lpparam.packageName.startsWith("com.android")) {
            return;
        }
        
        // Auto-detect Unity Ads SDK
        if (lpparam.packageName.contains("unity") || 
            lpparam.packageName.contains("game") ||
            isUnityAdsPresent(lpparam)) {
            
            log("Target app detected: " + lpparam.packageName);
            
            XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    
                    // Load config from app data
                    loadConfig(context);
                    
                    // Inject Unity Ads Hook
                    new UnityAdsHook().hook(lpparam, context);
                    
                    // Inject Anti-Cheat Bypass
                    if (isAntiCheatBypassEnabled()) {
                        new AntiCheatBypass().handleLoadPackage(lpparam);
                    }
                    
                    // Inject Storage Bypass
                    new StorageBypass().hook(lpparam);
                    
                    // Inject Traffic Manipulator
                    if (isTrafficManipulationEnabled()) {
                        new TrafficManipulator().handleLoadPackage(lpparam);
                    }
                    
                    // Inject Reward Spoofer
                    new RewardSpoofer().handleLoadPackage(lpparam);
                    
                    // Inject AppLovin Blocker
                    if (isBlockAppLovin()) {
                        new AppLovinBlocker().handleLoadPackage(lpparam);
                    }
                    
                    // Inject Pangle Blocker
                    if (isBlockPangle()) {
                        new PangleBlocker().handleLoadPackage(lpparam);
                    }
                    
                    incrementGamesHooked();
                    log("Hooks applied successfully for: " + lpparam.packageName);
                }
            });
        }
    }
    
    private boolean isUnityAdsPresent(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class.forName("com.unity3d.ads.UnityAds", false, lpparam.classLoader);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    private void loadConfig(Context context) {
        try {
            File configFile = new File(context.getFilesDir().getAbsolutePath() + CONFIG_FILE);
            if (!configFile.exists()) {
                log("Config file not found, using defaults");
                return;
            }
            
            StringBuilder jsonString = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(configFile));
            String line;
            while ((line = reader.readLine()) != null) {
                jsonString.append(line);
            }
            reader.close();
            
            JSONObject json = new JSONObject(jsonString.toString());
            rewardMultiplier = json.optInt("reward_multiplier", 100000);
            antiCheatBypassEnabled = json.optBoolean("anti_cheat_bypass", true);
            trafficManipulationEnabled = json.optBoolean("traffic_manipulation", true);
            blockAnalytics = json.optBoolean("block_analytics", true);
            debugMode = json.optBoolean("debug_mode", false);
            blockAppLovin = json.optBoolean("block_applovin", true);
            blockPangle = json.optBoolean("block_pangle", true);
            gamesHooked = json.optInt("games_hooked", 0);
            
        } catch (Exception e) {
            log("Error loading config: " + e.getMessage());
        }
    }
    
    // Static getter methods
    public static int getRewardMultiplier() {
        return rewardMultiplier;
    }
    
    public static boolean isAntiCheatBypassEnabled() {
        return antiCheatBypassEnabled;
    }
    
    public static boolean isTrafficManipulationEnabled() {
        return trafficManipulationEnabled;
    }
    
    public static boolean isBlockAnalytics() {
        return blockAnalytics;
    }
    
    public static boolean isDebugMode() {
        return debugMode;
    }
    
    public static boolean isBlockAppLovin() {
        return blockAppLovin;
    }
    
    public static boolean isBlockPangle() {
        return blockPangle;
    }
    
    public static void incrementGamesHooked() {
        gamesHooked++;
    }
    
    public static void log(String message) {
        if (debugMode) {
            XposedBridge.log(TAG + ": " + message);
        }
    }
}