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

/*
 * The 'composition root', as in the models apps: the one place that creates the
 *  long-lived objects. Before, the ViewModel reached out to a static 'RoomApplication.instance'
 *  (a hidden dependency, untestable without a running app). Now it gets the repository through
 *  its constructor, see ShopViewModel.Factory. Feel free to use a DI framework.
 *
 *  'by lazy' here for a reason: the database needs a Context, and property initializers run
 *  in the constructor - before the Application is attached to its Context. Lazily, it is
 *  created on first use, long after that.
 */
class RoomApplication : Application() {
    private val localDatabase: ShopDatabase by lazy { ShopDatabase.create(this) }
    val dataRepository: ShopRepository by lazy { ShopRepository(localDatabase) }
}
