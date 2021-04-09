package com.miracl.trust.configuration

import com.miracl.trust.configuration.Configuration.Builder
import com.miracl.trust.factory.ComponentFactory
import com.miracl.trust.network.HttpRequestExecutor
import com.miracl.trust.storage.UserStorage
import com.miracl.trust.util.log.MiraclLogger
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

/**
 * The Configuration class is used to set up the MIRACLTrust SDK. It provides a way
 * to customize some of the SDK components.
 *
 * Instance is created though its [Builder]
 */
class Configuration private constructor(
    internal val projectId: String,
    internal val clientId: String,
    internal val baseUrl: String,
    internal val httpRequestExecutor: HttpRequestExecutor? = null,
    internal val componentFactory: ComponentFactory? = null,
    internal val userStorage: UserStorage? = null,
    internal val miraclLogger: MiraclLogger? = null,
    internal val logLevel: MiraclLogger.LogLevel? = null,
    internal val miraclCoroutineContext: CoroutineContext
) {
    companion object {
        private const val DEFAULT_BASE_URL = "https://api.mpin.io"
    }

    private constructor(builder: Builder) :
            this(
                builder.projectId,
                builder.clientId,
                builder.baseUrl,
                builder.httpRequestExecutor,
                builder.componentFactory,
                builder.userStorage,
                builder.miraclLogger,
                builder.logLevel,
                builder.coroutineContext
            )

    /**
     * @param projectId required to link the SDK with the project on the MiraclTrust platform.
     * @param clientId required to link the SDK with the client on the MiraclTrust platform.
     */
    class Builder(internal val projectId: String, internal val clientId: String) {
        internal var baseUrl: String = DEFAULT_BASE_URL
            private set
        internal var httpRequestExecutor: HttpRequestExecutor? = null
            private set
        internal var componentFactory: ComponentFactory? = null
            private set
        internal var coroutineContext: CoroutineContext = Dispatchers.IO
            private set
        internal var userStorage: UserStorage? = null
            private set
        internal var miraclLogger: MiraclLogger? = null
            private set
        internal var logLevel: MiraclLogger.LogLevel? = null
            private set

        internal fun componentFactory(componentFactory: ComponentFactory) =
            apply { this.componentFactory = componentFactory }

        internal fun coroutineContext(coroutineContext: CoroutineContext) =
            apply { this.coroutineContext = coroutineContext }

        fun baseUrl(baseUrl: String) =
            apply { this.baseUrl = baseUrl }

        /**
         * Provides implementation of the [HttpRequestExecutor] interface to be used by the SDK.
         */
        fun httpRequestExecutor(httpRequestExecutor: HttpRequestExecutor) =
            apply { this.httpRequestExecutor = httpRequestExecutor }

        /**
         * Provides implementation of the [UserStorage] interface to be used by the SDK.
         */
        fun userStorage(userStorage: UserStorage) =
            apply { this.userStorage = userStorage }

        /**
         * Provides implementation of the [MiraclLogger] interface to be used by the SDK.
         */
        fun miraclLogger(miraclLogger: MiraclLogger) =
            apply { this.miraclLogger = miraclLogger }

        /**
         * Provides specific [MiraclLogger.LogLevel] to be used by the SDK default logger.
         *
         * The default is [MiraclLogger.LogLevel.NONE]
         * >
         * **Has no effect if using custom logger provided by
         * [miraclLogger(miraclLogger: MiraclLogger)][miraclLogger]**
         */
        fun logLevel(logLevel: MiraclLogger.LogLevel) =
            apply { this.logLevel = logLevel }

        fun build(): Configuration = Configuration(this)
    }
}