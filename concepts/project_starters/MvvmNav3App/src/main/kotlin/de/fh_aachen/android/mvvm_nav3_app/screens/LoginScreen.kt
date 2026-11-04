// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_nav3_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.mvvm_nav3_app.R
import de.fh_aachen.android.mvvm_nav3_app.viewmodels.LoginViewModel

// Stateful: connects LoginViewModel and LoginScreen; navigation comes in as a lambda.
@Composable
fun LoginRoute(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val loggedInUser by viewModel.loggedInUser.collectAsStateWithLifecycle()
    LoginScreen(
        loggedInUser = loggedInUser,
        userName = viewModel.userName,
        canLogin = viewModel.canLogin,
        onUserNameChange = viewModel::onUserNameChange,
        onLogin = {
            viewModel.login()
            onLoggedIn()
        },
        onLogout = viewModel::logout,
    )
}

// Stateless: the login form, or who is logged in and a logout button.
@Composable
fun LoginScreen(
    loggedInUser: String?,
    userName: String,
    canLogin: Boolean,
    onUserNameChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (loggedInUser != null) {
            Text(stringResource(R.string.logged_in_as, loggedInUser), style = MaterialTheme.typography.titleMedium)
            OutlinedButton(onClick = onLogout) {
                Text(stringResource(R.string.logout))
            }
        } else {
            OutlinedTextField(
                value = userName,
                onValueChange = onUserNameChange,
                label = { Text(stringResource(R.string.user_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (canLogin) onLogin() }),
            )
            Button(onClick = onLogin, enabled = canLogin) {
                Text(stringResource(R.string.login))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    StarterTheme {
        LoginScreen(loggedInUser = null, userName = "Ada", canLogin = true, onUserNameChange = {}, onLogin = {}, onLogout = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun LoggedInScreenPreview() {
    StarterTheme {
        LoginScreen(loggedInUser = "Ada", userName = "", canLogin = false, onUserNameChange = {}, onLogin = {}, onLogout = {})
    }
}
