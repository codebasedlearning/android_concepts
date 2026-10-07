# android_concepts

## Unit 0x01
- Kotlin Essentials I

## Unit 0x02

- starter
  - XmlBasedApp
  - ComposableBasedApp
- composables
  - TheIdeaOfComposables

## Unit 0x03
- Kotlin Essentials II

## Unit 0x04

[...]

## All modules

Each folder in `concepts/` is a Gradle module (found automatically by `settings.gradle.kts`).
SDK levels and the JVM toolchain are set centrally in `gradle.properties` (`fhac.*`) and applied by
the convention plugin in `build-logic`; all versions live in `gradle/libs.versions.toml`.

| Folder | Modules | Topics |
| --- | --- | --- |
| `starters` | XmlBasedApp, ComposableBasedApp | Views + XML vs. Compose |
| `composables` | TheIdeaOfComposables, ComposableExamples, ComposablesWithState | composables, layouts, modifiers, state, recomposition |
| `navigation` | NavigationBasics, NavigationWithDrawer, AppWithScreens, NavigationV3 | Navigation Compose, drawer, Navigation 3 |
| `models` | RawSensorMVVM, RawSensorMVI | MVVM vs. MVI, StateFlow, service locator |
| `permissions` | Permissions | runtime permissions, rationale, Accompanist |
| `rest` | Rest | Retrofit, OkHttp, Moshi |
| `database` | Room | Room, DAOs, Flows, transactions |
| `preferences` | Preferences | DataStore |
| `sensors` | Sensors, Location, Camera, Photo1, Photo2 | sensors, callbackFlow, location + maps, camera intents, MediaStore, CameraX |
| `dialogs` | ModalDialog | dialogs, pickers, bottom sheet, snackbar |
| `libs` | UiTools | shared UI library (navigation scaffold, widgets, permission helpers) |

The Location app needs a Google Maps API key in `local.properties` (see `local.properties.example`).
