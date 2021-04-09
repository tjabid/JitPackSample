package com.miracl.trust.util

@ExperimentalUnsignedTypes
internal fun ByteArray.toHexString(): String =
    toUByteArray().joinToString("") { it.toUInt().toString(16).padStart(2, '0') }


@ExperimentalUnsignedTypes
internal fun String.hexStringToByteArray(): ByteArray =
    this.chunked(2).map { it.toUInt(16).toByte() }.toByteArray()
