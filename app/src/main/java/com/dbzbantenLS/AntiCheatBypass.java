package com.dbzbantenLS;

import android.app.Activity;
import android.content.Context;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Debug;
import android.os.SystemClock;
import android.provider.Settings;

import java.io.File;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AntiCheatBypass {

    private static final String TAG = "AntiCheatBypass";

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!MainHook.isAntiCheatBypassEnabled()) return;

        try {
            bypassRootDetection(lpparam);
            bypassEmulatorDetection(lpparam);
            bypassDebugDetection(lpparam);
            bypassIntegrityChecks(lpparam);
            bypassXposedSpecificDetection(lpparam);
            preventCrashes(lpparam);

        } catch (Exception e) {
            MainHook.log("Error in AntiCheatBypass: " + e.getMessage());
        }
    }

    private void bypassXposedSpecificDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(ClassLoader.class, "loadClass", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String className = (String) param.args[0];
                        if (className == null) return;

                        String lowerName = className.toLowerCase();
                        if (lowerName.contains("xposed") ||
                            lowerName.contains("de.robv.android") ||
                            lowerName.contains("lsposed") ||
                            lowerName.contains("lspatch")) {
                            param.setThrowable(new ClassNotFoundException(className));
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(Class.class, "forName", String.class, boolean.class,
                ClassLoader.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String className = (String) param.args[0];
                        if (className == null) return;

                        String lowerName = className.toLowerCase();
                        if (lowerName.contains("xposed") ||
                            lowerName.contains("de.robv.android") ||
                            lowerName.contains("lsposed")) {
                            param.setThrowable(new ClassNotFoundException(className));
                        }
                    }
                });

            try {
                XposedHelpers.findAndHookMethod("android.content.pm.PackageManager", lpparam.classLoader,
                    "getPackageInfo", String.class, int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            String packageName = (String) param.args[0];
                            if (packageName == null) return;

                            String lowerName = packageName.toLowerCase();
                            if (lowerName.contains("xposed") ||
                                lowerName.contains("lsposed") ||
                                lowerName.contains("hook") ||
                                packageName.equals("com.dbzbantenLS")) {
                                param.setThrowable(new android.content.pm.PackageManager.NameNotFoundException());
                            }
                        }
                    });
            } catch (Exception e) {
            }

            try {
                XposedHelpers.findAndHookMethod("android.app.ActivityThread", lpparam.classLoader,
                    "currentApplication",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) {
                            try {
                                Object app = param.getResult();
                                if (app != null) {
                                    XposedHelpers.findAndHookMethod(app.getClass(), "getApplicationInfo",
                                        new XC_MethodHook() {
                                            @Override
                                            protected void afterHookedMethod(MethodHookParam param2) {
                                                Object info = param2.getResult();
                                                if (info != null) {
                                                    try {
                                                        java.util.Map metaData = (java.util.Map) XposedHelpers.getObjectField(info, "metaData");
                                                        if (metaData != null) {
                                                            metaData.remove("xposedmodule");
                                                            metaData.remove("xposedminversion");
                                                            metaData.remove("xposeddescription");
                                                        }
                                                    } catch (Exception e) {
                                                    }
                                                }
                                            }
                                        });
                                }
                            } catch (Exception e) {
                            }
                        }
                    });
            } catch (Exception e) {
            }

            MainHook.log("Xposed-specific detection bypass active");

        } catch (Exception e) {
            MainHook.log("Error in Xposed-specific bypass: " + e.getMessage());
        }
    }

    private void bypassRootDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(File.class, "exists", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    File file = (File) param.thisObject;
                    String path = file.getAbsolutePath().toLowerCase();

                    String[] rootPaths = {
                        "/su", "/system/bin/su", "/system/xbin/su",
                        "/sbin/su", "/su/bin/su", "/magisk",
                        "/data/adb/magisk", "/data/adb/ksu",
                        "/data/adb/apatch", "/system/app/superuser.apk",
                        "/system/xbin/daemonsu", "/system/etc/init.d",
                        "/system/bin/.ext", "/system/xbin/.ext",
                        "/dev/com.koushikdutta.superuser.daemon",
                        "/system/app/Kinguser.apk", "/data/adb/modules"
                    };

                    for (String rootPath : rootPaths) {
                        if (path.equals(rootPath) || path.startsWith(rootPath + "/")) {
                            param.setResult(false);
                            return;
                        }
                    }
                }
            });

            XposedHelpers.findAndHookMethod(Runtime.class, "exec",
                String[].class, String[].class, File.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String[] cmd = (String[]) param.args[0];
                        if (cmd != null && cmd.length > 0) {
                            String command = cmd[0].toLowerCase();

                            String[] blockedCmds = {
                                "su", "which", "busybox", "magisk", "ksud",
                                "apatch", "supersu", "root"
                            };

                            for (String blocked : blockedCmds) {
                                if (command.contains(blocked)) {
                                    param.setThrowable(new java.io.IOException("Command not found"));
                                    return;
                                }
                            }
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(ProcessBuilder.class, "start",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        // Block process builder execution for root commands
                    }
                });

            MainHook.log("Root detection bypass active");

        } catch (Exception e) {
            MainHook.log("Error bypassing root detection: " + e.getMessage());
        }
    }

    private void bypassEmulatorDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.setStaticObjectField(Build.class, "DEVICE", "sailfish");
            XposedHelpers.setStaticObjectField(Build.class, "MANUFACTURER", "Google");
            XposedHelpers.setStaticObjectField(Build.class, "BRAND", "google");
            XposedHelpers.setStaticObjectField(Build.class, "MODEL", "Pixel");
            XposedHelpers.setStaticObjectField(Build.class, "PRODUCT", "sailfish");
            XposedHelpers.setStaticObjectField(Build.class, "HARDWARE", "sailfish");
            XposedHelpers.setStaticObjectField(Build.class, "BOARD", "sailfish");
            XposedHelpers.setStaticObjectField(Build.class, "FINGERPRINT",
               "google/sailfish/sailfish:13/TQ3A.230805.001/10320090:user/release-keys");
            XposedHelpers.setStaticObjectField(Build.class, "ID", "TQ3A.230805.001");
            XposedHelpers.setStaticObjectField(Build.class, "TAGS", "release-keys");
            XposedHelpers.setStaticObjectField(Build.class, "TYPE", "user");
            XposedHelpers.setStaticObjectField(Build.class, "USER", "android-build");

            try {
                XposedHelpers.setStaticObjectField(Build.class, "BOOTLOADER", "sailfish-8996-012345");
            } catch (Exception e) {
            }

            try {
                XposedHelpers.setStaticObjectField(Build.class, "RADIO", "8996-012345-123456");
            } catch (Exception e) {
            }

            try {
                XposedHelpers.setStaticObjectField(Build.class, "SERIAL", "HT1234567890");
            } catch (Exception e) {
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                XposedHelpers.findAndHookMethod(Build.class, "getSerial",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.setResult("HT1234567890");
                        }
                    });
            }

            XposedHelpers.findAndHookMethod(NetworkInterface.class, "getByName", String.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String name = (String) param.args[0];
                        if (name != null && (name.toLowerCase().contains("eth") ||
                            name.toLowerCase().contains("tun") ||
                            name.toLowerCase().contains("veth"))) {
                            param.setResult(null);
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(NetworkInterface.class, "getNetworkInterfaces",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        try {
                            Enumeration<NetworkInterface> interfaces = (Enumeration<NetworkInterface>) param.getResult();
                            if (interfaces != null) {
                                java.util.List<NetworkInterface> filteredList = new java.util.ArrayList<>();
                                while (interfaces.hasMoreElements()) {
                                    NetworkInterface ni = interfaces.nextElement();
                                    String name = ni.getName().toLowerCase();
                                    if (!name.contains("eth") && !name.contains("veth") &&
                                        !name.contains("tun") && !name.contains("virbr")) {
                                        filteredList.add(ni);
                                    }
                                }
                                param.setResult(Collections.enumeration(filteredList));
                            }
                        } catch (Exception e) {
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(Settings.Secure.class, "getString",
                ContentResolver.class, String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String key = (String) param.args[1];
                        if (key != null && key.equals(Settings.Secure.ANDROID_ID)) {
                            String pkg = lpparam.packageName;
                            String fakeId = String.format("%016x", pkg.hashCode());
                            fakeId = fakeId + fakeId;
                            param.setResult(fakeId.substring(0, 16));
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(Settings.System.class, "getString",
                ContentResolver.class, String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        String key = (String) param.args[1];
                        if (key != null && key.equals("device_name")) {
                            param.setResult("Pixel");
                        }
                    }
                });

            XposedHelpers.findAndHookMethod("android.app.ActivityManager", lpparam.classLoader,
                "getRunningAppProcesses",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        java.util.List<?> processes = (java.util.List<?>) param.getResult();
                        if (processes != null) {
                            java.util.Iterator<?> it = processes.iterator();
                            while (it.hasNext()) {
                                Object proc = it.next();
                                try {
                                    String procName = (String) XposedHelpers.getObjectField(proc, "processName");
                                    if (procName != null && (
                                        procName.contains("xposed") ||
                                        procName.contains("lsposed") ||
                                        procName.contains("lspatch"))) {
                                        it.remove();
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                });

            MainHook.log("Emulator detection bypass active");

        } catch (Exception e) {
            MainHook.log("Error bypassing emulator detection: " + e.getMessage());
        }
    }

    private void bypassDebugDetection(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(Debug.class, "isDebuggerConnected",
                XC_MethodReplacement.returnConstant(false));

            XposedHelpers.findAndHookMethod(Debug.class, "waitingForDebugger",
                XC_MethodReplacement.returnConstant(false));

            XposedHelpers.findAndHookMethod(SystemClock.class, "uptimeMillis",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        long uptime = (Long) param.getResult();
                        param.setResult(uptime + (long)(Math.random() * 5000));
                    }
                });

            XposedHelpers.findAndHookMethod(SystemClock.class, "elapsedRealtime",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        long time = (Long) param.getResult();
                        param.setResult(time + (long)(Math.random() * 5000));
                    }
                });

            XposedHelpers.findAndHookMethod(System.class, "currentTimeMillis",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        long time = (Long) param.getResult();
                        param.setResult(time + (long)(Math.random() * 100));
                    }
                });

            XposedHelpers.findAndHookMethod(Debug.class, "getNativeHeapAllocatedSize",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        param.setResult(50L * 1024 * 1024);
                    }
                });

            XposedHelpers.findAndHookMethod(Debug.class, "getNativeHeapSize",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        param.setResult(100L * 1024 * 1024);
                    }
                });

            MainHook.log("Debug detection bypass active");

        } catch (Exception e) {
            MainHook.log("Error bypassing debug detection: " + e.getMessage());
        }
    }

    private void bypassIntegrityChecks(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod("android.content.pm.PackageManager", lpparam.classLoader,
                "getPackageInfo", String.class, int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        int flags = (int) param.args[1];
                        if ((flags & android.content.pm.PackageManager.GET_SIGNATURES) != 0 ||
                            (flags & 0x08000000) != 0) {
                            Object packageInfo = param.getResult();
                            if (packageInfo != null) {
                                try {
                                    XposedHelpers.setObjectField(packageInfo, "signatures", null);
                                    XposedHelpers.setObjectField(packageInfo, "signingInfo", null);
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                });

            XposedHelpers.findAndHookMethod("java.security.Signature", lpparam.classLoader,
                "verify", byte[].class,
                XC_MethodReplacement.returnConstant(true));

            XposedHelpers.findAndHookMethod("java.security.MessageDigest", lpparam.classLoader,
                "isEqual", byte[].class, byte[].class,
                XC_MethodReplacement.returnConstant(true));

            XposedHelpers.findAndHookMethod("javax.crypto.Cipher", lpparam.classLoader,
                "doFinal",
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                    }
                });

            XposedHelpers.findAndHookMethod("android.content.pm.PackageManager", lpparam.classLoader,
                "checkSignatures", String.class, String.class,
                XC_MethodReplacement.returnConstant(android.content.pm.PackageManager.SIGNATURE_MATCH));

            MainHook.log("Integrity checks bypass active");

        } catch (Exception e) {
            MainHook.log("Error bypassing integrity checks: " + e.getMessage());
        }
    }

    private void preventCrashes(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            XposedHelpers.findAndHookMethod(Thread.class, "setDefaultUncaughtExceptionHandler",
                Thread.UncaughtExceptionHandler.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Thread.UncaughtExceptionHandler handler =
                            (Thread.UncaughtExceptionHandler) param.args[0];
                        if (handler != null) {
                            param.args[0] = new Thread.UncaughtExceptionHandler() {
                                @Override
                                public void uncaughtException(Thread t, Throwable e) {
                                    Throwable filtered = filterStackTrace(e);
                                    handler.uncaughtException(t, filtered);
                                }
                            };
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(System.class, "exit", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int code = (int) param.args[0];
                        if (code != 0) {
                            MainHook.log("System.exit(" + code + ") called");
                        }
                    }
                });

            XposedHelpers.findAndHookMethod(Runtime.class, "halt", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        MainHook.log("Runtime.halt() blocked");
                        param.setResult(null);
                    }
                });

            XposedHelpers.findAndHookMethod("android.os.Process", lpparam.classLoader,
                "killProcess", int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        int pid = (int) param.args[0];
                        if (pid == android.os.Process.myPid()) {
                            MainHook.log("Process.killProcess(self) blocked");
                            param.setResult(null);
                        }
                    }
                });

            MainHook.log("Crash prevention active");

        } catch (Exception e) {
            MainHook.log("Error in crash prevention: " + e.getMessage());
        }
    }

    private Throwable filterStackTrace(Throwable throwable) {
        if (throwable == null) return null;

        StackTraceElement[] stack = throwable.getStackTrace();
        java.util.List<StackTraceElement> filtered = new java.util.ArrayList<>();

        for (StackTraceElement element : stack) {
            String className = element.getClassName();
            if (!className.contains("xposed") &&
                !className.contains("XposedBridge") &&
                !className.contains("de.robv.android")) {
                filtered.add(element);
            }
        }

        throwable.setStackTrace(filtered.toArray(new StackTraceElement[0]));

        Throwable cause = throwable.getCause();
        if (cause != null) {
            filterStackTrace(cause);
        }

        return throwable;
    }
}