// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room

import android.app.Application
import de.fh_aachen.android.room.database.ShopDatabase
import de.fh_aachen.android.room.model.ShopRepository

/*
 * For an Android app that needs a local relational DB:
 * Default choice:
 *  – Room over SQLite (AndroidX, fully supported, good tooling).
 * If you’re doing Kotlin Multiplatform (KMP, formerly 'KMM') or want a cross-platform DB:
 *  – Room as well: KMP support since Room 2.7, and Room 3 (androidx.room3) is KMP-first.
 *  – SQLDelight (SQL first: you write the SQL, it generates typesafe Kotlin).
 * If you’re writing a server in Kotlin:
 *  – Exposed, JPA, etc., but that’s off-device.
 */

class RoomApplication : Application() {

    // feel free to use a DI framework
    companion object {
        lateinit var instance: RoomApplication
    }

    private val localDatabase: ShopDatabase by lazy { ShopDatabase.getDatabase(this) }
    val dataRepository: ShopRepository by lazy { ShopRepository(localDatabase) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
