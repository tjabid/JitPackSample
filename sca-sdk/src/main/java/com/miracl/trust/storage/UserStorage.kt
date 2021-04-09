package com.miracl.trust.storage

import com.miracl.trust.model.AuthenticationUser
import com.miracl.trust.model.SigningUser

/**
 * ## A type representing storage
 * Already registered users will be kept in it between app launches.
 * >
 * Methods of this interface must not be called outside of the SDK, as they are intended
 * to be only for internal usage.
 *
 * Keep in mind, that this interface doesn't provide any data encryption and developers should take
 * care of this by themselves.
 * >
 * By default this SDK uses a concrete implementation of this interface [RoomUserStorage][com.miracl.trust.storage.room.RoomUserStorage].
 */
interface UserStorage {
    /**
     * Prepares the user storage to be used.
     * > Called once on initialization of the SDK.
     */
    fun loadStorage()

    /**
     * Adds a registered user to the user storage.
     * @param authenticationUser registered user.
     */
    fun add(authenticationUser: AuthenticationUser)

    /**
     * Deletes a registered user from the user storage.
     * @param authenticationUser The registered user to delete.
     */
    fun delete(authenticationUser: AuthenticationUser)

    /**
     * Checks whether there is a user with the same Id in the user storage.
     * @param userId Id to check for
     * @return True if the user already exists in the storage, False otherwise.
     */
    fun userExists(userId: String): Boolean

    /**
     * Returns all users from the user storage.
     */
    fun authenticationUsers(): List<AuthenticationUser>

    /**
     * Adds new Signing User to the storage.
     * @param signingUser user that needs to be added to the storage.
     */
    fun add(signingUser: SigningUser)

    /**
     * Deletes a Signing user.
     * @param signingUser Signing user that has to be deleted.
     */
    fun delete(signingUser: SigningUser)

    /**
     * Check whether there is a Signing User in the storage with the given user id.
     * @param userId user id, that is checked in the storage.
     */
    fun signingUserExists(userId: String): Boolean

    /**
     * Get all Signing users written in the storage.
     */
    fun signingUsers(): List<SigningUser>
}