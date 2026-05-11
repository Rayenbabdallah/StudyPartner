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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.studypartner.ui.theme.BookmarkGold
import com.example.studypartner.ui.theme.DeepOrange
import com.example.studypartner.ui.theme.LimeCheck
import com.example.studypartner.ui.theme.OrangeCheck
import com.example.studypartner.ui.theme.SoftGold
import com.example.studypartner.ui.theme.WarmAmber
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val eyebrow: String,
    val title: String,
    val body: String,
    val accent: Color
)

private val pages = listOf(
    OnboardingPage(
        icon    = Icons.Default.Star,
        eyebrow = "STEP ONE",
        title   = "Smart\npriority.",
        body    = "Difficulty + urgency + grade weight calculate a single score so you always know what to tackle first.",
        accent  = DeepOrange
    ),
    OnboardingPage(
        icon    = Icons.Default.AutoAwesome,
        eyebrow = "STEP TWO",
        title   = "AI-powered\nadvice.",
        body    = "Get real-time study recommendations that adapt to your workload, deadlines, and risk levels.",
        accent  = BookmarkGold
    ),
    OnboardingPage(
        icon    = Icons.Default.DateRange,
        eyebrow = "STEP THREE",
        title   = "Deadlines\ntracked.",
        body    = "Set deadlines on any task and get notified before they slip. Overdue tasks rise to the top automatically.",
        accent  = OrangeCheck
    ),
    OnboardingPage(
        icon    = Icons.Default.CheckCircle,
        eyebrow = "READY",
        title   = "You're all\nset.",
        body    = "Add your first task, set its priority, and let StudyPartner guide your sessions. Your grades will thank you.",
        accent  = LimeCheck
    )
)

@Composable
fun OnboardingScreen(navController: NavController) {

    val context    = LocalContext.current
    val scope      = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val isLast     = pagerState.currentPage == pages.size - 1

    val currentAccent = pages[pagerState.currentPage].accent
    val bgGradient = Brush.verticalGradient(
        listOf(
            currentAccent.copy(alpha = 0.15f),
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
                                if (isSelected) currentAccent
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
                    .height(56.dp),
                shape          = RoundedCornerShape(16.dp),
                colors         = ButtonDefaults.buttonColors(
                    containerColor = DeepOrange,
                    contentColor   = Color.White
                )
            ) {
                Text(
                    text  = if (isLast) "Get started" else "Continue",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
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
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {
        // Bookmark-style icon block
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(page.accent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = page.icon,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = Color.White
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text       = page.eyebrow,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = page.accent
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text       = page.title,
            fontSize   = 56.sp,
            lineHeight = 60.sp,
            fontWeight = FontWeight.ExtraBold,
            color      = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text  = page.body,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(180.dp))
    }
}
