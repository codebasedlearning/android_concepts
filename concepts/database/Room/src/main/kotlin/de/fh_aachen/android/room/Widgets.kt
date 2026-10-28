// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CircularIconButton(
    iconResourceId: Int,                // Pass your drawable ID here
    contentDescription: String,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,  // Background color of the circle
    iconTint: Color = MaterialTheme.colorScheme.onSecondaryContainer,       // Color of the icon itself
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.38f
    IconButton(onClick = onClick, enabled = enabled) {
        Box(
            modifier = Modifier
                .size(48.dp)                                    // Size of the circle
                .clip(CircleShape)                              // Clip to circular shape
                .background(backgroundColor.copy(alpha = alpha)),
            contentAlignment = Alignment.Center                 // Center the content
        ) {
            Icon(
                painter = painterResource(id = iconResourceId),
                contentDescription = contentDescription,
                tint = iconTint.copy(alpha = alpha),
                modifier = Modifier.size(36.dp)                 // Size of the icon within the circle
            )
        }
    }
}
