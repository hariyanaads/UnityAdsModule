# Keep Xposed entry point and hook classes.
-keep class com.rewardmax.hook.MainHook { *; }
-keep class com.rewardmax.hook.** { *; }

# Xposed annotations/callbacks are loaded reflectively.
-keepattributes *Annotation*
