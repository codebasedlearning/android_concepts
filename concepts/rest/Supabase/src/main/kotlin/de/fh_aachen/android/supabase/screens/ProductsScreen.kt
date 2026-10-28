// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.supabase.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.supabase.BuildConfig
import de.fh_aachen.android.supabase.R
import de.fh_aachen.android.supabase.model.Category
import de.fh_aachen.android.supabase.model.Product
import de.fh_aachen.android.supabase.model.ProductsUiState
import de.fh_aachen.android.supabase.model.ProductsViewModel
import de.fh_aachen.android.supabase.model.Session
import de.fh_aachen.android.ui_tools.ConceptTheme

/*
 *  New: a real screen for what the course version did in Logcat (sign in, read the
 *  products). Stateful route + stateless screen, as in the other concept apps.
 */
@Composable
fun ProductsRoute(viewModel: ProductsViewModel = viewModel(factory = ProductsViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductsScreen(
        state = state,
        onSignIn = viewModel::signIn,
        onSignOut = viewModel::signOut,
        onReload = viewModel::loadProducts,
    )
}

@Composable
fun ProductsScreen(
    state: ProductsUiState,
    onSignIn: (email: String, password: String) -> Unit,
    onSignOut: () -> Unit,
    onReload: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopCenter) {
        Surface(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (val session = state.session) {
                    Session.Initializing -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    Session.SignedOut -> SignInForm(isLoading = state.isLoading, onSignIn = onSignIn)
                    is Session.SignedIn -> ProductList(session, state, onSignOut, onReload)
                }
                state.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun SignInForm(isLoading: Boolean, onSignIn: (String, String) -> Unit) {
    // Pre-filled from local.properties for convenience in this demo (see build.gradle.kts).
    var email by rememberSaveable { mutableStateOf(BuildConfig.SUPABASE_USER_EMAIL) }
    var password by rememberSaveable { mutableStateOf(BuildConfig.SUPABASE_USER_PASSWORD) }

    Text(stringResource(R.string.sign_in_title), style = MaterialTheme.typography.titleLarge)
    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text(stringResource(R.string.email)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text(stringResource(R.string.password)) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )
    Button(onClick = { onSignIn(email, password) }, enabled = !isLoading && email.isNotBlank() && password.isNotEmpty()) {
        Text(stringResource(R.string.sign_in))
    }
}

// an extension on ColumnScope: it is placed inside the card's Column and may use Modifier.align
@Composable
private fun ColumnScope.ProductList(session: Session.SignedIn, state: ProductsUiState, onSignOut: () -> Unit, onReload: () -> Unit) {
    Text(stringResource(R.string.signed_in_as, session.email ?: "?"), style = MaterialTheme.typography.titleMedium)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onReload, enabled = !state.isLoading) { Text(stringResource(R.string.reload)) }
        OutlinedButton(onClick = onSignOut, enabled = !state.isLoading) { Text(stringResource(R.string.sign_out)) }
    }
    if (state.isLoading) {
        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    } else if (state.products.isEmpty()) {
        Text(stringResource(R.string.no_products))
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(state.products, key = { it.id }) { product ->
            Text(
                stringResource(R.string.product_line, product.name, product.category?.name ?: "–", product.price, product.unit),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview
@Composable
private fun ProductsScreenPreview() {
    ConceptTheme {
        ProductsScreen(
            state = ProductsUiState(
                session = Session.SignedIn("demo@example.com"),
                products = listOf(Product(1, "Spinach", Category(1, "Frozen Goods"), "PK", 1.99, 0.07)),
            ),
            onSignIn = { _, _ -> }, onSignOut = {}, onReload = {},
        )
    }
}
