// (C) A.Voß, a.voss@fh-aachen.de, apps@codebasedlearning.dev

package de.fh_aachen.android.rest

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import de.fh_aachen.android.rest.ui.theme.FirstAppTheme
import de.fh_aachen.android.ui_tools.LocalNavController
import de.fh_aachen.android.ui_tools.NavScaffold
import de.fh_aachen.android.ui_tools.NavScreen
import de.fh_aachen.android.ui_tools.navScreensOf

enum class Screen { Home, Table }

class MainActivity : ComponentActivity() {

    val viewModel: UserActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FirstAppTheme {
                NavScaffold(
                    navScreensOf(
                        Screen.Home to NavScreen(icon_home, background_castle) { LoginScreen() },
                        Screen.Table to NavScreen(icon_table, background_database) { UserActivityScreen(viewModel) },
                    )
                )
            }
        }
    }
}

@Composable
fun LoginScreen() {
    val navController = LocalNavController.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = { navController.navigate(Screen.Table.name) }) {
            Text("Rest - Login", fontSize = 24.sp, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun UserActivityScreen(viewModel: UserActivityViewModel) {
    val users by viewModel.users.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val selectedUserId by viewModel.selectedUserId.collectAsState()

    Row(Modifier.fillMaxSize()) {
        Column(Modifier.weight(1f).padding(16.dp).background(Color(0x80ff0000))) {
            Text("Users",
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth().background(Color(0x80000000)),
                color = Color.White, textAlign = TextAlign.Center, fontSize = 24.sp)
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

        Column(Modifier.weight(2f).padding(16.dp).background(Color(0xa000ffff))) {
            Text("Posts by User ${selectedUserId ?: "-"}",
                modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth().background(Color(0x80000000)),
                color = Color.White, textAlign = TextAlign.Center, fontSize = 24.sp
            )
            LazyColumn {
                items(posts) { post -> PostRow(post = post) }
            }
        }
    }
}

@Composable
fun UserRow(user: UserModel, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) Color.LightGray else Color.Transparent
    Row(Modifier.fillMaxWidth().padding(8.dp).background(backgroundColor)
        .clickable(onClick = onClick)
        .padding(8.dp)
    ) {
        Text("[${user.id}] ${user.name}", fontSize = 20.sp, color = Color.White)
    }
}

@Composable
fun PostRow(post: UserPostModel) {
    Column(Modifier.padding(8.dp)) {
        Text("[${post.id}] ${post.title}",
            fontSize = 16.sp, color = Color.Black)  // , style = MaterialTheme.typography.subtitle1
    }
}
