package com.miracl.trust.storage.room.model

import androidx.room.Embedded
import androidx.room.Entity
import com.miracl.trust.model.Identity

@Entity(tableName = "signing_users", primaryKeys = ["mpinId", "token"])
internal data class SigningUserModel(
    @Embedded val identity: Identity,
    val publicKey: ByteArray
)