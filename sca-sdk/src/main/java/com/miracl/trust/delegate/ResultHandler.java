package com.miracl.trust.delegate;

import androidx.annotation.NonNull;

import com.miracl.trust.MiraclResult;

/**
 * An interface used to connect MIRACLTrust SDK output to your application.
 * The result could be either MiraclSuccess or MiraclError.
 *
 * @param <SUCCESS> type of the value on success.
 * @param <FAIL>    type of the value on failure.
 */
public interface ResultHandler<SUCCESS, FAIL> {
    void onResult(@NonNull MiraclResult<SUCCESS, FAIL> result);
}