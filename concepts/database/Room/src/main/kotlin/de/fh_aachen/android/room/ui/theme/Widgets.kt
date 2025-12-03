package de.fh_aachen.android.room.ui.theme

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
    iconResourceId: Int, // Pass your drawable ID here
    contentDescription: String,
    backgroundColor: Color = MaterialTheme.colorScheme.primary, // Background color of the circle
    iconTint: Color = Color.White, // Color of the icon itself
    onClick: () -> Unit
) {
    IconButton(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(48.dp) // Size of the circle
                .clip(CircleShape) // Clip to circular shape
                .background(Color(0x22000000)), // Set background color
            contentAlignment = Alignment.Center // Center the content
        ) {
            Icon(
                painter = painterResource(id = iconResourceId),
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(36.dp) // Size of the icon within the circle
            )
        }
    }
}
