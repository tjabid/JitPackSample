package com.miracl.trust.util.log

import timber.log.Timber

internal class DefaultMiraclLogger(private val logLevel: MiraclLogger.LogLevel) : MiraclLogger {
    override fun error(logTag: String, message: String) {
        if (logLevel >= MiraclLogger.LogLevel.ERROR) {
            Timber.tag(logTag).e(message)
        }
    }

    override fun info(logTag: String, message: String) {
        if (logLevel >= MiraclLogger.LogLevel.INFO) {
            Timber.tag(logTag).i(message)
        }
    }

    override fun debug(logTag: String, message: String) {
        if (logLevel >= MiraclLogger.LogLevel.DEBUG) {
            Timber.tag(logTag).d(message)
        }
    }
}