package com.example.studypartner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.studypartner.ui.theme.BookmarkGold
import com.example.studypartner.ui.theme.DeepOrange
import com.example.studypartner.ui.theme.Dimens
import com.example.studypartner.ui.theme.LimeCheck
import com.example.studypartner.ui.theme.OrangeCheck
import com.example.studypartner.ui.theme.SoftGold
import com.example.studypartner.ui.theme.WarmAmber

private data class QuickPrompt(val display: String, val question: String, val accent: Color)

private val QUICK_PROMPTS = listOf(
    QuickPrompt("What should I do now?",        "What is the single most important task I should work on right now and why?", DeepOrange),
    QuickPrompt("Plan my next 3 days",          "Create a realistic 3-day study plan based on my current tasks and deadlines.", BookmarkGold),
    QuickPrompt("Why is my top task risky?",    "Explain clearly why my highest-priority task is risky and what I should do about it.", OrangeCheck),
    QuickPrompt("Break top task down",          "Break my highest-priority task into 5 concrete, actionable subtasks I can do one by one.", LimeCheck),
    QuickPrompt("I'm overwhelmed",              "I'm feeling overwhelmed by my workload. Calm me down and give me ONE concrete next step.", WarmAmber),
    QuickPrompt("Best use of 1 hour",           "If I only have 1 hour to study right now, what should I focus on and how should I split that time?", SoftGold),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    navController: NavController,
    viewModel: StudyViewModel,
    outerPadding: PaddingValues = PaddingValues()
) {

    val assistantState by viewModel.assistantState.collectAsState()
    var customQuestion  by rememberSaveable { mutableStateOf("") }
    var lastQuestion    by rememberSaveable { mutableStateOf("") }
    val isLoading       = assistantState is AiState.Loading

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BookmarkGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF3D2400), modifier = Modifier.size(16.dp))
                        }
                        Text("AI Assistant", fontWeight = FontWeight.SemiBold)
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
                    containerColor    = MaterialTheme.colorScheme.background,
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
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceLg)
            ) {
                Spacer(Modifier.height(Dimens.SpaceXs))

                // Hero
                Column {
                    Text(
                        "ASK YOUR AI",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(Dimens.SpaceXs))
                    Text(
                        text       = "What's on\nyour mind?",
                        fontSize   = 38.sp,
                        lineHeight = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Tab pills (Prompts / Plan / Recovery)
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)) {
                    NavPill(label = "Prompts",  accent = DeepOrange,    onClick = { navController.navigate(Screen.AiPrompts.route) })
                    NavPill(label = "Plan",     accent = LimeCheck,     onClick = { navController.navigate(Screen.AiGeneratedPlan.route) })
                    NavPill(label = "Recovery", accent = MaterialTheme.colorScheme.error, onClick = { navController.navigate(Screen.RecoveryPlan.route) })
                }

                // Response area
                AnimatedContent(
                    targetState   = assistantState,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label         = "assistantState"
                ) { state ->
                    when (state) {
                        is AiState.Idle -> IdleHero()

                        is AiState.Loading -> Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color    = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape    = RoundedCornerShape(20.dp),
                            border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(Dimens.CardPadding),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                QuestionLine(lastQuestion)
                                Spacer(Modifier.height(4.dp))
                                ShimmerBox(Modifier.fillMaxWidth(0.95f), height = 14.dp)
                                ShimmerBox(Modifier.fillMaxWidth(0.80f), height = 14.dp)
                                ShimmerBox(Modifier.fillMaxWidth(0.90f), height = 14.dp)
                                ShimmerBox(Modifier.fillMaxWidth(0.65f), height = 14.dp)
                            }
                        }

                        is AiState.Success    -> ResponseCard(question = lastQuestion, response = state.response)
                        is AiState.Failure    -> ResponseCard(question = lastQuestion, response = viewModel.getLocalAssistantResponse(lastQuestion), isLocalFallback = true, errorDetail = state.message)
                        is AiState.Unavailable -> ResponseCard(question = lastQuestion, response = viewModel.getLocalAssistantResponse(lastQuestion), isLocalFallback = true)
                    }
                }

                // Quick prompts
                Text(
                    "QUICK PROMPTS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceMd)
                ) {
                    QUICK_PROMPTS.forEach { prompt ->
                        PromptCard(
                            label   = prompt.display,
                            accent  = prompt.accent,
                            enabled = !isLoading,
                            onClick = {
                                lastQuestion = prompt.question
                                viewModel.fetchAssistantResponse(prompt.question)
                            }
                        )
                    }
                }

                Spacer(Modifier.height(Dimens.SpaceMd))
            }

            // Pinned input bar
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                tonalElevation = 0.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = Dimens.ScreenPadding, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceSm)
                ) {
                    PaperInput(
                        value = customQuestion,
                        onChange = { customQuestion = it },
                        modifier = Modifier.weight(1f),
                        placeholder = "Ask anything…",
                        onSubmit = {
                            if (customQuestion.isNotBlank() && !isLoading) {
                                lastQuestion = customQuestion
                                viewModel.fetchAssistantResponse(customQuestion)
                                customQuestion = ""
                            }
                        }
                    )
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clickable(enabled = customQuestion.isNotBlank() && !isLoading) {
                                lastQuestion = customQuestion
                                viewModel.fetchAssistantResponse(customQuestion)
                                customQuestion = ""
                            },
                        color = if (customQuestion.isNotBlank() && !isLoading) DeepOrange
                                else MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (customQuestion.isNotBlank()) Color.White
                                           else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── COMPONENTS ──────────────────────────────────────────────────────────────

@Composable
private fun NavPill(label: String, accent: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color    = MaterialTheme.colorScheme.surfaceContainer,
        shape    = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accent))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun PromptCard(label: String, accent: Color, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .width(170.dp)
            .height(96.dp)
            .clickable(enabled = enabled) { onClick() },
        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(16.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 12.dp)
                    .width(8.dp)
                    .height(18.dp)
                    .background(accent, shape = RoundedCornerShape(bottomStart = 2.dp, bottomEnd = 2.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3
                )
            }
        }
    }
}

@Composable
private fun IdleHero() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.primaryContainer,
        shape    = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DeepOrange))
                Text(
                    "STUDIO ASSISTANT",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Text(
                "Ask anything about your tasks — what to prioritise, plan your week, why a task is risky, or how to break it down.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Surface(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "OpenRouter · falls back to local rules when offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color    = MaterialTheme.colorScheme.surfaceContainerLowest,
        shape    = RoundedCornerShape(20.dp),
        border   = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            QuestionLine(question)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Text(
                text  = response,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isLocalFallback) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "OpenRouter ${if (errorDetail != null) "error: $errorDetail" else "unreachable"} · showing local response",
                        style    = MaterialTheme.typography.labelSmall,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionLine(question: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(BookmarkGold),
            contentAlignment = Alignment.Center
        ) {
            Text("Q", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF3D2400))
        }
        Text(
            text = question.ifBlank { "—" },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3
        )
    }
}

@Composable
private fun PaperInput(
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String,
    onSubmit: () -> Unit
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            if (value.isEmpty()) {
                Text(
                    placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp),
                cursorBrush = SolidColor(DeepOrange),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSubmit() }),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
