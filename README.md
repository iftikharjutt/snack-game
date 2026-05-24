# Snack Game

A simple Android Snake game written in Java with a custom `View`.

## Build

The repository includes a GitHub Actions workflow that builds a debug APK on every push to `main`.

To build locally, install the Android SDK and Gradle, then run:

```bash
gradle assembleDebug
```

The APK is produced at:

```text
app/build/outputs/apk/debug/app-debug.apk
```
