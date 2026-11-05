# Nothing Calendar

Native Kotlin Android app providing four independent home-screen widgets inspired by Apple Calendar layouts and Nothing OS visual language.

## Run 4.1 status

Run 4.1 establishes Nothing-inspired typography and design tokens across all four widgets while retaining the Run 3.7 calendar pipeline:

- 2×2 Month Calendar widget
- 2×2 Upcoming Events widget
- 4×4 Combined Month + Upcoming widget
- 4×2 Today + Upcoming widget
- Calendar permission settings screen
- Deterministic opaque dark widget surfaces with semantic color tokens
- Locally bundled Doto display typography
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
- Responsive complete-row upcoming-event capacity
- Locale/device-zone 12-hour or 24-hour event labels
- Ongoing, all-day, tomorrow, and later-date labels
- Accurate overflow counts and calendar-color accent bars
- Safe untitled-event and empty/error states
- Vertically stacked complete month and upcoming-events sections
- Deterministic 4×4 combined sizing with a nested rounded agenda panel
- Shared RemoteViews month and agenda section renderers
- Safe calendar-opening fallback from the combined month heading
- Locale- and time-zone-aware large current-date presentation
- Width, height, and font-scale-aware horizontal agenda capacity
- Safe event-specific calendar intents with occurrence timestamps
- Shared rolling 12-hour agenda eligibility across all agenda widgets
- Exact overflow counts limited to undisplayed events inside that window
- Explicit 12-hour AM/PM event presentation
- Per-widget snapshot isolation and current-month resize reuse
- Permission-safe cache invalidation
- Shared cross-widget ordering, deduplication, and eligibility behavior

All four widgets render real calendar data. No fabricated events are shown.

## Typography and font license

Display typography uses the variable [Doto](https://github.com/oliverlalan/Doto) font by Óliver Lalan and the Doto Project Authors. The bundled file comes from Google Fonts revision `2c982e6bdf88fefbe9e34e78695d8e9e02d788ec` (SHA-256 `6f4fe7d37853b91df3698daa84cde2dbe1c9695d88c986e6510134910337d426`) and is licensed under the SIL Open Font License 1.1. The complete license is packaged at `app/src/main/res/raw/doto_ofl.txt`; no font is downloaded at runtime.

- Month and section headings: Doto Bold, weight 700
- Large Today date: Doto ExtraBold, weight 800
- Event titles and calendar numbers: Android `sans-serif-medium`, weight 500
- Event metadata and weekday labels: Android `sans-serif`, weight 400

Android's font fallback chain handles glyphs not included in Doto. Event titles, times, weekdays, calendar dates, status messages, and overflow labels intentionally remain system sans-serif for compact readability.

## Widget design tokens

`widget_colors.xml` defines opaque dark surfaces, primary/secondary/muted text, divider, subtle border, Nothing red, and today foreground colors. Calendar-provider colors remain event accent colors when contrast is sufficient. `widget_dimens.xml` defines shared spacing, radii, accent thickness, indicators, divider, and component padding. `widget_styles.xml` maps semantic typography roles to Doto or Android sans-serif.

`WidgetResponsiveSizingPolicy` remains the source of truth for runtime padding, text sizing, row heights, complete month geometry, and agenda capacity. XML dimensions provide component defaults only. Combined still supports six event rows where available space permits.

Widgets use native XML `TextView`, `ImageView`, `LinearLayout`, and bundled font resources through `RemoteViews`. No custom views, downloadable fonts, bitmap text, Compose, Glance, wallpaper colors, blur, or translucency are used. Resource tests can verify typeface loading and layout role mapping, but launcher-level font appearance still requires device validation.

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
4. Add Month, Upcoming, Combined, or Today + Upcoming independently.

Launcher grid sizing varies. Metadata requests 2×2, 2×2, 4×4, and 4×2 target cells respectively.

## Calendar access and refresh behavior

Nothing Calendar requests read-only `READ_CALENDAR` access. It never creates, modifies, or deletes calendar data.

Widgets refresh when Android delivers an AppWidget update, when a widget is placed, and when a data refresh is otherwise requested by the existing widget lifecycle. Layout-only resize reuses a valid current-month snapshot and re-evaluates the rolling 12-hour window against the current clock without another CalendarContract query.

Permission changes observed by the settings screen request an explicit refresh for all existing widget instances. There is currently no polling service, periodic worker, or CalendarContract content observer. External event edits, calendar visibility changes, locale changes, and timezone changes may require the next system widget update or widget re-addition before fresh provider data appears. Active provider-change refresh and navigation hardening remains deferred to Run 5.

## Test

Run local verification:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew assembleDebugAndroidTest
```

Run instrumentation tests only with a connected device or emulator:

```bash
./gradlew connectedDebugAndroidTest
```

Compiling the Android test APK does not execute instrumentation tests. Validate all four widgets on the target launcher after installation, including resize and font-scale behavior.

## Architecture

Source packages separate calendar data/domain, each widget provider, rendering, theme, settings, and navigation.

- `AndroidCalendarDataSource` owns permission-guarded CalendarContract cursor access.
- `AndroidCalendarRepository` applies one calendar selection to agenda and month queries off main thread.
- `CalendarEventProcessor` handles deterministic sorting, deduplication, date spans, and month grouping.
- `CalendarSelectionResolver` distinguishes default selection from an explicit empty selection.
- `CalendarDataModule.repository(context)` constructs the production repository.
- `WidgetDataLoader` builds one shared snapshot from one selected-calendar occurrence query and DataStore state.
- `WidgetSnapshotFactory` performs pure month-grid and agenda transformations.
- `WidgetUpdateCoordinator` runs provider loads asynchronously and rejects stale/deleted widget updates.
- `MonthWidgetRenderingEngine` renders the compact month using RemoteViews only.
- `AgendaWidgetRenderingEngine` renders responsive upcoming-event rows.
- `AgendaEventFormatter` and `AgendaCapacityPolicy` keep formatting and sizing deterministic.
- `CombinedWidgetRenderingEngine` stacks both reusable sections from one immutable snapshot.
- `CombinedLayoutPolicy` reserves the complete month before allocating agenda rows.
- `TodayAgendaWidgetRenderingEngine` renders the horizontal large-date and agenda layout.
- `TodayDatePresenter` and `TodayAgendaCapacityPolicy` keep date and sizing logic deterministic.
- `AgendaWindowPolicy` applies half-open interval overlap before row capacity.

## Known limitations

- No calendar-selection UI yet; persistence API is ready for future UI
- No widget configuration UI
- No active CalendarContract change observer or periodic refresh scheduling
- Calendar-provider, locale, and timezone refresh triggers remain Run 5 work
- Event-specific navigation exists only where currently wired; full fallback hardening remains Run 5 work
- Pixel-level Nothing Launcher validation requires a connected Nothing device
- Doto launcher rendering and screenshots remain pending when no Android device is connected
- Wallpaper-derived colors and additional visual treatments are deferred to Runs 4.2 and 4.3

## Recommended next steps

1. Continue approved visual work in Runs 4.2 and 4.3.
2. Add refresh triggers and full navigation hardening in Run 5.
3. Add calendar-selection UI.
