// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.mvvm_nav3_app.R
import de.fh_aachen.android.mvvm_nav3_app.data.UserPreferences
import de.fh_aachen.android.mvvm_nav3_app.viewmodels.HomeViewModel

// Stateful: connects HomeViewModel and HomeScreen.
@Composable
fun HomeRoute(viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val preferences by viewModel.preferences.collectAsStateWithLifecycle()
    HomeScreen(userName = userName, preferences = preferences)
}

// The "do-whatever" screen: shows the stored preferences. Put your own stuff here.
@Composable
fun HomeScreen(
    userName: String?,
    preferences: UserPreferences?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (userName != null) {
            Text(stringResource(R.string.hello, userName), style = MaterialTheme.typography.headlineMedium)
        }

        if (preferences == null) {
            CircularProgressIndicator()
        } else {
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.stored_text, preferences.text))
                    Text(stringResource(R.string.stored_number, preferences.number))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    StarterTheme {
        HomeScreen(userName = "Ada", preferences = UserPreferences(text = "Hello", number = 42))
    }
}
