package com.miracl.trust.util.log

import com.miracl.trust.util.log.MiraclLogger.LogLevel
import com.miracl.trust.util.log.MiraclLogger.LogLevel.*

/**
 * ## A type representing message logger
 * Some important and useful information will be outputted through this interface
 * while a debug build.
 * >
 * By default this SDK uses a concrete implementation of this interface [DefaultMiraclLogger][com.miracl.trust.util.log.DefaultMiraclLogger].
 *
 * @see LogLevel
 */
interface MiraclLogger {

    /**
     * Controls which logs to be written to the console when using a debug build of the SDK.
     *
     * Available log levels are:
     * - *[NONE]* (default) - disables all output
     * - *[ERROR]* - enables only error logs
     * - *[INFO]* - enables error and info logs
     * - *[DEBUG]* - enables error, info and debug logs
     *
     * **Note:** All logs are disabled on release builds
     */
    enum class LogLevel {
        /**
         * **Default**
         *
         * Disables all output.
         */
        NONE,

        /**
         * Enables only error logs.
         */
        ERROR,

        /**
         * Enables error and info logs.
         */
        INFO,

        /**
         * Enables error, info and debug logs.
         */
        DEBUG
    }

    /**
     * Writes an [ERROR] level log using the provided implementation.
     */
    fun error(logTag: String, message: String)

    /**
     * Writes an [INFO] level log using the provided implementation.
     */
    fun info(logTag: String, message: String)

    /**
     * Writes a [DEBUG] level log using the provided implementation.
     */
    fun debug(logTag: String, message: String)
}

