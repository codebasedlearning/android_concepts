// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room.model

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

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

    // This is temporarily for demonstration purpose.
    suspend fun resetLocalDatabase() {
        // clearAllTables is a blocking call (not suspend), so it needs a background thread,
        // and it must not run inside a transaction, so it comes first ...
        withContext(Dispatchers.IO) { shopDatabase.clearAllTables() }

        // ... and all inserts form one transaction: observers (our Flows) see the new data
        // once, complete, instead of every intermediate step.
        shopDatabase.withTransaction {
            // Create categories and products and add them in groups.

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
}

class ShopViewModel : ViewModel() {
    // feel free to use a service locator or DI framework
    private val repository = RoomApplication.instance.dataRepository

    // First the Flows from the repo. They look similar, but their purpose and data flow
    // are not the same.

    /*
     * categories + .stateIn is a cold to hot conversion. Room automatically re-emits
     * whenever the category table changes. It has an initial value and stops collecting
     * when no one observes. You don’t manually update it.
     */
    val categories: StateFlow<List<CategoryEntity>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /*
     * The products depend on the selected category. Instead of starting a new collect for
     * every selection (the old collectors would keep running and overwrite the list whenever
     * the product table changes), we keep the selection as state and let flatMapLatest
     * switch to the Room Flow of the new category - cancelling the previous one.
     */
    private val selectedCategoryId = MutableStateFlow<UUID?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductEntity>> = selectedCategoryId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList()) else repository.getAllProductsFromCategory(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Now the operations on the database. Room's suspend DAO functions and Flows are main-safe,
    // they switch to their own executor - no Dispatchers.IO needed (only for blocking calls,
    // see clearAllTables).

    fun selectCategory(categoryId: UUID) {
        selectedCategoryId.value = categoryId
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

    fun addProduct(name: String, categoryId: UUID) = launchDb("addProduct") {
        repository.addProduct(ProductEntity(name = name, categoryId = categoryId))
    }

    fun updateProductLabel(productId: UUID, newLabel: String) = launchDb("updateProductLabel") {
        repository.updateProductLabel(productId, newLabel)
    }

    fun resetData() = launchDb("resetData") {
        repository.resetLocalDatabase()
    }

    fun syncWithExternalData() = launchDb("syncWithExternalData") {
        repository.syncWithExternalDatabase()
    }
}
