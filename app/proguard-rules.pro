# Keep app entry points and simple runtime classes
-keep class daka.work.day.** { *; }
-keepattributes *Annotation*

# Ignore warnings during optimization
-dontwarn androidx.**
-dontwarn kotlin.**
