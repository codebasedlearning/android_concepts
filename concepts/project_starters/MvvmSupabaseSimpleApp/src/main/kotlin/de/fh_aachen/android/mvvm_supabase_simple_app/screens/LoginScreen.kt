// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_simple_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.mvvm_supabase_simple_app.R
import de.fh_aachen.android.mvvm_supabase_simple_app.data.Session
import de.fh_aachen.android.mvvm_supabase_simple_app.viewmodels.LoginViewModel

// Stateful: connects LoginViewModel and LoginScreen; navigation comes in as a lambda.
@Composable
fun LoginRoute(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = LoginViewModel.Factory),
) {
    val session by viewModel.session.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel.hasLoggedIn) {
        if (viewModel.hasLoggedIn) onLoggedIn()
    }
    LoginScreen(
        session = session,
        email = viewModel.email,
        password = viewModel.password,
        isBusy = viewModel.isBusy,
        error = viewModel.error,
        canLogin = viewModel.canLogin,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLogin = viewModel::login,
        onLogout = viewModel::logout,
    )
}

// Stateless: the login form, or who is logged in and a logout button.
@Composable
fun LoginScreen(
    session: Session,
    email: String,
    password: String,
    isBusy: Boolean,
    error: String?,
    canLogin: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (session) {
            Session.Loading -> CircularProgressIndicator()
            is Session.LoggedIn -> {
                Text(stringResource(R.string.logged_in_as, session.email), style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = onLogout, enabled = !isBusy) {
                    Text(stringResource(R.string.logout))
                }
            }
            Session.LoggedOut -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = { Text(stringResource(R.string.email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.password)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (canLogin) onLogin() }),
                )
                Button(onClick = onLogin, enabled = canLogin) {
                    if (isBusy) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.login))
                    }
                }
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    SupabaseTheme {
        LoginScreen(
            session = Session.LoggedOut, email = "ada@example.com", password = "secret", isBusy = false,
            error = null, canLogin = true, onEmailChange = {}, onPasswordChange = {}, onLogin = {}, onLogout = {},
        )
    }
}
