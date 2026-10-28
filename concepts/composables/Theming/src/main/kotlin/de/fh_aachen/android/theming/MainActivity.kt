// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.theming

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import de.fh_aachen.android.theming.theme.AppDarkColors
import de.fh_aachen.android.theming.theme.AppLightColors
import de.fh_aachen.android.theming.theme.AppTheme
import de.fh_aachen.android.theming.theme.extendedColors

/* ---- TOC ----
   Theming with Material 3:
    - the theme is state at the top of the tree (dark mode, dynamic color as switches)
    - color ROLES instead of color values (a swatch grid of the scheme)
    - typography and shapes, picked up by components automatically
    - own tokens (success, warning) the same way MaterialTheme does it
    - a nested theme for one subtree
    - @PreviewLightDark

   See theme/Color.kt and theme/Theme.kt for the theme itself. All other concept apps use
   the 'pure' ConceptTheme from UiTools instead.
 */

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ThemingApp(
                // The status bar is part of the window, not of Compose. enableEdgeToEdge() picks
                // light/dark icons from the system setting; when our switch differs from it, the
                // icons would be unreadable. So we tell the window, too (it may be called again).
                onDarkThemeChange = { dark ->
                    enableEdgeToEdge(
                        statusBarStyle = SystemBarStyle.auto(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        ) { dark }
                    )
                }
            )
        }
    }
}

/*
 * The theme is just state - and it lives at the very top, so it can flow down to everything.
 * Dark mode and dynamic color are hoisted here; the screen gets values and callbacks.
 * (Unidirectional data flow once more: switches send events up, the theme flows down.)
 * rememberSaveable: the choice survives rotation; the dark switch starts with the system setting.
 */
@Composable
fun ThemingApp(onDarkThemeChange: (Boolean) -> Unit = {}) {
    val systemDark = isSystemInDarkTheme()
    var darkTheme by rememberSaveable { mutableStateOf(systemDark) }
    var dynamicColor by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(darkTheme) { onDarkThemeChange(darkTheme) }  // side effect: talk to the window

    AppTheme(darkTheme = darkTheme, dynamicColor = dynamicColor) {
        ShowcaseScreen(
            darkTheme = darkTheme,
            onDarkThemeChange = { darkTheme = it },
            dynamicColor = dynamicColor,
            onDynamicColorChange = { dynamicColor = it },
        )
    }
}

@Composable
fun ShowcaseScreen(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Theming") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard(darkTheme, onDarkThemeChange, dynamicColor, onDynamicColorChange)
            ColorRolesCard()
            TypographyCard()
            ComponentsCard()
            ExtendedColorsCard()
            NestedThemeCard(darkTheme)
        }
    }
}

// A Card takes its color (surfaceContainerHighest) and shape (shapes.medium, cut corners) from the theme.
@Composable
private fun SectionCard(title: String, note: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(note, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun SettingsCard(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    dynamicColor: Boolean,
    onDynamicColorChange: (Boolean) -> Unit,
) {
    val dynamicAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    SectionCard("Theme as state", "Two booleans at the top of the tree decide every color below.") {
        SwitchRow("Dark theme", darkTheme, onDarkThemeChange)
        SwitchRow(
            label = if (dynamicAvailable) "Dynamic color (wallpaper)" else "Dynamic color (needs Android 12)",
            checked = dynamicColor,
            onCheckedChange = onDynamicColorChange,
            enabled = dynamicAvailable
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

/*
 * The scheme as a grid of roles. Each swatch shows a role with its 'on' partner as text color:
 * that pairing is the contract - text in onX on a background in X is always readable.
 * Toggle dark mode and dynamic color and watch the same names get new values.
 */
@Composable
private fun ColorRolesCard() {
    val c = MaterialTheme.colorScheme
    val roles = listOf(
        Triple("primary", c.primary, c.onPrimary),
        Triple("primaryContainer", c.primaryContainer, c.onPrimaryContainer),
        Triple("secondary", c.secondary, c.onSecondary),
        Triple("secondaryContainer", c.secondaryContainer, c.onSecondaryContainer),
        Triple("tertiary", c.tertiary, c.onTertiary),
        Triple("tertiaryContainer", c.tertiaryContainer, c.onTertiaryContainer),
        Triple("error", c.error, c.onError),
        Triple("errorContainer", c.errorContainer, c.onErrorContainer),
        Triple("surface", c.surface, c.onSurface),
        Triple("surfaceContainerHighest", c.surfaceContainerHighest, c.onSurface),
        Triple("inverseSurface", c.inverseSurface, c.inverseOnSurface),
        Triple("outline", c.outline, c.surface),
    )
    SectionCard("Color roles", "Names, not values. Components only ever ask for these.") {
        roles.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { (name, color, onColor) ->
                    Swatch(name, color, onColor, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Swatch(name: String, color: Color, onColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(8.dp),
        contentAlignment = Alignment.BottomStart
    ) {
        Text(name, color = onColor, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun TypographyCard() {
    val t = MaterialTheme.typography
    SectionCard("Typography", "Serif headings and monospace labels come from AppTypography; the rest are M3 defaults.") {
        Text("Display Small", style = t.displaySmall)
        Text("Headline Small", style = t.headlineSmall)
        Text("Title Large", style = t.titleLarge)
        Text("Body Large – the text you read most", style = t.bodyLarge)
        Text("LABEL SMALL", style = t.labelSmall)
    }
}

/*
 * Not a single color or shape is set here. Every component asks the theme:
 * Button -> primary, FilledTonalButton -> secondaryContainer, OutlinedButton -> outline,
 * AssistChip -> shapes.small, Slider/Switch -> primary.
 */
@Composable
private fun ComponentsCard() {
    var sliderValue by remember { mutableFloatStateOf(0.4f) }
    SectionCard("Components", "No colors or shapes passed - they all read the theme.") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {}) { Text("Button") }
            FilledTonalButton(onClick = {}) { Text("Tonal") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = {}) { Text("Outlined") }
            AssistChip(onClick = {}, label = { Text("Chip") })
        }
        Slider(value = sliderValue, onValueChange = { sliderValue = it })
    }
}

// Own tokens, used exactly like the built-in roles.
@Composable
private fun ExtendedColorsCard() {
    val ext = MaterialTheme.extendedColors
    SectionCard("Own tokens", "success and warning are not in Material - see ExtendedColors in Theme.kt.") {
        Banner("✓ Saved successfully", ext.successContainer, ext.onSuccessContainer)
        Banner("⚠ Battery low", ext.warningContainer, ext.onWarningContainer)
    }
}

@Composable
private fun Banner(text: String, container: Color, content: Color) {
    Text(
        text = text,
        color = content,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(container)
            .padding(12.dp)
    )
}

/*
 * Themes nest. An inner MaterialTheme overrides only what you pass (here: the color scheme);
 * typography and shapes are inherited from the outer AppTheme. Useful for a dark promo banner
 * in a light app, a branded section, a preview of 'the other mode'.
 */
@Composable
private fun NestedThemeCard(darkTheme: Boolean) {
    SectionCard("Nested theme", "The card below uses the opposite scheme; fonts and shapes are inherited.") {
        MaterialTheme(colorScheme = if (darkTheme) AppLightColors else AppDarkColors) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (darkTheme) "Light inside dark" else "Dark inside light", style = MaterialTheme.typography.titleLarge)
                    Button(onClick = {}) { Text("Same Button, other scheme") }
                }
            }
        }
    }
}

/*
 * @PreviewLightDark renders the composable twice, with the system in light and in dark mode,
 * so isSystemInDarkTheme() differs - no need to run the app to check both.
 */
@PreviewLightDark
@Composable
private fun ShowcasePreview() {
    val dark = isSystemInDarkTheme()
    AppTheme(darkTheme = dark) {
        ShowcaseScreen(darkTheme = dark, onDarkThemeChange = {}, dynamicColor = false, onDynamicColorChange = {})
    }
}
