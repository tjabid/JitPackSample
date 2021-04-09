package com.miracl.trust

import android.content.Context
import android.os.Build
import com.miracl.trust.MiraclTrust.Companion.configure
import com.miracl.trust.MiraclTrust.Companion.getInstance
import com.miracl.trust.authentication.AuthenticationApiManager
import com.miracl.trust.authentication.AuthenticationResponses
import com.miracl.trust.authentication.AuthenticatorContract
import com.miracl.trust.authentication.AuthenticatorScopes
import com.miracl.trust.configuration.Configuration
import com.miracl.trust.configuration.ConfigurationApiManager
import com.miracl.trust.configuration.ConfigurationFailException
import com.miracl.trust.configuration.ConfiguratorContract
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.delegate.ResultHandler
import com.miracl.trust.factory.ComponentFactory
import com.miracl.trust.model.AuthenticationUser
import com.miracl.trust.model.SigningUser
import com.miracl.trust.network.ApiSettings
import com.miracl.trust.network.OkHttpRequestExecutor
import com.miracl.trust.registration.*
import com.miracl.trust.signing.*
import com.miracl.trust.storage.UserStorage
import com.miracl.trust.util.json.GsonJsonUtil
import com.miracl.trust.util.log.DefaultMiraclLogger
import com.miracl.trust.util.log.LoggerConstants
import com.miracl.trust.util.log.MiraclLogger
import kotlinx.coroutines.*
import tg.sdk.sca.BuildConfig
import java.util.*

/**
 * Miracl Trust is the entry point of the Miracl Trust SDK. It is configured and connects
 * with the MIRACLTrust Platform on its initialization.
 *
 * Initialization is done through [configure(context,configuration)][configure]. After initialization,
 * the SDK can be accessed through [getInstance()][getInstance].
 */
class MiraclTrust private constructor(
    context: Context,
    configuration: Configuration
) {
    companion object {
        internal var miraclLogger: MiraclLogger? = null
            private set

        private const val NOT_INITIALIZED_EXCEPTION = "MIRACLTrust SDK not initialized!"

        private lateinit var instance: MiraclTrust

        @JvmStatic
        fun getInstance() =
            if (this::instance.isInitialized) {
                instance
            } else {
                throw Exception(NOT_INITIALIZED_EXCEPTION)
            }

        /**
         * Initialize the MIRACLTrust SDK.
         *
         * > **To be used once**. Multiple uses could lead to unidentified behavior!
         *
         * @param context application context, used for managing storage.
         * @param configuration instance of [Configuration], used to configure the SDK.
         */
        @JvmStatic
        fun configure(context: Context, configuration: Configuration) {
            instance = MiraclTrust(context, configuration)
        }
    }

    /**
     * The registered user identities, stored inside the user storage
     */
    var authenticationUsers: List<AuthenticationUser> = listOf()
        private set

    /**
     * The registered signing identities, stored inside the user storage
     */
    var signingUsers: List<SigningUser> = listOf()
        private set

    private val configurator: ConfiguratorContract
    private val verificator: Verificator
    private val registrator: RegistratorContract
    private val signingRegistrator: SigningRegistrator
    private val documentSigner: DocumentSigner
    private val authenticator: AuthenticatorContract
    private val userStorage: UserStorage

    private val miraclTrustScope: CoroutineScope

    private val clientId: String

    init {
        miraclLogger = if (BuildConfig.DEBUG) {
            configuration.miraclLogger
                ?: DefaultMiraclLogger(
                    configuration.logLevel ?: MiraclLogger.LogLevel.NONE
                )
        } else {
            null
        }

        val httpRequestExecutor =
            configuration.httpRequestExecutor ?: OkHttpRequestExecutor()
        val componentFactory =
            configuration.componentFactory ?: ComponentFactory(context)
        val apiSettings = ApiSettings(configuration.baseUrl)

        miraclTrustScope = CoroutineScope(SupervisorJob() + configuration.miraclCoroutineContext)

        userStorage = configuration.userStorage ?: componentFactory.defaultUserStorage()
        userStorage.loadStorage()

        val configurationApi =
            ConfigurationApiManager(
                httpRequestExecutor = httpRequestExecutor,
                projectId = configuration.projectId,
                jsonUtil = GsonJsonUtil,
                apiSettings = apiSettings
            )

        configurator =
            componentFactory.createConfigurator(configurationApi)
        configureSdk()

        val verificationApi = VerificationApiManager(
            jsonUtil = GsonJsonUtil,
            httpRequestExecutor = httpRequestExecutor,
            apiSettings = apiSettings
        )

        verificator = componentFactory.createVerificator(verificationApi)

        val registrationApi = RegistrationApiManager(
            httpRequestExecutor = httpRequestExecutor,
            projectId = configuration.projectId,
            jsonUtil = GsonJsonUtil,
            apiSettings = apiSettings
        )

        registrator =
            componentFactory.createRegistrator(registrationApi, userStorage)

        val authenticationApi =
            AuthenticationApiManager(
                httpRequestExecutor = httpRequestExecutor,
                projectId = configuration.projectId,
                jsonUtil = GsonJsonUtil,
                apiSettings = apiSettings
            )

        authenticator =
            componentFactory.createAuthenticator(authenticationApi)

        val signingRegistrationApi =
            SigningRegistrationApiManager(
                httpRequestExecutor = httpRequestExecutor,
                projectId = configuration.projectId,
                jsonUtil = GsonJsonUtil,
                apiSettings = apiSettings
            )

        signingRegistrator = componentFactory.createSigningRegistrator(
            signingRegistrationApi = signingRegistrationApi,
            authenticator = authenticator,
            userStorage = userStorage
        )

        documentSigner = componentFactory.createDocumentSigner(
            authenticator = authenticator
        )

        clientId = configuration.clientId

        fetchUsers()
        fetchSigningUsers()
    }

    private fun configureSdk() {
        runBlocking {
            try {
                withContext(Dispatchers.IO) {
                    configurator.configure()
                }
            } catch (ex: ConfigurationFailException) {
                throw ex
            }
        }
    }

    /**
     * Default method to verify user identity against the MIRACL platform. In the current
     * implementation it is done by sending an email message.
     *
     * @param userId identifier of the user identity. To verify identity this identifier
     * needs to be valid email address.
     * @param accessId a session identifier used to get information from web session.
     * @param resultHandler a callback to handle the result of the verification.
     * - If successful, the result is [MiraclSuccess].
     * - If an error occurs, the result is [MiraclError] with a message. On exception,
     * the exception object is also passed.
     * @param deviceName provides a device name. Defaults to the model of the device.
     */
    @JvmOverloads
    fun verify(
        userId: String,
        accessId: String,
        resultHandler: ResultHandler<Unit, Error>,
        deviceName: String = Build.MODEL
    ) {
        miraclTrustScope.launch {
            verificator.verify(
                userId,
                clientId,
                accessId,
                deviceName
            ).also { result ->
                when (result) {
                    is MiraclSuccess -> {
                    }
                    is MiraclError -> logError(
                        LoggerConstants.VERIFICATOR_TAG,
                        LoggerConstants.FLOW_ERROR.format(
                            result.value.message ?: VerificationErrorResponses.FAIL.message
                        )
                    )
                }

                resultHandler.onResult(result)
            }
        }
    }

    /**
     * Default method to obtain activation token using the URL provided in the
     * verification email.
     *
     * @param verificationUrl a verification URL that has been sent to the user email
     * @param resultHandler a callback to handle the result of the verification.
     * - If successful, the result is [MiraclSuccess] with the [ActivationTokenResponse]
     * containing the *userId* and the [*activationToken*][ActivationToken] value.
     * - If an error occurs, the result is [MiraclError] with a message. On exception,
     * the exception object is also passed.
     */
    fun getActivationToken(
        verificationUrl: String,
        resultHandler: ResultHandler<ActivationTokenResponse, Error>
    ) {
        miraclTrustScope.launch {
            verificator.getActivationToken(
                verificationUrl
            ).also { result ->
                when (result) {
                    is MiraclSuccess -> {
                    }
                    is MiraclError -> logError(
                        LoggerConstants.VERIFICATOR_TAG,
                        LoggerConstants.FLOW_ERROR.format(
                            result.value.message
                                ?: VerificationErrorResponses.FAIL_ACTIVATION_TOKEN.message
                        )
                    )
                }

                resultHandler.onResult(result)
            }
        }
    }

    /**
     * Provides end-user registration. Registers an end-user for a given MiraclTrust Customer
     * to the MiraclTrust platform.
     *
     * @param userId provides an unique user id (i.e. email).
     * @param activationToken provides an activate token for verification.
     * @param pinProvider a callback called from the SDK, when the identity PIN is required.
     * @param resultHandler a callback to handle the result of the registration.
     * - If successful, the result is [MiraclSuccess] with value of the registered user.
     * - If an error occurs, the result is [MiraclError] with a message. On exception,
     * the exception object is also passed.
     * @param deviceName provides a device name. Defaults to the model of the device.
     * @param pushNotificationsToken current device push notifications token. This is used
     * when push notifications for authentication are enabled in the platform.
     */
    @JvmOverloads
    fun register(
        userId: String,
        activationToken: ActivationToken,
        pinProvider: PinProvider,
        resultHandler: ResultHandler<AuthenticationUser, Error>,
        deviceName: String = Build.MODEL,
        pushNotificationsToken:String? = null
    ) {
        if (userStorage.userExists(userId)) {
            logError(
                LoggerConstants.REGISTRATOR_TAG,
                LoggerConstants.FLOW_ERROR
                    .format(RegistrationResponses.FAIL_USER_ALREADY_REGISTERED.message)
            )

            resultHandler.onResult(
                MiraclError(
                    Error(
                        RegistrationResponses.FAIL_USER_ALREADY_REGISTERED.message
                    )
                )
            )

            return
        }

        miraclTrustScope.launch {
            registrator.register(
                userId,
                activationToken,
                pinProvider,
                deviceName,
                pushNotificationsToken
            ).also { result ->
                when (result) {
                    is MiraclSuccess -> fetchUsers()
                    is MiraclError -> logError(
                        LoggerConstants.REGISTRATOR_TAG,
                        LoggerConstants.FLOW_ERROR
                            .format(result.value.message ?: RegistrationResponses.FAIL.message)
                    )
                }

                resultHandler.onResult(result)
            }
        }
    }

    /**
     * Authenticate a registered user.
     * @param authenticationUser the user to authenticate.
     * @param accessId provides an access id by url or camera and QR code.
     * @param pinProvider a callback called from the SDK, when the identity PIN is required.
     * @param resultHandler a callback to handle the result of the authentication.
     * - If successful, the result is [MiraclSuccess].
     * - If an error occurs, the result is [MiraclError] with a message. On exception,
     * the exception object is also passed.
     */
    fun authenticate(
        authenticationUser: AuthenticationUser,
        accessId: String,
        pinProvider: PinProvider,
        resultHandler: ResultHandler<Unit, Error>
    ) {
        miraclTrustScope.launch {
            authenticator.authenticate(
                authenticationUser.identity,
                accessId,
                pinProvider,
                arrayOf(AuthenticatorScopes.OIDC.value)
            ).also { result ->
                when (result) {
                    is MiraclSuccess -> resultHandler.onResult(MiraclSuccess(Unit))
                    is MiraclError -> {
                        logError(
                            LoggerConstants.AUTHENTICATOR_TAG,
                            LoggerConstants.FLOW_ERROR
                                .format(
                                    result.value.message ?: AuthenticationResponses.FAIL.message
                                )
                        )
                        resultHandler.onResult(MiraclError(result.value))
                    }
                }
            }
        }
    }

    /**
     * Generate an authentication code for a registered user.
     * @param authenticationUser the user to authenticate.
     * @param accessId provides an access id by url or camera and QR code.
     * @param pinProvider a callback called from the SDK, when the identity PIN is required.
     * @param resultHandler a callback to handle the result of the authentication.
     * - If successful, the result is [MiraclSuccess].
     * - If an error occurs, the result is [MiraclError] with a message. On exception,
     * the exception object is also passed.
     */
    fun generateAuthCode(
        authenticationUser: AuthenticationUser,
        accessId: String,
        pinProvider: PinProvider,
        resultHandler: ResultHandler<String, Error>
    ) {
        miraclTrustScope.launch {
            authenticator.authenticate(
                authenticationUser.identity,
                accessId,
                pinProvider,
                arrayOf(AuthenticatorScopes.AUTH_CODE.value)
            ).also { result ->
                when (result) {
                    is MiraclSuccess -> {
                        val authCode = (result as MiraclSuccess).value.code
                        if (authCode != null) {
                            resultHandler.onResult(MiraclSuccess(authCode))
                        } else {
                            resultHandler.onResult(MiraclError(Error("Could not find auth code in response")))
                        }
                    }
                    is MiraclError -> {
                        logError(
                            LoggerConstants.AUTHENTICATOR_TAG,
                            LoggerConstants.FLOW_ERROR
                                .format(
                                    result.value.message ?: AuthenticationResponses.FAIL.message
                                )
                        )
                        resultHandler.onResult(MiraclError(result.value))
                    }
                }
            }
        }
    }

    /**
     * Creates new signing identity in the MIRACL platform.
     * @param authenticationUser already registered identity.
     * @param accessId session identifier used to get information from web session.
     * @param authenticationPinProvider a callback called from the SDK, when the identity PIN is required.
     * @param signingPinProvider a callback called from the SDK, when the signing identity PIN is required.
     * @param resultHandler a callback to handle the result of registering the signing identity.
     * - If successful, the result is [MiraclSuccess].
     * - If an error occurs, the result is [MiraclError] with a message. On exception,
     * the exception object is also passed.
     * @param deviceName provides a device name. Defaults to the model of the device.
     */
    @JvmOverloads
    fun signingRegister(
        authenticationUser: AuthenticationUser,
        accessId: String,
        authenticationPinProvider: PinProvider,
        signingPinProvider: PinProvider,
        resultHandler: ResultHandler<SigningUser, Error>,
        deviceName: String = Build.MODEL
    ) {
        miraclTrustScope.launch {
            signingRegistrator.register(
                authenticationUser,
                accessId,
                authenticationPinProvider,
                signingPinProvider,
                deviceName
            ).also { result ->
                when (result) {
                    is MiraclSuccess -> fetchSigningUsers()
                    is MiraclError -> logError(
                        LoggerConstants.SIGNING_REGISTRATOR_TAG,
                        LoggerConstants.FLOW_ERROR
                            .format(
                                result.value.message
                                    ?: SigningRegistrationErrorResponses.FAIL.message
                            )
                    )
                }

                resultHandler.onResult(result)
            }
        }
    }

    /**
     * Create a cryptographic signature of the given document.
     * @param message the hash of the given document.
     * @param timestamp when the document is signed.
     * @param signingUser an already registered signing identity.
     * @param pinProvider a callback called from the SDK, when the signing identity PIN is required.
     * @param resultHandler a callback to handle the result of the signing.
     * - If successful, the result is [MiraclSuccess].
     * - If an error occurs, the result is [MiraclError] with a message. On exception,
     * the exception object is also passed.
     */
    fun sign(
        message: ByteArray,
        timestamp: Date,
        signingUser: SigningUser,
        pinProvider: PinProvider,
        resultHandler: ResultHandler<Signature, Error>
    ) {
        miraclTrustScope.launch {
            documentSigner
                .sign(
                    message,
                    timestamp,
                    signingUser,
                    pinProvider
                )
                .also { result ->
                    if (result is MiraclError) {
                        logError(
                            LoggerConstants.DOCUMENT_SIGNER_TAG,
                            LoggerConstants.FLOW_ERROR
                                .format(
                                    result.value.message
                                        ?: DocumentSigningErrorResponses.FAIL.message
                                )
                        )
                    }

                    resultHandler.onResult(result)
                }
        }
    }

    /**
     * Delete a registered user.
     * @param authenticationUser the user to be deleted.
     */
    fun deleteUser(authenticationUser: AuthenticationUser) {
        userStorage.delete(authenticationUser)
        fetchUsers()
    }

    /**
     * Delete a signing user.
     * @param signingUser the user to be deleted.
     */
    fun deleteSigningUser(signingUser: SigningUser) {
        userStorage.delete(signingUser)
        fetchSigningUsers()
    }

    private fun fetchUsers() {
        authenticationUsers = userStorage.authenticationUsers()
    }

    private fun fetchSigningUsers() {
        signingUsers = userStorage.signingUsers()
    }

    private fun logError(tag: String, message: String) {
        miraclLogger?.error(
            tag,
            message
        )
    }
}
