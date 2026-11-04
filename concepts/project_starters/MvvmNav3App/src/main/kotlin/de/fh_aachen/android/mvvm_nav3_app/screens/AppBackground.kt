// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview

// Soft, slowly drifting color blobs behind all screens; follows the theme (light/dark, dynamic colors).
@Composable
fun AppBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val drift by rememberInfiniteTransition(label = "background").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 20_000, easing = LinearEasing), RepeatMode.Reverse),
        label = "drift",
    )

    // 'drift' is read inside drawBehind: the animation only redraws, it never recomposes.
    Box(
        modifier = modifier.fillMaxSize().drawBehind {
            drawRect(colors.surface)
            blob(colors.primaryContainer, Offset(size.width * (0.1f + 0.3f * drift), size.height * 0.15f), size.maxDimension * 0.7f)
            blob(colors.tertiaryContainer, Offset(size.width * (0.9f - 0.3f * drift), size.height * 0.85f), size.maxDimension * 0.7f)
            blob(colors.secondaryContainer, Offset(size.width * 0.8f, size.height * (0.3f + 0.3f * drift)), size.maxDimension * 0.5f)
        },
    ) {
        content()
    }
}

// A radial gradient from 'color' to transparent.
private fun DrawScope.blob(color: Color, center: Offset, radius: Float) {
    drawRect(Brush.radialGradient(listOf(color, Color.Transparent), center = center, radius = radius))
}

@Preview
@Composable
private fun AppBackgroundPreview() {
    StarterTheme {
        AppBackground {}
    }
}
