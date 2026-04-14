package com.example.studypartner

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val body: String
)

private val pages = listOf(
    OnboardingPage(
        icon  = Icons.Default.Star,
        title = "Smart Priority",
        body  = "Add your study tasks with difficulty and urgency ratings. StudyPartner automatically calculates a weighted score so you always know what to tackle first."
    ),
    OnboardingPage(
        icon  = Icons.Default.AutoAwesome,
        title = "AI-Powered Advice",
        body  = "Get real-time study recommendations powered by a local AI model. The advice adapts to your task load, deadlines, and risk levels."
    ),
    OnboardingPage(
        icon  = Icons.Default.DateRange,
        title = "Deadline Tracking",
        body  = "Set deadlines on any task and get notified before they slip. Overdue tasks rise to the top automatically — nothing falls through the cracks."
    ),
    OnboardingPage(
        icon  = Icons.Default.CheckCircle,
        title = "You're All Set",
        body  = "Add your first task, set its priority, and let StudyPartner guide your study sessions. Your grades will thank you."
    )
)

@Composable
fun OnboardingScreen(navController: NavController) {

    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLast     = pagerState.currentPage == pages.size - 1

    val bgGradient = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            PageContent(page = pages[pageIndex])
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Pill indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue   = if (isSelected) 28.dp else 8.dp,
                        animationSpec = tween(300),
                        label         = "dot_width_$index"
                    )
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant
                            )
                    )
                }
            }

            // Primary button
            Button(
                onClick = {
                    if (isLast) {
                        scope.launch {
                            UserPreferences.setOnboardingDone(context)
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier       = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape          = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text  = if (isLast) "Get Started" else "Next",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            // Skip link
            if (!isLast) {
                TextButton(
                    onClick = {
                        scope.launch {
                            UserPreferences.setOnboardingDone(context)
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    }
                ) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon circle
        Surface(
            shape    = CircleShape,
            color    = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(128.dp),
            tonalElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = page.icon,
                    contentDescription = null,
                    modifier           = Modifier.size(60.dp),
                    tint               = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(Modifier.height(56.dp))

        Text(
            text       = page.title,
            style      = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign  = TextAlign.Center,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text      = page.body,
            style     = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color     = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Reserve space for the bottom controls
        Spacer(Modifier.height(200.dp))
    }
}
