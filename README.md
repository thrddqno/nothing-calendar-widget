# Nothing Calendar

Native Kotlin Android app providing three independent home-screen widgets inspired by Apple Calendar layouts and Nothing OS visual language.

## Run 2 status

Run 2 adds read-only Android calendar integration:

- 2×2 Month Calendar widget placeholder
- 2×2 Upcoming Events widget placeholder
- 4×6 Combined widget placeholder
- Calendar permission settings screen
- Material You dynamic colors with fallback colors
- Calendar discovery across provider/account types
- CalendarContract.Instances occurrence queries
- Recurrence, all-day, overnight, and local time-zone transformations
- Chronological upcoming-event processing with ongoing events
- Per-date month indicator counts and calendar colors
- Persistent DataStore calendar selection
- Explicit permission-denied and provider-failure results

Widget views remain Run 1 placeholders. Run 2 builds and tests data layer only; no fake events are shown.

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

Open **Nothing Calendar**, then grant calendar access. Calendar repository queries remain read-only.

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

Source packages separate calendar data/domain, each widget provider, rendering, theme, settings, and navigation.

- `AndroidCalendarDataSource` owns permission-guarded CalendarContract cursor access.
- `AndroidCalendarRepository` applies one calendar selection to agenda and month queries off main thread.
- `CalendarEventProcessor` handles deterministic sorting, deduplication, date spans, and month grouping.
- `CalendarSelectionResolver` distinguishes default selection from an explicit empty selection.
- `CalendarDataModule.repository(context)` constructs the production repository.

## Run 2 limitations

- No real month grid
- No event data rendered in widgets yet
- No calendar-selection UI yet; persistence API is ready for future UI
- No widget configuration UI
- No periodic refresh scheduling

## Recommended next steps

1. Bind repository results to RemoteViews renderers.
2. Build locale-aware month grid renderer.
3. Render upcoming all-day and timed events.
4. Add calendar-selection settings UI.
5. Add refresh triggers for calendar changes, time, locale, and timezone.
