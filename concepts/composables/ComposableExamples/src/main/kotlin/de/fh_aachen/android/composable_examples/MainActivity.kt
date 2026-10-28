// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.composable_examples

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.ui_tools.ConceptTheme

/*
The MainActivity of this application demonstrates various ways to arrange and display
UI elements using Jetpack Compose. It showcases different layout components like Row, Column,
Box, and LazyRow, along with modifiers for styling and interaction.

Refs.:
  - https://developer.android.com/develop/ui/compose/components
 */

const val TAG = "MAIN"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // all in one...
        Row {
            Text("This")
            Text("is")
            Text("Text")
        }
        // or as a Composable
        SimpleRow()
        RowWithModifiers()
        RowAndBoxWithAlignments()
        CrowdedRow()
        HorizontallyScrolledRow()
        ScrollableRow()
        RowWithImage()
    }
}

@Composable
fun SimpleRow() {
    Row {
        Text("This")
        Text("is")
        Text("Text")
    }
}

/*
 * Modifier order matters. Modifiers wrap each other from left to right (outside in):
 *  - padding, then background: the padding is outside the colored area (like a margin),
 *  - background, then padding: the padding is inside the colored area.
 * There is no 'margin' in Compose - it is just padding before background.
 */
@Composable
fun RowWithModifiers() {
    Column {
        Row(modifier = Modifier
            .padding(all = 14.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Text("padding")
            Text("then", modifier = Modifier.padding(all = 4.dp))
            Text("background")
        }
        Row(modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(all = 14.dp)
        ) {
            Text("background")
            Text("then", modifier = Modifier.padding(all = 4.dp))
            Text("padding")
        }
    }
}

@Composable
fun RowAndBoxWithAlignments() {
    Row(modifier = Modifier
        .background(MaterialTheme.colorScheme.secondaryContainer)
        .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text("This")
        }
        Box(modifier = Modifier
            .weight(3f)
            .background(MaterialTheme.colorScheme.tertiaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("is")
        }
        Box(modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Text("Text")
        }
    }
}

// Too much content for a Row: the rest is simply cut off.
@Composable
fun CrowdedRow() {
    Row(modifier = Modifier
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .clickable { Log.v(TAG, "clicked1") }
    ) {
        for (i in 1..15)
            Text("Text-$i ")
    }
}

/*
 * All 15 Texts are composed at once - fine for a few elements, but for long lists the LazyRow
 * below composes only what is visible.
 */
@Composable
fun HorizontallyScrolledRow() {
    Row(modifier = Modifier
        .background(MaterialTheme.colorScheme.surfaceVariant)
        .horizontalScroll(rememberScrollState())
    ) {
        for (i in 1..15)
            Text("Text-$i ")
    }
}

@Composable
fun ScrollableRow() {
    LazyRow  {
        items(15) { i ->                    // or items(list) { element -> ... }
            Text("Text-${i + 1} ")
        }
    }
}

/*
 * Two ways to make an image clickable:
 *  - Modifier.clickable: the order matters again - clip first, so the ripple is round, too,
 *  - Surface(onClick = ...): shape, ripple and accessibility semantics (role, enabled) in one.
 * Before, the second image was wrapped in an IconButton - made for 24 dp icons, not for photos.
 * And: a clickable element needs a contentDescription, otherwise TalkBack announces nothing
 * useful. Only purely decorative images get null.
 */
@Composable
fun RowWithImage() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(R.drawable.beach),
            contentDescription = "Beach photo",
            modifier = Modifier
                .size(75.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable { Log.v(TAG, "Image clicked") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text("Beach")
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            onClick = { Log.v(TAG, "Surface clicked") },
            shape = CircleShape,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
            modifier = Modifier.size(75.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.beach),
                contentDescription = "Beach photo, as a button",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

//----- preview area

/*
 * Previews of the building blocks - the quickest way to try layout changes.
 * A Surface gives them the theme's background and content color (light and dark).
 */
@Composable
private fun PreviewFrame(content: @Composable () -> Unit) {
    ConceptTheme {
        Surface { content() }
    }
}

@PreviewLightDark
@Composable
private fun MainScreenPreview() {
    PreviewFrame { MainScreen() }
}

@Preview
@Composable
private fun RowWithModifiersPreview() {
    PreviewFrame { RowWithModifiers() }
}

@Preview
@Composable
private fun RowAndBoxWithAlignmentsPreview() {
    PreviewFrame { RowAndBoxWithAlignments() }
}

@Preview
@Composable
private fun RowWithImagePreview() {
    PreviewFrame { RowWithImage() }
}
