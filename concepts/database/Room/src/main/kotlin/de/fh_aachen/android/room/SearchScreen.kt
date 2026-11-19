// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.room.database.ProductSearchResult
import de.fh_aachen.android.room.models.SearchViewModel

// Stateful: connects SearchViewModel and SearchContent.
@Composable
fun SearchScreen(viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory)) {
    val results by viewModel.results.collectAsStateWithLifecycle()
    SearchContent(
        query = viewModel.query,
        results = results,
        onQueryChange = viewModel::onQueryChange,
    )
}

// Stateless: the search field ("variant 2": an OutlinedTextField dressed as a search field) and the hits.
@Composable
fun SearchContent(
    query: String,
    results: List<ProductSearchResult>?,
    onQueryChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val rowColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.9f)

    Column(modifier = Modifier.fillMaxSize().padding(8.dp).imePadding()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = { Icon(painterResource(R.drawable.icon_search), contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(painterResource(R.drawable.icon_clear), contentDescription = stringResource(R.string.search_clear))
                    }
                }
            },
            singleLine = true,
            // the keyboard shows a magnifier instead of 'Enter'; the list is already up to date,
            // so 'search' only closes the keyboard to show more of it
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = rowColor,
                unfocusedContainerColor = rowColor,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        when {
            results == null -> {}                       // first query still running: show nothing
            results.isEmpty() -> Text(
                stringResource(R.string.search_nothing_found, query.trim()),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp).background(rowColor).padding(12.dp),
            )
            else -> {
                Text(
                    stringResource(R.string.search_count, results.size),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp).background(rowColor).padding(horizontal = 12.dp, vertical = 4.dp),
                )
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(results, key = { it.id }) { product ->
                        Column(modifier = Modifier.fillMaxWidth().background(rowColor).padding(12.dp)) {
                            Text(highlight(product.name, query.trim()), style = MaterialTheme.typography.titleMedium)
                            Text(product.categoryName, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

// "Spinach Pizza" with "pizza" -> "Spinach **Pizza**"; same rule as SQLite's LIKE (ignore case).
private fun highlight(text: String, part: String): AnnotatedString = buildAnnotatedString {
    val start = if (part.isEmpty()) -1 else text.indexOf(part, ignoreCase = true)
    if (start < 0) {
        append(text)
        return@buildAnnotatedString
    }
    append(text.substring(0, start))
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(text.substring(start, start + part.length)) }
    append(text.substring(start + part.length))
}
