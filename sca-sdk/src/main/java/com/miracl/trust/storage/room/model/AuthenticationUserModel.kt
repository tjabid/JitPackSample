package com.miracl.trust.storage.room.model

import androidx.room.Embedded
import androidx.room.Entity
import com.miracl.trust.model.Identity

@Entity(tableName = "authentication_users", primaryKeys = ["mpinId", "token"])
internal data class AuthenticationUserModel(
    @Embedded val identity: Identity
)