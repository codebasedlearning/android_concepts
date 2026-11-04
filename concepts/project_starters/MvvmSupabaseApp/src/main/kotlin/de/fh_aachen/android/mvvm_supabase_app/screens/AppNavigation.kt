// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.screens

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import de.fh_aachen.android.mvvm_supabase_app.R
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.AppViewModel
import kotlinx.serialization.Serializable

// The destinations. Serializable, so the back stack survives process death.
@Serializable data object Login : NavKey
@Serializable data object Catalog : NavKey
@Serializable data class Category(val categoryId: Int, val name: String) : NavKey     // drill-down
@Serializable data class Product(val productId: Int, val name: String) : NavKey       // drill-down
@Serializable data object Pictures : NavKey
@Serializable data object Chat : NavKey

// One tab of the bottom bar; all but Login need a logged-in user.
private data class TopLevelDestination(
    val key: NavKey,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val needsLogin: Boolean = true,
)

private val topLevelDestinations = listOf(
    TopLevelDestination(Login, R.string.title_login, R.drawable.icon_person, needsLogin = false),
    TopLevelDestination(Catalog, R.string.title_catalog, R.drawable.icon_list),
    TopLevelDestination(Pictures, R.string.title_pictures, R.drawable.icon_image),
    TopLevelDestination(Chat, R.string.title_chat, R.drawable.icon_chat),
)

private val topLevelKeys = topLevelDestinations.map { it.key }.toSet()

// A tab replaces the whole back stack (also any drill-down of the catalog).
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
    val selectedTab = backStack.findLast { it in topLevelKeys }      // a drill-down keeps its tab selected

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
                    navigationIcon = {
                        if (backStack.size > 1) {
                            IconButton(onClick = { backStack.removeLastOrNull() }) {
                                Icon(painterResource(R.drawable.icon_arrow_back), contentDescription = stringResource(R.string.back))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)) {
                    topLevelDestinations.forEach { dest ->
                        NavigationBarItem(
                            selected = selectedTab == dest.key,
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
                // consumeWindowInsets: screens (chat) can use imePadding without counting the bars twice
                modifier = Modifier.padding(padding).consumeWindowInsets(padding).fillMaxSize(),
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),     // rememberSaveable state per entry
                    rememberViewModelStoreNavEntryDecorator(),          // ViewModels per entry, cleared on pop
                ),
                entryProvider = entryProvider<NavKey> {
                    entry<Login> {
                        LoginRoute(onLoggedIn = { backStack.resetTo(Catalog) })
                    }
                    entry<Catalog> {
                        CategoriesRoute(onOpenCategory = { category -> backStack.add(Category(category.id, category.name)) })
                    }
                    entry<Category> { key ->
                        ProductsRoute(
                            categoryId = key.categoryId,
                            onOpenProduct = { product -> backStack.add(Product(product.id, product.name)) },
                        )
                    }
                    entry<Product> { key ->
                        ProductRoute(productId = key.productId)
                    }
                    entry<Pictures> {
                        PicturesRoute()
                    }
                    entry<Chat> {
                        ChatRoute()
                    }
                },
            )
        }
    }
}

@Composable
private fun titleOf(key: NavKey?): String = when (key) {
    is Category -> key.name
    is Product -> key.name
    else -> stringResource(topLevelDestinations.firstOrNull { it.key == key }?.label ?: R.string.app_name)
}
