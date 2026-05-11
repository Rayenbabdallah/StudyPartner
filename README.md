# StudyPartner

An adaptive Android study companion that scores, prioritises and rescues your workload using a 6-factor priority engine and an AI assistant with offline fallback.

Built with Jetpack Compose, Room, MVVM, and Material 3.

---

## Features

- **Adaptive priority engine** — 6-factor 0–100 score combining urgency, grade impact, difficulty, low-progress, deadline proximity and overload accumulation. Score bands: Critical · High · Medium · Low.
- **Risk insights** — High-Risk / Moderate / Safe classification per task, course-level risk score, study-readiness score, deadline-risk timeline, panic-mode detection.
- **AI assistant** — quick prompts + custom questions via OpenRouter, with AWS Bedrock fallback and a rule-based local engine when offline. The home advice card and the assistant screen each have independent state.
- **Survival Mode (Ghasret Lekleb)** — activated when deadlines collapse: switches the priority surface to "minimum viable work" and presents a rescue plan.
- **Focus timer** — Canvas ring countdown, motivational cycling, rotation-safe state, marks task progress on completion.
- **Weekly review** — Mon–Sun grid with completion %, average progress and per-day risk markers; week navigation.
- **Statistics** — completion rate ring, subject breakdown, risk distribution, recommended session length.
- **Course tracking** — colour-coded courses, per-course readiness, exam-date countdown, course-detail dashboard.
- **Subtasks** — break a task into checkable steps with progress aggregation.
- **Task list** — search, filter pills (All / Active / Risk / Done), swipe-to-delete with undo, pull-to-refresh.

## Architecture

```
UI (Compose)  ──▶  ViewModel (StateFlow + viewModelScope)
                       │
                       ▼
                 Repositories
                       │
                       ▼
              Room DB  ·  DataStore  ·  WorkManager  ·  AiRepository (HTTP)
```

- **MVVM** with a single `StudyViewModel` exposing `StateFlow<TaskUiState>` + dedicated flows for AI advice, AI assistant, app mode, sessions, and current subtasks.
- **Room** schema v5 with explicit migrations 1→2→3→4→5. Entities: `StudyTask`, `Course`, `SubTask`, `StudySession`.
- **Engines**: `PriorityEngine` (pure, deterministic), `AiRepository` (network + fallback chain).
- **Navigation**: Compose Navigation with sealed `Screen` route definitions and parameterised routes.
- **Persistence**: Room for entities, DataStore for prefs (notifications, defaults, onboarding flag, OpenRouter key).
- **Background**: WorkManager runs a daily deadline-check + notification.

## Tech stack

| Layer | Library |
|---|---|
| UI | Jetpack Compose, Material 3, Material Icons Extended, Splash Screen API |
| State | StateFlow, coroutines, lifecycle-runtime-ktx |
| Persistence | Room (KSP), DataStore Preferences |
| Background | WorkManager |
| Networking | Java HttpURLConnection, AWS SDK for Kotlin (Bedrock) |
| Navigation | androidx.navigation:navigation-compose |
| Testing | JUnit, Robolectric, Compose UI Test, Room Testing, kotlinx-coroutines-test |

## Project layout

```
app/src/main/java/com/example/studypartner/
├── MainActivity.kt                       Entry, edge-to-edge, theme, NavHost
├── StudyViewModel.kt                     Single source of UI truth
├── PriorityEngine.kt                     Scoring, sorting, advice, panic detection
├── AiRepository.kt                       OpenRouter → Bedrock → local fallback chain
├── AppDatabase.kt                        Room DB + migrations
├── *Dao.kt / *Repository.kt              Data layer
├── *Screen.kt                            Compose screens (40+ routes incl. auth, planner, focus, gallery, recovery)
├── ui/theme/                             Color, Type, Shape, Dimens, Theme
└── …
```

## Build & run

```bash
./gradlew assembleDebug
./gradlew installDebug
```

Min SDK 24, target SDK 36, Kotlin/JVM 21.

## Tests

```bash
./gradlew test                # unit + Robolectric (host JVM)
./gradlew connectedAndroidTest  # instrumented (device required)
```

| Suite | Files | Coverage |
|---|---|---|
| `PriorityEngineTest` | 1 file, 18 cases | score bands, sorting, panic detection, readiness, advice |
| `StudyViewModelTest` | 1 file, ~350 LOC | Robolectric + Room + dispatchers + state flows |
| DAO tests | 4 files | StudyTask, Course, SubTask, StudySession CRUD |
| `RepositoryTest` | 1 file | repo facade contracts |
| `StudyTaskExtensionsTest`, `EnumsTest`, `StudySessionPlanTest` | 3 files | model invariants |
| `OnboardingScreenTest`, `EmptyStateViewTest` | 2 files | Compose UI |
| `ScreenRoutesTest` | 1 file | navigation route uniqueness + parameterised path generation |
| `NavigationSmokeTest` (Robolectric) | 1 file | NavHost wiring, parameterised routes, back-stack pop |
| `MainActivityTest` (instrumented) | 1 file | activity launches, reaches RESUMED, survives recreate |

## Design system

The new "Sun-Soaked Paper" palette is wired through M3 slots in `ui/theme/Color.kt`. See `Theme.kt`, `Shape.kt`, `Dimens.kt` for tokens.

| Role | Hex |
|---|---|
| Deep Orange (primary) | `#FF9F1C` |
| Warm Amber (secondary) | `#FFB627` |
| Soft Gold (primary container) | `#FFE066` |
| Bookmark Gold (accent) | `#FFC300` |
| Lime Check (tertiary / safe) | `#A8D62F` |
| Orange Check (warning) | `#FFAA1D` |
| Soft White (background) | `#F8F7F5` |
| Stroke / Shadow | `#D9D4CF` / `#B8B1AA` |

Design language: oversized hero numbers (64–88sp), ribbon-tab quick actions, paper-panel cards with thin outline, Canvas ring progress, accent-driven motion, color-coded section eyebrows.

## Configuration

- **OpenRouter API key**: enter in Settings (stored in DataStore, never hardcoded).
- **AWS Bedrock**: optional fallback; configure access key / secret / region in Settings.
- **Notifications**: toggle in Settings; daily deadline-check runs via WorkManager.

## Submission notes

- Targets `compileSdk 36`, `minSdk 24`, Kotlin/JVM 21. Open in Android Studio Koala+ (or any AGP 8.5+ IDE), let Gradle sync, then `Run 'app'`.
- The app works fully offline. The AI assistant degrades gracefully to a deterministic local engine when no API keys are configured in Settings.
- No secrets are checked in. OpenRouter / AWS Bedrock credentials are entered at runtime via Settings (stored in DataStore).
- Unit + Robolectric tests run on the host JVM (`./gradlew test`). The single instrumented test (`MainActivityTest`) requires a device or emulator (`./gradlew connectedAndroidTest`).

## Known limitations

- `selectedCourse` and enum picker state in `EditTaskScreen` use plain `remember` — autoSaver doesn't handle them; rotate-during-edit resets those fields. Custom `Saver` would fix.
- Release build is currently `isMinifyEnabled = false`; enable + verify ProGuard rules before any production release.
- `applicationId = "com.example.studypartner"` — change before any Play Store upload.
