package com.miracl.trust.storage.room

import com.miracl.trust.model.AuthenticationUser
import com.miracl.trust.model.Identity
import com.miracl.trust.model.SigningUser
import com.miracl.trust.storage.UserStorage
import com.miracl.trust.storage.room.model.AuthenticationUserModel
import com.miracl.trust.storage.room.model.SigningUserModel
import kotlinx.coroutines.runBlocking

internal class RoomUserStorage(private val userDatabase: UserDatabase) : UserStorage {
    override fun loadStorage() {
    }

    override fun add(authenticationUser: AuthenticationUser) {
        runBlocking {
            userDatabase.authenticationUserDao().insert(authenticationUser.toUserModel())
        }
    }

    override fun delete(authenticationUser: AuthenticationUser) {
        runBlocking {
            userDatabase.authenticationUserDao().delete(authenticationUser.toUserModel())
        }
    }

    override fun userExists(userId: String): Boolean {
        return runBlocking {
            userDatabase.authenticationUserDao().countByUserId(userId) == 1
        }
    }

    override fun authenticationUsers(): List<AuthenticationUser> {
        return runBlocking {
            userDatabase.authenticationUserDao().getAll()
                .map { it.toUser() }
        }
    }

    override fun add(signingUser: SigningUser) {
        runBlocking {
            userDatabase.signingUserDao().insert(signingUser.toDVSUserModel())
        }
    }

    override fun delete(signingUser: SigningUser) {
        runBlocking {
            userDatabase.signingUserDao().delete(signingUser.toDVSUserModel())
        }
    }

    override fun signingUserExists(userId: String): Boolean {
        return runBlocking {
            userDatabase.signingUserDao().countByUserId(userId) == 1
        }
    }

    override fun signingUsers(): List<SigningUser> {
        return runBlocking {
            userDatabase.signingUserDao().getAll()
                .map { it.toDVSUser() }
        }
    }

    private fun AuthenticationUserModel.toUser(): AuthenticationUser =
        AuthenticationUser(
            Identity(
                identity.userId,
                identity.pinLength,
                identity.isBlocked,
                identity.mpinId,
                identity.token,
                identity.dtas
            )
        )

    private fun AuthenticationUser.toUserModel(): AuthenticationUserModel =
        AuthenticationUserModel(identity)

    private fun SigningUserModel.toDVSUser(): SigningUser =
        SigningUser(
            Identity(
                identity.userId,
                identity.pinLength,
                identity.isBlocked,
                identity.mpinId,
                identity.token,
                identity.dtas
            ),
            publicKey
        )

    private fun SigningUser.toDVSUserModel(): SigningUserModel =
        SigningUserModel(identity, publicKey)
}