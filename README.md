# VitalsDiary

A private, on-device symptom timeline organizer. Log how a health issue is
progressing — severity, notes, a photo, a voice note — and generate a clean
PDF timeline to bring to a doctor's visit.

**VitalsDiary does not diagnose, predict, or assess any medical condition.**
It exists to solve a much more common problem: forgetting the details between
"it started a while ago" and the moment you're actually in front of a doctor.

## Features
- Track one or more health "cases" (e.g. "Persistent cough", "Skin rash")
- Log dated entries: severity (1–10), a note, an optional selfie photo, an optional voice note
- A trend indicator (Improving / Worsening / Stable) computed with a simple
  least-squares regression over your logged severities — not a diagnosis, just
  a way to see the shape of the data at a glance
- One-tap PDF export of the full timeline, generated on-device with Android's
  built-in PDF engine, ready to share or print for a doctor's visit
- Fully offline: no internet permission, nothing ever leaves the device
- Freemium: 1 free case, unlimited with Premium

## Requirements
- Android 8.0 (API 26) or newer
- Camera and microphone are optional; the app works without granting either

## Build and test
Needs JDK 17 and the Android Gradle Plugin 8.5.

Easiest: open the folder in **Android Studio** and press Run.

From the command line:
```bash
gradle testDebugUnitTest   # run the unit tests for the core logic
gradle assembleDebug       # build app/build/outputs/apk/debug/app-debug.apk
```

The domain logic (`in.devexis.vitalsdiary.core`) has no Android dependency and
is fully unit tested on the JVM:
- `TrendAnalyzer` — improving/worsening/stable detection
- `CaseLimitPolicy` — the free-vs-premium case limit
- `TimelineTextBuilder` — the text that becomes the exported PDF

## Continuous integration and releases
`.github/workflows/android.yml` runs the unit tests and builds a debug APK on
every push and pull request. Push a tag such as `v1.0.0` and it also attaches
the APK to a GitHub Release.
*(Note: GitHub blocks pushing workflow files via the standard Contents API, so
add this file manually once via the GitHub web UI — see the commit history
for the exact YAML, or ask whoever set up the repo.)*

## Publishing to Google Play
1. Create a keystore once and keep it safe:
   `keytool -genkey -v -keystore vitalsdiary.jks -keyalg RSA -keysize 2048 -validity 10000 -alias vitalsdiary`
2. Set `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` and run `gradle bundleRelease`
3. Upload `app/build/outputs/bundle/release/app-release.aab` in the Play Console
4. Use [`docs/STORE_LISTING.md`](docs/STORE_LISTING.md) for the listing text, and add screenshots
5. Wire `PremiumManager.setPremium()` to a verified purchase from the
   [Google Play Billing Library](https://developer.android.com/google/play/billing)
   instead of the demo Settings toggle before shipping
6. Because this app touches health-adjacent data, read Play's
   [Health content policy](https://support.google.com/googleplay/android-developer/answer/9878810)
   before submitting — the language throughout this app is intentionally
   framed as record-keeping, never diagnosis, to stay compliant

## Privacy
See [`docs/PRIVACY.md`](docs/PRIVACY.md). In short: no internet permission,
no analytics, no cloud sync. Everything stays in the app's private storage on
the device until the user explicitly shares a PDF.

## License
MIT, see [LICENSE](LICENSE). Made by Devexis India.
