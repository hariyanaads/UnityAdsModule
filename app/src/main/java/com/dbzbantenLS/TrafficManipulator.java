package com.dbzbantenLS;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TrafficManipulator {

    private static final String TAG = "TrafficManipulator";
    private static final Random random = new Random();

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!MainHook.isTrafficManipulationEnabled()) return;

        try {
            hookUrlConnections(lpparam);
            hookDns(lpparam);
            hookHttp(lpparam);

        } catch (Exception e) {
            MainHook.log("Error in TrafficManipulator: " + e.getMessage());
        }
    }

    private void hookUrlConnections(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(URL.class, "openConnection",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        URL url = (URL) param.thisObject;
                        String host = url.getHost().toLowerCase();

                        if (isAdHost(host)) {
                            try {
                                Thread.sleep(100 + random.nextInt(400));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(URL.class, "openStream",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        URL url = (URL) param.thisObject;
                        String host = url.getHost().toLowerCase();

                        if (isAdHost(host)) {
                            try {
                                Thread.sleep(100 + random.nextInt(400));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }
                });

        } catch (Exception e) {
            MainHook.log("Error hooking URL connections: " + e.getMessage());
        }
    }

    private void hookDns(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(InetAddress.class, "getByName", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String host = (String) param.args[0];
                        if (host == null) return;

                        String lowerHost = host.toLowerCase();

                        if (isAdHost(lowerHost)) {
                            try {
                                param.setResult(InetAddress.getByName("127.0.0.1"));
                                MainHook.log("DNS blocked: " + host);
                            } catch (Exception e) {
                            }
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(InetAddress.class, "getAllByName", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String host = (String) param.args[0];
                        if (host == null) return;

                        if (isAdHost(host.toLowerCase())) {
                            try {
                                param.setResult(new InetAddress[] {
                                    InetAddress.getByName("127.0.0.1")
                                });
                            } catch (Exception e) {
                            }
                        }
                    }
                });

        } catch (Exception e) {
            MainHook.log("Error hooking DNS: " + e.getMessage());
        }
    }

    private void hookHttp(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(HttpURLConnection.class, "getResponseCode",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        try {
                            Thread.sleep(50 + random.nextInt(200));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(HttpURLConnection.class, "getInputStream",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        try {
                            Thread.sleep(50 + random.nextInt(200));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });

        } catch (Exception e) {
            MainHook.log("Error hooking HTTP: " + e.getMessage());
        }
    }

    private boolean isAdHost(String host) {
        String[] adPatterns = {
            "unityads", "applvn", "applovin", "supersonicads", "ironsrc",
            "mopub", "inmobi", "vungle", "adcolony", "chartboost", "tapjoy",
            "fyber", "hyprmx", "pangle", "bigo", "mintegral", "facebook",
            "doubleclick", "googlesyndication", "googleads", "admob",
            "adjust", "appsflyer", "kochava", "singular", "tenjin",
            "gameanalytics", "firebase", "crashlytics"
        };

        String lowerHost = host.toLowerCase();
        for (String pattern : adPatterns) {
            if (lowerHost.contains(pattern)) {
                return true;
            }
        }

        return false;
    }
}