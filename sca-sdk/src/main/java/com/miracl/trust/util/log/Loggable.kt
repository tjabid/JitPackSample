package com.miracl.trust.util.log

import com.miracl.trust.MiraclTrust

internal interface Loggable {
    val miraclLogger: MiraclLogger?
        get() = MiraclTrust.miraclLogger
}