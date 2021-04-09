package com.miracl.trust.factory

import android.content.Context
import com.miracl.trust.authentication.AuthenticationApi
import com.miracl.trust.authentication.Authenticator
import com.miracl.trust.authentication.AuthenticatorContract
import com.miracl.trust.configuration.ConfigurationApi
import com.miracl.trust.configuration.Configurator
import com.miracl.trust.configuration.ConfiguratorContract
import com.miracl.trust.crypto.Crypto
import com.miracl.trust.registration.*
import com.miracl.trust.signing.DocumentSigner
import com.miracl.trust.signing.SigningRegistrationApi
import com.miracl.trust.signing.SigningRegistrator
import com.miracl.trust.storage.UserStorage
import com.miracl.trust.storage.room.RoomDatabaseModule

@ExperimentalUnsignedTypes
internal class ComponentFactory(
    private val context: Context
) {
    private val crypto: Crypto = Crypto()

    fun defaultUserStorage(): UserStorage =
        RoomDatabaseModule(context).userStorage()

    fun createConfigurator(configurationApi: ConfigurationApi): ConfiguratorContract =
        Configurator(configurationApi)

    fun createVerificator(
        verificationApi: VerificationApi
    ): Verificator = Verificator(verificationApi)

    fun createRegistrator(
        registrationApi: RegistrationApi,
        userStorage: UserStorage
    ): RegistratorContract =
        Registrator(
            registrationApi,
            crypto,
            userStorage
        )

    fun createAuthenticator(authenticationApi: AuthenticationApi): AuthenticatorContract =
        Authenticator(
            authenticationApi,
            crypto
        )

    fun createSigningRegistrator(
        signingRegistrationApi: SigningRegistrationApi,
        authenticator: AuthenticatorContract,
        userStorage: UserStorage
    ): SigningRegistrator =
        SigningRegistrator(
            signingRegistrationApi,
            crypto,
            userStorage,
            authenticator
        )

    fun createDocumentSigner(authenticator: AuthenticatorContract): DocumentSigner =
        DocumentSigner(crypto, authenticator)
}
