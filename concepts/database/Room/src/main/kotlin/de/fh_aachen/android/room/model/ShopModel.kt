// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room.model

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.withTransaction
import de.fh_aachen.android.room.RoomApplication
import de.fh_aachen.android.room.database.CategoryDao
import de.fh_aachen.android.room.database.CategoryEntity
import de.fh_aachen.android.room.database.ProductDao
import de.fh_aachen.android.room.database.ProductEntity
import de.fh_aachen.android.room.database.ShopDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

const val TAG = "ROOM"

class ShopRepository(private val shopDatabase: ShopDatabase) {
    // This is the connection to the Daos with all operations.
    private val categoryDao: CategoryDao = shopDatabase.categoryDao()
    private val productDao: ProductDao = shopDatabase.productDao()

    // Operations you want to expose. Remember the 'suspend' - Flow - comment.

    fun getAllCategories() = categoryDao.getAllCategories()

    fun getAllProductsFromCategory(categoryId: UUID) = productDao.getItemsForCategory(categoryId)

    // A good example for similar but not identical views or operations. From a database
    // point of view this is a CRUD-op but from UI or from the user it adds a product.
    suspend fun addProduct(product: ProductEntity) = productDao.insert(product)

    suspend fun updateProductLabel(productId: UUID, newLabel: String) = productDao.updateItemLabel(productId, newLabel)

    suspend fun syncWithExternalDatabase() { /* do what ever you have to */ }

    /*
     * An empty database (first start) gets the sample data, so the screen is not
     *  empty until somebody finds the 'Reset' button. Alternatives: a RoomDatabase.Callback
     *  (onCreate) on the builder, or a prepackaged database file (createFromAsset).
     *  The count and the inserts form one transaction, so two callers cannot both seed.
     */
    suspend fun seedIfEmpty() {
        shopDatabase.withTransaction {
            if (categoryDao.count() == 0) insertSampleData()
        }
    }

    // This is temporarily for demonstration purpose.
    suspend fun resetLocalDatabase() {
        // clearAllTables is a blocking call (not suspend), so it needs a background thread,
        // and it must not run inside a transaction, so it comes first ...
        withContext(Dispatchers.IO) { shopDatabase.clearAllTables() }

        // ... and all inserts form one transaction: observers (our Flows) see the new data
        // once, complete, instead of every intermediate step.
        shopDatabase.withTransaction { insertSampleData() }
    }

    // Create categories and products and add them in groups (call inside a transaction).
    private suspend fun insertSampleData() {
        val catFrozen = CategoryEntity(name = "Frozen Goods")
        categoryDao.insert(catFrozen)
        productDao.insertAll(listOf(
            ProductEntity(name = "Cheese Pizza", categoryId = catFrozen.id),
            ProductEntity(name = "Spinach Pizza", categoryId = catFrozen.id),
        ))

        val catFruits = CategoryEntity(name = "Fruits, Vegetables")
        categoryDao.insert(catFruits)
        productDao.insertAll(listOf(
            ProductEntity(name = "Bananas", categoryId = catFruits.id),
            ProductEntity(name = "Carrots", categoryId = catFruits.id),
            ProductEntity(name = "Onions", categoryId = catFruits.id),
        ))

        val catDrinks = CategoryEntity(name = "Drinks")
        categoryDao.insert(catDrinks)
        productDao.insertAll(listOf(
            ProductEntity(name = "Water", categoryId = catDrinks.id),
            ProductEntity(name = "Coke", categoryId = catDrinks.id),
            ProductEntity(name = "Beer", categoryId = catDrinks.id),
            ProductEntity(name = "Wine", categoryId = catDrinks.id),
        ))
    }
}

/*
 * Everything the screen shows, in one immutable object (cf. models/RawSensorMVI).
 *  Before, the selection lived in two places: 'selectedCategoryId' here and 'catId', 'productId',
 *  'productName' plus the combo boxes' own text in the composable. After 'Reset' (new categories
 *  with new random ids) the composable still held the old id: '+' then violated the foreign key,
 *  the error was only logged, and the combo box still showed the old name.
 */
data class ShopUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val products: List<ProductEntity> = emptyList(),
    val selectedCategory: CategoryEntity? = null,
    val selectedProduct: ProductEntity? = null,
)

class ShopViewModel(
    // Constructor injection instead of RoomApplication.instance, see Factory below
    private val repository: ShopRepository
) : ViewModel() {

    // Only the IDs of the selection are state here; the selected objects are derived below.
    private val selectedCategoryId = MutableStateFlow<UUID?>(null)
    private val selectedProductId = MutableStateFlow<UUID?>(null)

    /*
     * The products depend on the selected category. Instead of starting a new collect for
     * every selection (the old collectors would keep running and overwrite the list whenever
     * the product table changes), we keep the selection as state and let flatMapLatest
     * switch to the Room Flow of the new category - cancelling the previous one.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val products = selectedCategoryId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getAllProductsFromCategory(id)
    }

    /*
     * One state, derived from the database and the selection. The key detail: the
     * selected objects are looked up in the current lists. If a selected row disappears (Reset,
     * delete), the lookup gives null - a stale selection cannot survive, by construction.
     *
     * Room automatically re-emits whenever a table changes; stateIn turns the cold flows into
     * one hot StateFlow that stops collecting when no one observes (WhileSubscribed).
     */
    val uiState: StateFlow<ShopUiState> = combine(
        repository.getAllCategories(), products, selectedCategoryId, selectedProductId
    ) { categories, products, categoryId, productId ->
        ShopUiState(
            categories = categories,
            products = products,
            selectedCategory = categories.find { it.id == categoryId },
            selectedProduct = products.find { it.id == productId },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ShopUiState())

    init {
        launchDb("seedIfEmpty") { repository.seedIfEmpty() }
    }

    // Now the operations on the database. Room's suspend DAO functions and Flows are main-safe,
    // they switch to their own executor - no Dispatchers.IO needed (only for blocking calls,
    // see clearAllTables).

    fun selectCategory(categoryId: UUID) {
        selectedCategoryId.value = categoryId
        selectedProductId.value = null          // a product of another category makes no sense
    }

    fun selectProduct(productId: UUID) {
        selectedProductId.value = productId
    }

    // An exception in viewModelScope crashes the app (e.g. a foreign key violation),
    // so database errors are caught and logged here.
    private fun launchDb(what: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: SQLiteException) {
                Log.e(TAG, "$what failed", e)
            }
        }
    }

    // The ViewModel decides which category and creates the name (was in the composable),
    // and the new product becomes the selected one.
    fun addProduct() {
        val category = uiState.value.selectedCategory ?: return     // only with a selected category
        val product = ProductEntity(
            name = "New Product no ${Random.nextInt(from = 1000, until = 9999)}",
            categoryId = category.id
        )
        launchDb("addProduct") {
            repository.addProduct(product)
            selectedProductId.value = product.id
        }
    }

    fun renameSelectedProduct(newLabel: String) {
        val product = uiState.value.selectedProduct ?: return
        launchDb("renameSelectedProduct") { repository.updateProductLabel(product.id, newLabel) }
    }

    fun resetData() {
        // The old rows (and their ids) are gone after a reset, so is the selection
        selectedCategoryId.value = null
        selectedProductId.value = null
        launchDb("resetData") { repository.resetLocalDatabase() }
    }

    fun syncWithExternalData() = launchDb("syncWithExternalData") {
        repository.syncWithExternalDatabase()
    }

    // Same factory pattern as in the models apps; this[APPLICATION_KEY] returns Application?
    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RoomApplication
                ShopViewModel(app.dataRepository)
            }
        }
    }
}
