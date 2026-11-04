# Nothing Calendar

Native Kotlin Android app providing three independent home-screen widgets inspired by Apple Calendar layouts and Nothing OS visual language.

## Run 3.4 status

Run 3.4 implements all three widgets on the shared calendar pipeline:

- 2×2 Month Calendar widget
- 2×2 Upcoming Events widget
- 4×6 Combined Month + Upcoming widget
- Calendar permission settings screen
- Material You dynamic colors with fallback colors
- Calendar discovery across provider/account types
- CalendarContract.Instances occurrence queries
- Recurrence, all-day, overnight, and local time-zone transformations
- Chronological upcoming-event processing with ongoing events
- Per-date month indicator counts and calendar colors
- Persistent DataStore calendar selection
- Explicit permission-denied and provider-failure results
- Shared immutable month and agenda presentation models
- Monday/Sunday 42-cell visible month grid calculation
- Device-zone date boundaries and deterministic clock support
- Asynchronous widget loading with stale-update and deletion guards
- Diagnostic RemoteViews output from real load states
- Complete seven-column 2×2 month calendar
- Locale-aware weekday labels and Monday/Sunday week starts
- Dynamic four-, five-, or six-row month grids
- Compact today highlight and neutral per-date event markers
- Width-aware abbreviated/full month heading
- Calendar remains usable when permission or provider data is unavailable
- Responsive one-to-four-row upcoming-event capacity
- Locale/device-zone 12-hour or 24-hour event labels
- Ongoing, all-day, tomorrow, and later-date labels
- Accurate overflow counts and calendar-color accent bars
- Safe untitled-event and empty/error states
- Vertically stacked complete month and upcoming-events sections
- Deterministic combined sizing that prioritizes the complete month grid
- Shared RemoteViews month and agenda section renderers
- Safe calendar-opening fallback from the combined month heading

Month, Upcoming, and Combined render real calendar data. No fabricated events are shown.

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
- `WidgetDataLoader` builds one shared snapshot from repository and DataStore state.
- `WidgetSnapshotFactory` performs pure month-grid and agenda transformations.
- `WidgetUpdateCoordinator` runs provider loads asynchronously and rejects stale/deleted widget updates.
- `MonthWidgetRenderingEngine` renders the compact month using RemoteViews only.
- `AgendaWidgetRenderingEngine` renders responsive upcoming-event rows.
- `AgendaEventFormatter` and `AgendaCapacityPolicy` keep formatting and sizing deterministic.
- `CombinedWidgetRenderingEngine` stacks both reusable sections from one immutable snapshot.
- `CombinedLayoutPolicy` reserves the complete month before allocating agenda rows.

## Run 3.4 limitations

- No calendar-selection UI yet; persistence API is ready for future UI
- No widget configuration UI
- No periodic refresh scheduling
- Event-specific and date-specific navigation remain prepared for Run 5

## Recommended next steps

1. Add full Nothing Dynamic/Translucent styling.
2. Add event-specific navigation.
3. Add calendar-selection settings UI.
4. Add refresh triggers for calendar changes, time, locale, and timezone.
