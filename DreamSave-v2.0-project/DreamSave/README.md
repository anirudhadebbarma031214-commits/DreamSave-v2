# DreamSave v2.0

A premium, futuristic savings-goal tracker for Android.
Kotlin, Jetpack Compose, Material 3. Package: `com.aniruddha.dreamsave`.

> **Honest note:** this project was written without access to an Android SDK, so it was **not compiled
> locally**. The included GitHub Actions workflow builds the APK on GitHub's servers. If the very first
> cloud build shows a compile error, copy the error text from the failed step and it can be fixed quickly.

## Features
- Multiple savings goals: create, edit, delete (name, target, starting amount, optional deadline, optional description)
- Quick deposits (₹10, ₹20, ₹50, ₹100, ₹200, ₹500, ₹1000) plus custom amount and note
- Animated progress ring and bars, percentage, remaining amount
- Daily saving recommendation (based on deadline, or a 30-day plan if there is none)
- Current streak and best streak
- Deposit history with search and 7-day / 30-day filters; delete any deposit
- Goal completion celebration (confetti)
- Statistics: totals, averages, 7-day chart, per-goal breakdown
- Settings: Dark / Light / System theme, celebrations on/off, reset data
- Data is saved to a private JSON file on the device and survives app restarts

## Pinned versions (chosen to be compatible)
| Tool | Version |
|---|---|
| Gradle | 8.9 |
| Android Gradle Plugin | 8.7.3 |
| Kotlin (+ Compose compiler plugin) | 2.0.21 |
| Compose BOM | 2024.10.01 (Material 3 1.3.0) |
| JDK | 17 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 26 |

## Build the APK in the cloud (no Android SDK needed on your computer)
1. Create a new **empty** repository on github.com.
2. Upload **everything inside the `DreamSave` folder** (including the hidden `.github` folder) to the repository root.
3. Open the **Actions** tab, choose **Build DreamSave APK**, click **Run workflow**.
4. Wait about 5 to 10 minutes for the green check.
5. Open the finished run, scroll to **Artifacts**, download **DreamSave-v2.0-apk** (a zip containing `DreamSave-v2.0.apk`).
6. Unzip it, copy the APK to your phone, allow "install unknown apps", and install.

The workflow (`.github/workflows/build-apk.yml`) checks out the code, installs JDK 17, the Android SDK
(platform 35, build-tools 34 and 35), sets up Gradle 8.9, creates the Gradle wrapper if it is missing,
runs `./gradlew assembleDebug`, verifies that `app/build/outputs/apk/debug/app-debug.apk` exists,
renames it to `DreamSave-v2.0.apk`, and uploads it as an artifact.

## About the Gradle wrapper
`gradle/wrapper/gradle-wrapper.properties` is included. The binary `gradle-wrapper.jar`, `gradlew` and
`gradlew.bat` are generated automatically by the workflow (`gradle wrapper --gradle-version 8.9`).
If you build locally in Android Studio, it creates them for you as well.

## Build locally (optional)
Open the folder in Android Studio (Ladybug or newer), let it sync, then run `./gradlew assembleDebug`.
Output: `app/build/outputs/apk/debug/app-debug.apk`.
