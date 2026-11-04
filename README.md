# Nothing Calendar

Native Kotlin Android app providing three independent home-screen widgets inspired by Apple Calendar layouts and Nothing OS visual language.

## Run 1 status

Run 1 supplies project foundation only:

- 2×2 Month Calendar widget placeholder
- 2×2 Upcoming Events widget placeholder
- 4×6 Combined widget placeholder
- Calendar permission settings screen
- Material You dynamic colors with fallback colors
- DataStore settings foundation
- Calendar repository, theme resolver, and widget renderer interfaces

No calendar events are queried or rendered yet. Placeholders never present fake events.

## Requirements

- Android Studio with Android SDK 37
- JDK 17 or newer supported by Gradle
- Android device or emulator running Android 10 (API 29) or newer

## Build

```bash
./gradlew assembleDebug
```

Debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Install

Connect a device with USB debugging enabled, then run:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Open **Nothing Calendar**, then grant calendar access if desired. Run 1 does not read calendar data.

## Add widgets

1. Long-press an empty launcher area.
2. Open widget picker.
3. Find **Nothing Calendar**.
4. Add Month, Upcoming, or Combined independently.

Launcher grid sizing varies. Metadata requests 2×2, 2×2, and 4×6 target cells respectively.

## Test

Run unit tests and lint:

```bash
./gradlew testDebugUnitTest lintDebug
```

Run instrumented registration tests on connected device/emulator:

```bash
./gradlew connectedDebugAndroidTest
```

## Architecture

Source packages separate calendar data/domain, each widget provider, rendering, theme, settings, and navigation. `CalendarRepository` is intentionally only an interface in Run 1; later work can implement it with `CalendarContract` after permission grant.

## Run 1 limitations

- No real month grid
- No CalendarContract query implementation
- No event list
- No widget configuration UI
- No periodic refresh scheduling

## Recommended next steps

1. Implement permission-aware CalendarContract repository.
2. Build locale-aware month grid renderer.
3. Render upcoming all-day and timed events.
4. Add refresh triggers for calendar changes, time, locale, and timezone.
5. Add widget appearance settings and visual regression tests.
