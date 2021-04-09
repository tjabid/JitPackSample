package com.miracl.trust

/**
 * MiraclResult is a class representing the MIRACLTrust SDK responses.
 */
sealed class MiraclResult<SUCCESS, FAIL>

/**
 * MiraclSuccess<SUCCESS, FAIL> is a success response from the MIRACLTrust SDK.
 * It provides a value of type SUCCESS.
 */
data class MiraclSuccess<SUCCESS, FAIL>(val value: SUCCESS) : MiraclResult<SUCCESS, FAIL>()

/**
 * MiraclError<SUCCESS, FAIL> is an error response from the MIRACLTrust SDK.
 * It provides a value of type FAIL and an optional exception.
 */
data class MiraclError<SUCCESS, FAIL>(val value: FAIL, val exception: Exception? = null) :
    MiraclResult<SUCCESS, FAIL>()
