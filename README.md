# LobeHub E-Ink for BOOX Leaf5C

Unofficial Android WebView shell for [LobeHub Cloud](https://app.lobehub.com), optimized for BOOX Leaf5C. Not affiliated with LobeHub or BOOX.

## Get APK

Open **Actions → Build BOOX Leaf5C APK** and select the latest successful build, or click **Run workflow**. Download the `LobeHub-EInk-Leaf5C-APK` artifact, unzip it, and install `LobeHub-EInk-Leaf5C-debug.apk` on your BOOX.

- Android 6.0+ (`minSdk 23`)
- No native shared libraries (`.so`): ABI-independent APK for 32/64-bit Android
- Java WebView with file picker, back navigation, high-contrast/light display guidance and optional animation reduction
- Uses the system WebView; very old WebView versions may not support the live LobeHub site
- BOOX refresh mode is configured through the device's E-Ink Center / EinkWise; no proprietary SDK included
- Google OAuth may block embedded WebViews. Browser logins do not transfer automatically into the app.

The artifact is a **debug-signed** APK; CI runner-generated signing keys may change between runs. Reinstallation might require uninstalling the old APK (which clears app-local storage). Do not rely on local data as the only copy.

## Local build

With JDK 17, Android SDK 35, and Gradle 8.9 installed:

```sh
gradle --no-daemon assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Project files are committed directly to the repository root. The CI checks that the built APK contains no native `.so` libraries.
