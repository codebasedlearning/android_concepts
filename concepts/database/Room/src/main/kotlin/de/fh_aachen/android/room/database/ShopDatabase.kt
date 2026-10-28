// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import androidx.room.TypeConverter

/*
 * Tables "category" and "product", described by entities.
 * This classes will have a mapping SQLite table in the database.
 */

@Entity(tableName = "category")
data class CategoryEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),   // for int: @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(tableName = "product",
        foreignKeys = [ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )]
)
data class ProductEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val categoryId: UUID
)

/*
 * Marks the class as a Data Access Object (DAO).
 *
 * Data Access Objects are the main classes where you define your database interactions.
 * They can include a variety of query methods. A class marked with @Dao should either
 * be an interface or an abstract class.
 *
 * Some operations are fully defined by your data class like @Insert, @update or
 * @Delete - so Room doesn’t require SQL.
 * You only need to write SQL when Room cannot safely generate it for you like for the
 * 'select's below.
 *
 * Use 'suspend' for one-shot, potentially expensive operations like @Insert, @Update,
 * @Delete or @Query that just loads data once.
 * If a DAO method returns Flow, it should not be suspend. Flow itself is the async abstraction.
 */

@Dao
interface CategoryDao {
    @Insert
    suspend fun insert(category: CategoryEntity)

    @Query("SELECT * FROM category")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // for seeding an empty database (a new query does not change the schema)
    @Query("SELECT COUNT(*) FROM category")
    suspend fun count(): Int

    // if you want to have a filter on a given id:
    //      @Query("SELECT * FROM category WHERE id = :id")
    //      fun getCategoryById(id: UUID): Flow<IntCategoryEntity?>
}

@Dao
interface ProductDao {
    @Insert
    suspend fun insert(item: ProductEntity)

    @Insert
    suspend fun insertAll(items: List<ProductEntity>)

    @Query("SELECT * FROM product WHERE categoryId = :categoryId")
    fun getItemsForCategory(categoryId: UUID): Flow<List<ProductEntity>>

    @Query("UPDATE product SET name = :newName WHERE id = :id")
    suspend fun updateItemLabel(id: UUID, newName: String)
}

/*
 * SQLite supports only a small set of primitive types like INTEGER, REAL, TEXT
 * or BLOB.
 * UUID is a Kotlin/JVM type, not a SQLite type, i.e. Room needs instructions
 * to serialize/deserialize it.
 */

class Converters {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid: String?): UUID? = uuid?.let { UUID.fromString(it) }
}

/*
 * A Room @Database class is a schema declaration + a factory for DAOs + metadata
 * about your entities. It is not a 'database' in the traditional sense and it is
 * always a local SQLite database under the hood.
 *
 * The Room Gradle plugin (see build.gradle.kts) exports the schema of every version as JSON
 * into 'schemas/'. Commit those files: they document the schema history and are needed for
 * AutoMigrations and migration tests.
 *
 * You can define migration strategies, e.g.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add a new column to the Category table
                database.execSQL("ALTER TABLE Category ADD COLUMN description TEXT")
            }
        }
 *
 */

@Database(entities = [CategoryEntity::class, ProductEntity::class], version = 7)
@TypeConverters(Converters::class)
abstract class ShopDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao

    /*
     * Before, there were two singletons at once:
     * this companion (with double-checked locking) and the lazy property in RoomApplication.
     * One owner is enough, and the Application is it: it exists exactly once per process.
     * Double-checked locking is what you need when there is no such owner, e.g. in a library:
     *      @Volatile private var instance: ShopDatabase? = null
     *      fun get(context: Context) = instance ?: synchronized(this) {
     *          instance ?: create(context).also { instance = it }   // second check matters:
     *      }                                                        // another thread may have won
     */
    companion object {
        const val SchemaName = "product_category_db"

        fun create(context: Context): ShopDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                ShopDatabase::class.java,
                SchemaName
            )
                // Careful, this reads like 'off' but means 'on': destructive migration is
                //  enabled (no migration path -> tables are dropped, data is gone). The parameter
                //  only decides whether all tables are dropped, including those Room does not know.
                //  The deprecated no-argument version is the same as 'true'.
                .fallbackToDestructiveMigration(dropAllTables = false)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                //.addMigrations(MIGRATION_1_2)
                .build()
    }
}
