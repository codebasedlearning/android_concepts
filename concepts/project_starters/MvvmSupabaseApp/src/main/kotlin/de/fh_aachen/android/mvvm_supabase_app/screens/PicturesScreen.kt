// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.mvvm_supabase_app.R
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.PictureItem
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.PicturesViewModel

// Stateful: connects PicturesViewModel and PicturesScreen.
@Composable
fun PicturesRoute(viewModel: PicturesViewModel = viewModel(factory = PicturesViewModel.Factory)) {
    LoadContent(viewModel.state, onRetry = viewModel::reload) { pictures ->
        PicturesScreen(pictures)
    }
}

// Stateless: a grid of all pictures with title and product.
@Composable
fun PicturesScreen(pictures: List<PictureItem>) {
    if (pictures.isEmpty()) {
        Centered { Text(stringResource(R.string.no_pictures)) }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(pictures, key = { it.picture.id }) { item ->
            ElevatedCard {
                StorageImage(
                    url = item.url,
                    objectPath = item.picture.objectPath,
                    contentDescription = item.picture.title ?: item.picture.fileName,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        item.picture.title ?: item.picture.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    item.picture.productName?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}
