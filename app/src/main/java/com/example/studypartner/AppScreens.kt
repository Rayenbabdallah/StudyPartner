package com.example.studypartner

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ScreenLink(val label: String, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplashPlaceholderScreen(
    navController: NavController,
    targetRoute: String
) {
    LaunchedEffect(targetRoute) {
        delay(450)
        navController.navigate(targetRoute) {
            popUpTo(Screen.Splash.route) { inclusive = true }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "StudyPartner",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Production splash entry",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.45f))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AuthScreenShell(
    navController: NavController,
    title: String,
    subtitle: String,
    body: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            body()
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val emailVerified by UserPreferences.emailVerified(context).collectAsState(initial = false)
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthScreenShell(
        navController = navController,
        title = "Login",
        subtitle = "Sign in to continue."
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                scope.launch {
                    UserPreferences.setLoggedIn(context, email)
                    val destination = if (emailVerified) Screen.Home.route else Screen.EmailVerification.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
            Text("Create account")
        }
        TextButton(onClick = { navController.navigate(Screen.ForgotPassword.route) }) {
            Text("Forgot password")
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthScreenShell(
        navController = navController,
        title = "Register",
        subtitle = "Create a new StudyPartner account."
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                scope.launch {
                    UserPreferences.setPendingRegistration(context, email.ifBlank { "$name@student.local" })
                    navController.navigate(Screen.EmailVerification.route)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Account")
        }
    }
}

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }

    AuthScreenShell(
        navController = navController,
        title = "Forgot Password",
        subtitle = "Enter your email and we will send reset instructions."
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { navController.navigate(Screen.Login.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send Reset Link")
        }
    }
}

@Composable
fun EmailVerificationScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    AuthScreenShell(
        navController = navController,
        title = "Email Verification",
        subtitle = "Confirm your account to unlock full app features."
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                "A verification email has been sent. Tap the link, then return.",
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Button(
            onClick = {
                scope.launch {
                    UserPreferences.setEmailVerified(context, true)
                    UserPreferences.setLoggedIn(context)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("I Verified My Email")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    outerPadding: PaddingValues = PaddingValues()
) {
    val state by viewModel.uiState.collectAsState()
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()
    val sessions by viewModel.allStudySessions.collectAsState()
    val now = System.currentTimeMillis()
    val todayTasks = tasks.filter { task ->
        val deadline = task.deadline ?: return@filter false
        val diff = deadline - now
        diff in 0..86_400_000L
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Planner") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = outerPadding.calculateBottomPadding() + 20.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Calendar Views",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            item {
                LinkRow("Daily view", Screen.PlannerDaily.createRoute(System.currentTimeMillis()), navController)
            }
            item {
                LinkRow("Weekly view", Screen.PlannerWeekly.route, navController)
            }
            item {
                LinkRow("Add study session", Screen.AddStudySession.createRoute(), navController)
            }
            item {
                LinkRow("Overload visualization", Screen.OverloadVisualization.route, navController)
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Today in planner",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (todayTasks.isEmpty()) {
                item {
                    Card {
                        Text(
                            "No sessions/tasks due today.",
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(todayTasks.take(6)) { task ->
                    ElevatedCard(
                        onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(task.title, fontWeight = FontWeight.SemiBold)
                                Text(
                                    task.deadlineLabel(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.Default.CalendarMonth, contentDescription = null)
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Recent Study Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            if (sessions.isEmpty()) {
                item {
                    Card {
                        Text(
                            "No saved study sessions yet.",
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            } else {
                items(sessions.take(6)) { session ->
                    ElevatedCard(
                        onClick = { navController.navigate(Screen.StudySessionDetail.createRoute(session.id)) }
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(session.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${session.durationMinutes} min • Break ${session.breakLengthMinutes}m / ${session.breakEveryMinutes}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsCenterScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    outerPadding: PaddingValues = PaddingValues()
) {
    val state by viewModel.uiState.collectAsState()
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()
    val overdue = tasks.filter { it.isOverdue() && !it.isCompleted }
    val highRisk = tasks.filter { it.isHighRisk() && !it.isCompleted }

    BaseFeatureScreen(
        navController = navController,
        title = "Notifications Center",
        subtitle = "Deadline alerts, risk alerts, reminders, and AI recommendations.",
        outerPadding = outerPadding
    ) {
        NotificationBlock("Deadline alerts", overdue.size)
        NotificationBlock("Risk alerts", highRisk.size)
        NotificationBlock("Study reminders", tasks.count { !it.isCompleted })
        NotificationBlock("AI recommendations", 1)
    }
}

@Composable
private fun NotificationBlock(title: String, count: Int) {
    ElevatedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(Modifier.size(10.dp))
                Text(title)
            }
            Text(
                "$count",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ProfileScreen(
    navController: NavController,
    outerPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val accountEmail by UserPreferences.accountEmail(context).collectAsState(initial = "")

    BaseFeatureScreen(
        navController = navController,
        title = "Profile",
        subtitle = "Account overview and quick access to settings modules.",
        outerPadding = outerPadding
    ) {
        ElevatedCard {
            Column(Modifier.padding(14.dp)) {
                Text(
                    accountEmail.ifBlank { "No account" },
                    fontWeight = FontWeight.Bold
                )
            }
        }
        LinkRow("Account Settings", Screen.AccountSettings.route, navController)
        LinkRow("Notification Settings", Screen.NotificationSettings.route, navController)
        LinkRow("App Preferences", Screen.AppPreferences.route, navController)
        LinkRow("Theme", Screen.ThemeSettings.route, navController)
        LinkRow("Language / Tone", Screen.LanguageTone.route, navController)
    }
}

@Composable
fun SubtasksOverviewScreen(
    navController: NavController,
    viewModel: StudyViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()

    BaseFeatureScreen(
        navController = navController,
        title = "Subtasks",
        subtitle = "Open a task to manage subtasks in detail."
    ) {
        Button(
            onClick = { navController.navigate(Screen.Add.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("Add Task")
        }

        if (tasks.isEmpty()) {
            Text("No tasks available yet.")
        } else {
            tasks.take(12).forEach { task ->
                ElevatedCard(
                    onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Tap to manage subtasks and checklist for this task.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accountEmail by UserPreferences.accountEmail(context).collectAsState(initial = "")
    val emailVerified by UserPreferences.emailVerified(context).collectAsState(initial = false)
    var editableEmail by remember(accountEmail) { mutableStateOf(accountEmail) }

    BaseFeatureScreen(
        navController = navController,
        title = "Account Settings",
        subtitle = "Manage account identity and verification."
    ) {
        OutlinedTextField(
            value = editableEmail,
            onValueChange = { editableEmail = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    scope.launch {
                        UserPreferences.setAccountEmail(context, editableEmail)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Email")
            }
            OutlinedButton(
                onClick = {
                    scope.launch {
                        UserPreferences.setEmailVerified(context, true)
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (emailVerified) "Verified" else "Verify Email")
            }
        }
        if (!emailVerified) {
            Text(
                "Email is not verified. Verify to unlock full app flow.",
                color = MaterialTheme.colorScheme.error
            )
        }
        OutlinedButton(
            onClick = {
                scope.launch {
                    UserPreferences.logout(context)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Out")
        }
    }
}

@Composable
fun NotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notificationsEnabled by UserPreferences.notificationsEnabled(context).collectAsState(initial = true)

    BaseFeatureScreen(
        navController = navController,
        title = "Notification Settings",
        subtitle = "Control deadlines, risk alerts, reminders, and AI prompts."
    ) {
        ElevatedCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Deadline reminders", fontWeight = FontWeight.SemiBold)
                Text(
                    if (notificationsEnabled) "Enabled" else "Disabled",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            scope.launch { UserPreferences.setNotificationsEnabled(context, true) }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Enable") }
                    OutlinedButton(
                        onClick = {
                            scope.launch { UserPreferences.setNotificationsEnabled(context, false) }
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text("Disable") }
                }
            }
        }
    }
}

@Composable
fun AppPreferencesScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val defaultDifficulty by UserPreferences.defaultDifficulty(context).collectAsState(initial = Level.LOW.value)
    val defaultUrgency by UserPreferences.defaultUrgency(context).collectAsState(initial = Level.LOW.value)
    val panicCount by UserPreferences.panicModeCount(context).collectAsState(initial = 0)

    BaseFeatureScreen(
        navController = navController,
        title = "App Preferences",
        subtitle = "Default values used when creating new tasks."
    ) {
        Text("Emergency mode activations: $panicCount")
        HorizontalDivider()
        Text("Default Difficulty", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Level.entries.forEach { level ->
                FilterChip(
                    selected = defaultDifficulty == level.value,
                    onClick = {
                        scope.launch { UserPreferences.setDefaultDifficulty(context, level.value) }
                    },
                    label = { Text(level.label) }
                )
            }
        }
        Text("Default Urgency", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Level.entries.forEach { level ->
                FilterChip(
                    selected = defaultUrgency == level.value,
                    onClick = {
                        scope.launch { UserPreferences.setDefaultUrgency(context, level.value) }
                    },
                    label = { Text(level.label) }
                )
            }
        }
    }
}

@Composable
fun ThemeSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val themeMode by UserPreferences.themeMode(context).collectAsState(initial = UserPreferences.THEME_SYSTEM)

    BaseFeatureScreen(
        navController = navController,
        title = "Theme",
        subtitle = "Choose between system, light, and dark modes."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = themeMode == UserPreferences.THEME_SYSTEM,
                onClick = { scope.launch { UserPreferences.setThemeMode(context, UserPreferences.THEME_SYSTEM) } },
                label = { Text("System") }
            )
            FilterChip(
                selected = themeMode == UserPreferences.THEME_LIGHT,
                onClick = { scope.launch { UserPreferences.setThemeMode(context, UserPreferences.THEME_LIGHT) } },
                label = { Text("Light") }
            )
            FilterChip(
                selected = themeMode == UserPreferences.THEME_DARK,
                onClick = { scope.launch { UserPreferences.setThemeMode(context, UserPreferences.THEME_DARK) } },
                label = { Text("Dark") }
            )
        }
    }
}

@Composable
fun LanguageToneScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tunisianMode by UserPreferences.tunisianMode(context).collectAsState(initial = false)
    val strings = AppStrings.get(tunisianMode)

    BaseFeatureScreen(
        navController = navController,
        title = "Language / Tone",
        subtitle = "Switch assistant tone and emergency wording."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = !tunisianMode,
                onClick = { scope.launch { UserPreferences.setTunisianMode(context, false) } },
                label = { Text("Standard") }
            )
            FilterChip(
                selected = tunisianMode,
                onClick = { scope.launch { UserPreferences.setTunisianMode(context, true) } },
                label = { Text("Tunisian") }
            )
        }
        ElevatedCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Preview", fontWeight = FontWeight.SemiBold)
                Text(strings.bannerTitle)
                Text(strings.focus1, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun GhasretActivationScreen(
    navController: NavController,
    viewModel: StudyViewModel
) {
    val panicTasks = viewModel.panicTasks()

    BaseFeatureScreen(
        navController = navController,
        title = "Ghasret Lekleb Activation",
        subtitle = "Emergency mode entry when deadlines and risk spike."
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Critical tasks detected: ${panicTasks.size}", fontWeight = FontWeight.SemiBold)
                Text(
                    if (panicTasks.isEmpty()) "No active emergency right now."
                    else "Immediate action is recommended. Open emergency dashboard now.",
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
        Button(
            onClick = { navController.navigate(Screen.GhasretLekleb.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Emergency Dashboard")
        }
    }
}

@Composable
fun TimerCountdownScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    taskId: Int
) {
    LaunchedEffect(taskId) {
        viewModel.startSessionForTask(taskId)
    }
    QuickFocusTimerScreen(navController, viewModel)
}

@Composable
fun SessionCompletionScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    taskId: Int
) {
    val task = viewModel.getTaskById(taskId)

    BaseFeatureScreen(
        navController = navController,
        title = "Session Completion",
        subtitle = "Review outcome and decide next action."
    ) {
        if (task == null) {
            Text("Task not found.")
            Button(
                onClick = { navController.navigate(Screen.Home.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Dashboard")
            }
            return@BaseFeatureScreen
        }

        ElevatedCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(task.title, fontWeight = FontWeight.SemiBold)
                Text("Current progress: ${task.progress}%")
                Text(task.deadlineLabel(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (!task.isCompleted) {
                        viewModel.toggleComplete(task)
                    }
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Mark Complete")
            }
            OutlinedButton(
                onClick = { navController.navigate(Screen.TimerCountdown.createRoute(task.id)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Restart Timer")
            }
        }
    }
}

@Composable
fun TaskStateScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    title: String,
    filter: (StudyTask) -> Boolean
) {
    val state by viewModel.uiState.collectAsState()
    val tasks = ((state as? TaskUiState.Success)?.tasks ?: emptyList()).filter(filter)

    BaseFeatureScreen(
        navController = navController,
        title = title,
        subtitle = "Task state view for production task management."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { navController.navigate(Screen.Add.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Add Task")
            }
            Button(
                onClick = { navController.navigate(Screen.FilterTasks.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Filter")
            }
        }

        if (tasks.isEmpty()) {
            Text("No tasks in this state.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            tasks.forEach { task ->
                ElevatedCard(
                    onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${task.subject} - ${task.deadlineLabel()}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseScopedScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    courseId: Int,
    title: String,
    subtitle: String
) {
    val courses by viewModel.allCourses.collectAsState()
    val state by viewModel.uiState.collectAsState()
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()
    val sessions by viewModel.allStudySessions.collectAsState()
    val course = courses.find { it.id == courseId }
    val courseTasks = tasks.filter { it.courseId == courseId }
    val courseSessions = sessions.filter { it.courseId == courseId }

    BaseFeatureScreen(
        navController = navController,
        title = title,
        subtitle = subtitle
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { navController.navigate(Screen.Add.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Add Task")
            }
            Button(
                onClick = { navController.navigate(Screen.AddCourse.route) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Add Course")
            }
        }
        Button(
            onClick = {
                navController.navigate(Screen.AddStudySession.createRoute(courseId = courseId))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("Add Study Session (AI Suggested)")
        }
        TextButton(onClick = { navController.navigate(Screen.EditCourse.createRoute(courseId)) }) {
            Text("Edit Selected Course")
        }

        Text(
            "Course: ${course?.title ?: "Unknown"}",
            fontWeight = FontWeight.SemiBold
        )
        HorizontalDivider()
        if (title.contains("Study Sessions", ignoreCase = true)) {
            if (courseSessions.isEmpty()) {
                Text("No saved study sessions for this course.")
            } else {
                courseSessions.take(8).forEach { session ->
                    ElevatedCard(
                        onClick = { navController.navigate(Screen.StudySessionDetail.createRoute(session.id)) }
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(session.name, fontWeight = FontWeight.SemiBold)
                            Text(
                                "${session.durationMinutes} min • Break ${session.breakLengthMinutes}/${session.breakEveryMinutes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else if (courseTasks.isEmpty()) {
            Text("No linked tasks yet.")
        } else {
            courseTasks.take(8).forEach { task ->
                Text("- ${task.title} (${task.deadlineLabel()})")
            }
        }
    }
}

@Composable
fun AiGeneratedPlanScreen(navController: NavController, viewModel: StudyViewModel) {
    val tasks = viewModel.sortedTasks().filter { !it.isCompleted }
    var prompt by remember { mutableStateOf("Build a realistic plan for the next 48 hours") }

    BaseFeatureScreen(
        navController = navController,
        title = "AI Generated Plan",
        subtitle = "Auto-generated based on weighted priority."
    ) {
        OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Plan request") },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    val contextSummary = tasks.take(3).joinToString("\n") { task ->
                        "- ${task.title} (${task.deadlineLabel()})"
                    }
                    val message = buildString {
                        appendLine(prompt)
                        if (contextSummary.isNotBlank()) {
                            appendLine("Context:")
                            append(contextSummary)
                        }
                    }
                    viewModel.fetchAssistantResponse(message)
                    navController.navigate(Screen.AiAssistant.route)
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(Modifier.size(6.dp))
                Text("Generate")
            }
            Button(
                onClick = { navController.navigate(Screen.AiAssistant.route) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Open AI Chat")
            }
        }

        if (tasks.isEmpty()) {
            Text("No active tasks to generate a plan.")
        } else {
            tasks.take(5).forEachIndexed { index, task ->
                ElevatedCard {
                    Column(Modifier.padding(14.dp)) {
                        Text("Step ${index + 1}", color = MaterialTheme.colorScheme.primary)
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        Text(
                            "Risk: ${task.riskLabel()}  •  ${task.deadlineLabel()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AiPromptsScreen(navController: NavController, viewModel: StudyViewModel) {
    val prompts = listOf(
        "Break my next assignment into 5 steps",
        "Build a recovery plan for missed deadlines",
        "Plan tonight's 2-hour study block",
        "Which task gives the best grade impact now?"
    )

    BaseFeatureScreen(
        navController = navController,
        title = "Predefined Prompts",
        subtitle = "Quick prompts to open AI flows."
    ) {
        OutlinedTextField(
            value = "",
            onValueChange = { },
            readOnly = true,
            label = { Text("Type directly in AI Assistant chat") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { navController.navigate(Screen.AiAssistant.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open AI Chat With Text Box")
        }

        prompts.forEach { prompt ->
            ElevatedCard(
                onClick = {
                    viewModel.fetchAssistantResponse(prompt)
                    navController.navigate(Screen.AiAssistant.route)
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Text(prompt)
                }
            }
        }
    }
}

@Composable
fun RecoveryPlanScreen(navController: NavController, viewModel: StudyViewModel) {
    var situation by remember { mutableStateOf("") }
    var availableHours by remember { mutableStateOf("2") }
    val rescueState by viewModel.rescuePlanState.collectAsState()

    BaseFeatureScreen(
        navController = navController,
        title = "Recovery Plan",
        subtitle = "Generate a recovery plan when deadlines slip."
    ) {
        OutlinedTextField(
            value = situation,
            onValueChange = { situation = it },
            label = { Text("What happened?") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = availableHours,
            onValueChange = { availableHours = it },
            label = { Text("Available hours today") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.fetchRescuePlan()
            }
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("Generate Recovery Plan")
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            val planText = when (val state = rescueState) {
                is AiState.Success -> state.response
                is AiState.Failure -> state.message
                is AiState.Unavailable -> viewModel.getLocalRescuePlan()
                is AiState.Loading -> "Generating recovery plan..."
                is AiState.Idle -> "Example output:\n1) Rescue high-impact task\n2) Trim low-impact tasks\n3) Protect one focus block"
            }
            Text(planText, modifier = Modifier.padding(14.dp))
        }
    }
}

@Composable
fun GlobalSearchScreen(navController: NavController, viewModel: StudyViewModel) {
    var query by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsState()
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()
    val courses by viewModel.allCourses.collectAsState()

    val taskHits = tasks.filter { it.title.contains(query, true) || it.subject.contains(query, true) }
    val courseHits = courses.filter { it.title.contains(query, true) || it.instructor.contains(query, true) }

    BaseFeatureScreen(
        navController = navController,
        title = "Global Search",
        subtitle = "Search across tasks and courses."
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search query") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )
        Text("Tasks", fontWeight = FontWeight.SemiBold)
        if (query.isBlank()) {
            Text("Type to search", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else if (taskHits.isEmpty()) {
            Text("No task matches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            taskHits.take(5).forEach { task ->
                LinkRow(task.title, Screen.TaskDetail.createRoute(task.id), navController)
            }
        }
        Text("Courses", fontWeight = FontWeight.SemiBold)
        if (query.isNotBlank() && courseHits.isEmpty()) {
            Text("No course matches.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            courseHits.take(5).forEach { course ->
                LinkRow(course.title, Screen.CourseDetail.createRoute(course.id), navController)
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FilterTasksScreen(navController: NavController, viewModel: StudyViewModel) {
    val state by viewModel.uiState.collectAsState()
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()

    var onlyHighRisk by remember { mutableStateOf(false) }
    var onlyOverdue by remember { mutableStateOf(false) }
    var onlyCompleted by remember { mutableStateOf(false) }

    val filtered = tasks.filter { task ->
        (!onlyHighRisk || task.isHighRisk()) &&
            (!onlyOverdue || task.isOverdue()) &&
            (!onlyCompleted || task.isCompleted)
    }

    BaseFeatureScreen(
        navController = navController,
        title = "Filter Tasks",
        subtitle = "Filter task list by key states."
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = onlyHighRisk, onClick = { onlyHighRisk = !onlyHighRisk }, label = { Text("High Risk") })
            FilterChip(selected = onlyOverdue, onClick = { onlyOverdue = !onlyOverdue }, label = { Text("Overdue") })
            FilterChip(selected = onlyCompleted, onClick = { onlyCompleted = !onlyCompleted }, label = { Text("Completed") })
        }
        Text("Matched: ${filtered.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(
            onClick = { navController.navigate(Screen.List.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Apply And Go To Tasks")
        }
    }
}

@Composable
fun StudySessionFormScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    title: String,
    subtitle: String,
    initialTaskId: Int? = null,
    initialCourseId: Int? = null,
    existingSessionId: Int? = null,
    defaultName: String = "",
    defaultMinutes: String = "50"
) {
    val state by viewModel.uiState.collectAsState()
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()
    val courses by viewModel.allCourses.collectAsState()
    val sessions by viewModel.allStudySessions.collectAsState()
    val existingSession = existingSessionId?.let { id -> sessions.find { it.id == id } }

    var name by remember { mutableStateOf(defaultName) }
    var duration by remember { mutableStateOf(defaultMinutes) }
    var breakEvery by remember { mutableStateOf("25") }
    var breakLength by remember { mutableStateOf("5") }
    var selectedTaskId by remember { mutableStateOf(initialTaskId) }
    var selectedCourseId by remember { mutableStateOf(initialCourseId) }
    var suggestionReason by remember { mutableStateOf("Tap AI Suggest to generate recommended focus and break pacing.") }

    val selectedTask = selectedTaskId?.let { id -> tasks.find { it.id == id } }
    val selectedCourse = selectedCourseId?.let { id -> courses.find { it.id == id } }
    val taskOptions = buildList {
        selectedTask?.let { add(it) }
        addAll(tasks.filter { it.id != selectedTaskId }.take(6))
    }
    val courseOptions = buildList {
        selectedCourse?.let { add(it) }
        addAll(courses.filter { it.id != selectedCourseId }.take(6))
    }

    LaunchedEffect(existingSessionId, existingSession?.updatedAt, initialTaskId, initialCourseId, tasks.size, courses.size) {
        if (existingSession != null) {
            val plan = viewModel.sessionPlanFromStored(existingSession)
            name = existingSession.name
            selectedTaskId = existingSession.taskId
            selectedCourseId = existingSession.courseId
            duration = plan.durationMinutes.toString()
            breakEvery = plan.breakEveryMinutes.toString()
            breakLength = plan.breakLengthMinutes.toString()
            suggestionReason = plan.suggestionReason
        } else if (initialTaskId != null || initialCourseId != null) {
            val task = initialTaskId?.let { id -> tasks.find { it.id == id } }
            val course = initialCourseId?.let { id -> courses.find { it.id == id } }
            val plan = viewModel.suggestStudySessionPlan(task, course)
            duration = plan.durationMinutes.toString()
            breakEvery = plan.breakEveryMinutes.toString()
            breakLength = plan.breakLengthMinutes.toString()
            suggestionReason = plan.suggestionReason
            if (selectedCourseId == null && task?.courseId != null) {
                selectedCourseId = task.courseId
            }
        }
    }

    BaseFeatureScreen(
        navController = navController,
        title = title,
        subtitle = subtitle
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Session name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Duration (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = breakEvery,
            onValueChange = { breakEvery = it },
            label = { Text("Break every (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = breakLength,
            onValueChange = { breakLength = it },
            label = { Text("Break length (minutes)") },
            modifier = Modifier.fillMaxWidth()
        )
        TextButton(
            onClick = {
                selectedTaskId = null
                selectedCourseId = null
            }
        ) {
            Text("Use General Session (No Task/Course Context)")
        }
        Text("Context Task", fontWeight = FontWeight.SemiBold)
        taskOptions.forEach { task ->
            Card(
                onClick = {
                    selectedTaskId = task.id
                    if (selectedCourseId == null && task.courseId != null) {
                        selectedCourseId = task.courseId
                    }
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedTaskId == task.id)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Text(
                    task.title,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
        Text("Context Course", fontWeight = FontWeight.SemiBold)
        courseOptions.forEach { course ->
            Card(
                onClick = { selectedCourseId = course.id },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCourseId == course.id)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                )
            ) {
                Text(
                    course.title,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
        Button(
            onClick = {
                val task = selectedTaskId?.let { id -> tasks.find { it.id == id } }
                val course = selectedCourseId?.let { id -> courses.find { it.id == id } }
                val plan = viewModel.suggestStudySessionPlan(task, course)
                duration = plan.durationMinutes.toString()
                breakEvery = plan.breakEveryMinutes.toString()
                breakLength = plan.breakLengthMinutes.toString()
                suggestionReason = plan.suggestionReason
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("AI Suggest Duration And Breaks")
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("AI Suggestion", fontWeight = FontWeight.SemiBold)
                Text(suggestionReason, style = MaterialTheme.typography.bodySmall)
            }
        }
        Button(
            onClick = {
                val task = selectedTask
                val course = selectedCourse ?: task?.courseId?.let { id -> courses.find { it.id == id } }
                val base = viewModel.suggestStudySessionPlan(task, course)
                val finalPlan = base.copy(
                    contextLabel = if (name.isBlank()) base.contextLabel else name,
                    taskId = task?.id,
                    courseId = course?.id,
                    durationMinutes = duration.toIntOrNull()?.coerceIn(5, 120) ?: base.durationMinutes,
                    breakEveryMinutes = breakEvery.toIntOrNull()?.coerceIn(5, 60) ?: base.breakEveryMinutes,
                    breakLengthMinutes = breakLength.toIntOrNull()?.coerceIn(1, 20) ?: base.breakLengthMinutes
                )
                viewModel.setActiveSessionPlan(finalPlan)
                viewModel.saveStudySession(
                    plan = finalPlan,
                    name = name,
                    existingSessionId = existingSessionId
                ) { savedId ->
                    navController.popBackStack()
                    navController.navigate(Screen.StudySessionDetail.createRoute(savedId))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Session")
        }
        Button(
            onClick = {
                val task = selectedTask
                val course = selectedCourse ?: task?.courseId?.let { id -> courses.find { it.id == id } }
                val base = viewModel.suggestStudySessionPlan(task, course)
                val finalPlan = base.copy(
                    contextLabel = if (name.isBlank()) base.contextLabel else name,
                    taskId = task?.id,
                    courseId = course?.id,
                    durationMinutes = duration.toIntOrNull()?.coerceIn(5, 120) ?: base.durationMinutes,
                    breakEveryMinutes = breakEvery.toIntOrNull()?.coerceIn(5, 60) ?: base.breakEveryMinutes,
                    breakLengthMinutes = breakLength.toIntOrNull()?.coerceIn(1, 20) ?: base.breakLengthMinutes
                )
                viewModel.setActiveSessionPlan(finalPlan)
                viewModel.saveStudySession(
                    plan = finalPlan,
                    name = name,
                    existingSessionId = existingSessionId
                ) {
                    navController.popBackStack()
                    navController.navigate(Screen.QuickFocusMode.route)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Start Session Timer")
        }
    }
}

@Composable
fun StudySessionDetailScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    sessionId: Int
) {
    val sessions by viewModel.allStudySessions.collectAsState()
    val activePlan by viewModel.activeSessionPlan.collectAsState()
    val session = sessions.find { it.id == sessionId }
    val plan = session?.let { viewModel.sessionPlanFromStored(it) } ?: activePlan

    BaseFeatureScreen(
        navController = navController,
        title = "Study Session Detail",
        subtitle = "Session #$sessionId details and controls."
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                session?.let {
                    Text("Saved Name: ${it.name}", fontWeight = FontWeight.SemiBold)
                }
                Text("Session Goal: ${plan.contextLabel}", fontWeight = FontWeight.SemiBold)
                Text("Recommended duration: ${plan.durationMinutes} minutes")
                Text("Break plan: ${plan.breakLengthMinutes} min every ${plan.breakEveryMinutes} min")
                Text("Reason: ${plan.suggestionReason}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { navController.navigate(Screen.EditStudySession.createRoute(sessionId)) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Edit")
            }
            Button(
                onClick = {
                    if (session != null) {
                        viewModel.activateSessionById(sessionId)
                    }
                    navController.navigate(Screen.QuickFocusMode.route)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Start Timer")
            }
        }
        session?.let {
            OutlinedButton(
                onClick = {
                    viewModel.deleteStudySession(it)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Session")
            }
        }
    }
}

@Composable
fun QuickFocusTimerScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    defaultMinutes: Int = 25
) {
    val plan by viewModel.activeSessionPlan.collectAsState()
    val totalSecondsDefault = (plan.durationMinutes.takeIf { it > 0 } ?: defaultMinutes).coerceIn(5, 120) * 60

    var totalSeconds by remember(plan.durationMinutes) { mutableIntStateOf(totalSecondsDefault) }
    var secondsLeft by remember(plan.durationMinutes) { mutableIntStateOf(totalSecondsDefault) }
    var running by remember { mutableStateOf(false) }
    var completed by remember { mutableStateOf(false) }
    var lastBreakPromptAt by remember(plan.durationMinutes, plan.breakEveryMinutes) { mutableIntStateOf(-1) }
    var breakHint by remember { mutableStateOf("") }

    LaunchedEffect(running, secondsLeft) {
        if (running && secondsLeft > 0) {
            delay(1000)
            secondsLeft -= 1
            if (secondsLeft == 0) {
                running = false
                completed = true
            }
        }
    }

    LaunchedEffect(running, secondsLeft, totalSeconds, plan.breakEveryMinutes, plan.breakLengthMinutes) {
        if (!running) return@LaunchedEffect
        val everySec = plan.breakEveryMinutes.coerceAtLeast(1) * 60
        val elapsed = totalSeconds - secondsLeft
        if (elapsed > 0 && elapsed % everySec == 0 && elapsed != lastBreakPromptAt && secondsLeft > 0) {
            lastBreakPromptAt = elapsed
            breakHint = "Break now for ${plan.breakLengthMinutes} minute(s), then continue."
        }
    }

    val progress = if (totalSeconds > 0) secondsLeft.toFloat() / totalSeconds else 0f
    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60
    val breakIntervalSeconds = plan.breakEveryMinutes.coerceAtLeast(1) * 60
    val elapsedSeconds = (totalSeconds - secondsLeft).coerceAtLeast(0)
    val nextBreakInSeconds = if (secondsLeft > 0) {
        val remainder = elapsedSeconds % breakIntervalSeconds
        if (remainder == 0) breakIntervalSeconds else breakIntervalSeconds - remainder
    } else {
        0
    }
    val breakCountdownLabel = if (secondsLeft <= 0 || nextBreakInSeconds >= secondsLeft) {
        "No break scheduled before session end"
    } else {
        "Next break in ${String.format("%02d:%02d", nextBreakInSeconds / 60, nextBreakInSeconds % 60)}"
    }

    BaseFeatureScreen(
        navController = navController,
        title = "Quick Focus Timer",
        subtitle = plan.contextLabel
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Session Context", fontWeight = FontWeight.SemiBold)
                Text("Target: ${plan.durationMinutes} min")
                Text("Breaks: ${plan.breakLengthMinutes} min every ${plan.breakEveryMinutes} min")
                Text(plan.suggestionReason, style = MaterialTheme.typography.bodySmall)
            }
        }

        Text(
            text = String.format("%02d:%02d", minutes, seconds),
            fontSize = 52.sp,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            breakCountdownLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (!running && !completed) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        totalSeconds = (totalSeconds - 5 * 60).coerceAtLeast(5 * 60)
                        secondsLeft = totalSeconds
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("-5 min")
                }
                Button(
                    onClick = {
                        totalSeconds = (totalSeconds + 5 * 60).coerceAtMost(120 * 60)
                        secondsLeft = totalSeconds
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("+5 min")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    if (completed) {
                        completed = false
                        secondsLeft = totalSeconds
                        running = false
                        breakHint = ""
                        lastBreakPromptAt = -1
                    } else {
                        running = !running
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    when {
                        completed -> "Restart"
                        running -> "Pause"
                        else -> "Start"
                    }
                )
            }
            Button(
                onClick = {
                    running = false
                    completed = false
                    secondsLeft = totalSeconds
                    breakHint = ""
                    lastBreakPromptAt = -1
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Reset")
            }
        }

        if (breakHint.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Break Reminder",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(breakHint, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    TextButton(onClick = { breakHint = "" }) { Text("Dismiss") }
                }
            }
        }

        if (completed) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Session complete. Great focus.",
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    plan.taskId?.let { taskId ->
                        OutlinedButton(
                            onClick = { navController.navigate(Screen.SessionCompletion.createRoute(taskId)) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Completion Screen")
                        }
                    }
                }
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back")
        }
    }
}

@Composable
fun PlannerDailyScreen(navController: NavController, dateMillis: Long) {
    BaseFeatureScreen(
        navController = navController,
        title = "Planner Daily",
        subtitle = "Daily planner for selected date: $dateMillis"
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Text(
                "No sessions yet for this day.",
                modifier = Modifier.padding(14.dp)
            )
        }
        Button(
            onClick = { navController.navigate(Screen.AddStudySession.createRoute()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("Add Study Session")
        }
    }
}

@Composable
fun PlannerWeeklyScreen(navController: NavController) {
    BaseFeatureScreen(
        navController = navController,
        title = "Planner Weekly",
        subtitle = "Week overview with quick planning actions."
    ) {
        LinkRow("Open Overload Visualization", Screen.OverloadVisualization.route, navController)
        Button(
            onClick = { navController.navigate(Screen.AddStudySession.createRoute()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("Add Study Session")
        }
    }
}

@Composable
fun OverloadAnalysisScreen(navController: NavController, viewModel: StudyViewModel) {
    val state by viewModel.uiState.collectAsState()
    val tasks = (state as? TaskUiState.Success)?.tasks ?: emptyList()
    val now = System.currentTimeMillis()
    val dayLoads = (0..6).map { offset ->
        val start = now + offset * 86_400_000L
        val end = start + 86_400_000L
        val count = tasks.count { task -> task.deadline?.let { it in start until end } == true && !task.isCompleted }
        Pair(offset, count)
    }
    val maxLoad = (dayLoads.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    BaseFeatureScreen(
        navController = navController,
        title = "Overload Analysis",
        subtitle = "Heavy days are highlighted from pending deadlines."
    ) {
        dayLoads.forEach { (offset, load) ->
            val label = when (offset) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> "Day +$offset"
            }
            val severityColor = when {
                load >= 4 -> MaterialTheme.colorScheme.error
                load >= 2 -> MaterialTheme.colorScheme.tertiary
                else -> MaterialTheme.colorScheme.primary
            }
            ElevatedCard {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(label, fontWeight = FontWeight.SemiBold)
                        Text("$load task(s)", color = severityColor, fontWeight = FontWeight.SemiBold)
                    }
                    LinearProgressIndicator(
                        progress = { (load.toFloat() / maxLoad).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = severityColor
                    )
                }
            }
        }
    }
}

@Composable
fun StudyReadinessSummaryScreen(navController: NavController, viewModel: StudyViewModel) {
    val courses by viewModel.allCourses.collectAsState()
    BaseFeatureScreen(
        navController = navController,
        title = "Study Readiness",
        subtitle = "Readiness score and risk per course."
    ) {
        if (courses.isEmpty()) {
            Text("No courses available.")
        } else {
            courses.forEach { course ->
                val readiness = viewModel.studyReadinessScore(course)
                val risk = viewModel.courseRiskScore(course)
                val color = when {
                    readiness >= 70 -> MaterialTheme.colorScheme.secondary
                    readiness >= 40 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.error
                }
                ElevatedCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(course.title, fontWeight = FontWeight.SemiBold)
                        Text("Readiness: $readiness%  •  Risk: ${String.format("%.0f", risk)}/100", color = color)
                    }
                }
            }
        }
    }
}

@Composable
fun TopPriorityTaskScreen(navController: NavController, viewModel: StudyViewModel) {
    val top = viewModel.sortedTasks().firstOrNull { !it.isCompleted }
    BaseFeatureScreen(
        navController = navController,
        title = "Top Priority Task",
        subtitle = "Most urgent and impactful task to execute now."
    ) {
        if (top == null) {
            Text("No active tasks.")
        } else {
            ElevatedCard(onClick = { navController.navigate(Screen.TaskDetail.createRoute(top.id)) }) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(top.title, fontWeight = FontWeight.Bold)
                    Text("${top.subject} • ${top.deadlineLabel()}")
                    Text("Score ${String.format("%.0f", top.score())}/100 • ${top.priorityLabel()}")
                }
            }
            Button(
                onClick = { navController.navigate(Screen.Focus.createRoute(top.id)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Focus Session")
            }
        }
    }
}

@Composable
fun SkipTheseScreen(navController: NavController, viewModel: StudyViewModel) {
    val tasks = viewModel.sortedTasks().filter { !it.isCompleted }
    val skipList = tasks.filter { viewModel.survivalLabel(it) == AppStrings.get(LocalTunisianMode.current).skip }
    BaseFeatureScreen(
        navController = navController,
        title = "Skip These Suggestions",
        subtitle = "Lower-value tasks to postpone under pressure."
    ) {
        if (skipList.isEmpty()) {
            Text("No tasks suggested for skipping.")
        } else {
            skipList.forEach { task ->
                ElevatedCard(
                    onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }
                ) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        Text("Reason: lower survival score for current deadline pressure.")
                    }
                }
            }
        }
    }
}

@Composable
fun TimeBasedRescueScreen(navController: NavController, viewModel: StudyViewModel) {
    val panicTasks = viewModel.panicTasks().take(4)
    BaseFeatureScreen(
        navController = navController,
        title = "Time-Based Rescue Plan",
        subtitle = "Hour-by-hour emergency sequence from top panic tasks."
    ) {
        if (panicTasks.isEmpty()) {
            Text("No panic tasks currently detected.")
        } else {
            panicTasks.forEachIndexed { index, task ->
                ElevatedCard(onClick = { navController.navigate(Screen.TaskDetail.createRoute(task.id)) }) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Hour ${index + 1}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        Text(task.title, fontWeight = FontWeight.SemiBold)
                        Text(viewModel.minimumViableWork(task), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        OutlinedButton(
            onClick = { navController.navigate(Screen.RescuePlanDetail.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Rescue Plan Detail")
        }
    }
}

@Composable
fun SyncStatusScreen(navController: NavController) {
    BaseFeatureScreen(
        navController = navController,
        title = "Sync Status",
        subtitle = "Background sync indicator state."
    ) {
        ElevatedCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Last Sync: Just now", fontWeight = FontWeight.SemiBold)
                Text("Status: Healthy", color = MaterialTheme.colorScheme.secondary)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { navController.navigate(Screen.OfflineBannerDemo.route) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Offline Banner")
            }
            OutlinedButton(
                onClick = { navController.navigate(Screen.DataConflictResolve.route) },
                modifier = Modifier.weight(1f)
            ) {
                Text("Conflict Resolver")
            }
        }
    }
}

@Composable
fun OfflineModeBannerScreen(navController: NavController) {
    BaseFeatureScreen(
        navController = navController,
        title = "Offline Mode",
        subtitle = "Offline state and retry UX."
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Text(
                "You are offline. Changes are queued and will sync automatically.",
                modifier = Modifier.padding(14.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
        Button(
            onClick = { navController.navigate(Screen.SyncStatus.route) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Retry Connection")
        }
    }
}

@Composable
fun DataConflictResolutionScreen(navController: NavController) {
    var resolution by remember { mutableStateOf("") }

    BaseFeatureScreen(
        navController = navController,
        title = "Data Conflict Resolution",
        subtitle = "Resolve local and remote change conflicts."
    ) {
        ElevatedCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Conflict detected on task progress", fontWeight = FontWeight.SemiBold)
                Text("Local: 40% • Remote: 70%")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { resolution = "Local value kept. Pending sync update queued." },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Keep Local")
                    }
                    Button(
                        onClick = { resolution = "Remote value applied. Local cache updated." },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Use Remote")
                    }
                }
                if (resolution.isNotBlank()) {
                    Text(resolution, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun AiTaskBreakdownScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    taskId: Int
) {
    val task = viewModel.getTaskById(taskId)
    var instruction by remember { mutableStateOf("Break this task into actionable subtasks") }
    var generatedSteps by remember(taskId) {
        mutableStateOf(
            listOf(
                "Clarify requirements",
                "Create solution outline",
                "Implement first draft",
                "Validate and test",
                "Final review and submit"
            )
        )
    }

    BaseFeatureScreen(
        navController = navController,
        title = "Task Breakdown",
        subtitle = "AI breakdown for selected task."
    ) {
        Text("Task: ${task?.title ?: "Unknown"}", fontWeight = FontWeight.SemiBold)
        OutlinedTextField(
            value = instruction,
            onValueChange = { instruction = it },
            label = { Text("Breakdown instruction") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                val subject = task?.title?.ifBlank { "the task" } ?: "the task"
                generatedSteps = listOf(
                    "Define the exact deliverable for $subject",
                    "Collect materials and references needed",
                    "Build the first complete draft",
                    "Review gaps and improve weak parts",
                    "Finalize and submit before deadline"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(Modifier.size(6.dp))
            Text("Generate Breakdown")
        }
        if (task != null) {
            OutlinedButton(
                onClick = {
                    generatedSteps.forEach { step ->
                        viewModel.addSubTask(task.id, step)
                    }
                    navController.navigate(Screen.TaskDetail.createRoute(task.id))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Steps As Subtasks")
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                generatedSteps.forEachIndexed { index, step ->
                    Text("${index + 1}. $step")
                }
            }
        }
    }
}

@Composable
fun SystemGalleryScreen(
    navController: NavController,
    title: String,
    subtitle: String
) {
    BaseFeatureScreen(
        navController = navController,
        title = title,
        subtitle = subtitle
    ) {
        ElevatedCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("State A", fontWeight = FontWeight.SemiBold)
                Text("No tasks available")
            }
        }
        ElevatedCard {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("State B", fontWeight = FontWeight.SemiBold)
                Text("Network timeout")
            }
        }
    }
}

@Composable
fun QuickHubScreen(
    navController: NavController,
    title: String,
    subtitle: String,
    links: List<ScreenLink>,
    outerPadding: PaddingValues = PaddingValues()
) {
    BaseFeatureScreen(
        navController = navController,
        title = title,
        subtitle = subtitle,
        outerPadding = outerPadding
    ) {
        links.forEach { link ->
            LinkRow(link.label, link.route, navController)
        }
    }
}

@Composable
fun DashboardSubscreensHub(navController: NavController) {
    QuickHubScreen(
        navController = navController,
        title = "Dashboard Details",
        subtitle = "Production dashboard drill-down pages.",
        links = listOf(
            ScreenLink("Full Risk Alerts", Screen.FullRiskAlerts.route),
            ScreenLink("Full Weekly Summary", Screen.FullWeeklySummary.route),
            ScreenLink("Global Search", Screen.GlobalSearch.route),
            ScreenLink("Notifications Center", Screen.Notifications.route)
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseFeatureScreen(
    navController: NavController,
    title: String,
    subtitle: String,
    outerPadding: PaddingValues = PaddingValues(),
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .padding(bottom = outerPadding.calculateBottomPadding())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun LinkRow(label: String, route: String, navController: NavController) {
    Card(
        onClick = { navController.navigate(route) },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}

@Composable
fun GenericPlaceholderScreen(
    navController: NavController,
    title: String,
    subtitle: String,
    links: List<ScreenLink> = emptyList(),
    outerPadding: PaddingValues = PaddingValues()
) {
    var quickInput by remember { mutableStateOf("") }

    BaseFeatureScreen(
        navController = navController,
        title = title,
        subtitle = subtitle,
        outerPadding = outerPadding
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Quick Actions", fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { navController.navigate(Screen.Add.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Task")
                    }
                    Button(
                        onClick = { navController.navigate(Screen.AddCourse.route) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Course")
                    }
                }
                OutlinedTextField(
                    value = quickInput,
                    onValueChange = { quickInput = it },
                    label = { Text("AI quick input") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = { navController.navigate(Screen.AiAssistant.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open AI Assistant")
                }
            }
        }

        if (links.isEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Text(
                    "Screen is implemented and routed. Detailed business logic can be iterated next.",
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            links.forEach { link ->
                LinkRow(link.label, link.route, navController)
            }
        }
    }
}
