# android_concepts

## All modules

Each folder in `concepts/` is a Gradle module (found automatically by `settings.gradle.kts`).
SDK levels and the JVM toolchain are set centrally in `gradle.properties` (`fhac.*`) and applied by
the convention plugin in `build-logic`; all versions live in `gradle/libs.versions.toml`.

| Folder | Modules | Topics |
| --- | --- | --- |
| `starters` | XmlBasedApp, ComposableBasedApp | Views + XML vs. Compose |
| `lifecycle` | ActivityLifecycle | Activity lifecycle, lifetime of state: remember, rememberSaveable, Application, ViewModel, SavedStateHandle |
| `composables` | TheIdeaOfComposables, ComposableExamples, ComposablesWithState, ComposableCalculator, Theming | composables, layouts, modifiers, state, recomposition, state hoisting, Material 3 theming |
| `navigation` | NavigationV3, NavigationV3Drawer | Navigation 3 (back stack as state), navigation drawer |
| `models` | RawSensorMVVM, RawSensorMVI, RawSensorLib | MVVM vs. MVI, unidirectional data flow, StateFlow, cold flows, constructor injection |
| `permissions` | Permissions | runtime permissions, rationale, Accompanist |
| `rest` | Rest, Supabase | Retrofit, OkHttp, Moshi; Supabase (Auth, Postgrest, Realtime) with SQL setup scripts |
| `database` | Room | Room, DAOs, Flows, transactions |
| `preferences` | Preferences | DataStore |
| `sensors` | Sensors, Location, Camera, Photo1, Photo2 | sensors, callbackFlow, location + maps, camera intents, MediaStore, CameraX |
| `dialogs` | ModalDialog | dialogs, pickers, bottom sheet, snackbar |
| `testing` | Testing | local unit tests (logic, ViewModel with fakes, coroutines-test), Compose UI tests |
| `libs` | UiTools | shared UI library (ConceptTheme, standard backgrounds, ConceptScaffold: toolbar + snackbar, widgets, permission helpers) |

The Location app needs a Google Maps API key in `local.properties` (see `local.properties.example`).
