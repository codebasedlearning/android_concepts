// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.mvvm_supabase_app.viewmodels

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.fh_aachen.android.mvvm_supabase_app.data.CatalogRepository
import de.fh_aachen.android.mvvm_supabase_app.data.CategoryStats
import de.fh_aachen.android.mvvm_supabase_app.data.PictureRepository
import de.fh_aachen.android.mvvm_supabase_app.data.ProductDetails

// Master: all categories with product counts and price range.
class CategoriesViewModel(private val catalog: CatalogRepository) : LoadViewModel<List<CategoryStats>>() {

    init { reload() }

    override suspend fun load() = catalog.categories()

    companion object {
        val Factory = viewModelFactory {
            initializer { CategoriesViewModel(container().catalogRepository) }
        }
    }
}

// Detail level 1: the products of one category.
class ProductsViewModel(
    private val catalog: CatalogRepository,
    private val categoryId: Int,
) : LoadViewModel<List<ProductDetails>>() {

    init { reload() }

    override suspend fun load() = catalog.products(categoryId)

    companion object {
        fun factory(categoryId: Int) = viewModelFactory {
            initializer { ProductsViewModel(container().catalogRepository, categoryId) }
        }
    }
}

// A product together with the URL of its primary picture (if it has one).
data class ProductWithPicture(val product: ProductDetails, val pictureUrl: String?)

// Detail level 2: one product.
class ProductViewModel(
    private val catalog: CatalogRepository,
    private val pictures: PictureRepository,
    private val productId: Int,
) : LoadViewModel<ProductWithPicture>() {

    init { reload() }

    override suspend fun load(): ProductWithPicture {
        val product = catalog.product(productId)
        val url = product.primaryPicturePath?.let { pictures.signedUrl(it) }
        return ProductWithPicture(product, url)
    }

    companion object {
        fun factory(productId: Int) = viewModelFactory {
            initializer {
                val container = container()
                ProductViewModel(container.catalogRepository, container.pictureRepository, productId)
            }
        }
    }
}
