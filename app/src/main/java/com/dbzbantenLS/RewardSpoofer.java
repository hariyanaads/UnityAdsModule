package com.dbzbantenLS;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class RewardSpoofer {

    private static final String TAG = "RewardSpoofer";
    private static final Random random = new Random();

    private static final Map<String, Boolean> hookedMethods = new HashMap<>();

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            hookSpecificClasses(lpparam);
            hookCurrency(lpparam);
            hookPremium(lpparam);

        } catch (Exception e) {
            MainHook.log("Error in RewardSpoofer: " + e.getMessage());
        }
    }

    private void hookSpecificClasses(XC_LoadPackage.LoadPackageParam lpparam) {
        String[] classPatterns = {
            "RewardManager", "CurrencyManager", "EconomyManager",
            "PlayerData", "GameData", "UserData", "Profile",
            "Inventory", "Wallet", "Bank", "Store",
            "AdManager", "AdController", "AdsManager",
            "GameManager", "AppManager", "MainManager"
        };

        for (String pattern : classPatterns) {
            try {
                Class<?> clazz = XposedHelpers.findClassIfExists(
                    lpparam.packageName + "." + pattern, lpparam.classLoader);

                if (clazz == null) {
                    clazz = XposedHelpers.findClassIfExists(pattern, lpparam.classLoader);
                }

                if (clazz != null) {
                    hookClassMethods(clazz);
                }

            } catch (Exception e) {
            }
        }
    }

    private void hookClassMethods(Class<?> clazz) {
        try {
            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                String methodKey = clazz.getName() + "." + method.getName();

                if (hookedMethods.containsKey(methodKey)) continue;

                String name = method.getName().toLowerCase();
                Class<?> returnType = method.getReturnType();

                boolean isRewardMethod = false;

                String[] rewardPatterns = {
                    "getreward", "getcoins", "getgems", "getgold",
                    "getmoney", "getcash", "getcredit", "getpoints",
                    "getbalance", "getamount", "getquantity",
                    "getprize", "getbonus", "getpayout"
                };

                for (String pattern : rewardPatterns) {
                    if (name.contains(pattern)) {
                        isRewardMethod = true;
                        break;
                    }
                }

                if (isRewardMethod && isNumericType(returnType)) {
                    hookRewardMethod(method, methodKey);
                }
            }

        } catch (Exception e) {
            MainHook.log("Error hooking class " + clazz.getName() + ": " + e.getMessage());
        }
    }

    private boolean isNumericType(Class<?> type) {
        return type == int.class || type == Integer.class ||
               type == long.class || type == Long.class ||
               type == double.class || type == Double.class ||
               type == float.class || type == Float.class;
    }

    private void hookRewardMethod(Method method, String methodKey) {
        try {
            hookedMethods.put(methodKey, true);

            XposedBridge.hookMethod(method, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    try {
                        Object result = param.getResult();
                        if (result == null) return;

                        int multiplier = MainHook.getRewardMultiplier();
                        if (multiplier <= 1) return;

                        if (result instanceof Integer) {
                            int original = (Integer) result;
                            if (original > 0 && original < 100000) {
                                int spoofed = original * multiplier;
                                param.setResult(spoofed);
                                MainHook.log("Spoofed: " + method.getName() + " " + original + " -> " + spoofed);
                            }
                        } else if (result instanceof Long) {
                            long original = (Long) result;
                            if (original > 0 && original < 100000) {
                                long spoofed = original * multiplier;
                                param.setResult(spoofed);
                                MainHook.log("Spoofed: " + method.getName() + " " + original + " -> " + spoofed);
                            }
                        } else if (result instanceof Double) {
                            double original = (Double) result;
                            if (original > 0 && original < 100000) {
                                double spoofed = original * multiplier;
                                param.setResult(spoofed);
                                MainHook.log("Spoofed: " + method.getName() + " " + original + " -> " + spoofed);
                            }
                        } else if (result instanceof Float) {
                            float original = (Float) result;
                            if (original > 0 && original < 100000) {
                                float spoofed = original * multiplier;
                                param.setResult(spoofed);
                                MainHook.log("Spoofed: " + method.getName() + " " + original + " -> " + spoofed);
                            }
                        }
                    } catch (Exception e) {
                        MainHook.log("Error in reward hook: " + e.getMessage());
                    }
                }
            });

        } catch (Exception e) {
            MainHook.log("Error hooking method " + method.getName() + ": " + e.getMessage());
        }
    }

    private void hookCurrency(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            String[] classPatterns = {
                "CurrencyManager", "EconomyManager", "PlayerData",
                "GameData", "Wallet", "Inventory", "Bank"
            };

            for (String pattern : classPatterns) {
                try {
                    Class<?> clazz = XposedHelpers.findClassIfExists(
                        lpparam.packageName + "." + pattern, lpparam.classLoader);

                    if (clazz == null) {
                        clazz = XposedHelpers.findClassIfExists(pattern, lpparam.classLoader);
                    }

                    if (clazz == null) continue;

                    Method[] methods = clazz.getDeclaredMethods();

                    for (Method method : methods) {
                        String name = method.getName().toLowerCase();

                        boolean isAddMethod = false;
                        String[] addPatterns = {"add", "give", "earn", "receive", "grant", "award"};
                        String[] currencyPatterns = {"coin", "gem", "gold", "money", "cash", "credit"};

                        for (String addPattern : addPatterns) {
                            if (name.contains(addPattern)) {
                                for (String currPattern : currencyPatterns) {
                                    if (name.contains(currPattern)) {
                                        isAddMethod = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (isAddMethod) {
                            String methodKey = clazz.getName() + "." + method.getName() + "_add";
                            if (hookedMethods.containsKey(methodKey)) continue;

                            hookedMethods.put(methodKey, true);

                            XposedBridge.hookMethod(method, new XC_MethodHook() {
                                @Override
                                protected void beforeHookedMethod(MethodHookParam param) {
                                    try {
                                        int multiplier = MainHook.getRewardMultiplier();
                                        if (multiplier <= 1) return;

                                        for (int i = 0; i < param.args.length; i++) {
                                            Object arg = param.args[i];

                                            if (arg instanceof Integer) {
                                                int original = (Integer) arg;
                                                if (original > 0 && original < 100000) {
                                                    int spoofed = original * multiplier;
                                                    param.args[i] = spoofed;
                                                    MainHook.log("Add spoofed: " + original + " -> " + spoofed);
                                                }
                                            } else if (arg instanceof Long) {
                                                long original = (Long) arg;
                                                if (original > 0 && original < 100000) {
                                                    long spoofed = original * multiplier;
                                                    param.args[i] = spoofed;
                                                    MainHook.log("Add spoofed: " + original + " -> " + spoofed);
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        MainHook.log("Error in add hook: " + e.getMessage());
                                    }
                                }
                            });
                        }
                    }

                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
            MainHook.log("Error hooking currency: " + e.getMessage());
        }
    }

    private void hookPremium(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            String[] premiumPatterns = {
                "isPremium", "isPro", "isVip", "isUnlocked",
                "hasPremium", "hasPro", "isPaid", "isLicensed",
                "isPurchased", "isBought", "isSubscribed"
            };

            String[] billingClasses = {
                "Billing", "IAP", "InAppPurchase", "Purchase",
                "BillingManager", "IAPManager", "Store"
            };

            for (String billingClass : billingClasses) {
                try {
                    Class<?> clazz = XposedHelpers.findClassIfExists(
                        lpparam.packageName + "." + billingClass, lpparam.classLoader);

                    if (clazz == null) {
                        clazz = XposedHelpers.findClassIfExists(billingClass, lpparam.classLoader);
                    }

                    if (clazz == null) continue;

                    Method[] methods = clazz.getDeclaredMethods();

                    for (Method method : methods) {
                        String name = method.getName();
                        String lowerName = name.toLowerCase();

                        boolean isPremiumMethod = false;
                        for (String pattern : premiumPatterns) {
                            if (lowerName.contains(pattern.toLowerCase())) {
                                isPremiumMethod = true;
                                break;
                            }
                        }

                        if (isPremiumMethod && method.getReturnType() == boolean.class) {
                            String methodKey = clazz.getName() + "." + name + "_premium";
                            if (hookedMethods.containsKey(methodKey)) continue;

                            hookedMethods.put(methodKey, true);

                            XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(true));
                            MainHook.log("Premium hook: " + method.getName());
                        }
                    }

                } catch (Exception e) {
                }
            }

        } catch (Exception e) {
            MainHook.log("Error hooking premium: " + e.getMessage());
        }
    }
}