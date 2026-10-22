# LobeHub E-Ink for BOOX Leaf5C

Unofficial minimal Android WebView client for https://app.lobehub.com/ on BOOX e-ink tablets. Not affiliated with BOOX or LobeHub.

## Get your APK

Open **Actions → Build BOOX Leaf5C APK**. Select the latest successful run (or use **Run workflow**), then download the artifact **LobeHub-EInk-Leaf5C-APK**. Extract and install `LobeHub-EInk-Leaf5C-debug.apk`.

This app uses Java and the system WebView, so the APK is **ABI-independent** with no native `.so` files (32-bit ARM compatible subject to Android OS and WebView version). Android 6.0+ is required. The build uses JDK 17, Gradle 8.9 and Android Gradle Plugin 8.7.3.

The toolbar has Home, Reload, e-ink CSS toggle and Open in Browser. Configure HD/Speed refresh using BOOX's E-Ink Center, and select LobeHub's light theme. Google OAuth may refuse embedded WebView sign-in. Downloads are delegated to the browser and authenticated downloads may fail.

**Signing note:** CI debug APK signing keys may differ between runs, preventing in-place updates. Uninstalling will erase local app data. A persistent release keystore can be added later.

### Local build

```bash
gradle --no-daemon assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
