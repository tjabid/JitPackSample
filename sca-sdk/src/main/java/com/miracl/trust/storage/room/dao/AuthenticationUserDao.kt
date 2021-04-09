package com.miracl.trust.storage.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.miracl.trust.storage.room.model.AuthenticationUserModel

@Dao
internal interface AuthenticationUserDao {
    @Insert
    suspend fun insert(authenticationUserModel: AuthenticationUserModel)

    @Delete
    suspend fun delete(authenticationUserModel: AuthenticationUserModel)

    @Query("SELECT * from authentication_users")
    suspend fun getAll(): List<AuthenticationUserModel>

    @Query("SELECT COUNT(*) from authentication_users Where userId == :userId")
    suspend fun countByUserId(userId: String): Int
}