// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import de.fh_aachen.android.mvvm_supabase_simple_app.R
import de.fh_aachen.android.mvvm_supabase_simple_app.viewmodels.AppViewModel
import kotlinx.serialization.Serializable

// The destinations. Serializable, so the back stack survives process death.
@Serializable data object Login : NavKey
@Serializable data object Cars : NavKey

// One tab of the bottom bar; all but Login need a logged-in user.
private data class TopLevelDestination(
    val key: NavKey,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val needsLogin: Boolean = true,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Login, R.string.title_login, R.drawable.icon_person, needsLogin = false),
    TopLevelDestination(Cars, R.string.title_cars, R.drawable.icon_car),
)

// A tab replaces the whole back stack (no drill-down here, so the stack has always one entry).
private fun NavBackStack<NavKey>.resetTo(key: NavKey) {
    if (size == 1 && last() == key) return
    clear()
    add(key)
}

// Owns the back stack, the bars, and the mapping from keys to screens.
// AppViewModel is created outside NavDisplay, so it is scoped to the Activity.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(viewModel: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val backStack = rememberNavBackStack(Login)
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()
    val current = backStack.lastOrNull()

    // Logged out (or the session is gone): back to the login screen. null = not known yet.
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn == false) backStack.resetTo(Login)
    }

    AppBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface,
            topBar = {
                TopAppBar(
                    title = { Text(titleOf(current)) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)) {
                    topLevelDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = current == dest.key,
                            onClick = { backStack.resetTo(dest.key) },
                            enabled = isLoggedIn == true || !dest.needsLogin,  // disabled = greyed out
                            icon = { Icon(painterResource(dest.icon), contentDescription = null) },
                            label = { Text(stringResource(dest.label)) },
                        )
                    }
                }
            },
        ) { padding ->
            NavDisplay(
                // consumeWindowInsets: the car list can use imePadding without counting the bars twice
                modifier = Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize(),
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),     // rememberSaveable state per entry
                    rememberViewModelStoreNavEntryDecorator(),          // ViewModels per entry, cleared on pop
                ),
                entryProvider = entryProvider<NavKey> {
                    entry<Login> {
                        LoginRoute(onLoggedIn = { backStack.resetTo(Cars) })
                    }
                    entry<Cars> {
                        CarsRoute()
                    }
                },
            )
        }
    }
}

@Composable
private fun titleOf(key: NavKey?): String =
    stringResource(topLevelDestinations.firstOrNull { it.key == key }?.label ?: R.string.app_name)
