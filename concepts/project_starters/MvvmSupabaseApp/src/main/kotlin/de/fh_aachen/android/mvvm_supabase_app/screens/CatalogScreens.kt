// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import de.fh_aachen.android.mvvm_supabase_app.R
import de.fh_aachen.android.mvvm_supabase_app.data.CategoryStats
import de.fh_aachen.android.mvvm_supabase_app.data.ProductDetails
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.CategoriesViewModel
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.ProductViewModel
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.ProductWithPicture
import de.fh_aachen.android.mvvm_supabase_app.viewmodels.ProductsViewModel

// ---- Master: categories

// Stateful: connects CategoriesViewModel and CategoriesScreen.
@Composable
fun CategoriesRoute(
    onOpenCategory: (CategoryStats) -> Unit,
    viewModel: CategoriesViewModel = viewModel(factory = CategoriesViewModel.Factory),
) {
    LoadContent(viewModel.state, onRetry = viewModel::reload) { categories ->
        CategoriesScreen(categories, onOpenCategory)
    }
}

// Stateless: one card per category with product count and price range.
@Composable
fun CategoriesScreen(categories: List<CategoryStats>, onOpenCategory: (CategoryStats) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories, key = { it.id }) { category ->
            ElevatedCard(onClick = { onOpenCategory(category) }, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(category.name, style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.category_products, category.products), style = MaterialTheme.typography.bodyMedium)
                    if (category.minPrice != null && category.maxPrice != null) {
                        Text(stringResource(R.string.price_range, category.minPrice, category.maxPrice), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

// ---- Detail 1: products of a category

// Stateful: connects ProductsViewModel and ProductsScreen.
@Composable
fun ProductsRoute(
    categoryId: Int,
    onOpenProduct: (ProductDetails) -> Unit,
    viewModel: ProductsViewModel = viewModel(factory = ProductsViewModel.factory(categoryId)),
) {
    LoadContent(viewModel.state, onRetry = viewModel::reload) { products ->
        ProductsScreen(products, onOpenProduct)
    }
}

// Stateless: one card per product with price per unit and number of pictures.
@Composable
fun ProductsScreen(products: List<ProductDetails>, onOpenProduct: (ProductDetails) -> Unit) {
    if (products.isEmpty()) {
        Centered { Text(stringResource(R.string.no_products)) }
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ElevatedCard(onClick = { onOpenProduct(product) }, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(product.name, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(R.string.price_per_unit, product.price, product.unit), style = MaterialTheme.typography.bodyMedium)
                    }
                    if (product.pictureCount > 0) {
                        Text(stringResource(R.string.picture_count, product.pictureCount), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ---- Detail 2: one product

// Stateful: connects ProductViewModel and ProductScreen.
@Composable
fun ProductRoute(
    productId: Int,
    viewModel: ProductViewModel = viewModel(factory = ProductViewModel.factory(productId)),
) {
    LoadContent(viewModel.state, onRetry = viewModel::reload) { item ->
        ProductScreen(item)
    }
}

// Stateless: primary picture (if any) and all fields of the product.
@Composable
fun ProductScreen(item: ProductWithPicture) {
    val product = item.product
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (item.pictureUrl != null && product.primaryPicturePath != null) {
            StorageImage(
                url = item.pictureUrl,
                objectPath = product.primaryPicturePath,
                contentDescription = product.name,
                modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(16.dp)),
            )
        }
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(product.name, style = MaterialTheme.typography.headlineSmall)
                HorizontalDivider()
                DetailRow(stringResource(R.string.label_category), product.category ?: stringResource(R.string.no_category))
                DetailRow(stringResource(R.string.label_unit), product.unit)
                DetailRow(stringResource(R.string.label_price), euro(product.price))
                DetailRow(stringResource(R.string.label_net), euro(product.priceNet))
                DetailRow(stringResource(R.string.label_vat), "${percent(product.vatPercent)} · ${euro(product.vatAmount)}")
                DetailRow(stringResource(R.string.label_pictures), product.pictureCount.toString())
            }
        }
    }
}

// One "label ... value" line.
@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun euro(value: Double) = "%.2f €".format(value)

private fun percent(value: Double) =
    if (value % 1.0 == 0.0) "${value.toInt()} %" else "$value %"

@Preview(showBackground = true)
@Composable
private fun ProductScreenPreview() {
    SupabaseTheme {
        ProductScreen(
            ProductWithPicture(
                ProductDetails(
                    id = 1, name = "Pretzel", unit = "PC", price = 0.89, vatPercent = 7.0,
                    priceNet = 0.83, vatAmount = 0.06, category = "Baked Goods",
                ),
                pictureUrl = null,
            )
        )
    }
}
