// (C) A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.rest

import androidx.compose.ui.res.stringResource
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.fh_aachen.android.rest.R.drawable.icon_home
import de.fh_aachen.android.rest.R.drawable.icon_table
import de.fh_aachen.android.rest.R.drawable.background_castle
import de.fh_aachen.android.rest.R.drawable.background_database
import de.fh_aachen.android.rest.model.UserActivityViewModel
import de.fh_aachen.android.rest.model.UserModel
import de.fh_aachen.android.rest.model.UserPostModel
import de.fh_aachen.android.ui_tools.ConceptDestination
import de.fh_aachen.android.ui_tools.ConceptScaffold
import de.fh_aachen.android.ui_tools.ConceptTheme
import de.fh_aachen.android.ui_tools.ScreenWithBackground
import de.fh_aachen.android.ui_tools.navigateToTopLevel
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

sealed interface Screen : NavKey {
    @Serializable data object Home : Screen
    @Serializable data object Table : Screen
}

class MainActivity : ComponentActivity() {

    // with the factory: the ViewModel gets its repository through the constructor
    val viewModel: UserActivityViewModel by viewModels { UserActivityViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConceptTheme {
                RestApp(viewModel)
            }
        }
    }
}

@Composable
fun RestApp(viewModel: UserActivityViewModel) {
    val backStack = rememberNavBackStack(Screen.Home)
    ConceptScaffold(
        destinations = listOf(
            ConceptDestination(Screen.Home, icon_home, stringResource(R.string.nav_home)),
            ConceptDestination(Screen.Table, icon_table, stringResource(R.string.nav_table)),
        ),
        selected = backStack.lastOrNull(),
        onSelect = { backStack.navigateToTopLevel(it) },
    ) { padding ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(padding),
            entryDecorators = listOf(rememberSaveableStateHolderNavEntryDecorator()),
            entryProvider = entryProvider<NavKey> {
                entry<Screen.Home> {
                    ScreenWithBackground(background_castle) {
                        LoginScreen(onLogin = { backStack.navigateToTopLevel(Screen.Table) })
                    }
                }
                entry<Screen.Table> {
                    ScreenWithBackground(background_database) { UserActivityScreen(viewModel) }
                }
            }
        )
    }
}

@Composable
fun LoginScreen(onLogin: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onLogin) {
            Text(stringResource(R.string.login), fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun UserActivityScreen(viewModel: UserActivityViewModel) {
    val users by viewModel.users.collectAsStateWithLifecycle()
    val posts by viewModel.posts.collectAsStateWithLifecycle()
    val selectedUserId by viewModel.selectedUserId.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val panelColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)

    Column(Modifier.fillMaxSize()) {
        error?.let {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.weight(1f))
                TextButton(onClick = viewModel::retry) { Text(stringResource(R.string.retry)) }
            }
        }
        Row(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f).padding(16.dp).background(panelColor)) {
                PanelHeader(stringResource(R.string.users))
                if (isLoading && users.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
                }
                LazyColumn {
                    items(users) { user ->
                        UserRow(
                            user = user,
                            isSelected = user.id == selectedUserId,
                            onClick = { viewModel.selectUser(user.id) }
                        )
                    }
                }
            }
            Column(Modifier.weight(2f).padding(16.dp).background(panelColor)) {
                PanelHeader(stringResource(R.string.posts_by_user, selectedUserId?.toString() ?: "–"))
                LazyColumn {
                    items(posts) { post -> PostRow(post = post) }
                }
            }
        }
    }
}

@Composable
private fun PanelHeader(text: String) {
    Text(
        text,
        modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer),
        color = MaterialTheme.colorScheme.onPrimaryContainer, textAlign = TextAlign.Center, fontSize = 24.sp
    )
}

@Composable
fun UserRow(user: UserModel, isSelected: Boolean, onClick: () -> Unit) {
    // a role pair for the selection; unselected rows keep the panel's background and text color
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
    val textColor = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color.Unspecified
    Row(Modifier.fillMaxWidth().padding(8.dp).background(backgroundColor)
        .clickable(onClick = onClick)
        .padding(8.dp)
    ) {
        Text("[${user.id}] ${user.name}", fontSize = 20.sp, color = textColor)
    }
}

@Composable
fun PostRow(post: UserPostModel) {
    Column(Modifier.padding(8.dp)) {
        Text("[${post.id}] ${post.title}",
            fontSize = 16.sp)
    }
}
