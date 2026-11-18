// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room.database

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/*
 * Tables "category" and "product", described by entities.
 * These classes will have a mapping SQLite table in the database.
 */

@Entity(
    tableName = "category"
)
data class CategoryEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),   // for int: @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String
)

@Entity(
    tableName = "product",
    foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.CASCADE
    )],
    // A foreign key column should have an index (Room warns otherwise): without it, every
    //  'WHERE categoryId = ...' and every delete of a category (CASCADE) scans the whole table.
    indices = [Index(value = ["categoryId"])]
)
data class ProductEntity(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val categoryId: UUID
)

/*
 * The app's own log, a third table without any relation to the shop data.
 *  - id: autoGenerate = the SQLite rowid, so 'ORDER BY id' is the order of insertion,
 *  - timestamp: epoch millis as INTEGER (no converter needed; Instant/Date would need one),
 *  - level: an enum - Room stores it by name as TEXT, built in, no converter needed either.
 */
enum class LogLevel { DEBUG, INFO, WARN, ERROR }

@Entity(
    tableName = "log_message"
)
data class LogMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,   // 0 = "not set yet", Room assigns it
    val timestamp: Long = System.currentTimeMillis(),
    val level: LogLevel,
    val tag: String,
    val message: String,
    val details: String? = null,                         // e.g. the exception with stack trace
)

/*
 * Marks the class as a Data Access Object (DAO).
 *
 * Data Access Objects are the main classes where you define your database interactions.
 * They can include a variety of query methods. A class marked with @Dao should either
 * be an interface or an abstract class.
 *
 * Some operations are fully defined by your data class like @Insert, @Update or
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

    // Without ORDER BY, SQLite guarantees no order at all - insertion order is luck.
    @Query("SELECT * FROM category ORDER BY name")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    // for seeding an empty database (a new query does not change the schema)
    @Query("SELECT COUNT(*) FROM category")
    suspend fun count(): Int

    // Deletes the products as well: ON DELETE CASCADE on product.categoryId (Room switches
    //  the foreign keys on, PRAGMA foreign_keys = ON, because the schema has some).
    @Query("DELETE FROM category")
    suspend fun deleteAll()

    // if you want to have a filter on a given id:
    //      @Query("SELECT * FROM category WHERE id = :id")
    //      fun getCategoryById(id: UUID): Flow<CategoryEntity?>
}

@Dao
interface ProductDao {
    @Insert
    suspend fun insert(item: ProductEntity)

    @Insert
    suspend fun insertAll(items: List<ProductEntity>)

    @Query("SELECT * FROM product WHERE categoryId = :categoryId ORDER BY name")
    fun getItemsForCategory(categoryId: UUID): Flow<List<ProductEntity>>

    @Query("UPDATE product SET name = :newName WHERE id = :id")
    suspend fun updateItemLabel(id: UUID, newName: String)
}

@Dao
interface LogDao {
    @Insert
    suspend fun insert(entry: LogMessageEntity)

    // newest first
    @Query("SELECT * FROM log_message ORDER BY id DESC LIMIT :limit")
    fun latest(limit: Int): Flow<List<LogMessageEntity>>

    // A ring buffer in SQL: keep the newest 'keep' rows, delete the rest.
    @Query("DELETE FROM log_message WHERE id NOT IN (SELECT id FROM log_message ORDER BY id DESC LIMIT :keep)")
    suspend fun keepNewest(keep: Int)

    @Query("DELETE FROM log_message")
    suspend fun clear()
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
 * Version 8 added the index on product.categoryId and the table log_message. Room can derive
 *  such changes itself from the exported schemas 7.json and 8.json: an AutoMigration, generated
 *  at compile time, keeps the data. For changes Room cannot infer (a renamed column, a split table) you add a
 *  spec class (AutoMigrationSpec) or write the migration by hand.
 *
 * You can define migration strategies by hand, e.g.
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add a new column to the Category table
                database.execSQL("ALTER TABLE Category ADD COLUMN description TEXT")
            }
        }
 *
 */

@Database(
    entities = [CategoryEntity::class, ProductEntity::class, LogMessageEntity::class],
    version = 8,
    autoMigrations = [AutoMigration(from = 7, to = 8)],   // needs schemas/7.json (committed)
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun productDao(): ProductDao
    abstract fun logDao(): LogDao

    companion object {
        const val SchemaName = "product_category_db"

        // The implementations come from Room’s annotation processor.
        // On every build, the Room compiler reads @Database, @Entity and @Dao, checks your SQL
        // against the tables (which is why a typo in @Query breaks the build, not the app),
        // and generates:
        // - ShopDatabase_Impl, a subclass of your abstract class that implements categoryDao(),
        //   productDao() and logDao();
        // - one …Dao_Impl per DAO interface with the actual SQL, the parameter binding,
        //   and the conversion from database rows to the entity data classes.
        fun create(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                SchemaName
            )
                // Careful, this reads like 'off' but means 'on': destructive migration is
                //  enabled (no migration path -> tables are dropped, data is gone). It is the
                //  fallback only: 7 -> 8 has a path (the AutoMigration above), so data survives. The parameter
                //  only decides whether all tables are dropped, including those Room does not know.
                //  The deprecated no-argument version is the same as 'true'.
                .fallbackToDestructiveMigration(dropAllTables = false)
                .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                //.addMigrations(MIGRATION_1_2)
                .build()
    }
}
