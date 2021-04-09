package com.miracl.trust.storage.room

import android.content.Context
import androidx.room.Room
import com.miracl.trust.storage.UserStorage
import com.miracl.trust.storage.security.KeyProtector
import com.miracl.trust.storage.security.KeyProvider
import net.sqlcipher.database.SupportFactory

internal class RoomDatabaseModule(private val context: Context) {
    companion object {
        private const val STORAGE_PREFERENCES = "storage_preferences"
        private const val DATABASE_FILE_NAME = "users.db"
    }

    fun userStorage(): UserStorage {
        return RoomUserStorage(getRoomDatabase())
    }

    private fun getRoomDatabase() =
        Room.databaseBuilder(
            context,
            UserDatabase::class.java,
            DATABASE_FILE_NAME
        )
            .fallbackToDestructiveMigrationOnDowngrade()
            .openHelperFactory(getSQLiteOpenHelperFactory())
            .build()


    private fun getSQLiteOpenHelperFactory() =
        SupportFactory(
            KeyProvider(getStoragePreferences(), getKeyProtector()).storageKey
        )

    private fun getStoragePreferences() =
        context.getSharedPreferences(STORAGE_PREFERENCES, Context.MODE_PRIVATE)

    private fun getKeyProtector() =
        KeyProtector(context)
}