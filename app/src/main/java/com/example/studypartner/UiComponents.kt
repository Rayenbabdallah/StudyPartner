package com.example.studypartner

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.studypartner.ui.theme.RiskHighBgDark
import com.example.studypartner.ui.theme.RiskHighBg
import com.example.studypartner.ui.theme.RiskHighColor
import com.example.studypartner.ui.theme.RiskHighColorDark
import com.example.studypartner.ui.theme.RiskHighContainerDark
import com.example.studypartner.ui.theme.RiskHighContainer
import com.example.studypartner.ui.theme.RiskMedBg
import com.example.studypartner.ui.theme.RiskMedBgDark
import com.example.studypartner.ui.theme.RiskMedColor
import com.example.studypartner.ui.theme.RiskMedColorDark
import com.example.studypartner.ui.theme.RiskMedContainer
import com.example.studypartner.ui.theme.RiskMedContainerDark
import com.example.studypartner.ui.theme.RiskSafeBg
import com.example.studypartner.ui.theme.RiskSafeBgDark
import com.example.studypartner.ui.theme.RiskSafeColor
import com.example.studypartner.ui.theme.RiskSafeColorDark
import com.example.studypartner.ui.theme.RiskSafeContainer
import com.example.studypartner.ui.theme.RiskSafeContainerDark

// ── Shimmer loading modifier ──────────────────────────────────────────────────

fun Modifier.shimmerEffect(): Modifier = composed {
    val dark = isSystemInDarkTheme()
    // Stitch warm-surface shimmer: surfaceContainerHigh → white (light) / dark equivalents
    val baseColor    = if (dark) Color(0xFF2B2926) else Color(0xFFEBE7E7)
    val shimmerColor = if (dark) Color(0xFF353230) else Color(0xFFFFFFFF)

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue   = 0f,
        targetValue    = 1000f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    background(
        brush = Brush.linearGradient(
            colors      = listOf(baseColor, shimmerColor, baseColor),
            start       = Offset(translateAnim - 300f, 0f),
            end         = Offset(translateAnim, 0f)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

// ── Shimmer placeholder blocks ────────────────────────────────────────────────

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp
) {
    Box(
        modifier = modifier
            .height(height)
            .shimmerEffect()
    )
}

// ── Generic empty state ───────────────────────────────────────────────────────

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Surface(
                color    = MaterialTheme.colorScheme.surfaceVariant,
                shape    = RoundedCornerShape(24.dp),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        modifier           = Modifier.size(48.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text      = title,
                style     = MaterialTheme.typography.titleMedium,
                color     = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text      = subtitle,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.height(4.dp))
                Button(onClick = onAction) {
                    Text(actionLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

// ── Risk color helpers (theme-aware, avoids isSystemInDarkTheme() duplication) ──

data class RiskColorSet(
    val text: Color,
    val background: Color,
    val container: Color
)

@Composable
fun riskColorSet(label: String): RiskColorSet {
    val dark = isSystemInDarkTheme()
    return when (label) {
        "High Risk" -> RiskColorSet(
            text       = if (dark) RiskHighColorDark else RiskHighColor,
            background = if (dark) RiskHighBgDark    else RiskHighBg,
            container  = if (dark) RiskHighContainerDark else RiskHighContainer
        )
        "Moderate" -> RiskColorSet(
            text       = if (dark) RiskMedColorDark else RiskMedColor,
            background = if (dark) RiskMedBgDark    else RiskMedBg,
            container  = if (dark) RiskMedContainerDark else RiskMedContainer
        )
        else -> RiskColorSet(
            text       = if (dark) RiskSafeColorDark else RiskSafeColor,
            background = if (dark) RiskSafeBgDark    else RiskSafeBg,
            container  = if (dark) RiskSafeContainerDark else RiskSafeContainer
        )
    }
}

@Composable
fun scoreToRiskColorSet(score: Double): RiskColorSet = when {
    score >= 80 -> riskColorSet("High Risk")
    score >= 50 -> riskColorSet("Moderate")
    else        -> riskColorSet("Safe")
}
