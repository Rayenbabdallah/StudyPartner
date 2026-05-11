# StudyPartner — Feature Checklist

---

## ✅ Completed

### Core Architecture
- [x] MVVM + Repository pattern
- [x] Room database (version 4) with migrations 1→2→3→4
- [x] StateFlow + coroutines throughout
- [x] Navigation Compose with slide/fade transitions
- [x] Edge-to-edge display + splash screen
- [x] DataStore preferences (notifications, defaults, onboarding flag)
- [x] WorkManager (daily deadline notification check)

### Entities & Data
- [x] StudyTask (title, subject, courseId, difficulty, urgency, deadline, type, gradeWeight, progress)
- [x] Course (title, instructor, creditWeight, colorHex, examDate)
- [x] SubTask (taskId, title, isCompleted)
- [x] Level enum (LOW → EXTREME)
- [x] TaskType enum (Assignment, Exam, Project, Reading, Review, Quiz)
- [x] GradeImpact enum (Minimal → Critical, maps to 0.1–1.0 weight)

### Engines
- [x] Adaptive Priority Engine — 6-factor 0–100 score
      (urgency 30% + grade impact 25% + difficulty 15% + low progress 10% + deadline 10% + overload 10%)
- [x] Risk detection — High Risk / Moderate / Safe (difficulty × urgency)
- [x] Priority label — Critical / High / Medium / Low (from 0–100 score)
- [x] Course risk score — weighted average + exam proximity bonus
- [x] Study Readiness Score — per course, based on progress + overdue + exam proximity
- [x] Score factor breakdown — contribution of each of the 6 factors
- [x] Local advice engine — rule-based, context-aware
- [x] Session recommendation — 30 / 60 / 90 / 120 min based on load

### Screens
- [x] Splash screen
- [x] Onboarding (4-page HorizontalPager)
- [x] Home dashboard (stats row, advice card, nav buttons)
- [x] Task List (search, filter chips, swipe-to-delete, pull-to-refresh, undo)
- [x] Task Detail (score ring, factor breakdown, subtask CRUD, progress bar, complete button)
- [x] Add Task (course dropdown, type picker, grade impact picker, date picker)
- [x] Edit Task (all above + progress slider)
- [x] Courses (list with risk label + readiness score per course)
- [x] Add / Edit Course (color swatch picker, credit weight, exam date)
- [x] Statistics (completion rate, subject breakdown, risk distribution, session)
- [x] Risk Insights (alert banner, priority distribution chart, course heatmap)
- [x] AI Assistant (6 quick prompts + custom input, Ollama + local fallback)
- [x] Settings (notification toggle, default difficulty/urgency)

### AI / Ollama
- [x] Ollama integration (llama3.2 via HTTP, emulator alias 10.0.2.2)
- [x] Structured home advice prompt (DO NOW / RISK ALERT / TIME ESTIMATE)
- [x] Structured assistant prompt (task-aware, question-specific)
- [x] Rule-based local fallback for all AI responses
- [x] Separate OllamaState flows for home card and assistant screen

### UX Details
- [x] Animated score ring on Task Detail
- [x] Animated progress bars on Statistics and Risk Insights
- [x] Animated factor breakdown bars on Task Detail
- [x] Haptic feedback on complete/delete
- [x] Swipe-to-delete with red background reveal
- [x] Undo delete via snackbar
- [x] Date picker (Material3 DatePickerDialog) on tasks and courses
- [x] Color-coded risk cards (red / orange / green backgrounds)
- [x] Dark mode support (Material3 custom scheme)
- [x] Empty state screens

---

## 🔲 Ghasret Lekleb Mode — "Last-Minute Survival Intelligence"

> Activate when the student is in panic mode.
> Shift from long-term optimization to survival output maximization.

### Step 1 — Detection Layer
- [ ] Add `isPanicMode(tasks: List<StudyTask>): Boolean` to `PriorityEngine`
  - Trigger when ANY task meets ALL of:
    - `daysUntilDeadline() in 0..2`
    - `progress < 50`
    - `gradeWeight >= 0.5` (medium+ grade impact)
  - Or when 3+ tasks meet: `daysUntilDeadline() in 0..3 && progress < 70`
- [ ] Add `panicTasks(tasks): List<StudyTask>` — returns the tasks that triggered panic
- [ ] Add `AppMode` enum to the codebase:
  ```kotlin
  enum class AppMode { NORMAL, GHASRET_LEKLEB }
  ```

### Step 2 — ViewModel Integration
- [ ] Add `val appMode: StateFlow<AppMode>` to `StudyViewModel`
  - Derived from `uiState` using `map { tasks -> if (PriorityEngine.isPanicMode(tasks)) AppMode.GHASRET_LEKLEB else AppMode.NORMAL }`
  - Use `stateIn(WhileSubscribed(5000), NORMAL)`
- [ ] Add `panicTasks: List<StudyTask>` computed property
- [ ] Add `dismissPanic()` — sets a flag so user can manually dismiss for the session (use `MutableStateFlow<Boolean>`)

### Step 3 — Survival Priority Engine
- [ ] Add `survivalScore(task: StudyTask): Double` to `PriorityEngine`
  - Formula: `(gradeWeight * 0.5 + deadlineScore * 0.4 + (1 - difficulty/5.0) * 0.1) * 100`
  - Logic: "most marks in least time" — favors high grade impact + near deadline + lower difficulty
- [ ] Add `sortBySurvival(tasks): List<StudyTask>` using `survivalScore`
- [ ] Add `survivalLabel(task): String` → "DO NOW" / "IF TIME" / "SKIP"
  - DO NOW: survivalScore >= 70
  - IF TIME: survivalScore in 40..69
  - SKIP: survivalScore < 40

### Step 4 — Minimum Viable Work (MVW)
- [ ] Add `minimumViableWork(task: StudyTask): String` to `PriorityEngine`
  - Returns a short string based on task type + progress:
    - EXAM: "Focus on high-weight topics only. Skip optional sections."
    - ASSIGNMENT: "Aim for 70% complete. Submit something over nothing."
    - PROJECT: "Finish the core requirement. Drop extras."
    - Default: "Complete the highest-impact 60% and submit."

### Step 5 — AI Rescue Plan Prompt
- [ ] Add `getAssistantRescuePlan(tasks, panicTasks)` to `OllamaRepository`
  - System prompt focuses on:
    - Hour-by-hour plan for tonight
    - Realistic breaks included
    - "What to drop" section
    - Expected outcome estimate ("If you follow this: ~12–14/20")
- [ ] Add `fetchRescuePlan()` to `StudyViewModel`
- [ ] Wire result to `assistantState` (reuse existing flow)

### Step 6 — Ghasret Lekleb Screen
- [ ] Create `GhasretLeklebScreen.kt`
  - Top banner: "⚠ Ghasret Lekleb Mode — Let's save this." (orange/red accent)
  - Section 1: **Survival Priority List** — tasks sorted by `survivalScore`, each labeled DO NOW / IF TIME / SKIP
  - Section 2: **Minimum Viable Work** — per-task MVW string
  - Section 3: **AI Rescue Plan** — button "Generate Rescue Plan" → calls Ollama → shows result
  - Section 4: **Expected Outcome** — "If you follow this plan: estimated ~12–14/20"
  - Bottom: "Exit Survival Mode" button → calls `viewModel.dismissPanic()`
- [ ] Add `Screen.GhasretLekleb` route
- [ ] Register in `MainActivity`

### Step 7 — Panic Banner on Home & Task List
- [ ] In `HomeScreen`, when `appMode == GHASRET_LEKLEB`:
  - Show a dismissible orange warning card above the advice card:
    - "⚠ Ghasret Lekleb Mode detected — ${panicTasks.size} task(s) in danger"
    - Button: "Open Survival Mode" → navigates to `Screen.GhasretLekleb`
- [ ] In `TaskListScreen`, when in panic mode:
  - Show a sticky orange banner below the filter chips
  - Apply `survivalLabel` badge to each panic task card instead of normal risk label

### Step 8 — Focus Mode (Panic Version)
- [ ] Create `FocusScreen.kt` (accessible from Ghasret Lekleb screen)
  - Full-screen, minimal UI
  - Shows ONE task: title + MVW string
  - Countdown timer (25 min Pomodoro or custom)
  - "Done" button → marks progress + returns
  - Aggressive motivational message cycling:
    - "Focus. One task at a time."
    - "Forget perfection. Deliver something."
    - "ركز — you got this."
- [ ] Use `CountDownTimer` or coroutine `delay` loop for the timer
- [ ] Add `Screen.Focus("{taskId}")` route

### Step 9 — Cultural Localization (Optional Toggle)
- [ ] Add `tunisianMode: Boolean` to `UserPreferences` (DataStore key)
- [ ] Add toggle in `SettingsScreen` under new "Personality" section
- [ ] When enabled, replace generic messages with Tunisian-flavored strings:
  - "Ghasret Lekleb Mode activated 😅"
  - "ghasra w tetaada"
  - "tjibha nchalah"
  - "tgued rouhek"
  - "Matkhalich rouhek — we got this."
  - "ركز شوية تو تمشي"
  - "خلص المهمة هذي و ارتاح"
- [ ] Create `AppStrings` object with `normal` and `tunisian` variants

### Step 10 — Post-Mortem Insights
- [ ] After a panic task's deadline passes:
  - `NotificationWorker` detects overdue task that WAS in panic mode
  - Send notification: "Post-mortem available for [task]"
- [ ] Create `PostMortemScreen.kt` (navigated to from notification or task detail)
  - Shows: when risk started, missed opportunities, what could have been done earlier
  - Simple computed: "Risk reached Critical X days before deadline. You started at Y% progress."
- [ ] Add `riskOnsetDays(task): Int?` to `PriorityEngine`
  - Estimates when the task crossed into Critical based on current score trajectory

### Step 11 — Metrics & Analytics Integration
- [ ] Add `panicModeCount: Int` to a new `SessionStats` DataStore key — incremented each time panic activates
- [ ] Show in `StatisticsScreen`: "Rescue mode activations this month: X"
- [ ] Add `rescueCompletionRate` — % of panic tasks that were eventually completed

---

## 🔲 Remaining from Vision (Future Phases)

- [ ] **Weekly Review screen** — Sunday summary: hours, completion rate, strongest/weakest course, next week risks
- [ ] **Study Sessions** — StudySession entity (plannedStart, plannedEnd, actualStart, actualEnd, status)
- [ ] **Daily Planner / Calendar view** — Agenda list or week grid
- [ ] **Onboarding data collection** — Collect courses + exam dates + weekly availability during onboarding
- [ ] **Richer notifications** — "You missed your 6 PM block", "Falling behind on X"
- [ ] **Course Detail screen** — Drill into a course: task list, readiness gauge, exam countdown
- [ ] **Auth / Login** (Phase 2) — Firebase Auth, profile management
- [ ] **Backend sync** (Phase 2) — Ktor/Spring Boot REST API, PostgreSQL, JWT
- [ ] **Google Calendar integration** (Phase 4)
