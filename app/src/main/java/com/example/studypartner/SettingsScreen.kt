package com.example.studypartner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    outerPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val notificationsEnabled by UserPreferences.notificationsEnabled(context)
        .collectAsState(initial = true)
    val defaultDifficulty by UserPreferences.defaultDifficulty(context)
        .collectAsState(initial = Level.LOW.value)
    val defaultUrgency by UserPreferences.defaultUrgency(context)
        .collectAsState(initial = Level.LOW.value)
    val tunisianMode by UserPreferences.tunisianMode(context)
        .collectAsState(initial = false)
    val openRouterApiKey by UserPreferences.openRouterApiKey(context)
        .collectAsState(initial = "")
    val awsAccessKey by UserPreferences.awsAccessKey(context)
        .collectAsState(initial = "")
    val awsSecretKey by UserPreferences.awsSecretKey(context)
        .collectAsState(initial = "")
    val awsRegion by UserPreferences.awsRegion(context)
        .collectAsState(initial = "us-east-1")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .padding(bottom = outerPadding.calculateBottomPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            // ── AI Configuration ──────────────────────────────────────────────
            SettingsSectionHeader("AI Configuration")

            SettingsGroup {
                var keyText by remember(openRouterApiKey) { mutableStateOf(openRouterApiKey) }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("OpenRouter API Key", style = MaterialTheme.typography.titleSmall)
                    }
                    OutlinedTextField(
                        value = keyText,
                        onValueChange = { keyText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("sk-or-v1-...") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = { scope.launch { UserPreferences.setOpenRouterApiKey(context, keyText) } },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Key")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── AWS Bedrock Fallback ──────────────────────────────────────────
            SettingsSectionHeader("AWS Bedrock Fallback (Nova)")

            SettingsGroup {
                var accessKey by remember(awsAccessKey) { mutableStateOf(awsAccessKey) }
                var secretKey by remember(awsSecretKey) { mutableStateOf(awsSecretKey) }
                var region    by remember(awsRegion) { mutableStateOf(awsRegion) }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = accessKey,
                        onValueChange = { accessKey = it },
                        label = { Text("AWS Access Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = secretKey,
                        onValueChange = { secretKey = it },
                        label = { Text("AWS Secret Key") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = region,
                        onValueChange = { region = it },
                        label = { Text("AWS Region (e.g., us-east-1)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = { scope.launch { UserPreferences.setAwsCredentials(context, accessKey, secretKey, region) } },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save AWS Credentials")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Notifications ─────────────────────────────────────────────────
            SettingsSectionHeader("Notifications")

            SettingsGroup {
                SettingsToggleRow(
                    icon     = Icons.Default.Notifications,
                    title    = "Deadline reminders",
                    subtitle = "Get notified when tasks are due soon",
                    checked  = notificationsEnabled,
                    onChange = { scope.launch { UserPreferences.setNotificationsEnabled(context, it) } }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Personality ───────────────────────────────────────────────────
            SettingsSectionHeader("Personality")

            SettingsGroup {
                SettingsToggleRow(
                    icon     = Icons.Default.Translate,
                    title    = "Tunisian Mode",
                    subtitle = "Use Tunisian dialect for Survival Mode strings",
                    checked  = tunisianMode,
                    onChange = { scope.launch { UserPreferences.setTunisianMode(context, it) } }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── New Task Defaults ─────────────────────────────────────────────
            SettingsSectionHeader("New Task Defaults")

            SettingsGroup {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SettingsLevelPicker(
                        label    = "Default Difficulty",
                        selected = Level.fromValue(defaultDifficulty),
                        onSelect = { scope.launch { UserPreferences.setDefaultDifficulty(context, it.value) } }
                    )
                    Spacer(Modifier.height(4.dp))
                    SettingsLevelPicker(
                        label    = "Default Urgency",
                        selected = Level.fromValue(defaultUrgency),
                        onSelect = { scope.launch { UserPreferences.setDefaultUrgency(context, it.value) } }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── About ─────────────────────────────────────────────────────────
            SettingsSectionHeader("About")

            SettingsGroup {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        color    = MaterialTheme.colorScheme.primaryContainer,
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.School, contentDescription = null,
                                tint     = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Column {
                        Text("StudyPartner",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.onSurface)
                        Text("Version 1.0 · AI-powered student planner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            SettingsSectionHeader("Navigation")

            SettingsGroup {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsNavRow(
                        icon = Icons.Default.Person,
                        title = "Profile",
                        subtitle = "Open profile and account modules",
                        onClick = { navController.navigate(Screen.Profile.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.Notifications,
                        title = "Notifications Center",
                        subtitle = "Deadline, risk, reminder, and AI alerts",
                        onClick = { navController.navigate(Screen.Notifications.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.Search,
                        title = "Global Search",
                        subtitle = "Search tasks, courses, and sessions",
                        onClick = { navController.navigate(Screen.GlobalSearch.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.Warning,
                        title = "Emergency Mode",
                        subtitle = "Open Ghasret Lekleb dashboard",
                        onClick = { navController.navigate(Screen.GhasretActivation.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.ViewAgenda,
                        title = "Empty States",
                        subtitle = "Production empty state screens",
                        onClick = { navController.navigate(Screen.EmptyStateGallery.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.HourglassEmpty,
                        title = "Loading States",
                        subtitle = "Skeleton and loading surfaces",
                        onClick = { navController.navigate(Screen.LoadingStateGallery.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.ErrorOutline,
                        title = "Error States",
                        subtitle = "Network and failure UI screens",
                        onClick = { navController.navigate(Screen.ErrorStateGallery.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.CheckCircle,
                        title = "Success Feedback",
                        subtitle = "Completion and success confirmations",
                        onClick = { navController.navigate(Screen.SuccessFeedbackDemo.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.HelpOutline,
                        title = "Confirmations",
                        subtitle = "Delete/complete confirmation dialogs",
                        onClick = { navController.navigate(Screen.ConfirmationsDemo.route) }
                    )
                    SettingsNavRow(
                        icon = Icons.Default.Sync,
                        title = "Sync & Offline",
                        subtitle = "Sync status, offline banner, conflict resolution",
                        onClick = { navController.navigate(Screen.SyncStatus.route) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        UserPreferences.logout(context)
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Sign Out")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(text: String) {
    Text(
        text       = text,
        style      = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color      = MaterialTheme.colorScheme.primary,
        modifier   = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape     = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) { content() }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier           = Modifier.size(22.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun SettingsLevelPicker(label: String, selected: Level, onSelect: (Level) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface)
            Text(
                selected.label,
                style      = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Level.entries.forEach { level ->
                FilterChip(
                    selected = level == selected,
                    onClick  = { onSelect(level) },
                    label    = { Text(level.label, style = MaterialTheme.typography.labelSmall) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
