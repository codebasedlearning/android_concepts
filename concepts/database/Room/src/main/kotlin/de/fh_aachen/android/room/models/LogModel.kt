// (C) A.Voß, a.voss@fh-aachen.de, info@codebasedlearning.dev

package de.fh_aachen.android.room.models

import android.database.sqlite.SQLiteException
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.room.withTransaction
import de.fh_aachen.android.room.RoomApplication
import de.fh_aachen.android.room.database.LogDao
import de.fh_aachen.android.room.database.LogLevel
import de.fh_aachen.android.room.database.LogMessageEntity
import de.fh_aachen.android.room.database.AppDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/*
 * The log as a repository, like the shop data: one place that writes, a Flow to read.
 *  Every entry also goes to Logcat - the table is for the user (and the tester without
 *  a cable), Logcat stays for the developer.
 *
 * The obvious catch: if the database itself is broken, the entry about that cannot be written
 *  into the very same database ("I lost my notebook", written in the notebook). That failure
 *  is caught and ends up in Logcat only - a logger must never crash the app.
 */
class LogRepository(private val appDatabase: AppDatabase) {
    private val logDao: LogDao = appDatabase.logDao()

    fun latest(limit: Int = SHOW): Flow<List<LogMessageEntity>> = logDao.latest(limit)

    suspend fun info(tag: String, message: String) = write(LogLevel.INFO, tag, message)

    suspend fun error(tag: String, message: String, error: Throwable? = null) =
        write(LogLevel.ERROR, tag, message, error)

    suspend fun clear() = logDao.clear()

    private suspend fun write(level: LogLevel, tag: String, message: String, error: Throwable? = null) {
        Log.println(level.priority, tag, if (error == null) message else "$message\n${Log.getStackTraceString(error)}")
        try {
            // insert + trim as one transaction: the table never holds more than KEEP rows
            appDatabase.withTransaction {
                logDao.insert(LogMessageEntity(level = level, tag = tag, message = message, details = error?.toString()))
                logDao.keepNewest(KEEP)
            }
        } catch (e: SQLiteException) {
            Log.e(tag, "could not write the log entry to the database", e)
        }
    }

    private val LogLevel.priority: Int
        get() = when (this) {
            LogLevel.DEBUG -> Log.DEBUG
            LogLevel.INFO -> Log.INFO
            LogLevel.WARN -> Log.WARN
            LogLevel.ERROR -> Log.ERROR
        }

    private companion object {
        const val KEEP = 200        // rows kept in the table
        const val SHOW = 100        // rows shown on the screen
    }
}

// The log screen: the newest entries, and 'clear'.
class LogViewModel(private val log: LogRepository) : ViewModel() {

    val entries: StateFlow<List<LogMessageEntity>> = log.latest()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun clear() {
        viewModelScope.launch {
            try {
                log.clear()
            } catch (e: SQLiteException) {
                log.error(TAG, "clear log failed", e)
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as RoomApplication
                LogViewModel(app.logRepository)
            }
        }
    }
}
