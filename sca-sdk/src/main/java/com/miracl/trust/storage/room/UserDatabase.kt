package com.miracl.trust.storage.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.miracl.trust.storage.room.dao.AuthenticationUserDao
import com.miracl.trust.storage.room.dao.SigningUserDao
import com.miracl.trust.storage.room.model.AuthenticationUserModel
import com.miracl.trust.storage.room.model.SigningUserModel

@Database(
    entities = [
        AuthenticationUserModel::class,
        SigningUserModel::class
    ], version = 1
)
internal abstract class UserDatabase : RoomDatabase() {
    abstract fun authenticationUserDao(): AuthenticationUserDao
    abstract fun signingUserDao(): SigningUserDao
}