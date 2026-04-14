package com.example.studypartner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private data class QuickPrompt(val display: String, val question: String)

private val QUICK_PROMPTS = listOf(
    QuickPrompt("What should I do now?",   "What is the single most important task I should work on right now and why?"),
    QuickPrompt("Plan my next 3 days",     "Create a realistic 3-day study plan based on my current tasks and deadlines."),
    QuickPrompt("Why is my top task risky?","Explain clearly why my highest-priority task is risky and what I should do about it."),
    QuickPrompt("Break top task down",     "Break my highest-priority task into 5 concrete, actionable subtasks I can do one by one."),
    QuickPrompt("I'm overwhelmed",         "I'm feeling overwhelmed by my workload. Calm me down and give me ONE concrete next step."),
    QuickPrompt("Best use of 1 hour",      "If I only have 1 hour to study right now, what should I focus on and how should I split that time?")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    outerPadding: PaddingValues = PaddingValues()
) {

    val assistantState by viewModel.assistantState.collectAsState()
    var customQuestion  by remember { mutableStateOf("") }
    var lastQuestion    by remember { mutableStateOf("") }
    val isLoading       = assistantState is AiState.Loading

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color    = MaterialTheme.colorScheme.primaryContainer,
                            shape    = CircleShape,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AutoAwesome, contentDescription = null,
                                    tint     = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("AI Assistant")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = assistantState !is AiState.Idle,
                        enter   = fadeIn(),
                        exit    = fadeOut()
                    ) {
                        IconButton(onClick = { viewModel.clearAssistant() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Clear")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(top = padding.calculateTopPadding())
                .padding(bottom = outerPadding.calculateBottomPadding())
                .fillMaxSize()
        ) {
            // ── Scrollable body ───────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(
                        onClick = { navController.navigate(Screen.AiPrompts.route) },
                        label = { Text("Prompts") }
                    )
                    AssistChip(
                        onClick = { navController.navigate(Screen.AiGeneratedPlan.route) },
                        label = { Text("Plan") }
                    )
                    AssistChip(
                        onClick = { navController.navigate(Screen.RecoveryPlan.route) },
                        label = { Text("Recovery") }
                    )
                }

                // ── Response area ─────────────────────────────────────────────
                AnimatedContent(
                    targetState   = assistantState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label         = "assistantState"
                ) { state ->
                    when (state) {
                        is AiState.Idle -> IdlePlaceholder()

                        is AiState.Loading -> Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors   = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            shape    = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    "Q: $lastQuestion",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                                )
                                Spacer(Modifier.height(6.dp))
                                ShimmerBox(Modifier.fillMaxWidth(0.95f), height = 16.dp)
                                ShimmerBox(Modifier.fillMaxWidth(0.80f), height = 16.dp)
                                ShimmerBox(Modifier.fillMaxWidth(0.90f), height = 16.dp)
                                ShimmerBox(Modifier.fillMaxWidth(0.65f), height = 16.dp)
                            }
                        }

                        is AiState.Success -> ResponseCard(
                            question = lastQuestion,
                            response = state.response
                        )

                        is AiState.Failure -> ResponseCard(
                            question        = lastQuestion,
                            response        = viewModel.getLocalAssistantResponse(lastQuestion),
                            isLocalFallback = true,
                            errorDetail     = state.message
                        )

                        is AiState.Unavailable -> ResponseCard(
                            question        = lastQuestion,
                            response        = viewModel.getLocalAssistantResponse(lastQuestion),
                            isLocalFallback = true
                        )
                    }
                }

                // ── Quick prompts ─────────────────────────────────────────────
                Text(
                    "Quick Prompts",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QUICK_PROMPTS.forEach { prompt ->
                        PromptChipCard(
                            label   = prompt.display,
                            enabled = !isLoading,
                            onClick = {
                                lastQuestion = prompt.question
                                viewModel.fetchAssistantResponse(prompt.question)
                            }
                        )
                    }
                }
            }

            // ── Pinned input bar ──────────────────────────────────────────────
            Surface(
                tonalElevation = 6.dp,
                color          = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value         = customQuestion,
                        onValueChange = { customQuestion = it },
                        placeholder   = { Text("Ask anything about your tasks…") },
                        singleLine    = true,
                        modifier      = Modifier.weight(1f),
                        shape         = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            if (customQuestion.isNotBlank() && !isLoading) {
                                lastQuestion = customQuestion
                                viewModel.fetchAssistantResponse(customQuestion)
                                customQuestion = ""
                            }
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    FilledIconButton(
                        onClick  = {
                            if (customQuestion.isNotBlank() && !isLoading) {
                                lastQuestion = customQuestion
                                viewModel.fetchAssistantResponse(customQuestion)
                                customQuestion = ""
                            }
                        },
                        enabled  = customQuestion.isNotBlank() && !isLoading,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptChipCard(label: String, enabled: Boolean, onClick: () -> Unit) {
    ElevatedCard(
        onClick   = onClick,
        enabled   = enabled,
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp),
        modifier  = Modifier.width(160.dp)
    ) {
        Text(
            text     = label,
            style    = MaterialTheme.typography.labelMedium,
            color    = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            maxLines = 3
        )
    }
}

@Composable
private fun IdlePlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape    = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome, contentDescription = null,
                    tint     = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Your AI Study Assistant",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                "Ask me anything about your tasks — what to prioritise, " +
                "how to plan your week, why a task is risky, or how to break it into steps.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Surface(
                color    = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape    = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Powered by OpenRouter · Falls back to local rules when offline",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ResponseCard(
    question: String, 
    response: String, 
    isLocalFallback: Boolean = false,
    errorDetail: String? = null
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome, contentDescription = null,
                    tint     = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Q: $question",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.65f),
                    maxLines   = 2
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                text  = response,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            if (isLocalFallback) {
                Surface(
                    color    = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.08f),
                    shape    = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            "OpenRouter error: ${errorDetail ?: "Service unreachable"} · showing local response",
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.55f)
                        )
                    }
                }
            }
        }
    }
}
