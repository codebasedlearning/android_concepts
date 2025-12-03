// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.fh_aachen.android.room.RoomApplication
import de.fh_aachen.android.room.database.CategoryDao
import de.fh_aachen.android.room.database.CategoryEntity
import de.fh_aachen.android.room.database.ProductDao
import de.fh_aachen.android.room.database.ProductEntity
import de.fh_aachen.android.room.database.ShopDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

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

    suspend fun updateProductLabel(categoryId: UUID, newLabel: String) = productDao.updateItemLabel(categoryId, newLabel)

    suspend fun syncWithExternalDatabase() { /* do what ever you have to */ }

    // This is temporarily for demonstration purpose.
    suspend fun resetLocalDatabase() {
        shopDatabase.clearAllTables()

        // Create categories and products and add them in groups manually.

        val catFrozen = CategoryEntity(name = "Frozen Goods")
        val itemPizza1 = ProductEntity(name = "Cheese Pizza", categoryId = catFrozen.id)
        val itemPizza2 = ProductEntity(name = "Spinach Pizza", categoryId = catFrozen.id)
        categoryDao.insert(catFrozen)
        productDao.insert(itemPizza1)
        productDao.insert(itemPizza2)

        val catFruits = CategoryEntity(name = "Fruits, Vegetables")
        val itemFruit1 = ProductEntity(name = "Bananas", categoryId = catFruits.id)
        val itemFruit2 = ProductEntity(name = "Carrots", categoryId = catFruits.id)
        val itemFruit3 = ProductEntity(name = "Onions", categoryId = catFruits.id)
        categoryDao.insert(catFruits)
        productDao.insert(itemFruit1)
        productDao.insert(itemFruit2)
        productDao.insert(itemFruit3)

        val catDrinks = CategoryEntity(name = "Drinks")
        val itemDrink1 = ProductEntity(name = "Water", categoryId = catDrinks.id)
        val itemDrink2 = ProductEntity(name = "Coke", categoryId = catDrinks.id)
        val itemDrink3 = ProductEntity(name = "Beer", categoryId = catDrinks.id)
        val itemDrink4 = ProductEntity(name = "Wine", categoryId = catDrinks.id)
        categoryDao.insert(catDrinks)
        productDao.insert(itemDrink1)
        productDao.insert(itemDrink2)
        productDao.insert(itemDrink3)
        productDao.insert(itemDrink4)
    }
}

class ShopViewModel() : ViewModel() {
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
     * _products + products are not tied to the database. It’s just a mutable state holder
     * inside the ViewModel. You control it manually, see getAllProductsFromCategory.
     */
    private val _products = MutableStateFlow<List<ProductEntity>>(emptyList())
    val products: StateFlow<List<ProductEntity>> = _products.asStateFlow()

    // Now the operations on the database.

    fun getAllProductsFromCategory(categoryId: UUID) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllProductsFromCategory(categoryId).collect { itemList ->
                _products.value = itemList
            }
        }
    }

    fun addProduct(name: String, categoryId: UUID) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addProduct(ProductEntity(name = name, categoryId = categoryId))
        }
    }

    fun updateProductLabel(categoryId: UUID, newLabel: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateProductLabel(categoryId, newLabel)
        }
    }

    fun resetData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetLocalDatabase()
        }
    }

    fun syncWithExternalData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.syncWithExternalDatabase()
        }
    }
}
