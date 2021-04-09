package com.miracl.trust.storage.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.miracl.trust.storage.room.model.SigningUserModel

@Dao
internal interface SigningUserDao {
    @Insert
    suspend fun insert(signingUserModel: SigningUserModel)

    @Delete
    suspend fun delete(signingUserModel: SigningUserModel)

    @Query("SELECT * from signing_users")
    suspend fun getAll(): List<SigningUserModel>

    @Query("SELECT COUNT(*) from signing_users where userId == :userId")
    suspend fun countByUserId(userId: String): Int
}