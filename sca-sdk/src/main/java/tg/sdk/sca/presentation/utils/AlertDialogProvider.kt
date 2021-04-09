package tg.sdk.sca.presentation.utils

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import tg.sdk.sca.presentation.utils.extensions.UNDEFINED_INT

class AlertDialogProvider {

    fun <T> getDialogWithOneButton(
        fragment: T,
        @StringRes titleId: Int = UNDEFINED_INT,
        @StringRes messageId: Int = UNDEFINED_INT,
        @StringRes positiveButtonTextId: Int = UNDEFINED_INT,
        requestCode: Int = UNDEFINED_INT,
        cancelable: Boolean = true
    ) where T : AlertDialogClickListener, T : Fragment? =
        getDialog(
            fragment,
            titleId,
            messageId,
            positiveButtonTextId,
            UNDEFINED_INT,
            UNDEFINED_INT,
            requestCode,
            cancelable
        )

    fun <T> getDialogWithTwoButtons(
        fragment: T,
        @StringRes titleId: Int = UNDEFINED_INT,
        @StringRes messageId: Int = UNDEFINED_INT,
        @StringRes positiveButtonTextId: Int = UNDEFINED_INT,
        @StringRes negativeButtonTextId: Int = UNDEFINED_INT,
        requestCode: Int = UNDEFINED_INT,
        cancelable: Boolean = true
    ) where T : AlertDialogClickListener, T : Fragment? =
        getDialog(
            fragment,
            titleId,
            messageId,
            positiveButtonTextId,
            negativeButtonTextId,
            UNDEFINED_INT,
            requestCode,
            cancelable
        )

    fun <T> getDialogWithThreeButtons(
        fragment: T,
        @StringRes titleId: Int = UNDEFINED_INT,
        @StringRes messageId: Int = UNDEFINED_INT,
        @StringRes positiveButtonTextId: Int = UNDEFINED_INT,
        @StringRes negativeButtonTextId: Int = UNDEFINED_INT,
        @StringRes neutralButtonTextId: Int = UNDEFINED_INT,
        requestCode: Int = UNDEFINED_INT,
        cancelable: Boolean = true
    ) where T : AlertDialogClickListener, T : Fragment? =
        getDialog(
            fragment,
            titleId,
            messageId,
            positiveButtonTextId,
            negativeButtonTextId,
            neutralButtonTextId,
            requestCode,
            cancelable
        )

    private fun <T> getDialog(
        fragment: T,
        @StringRes titleId: Int = UNDEFINED_INT,
        @StringRes messageId: Int = UNDEFINED_INT,
        @StringRes positiveButtonTextId: Int = UNDEFINED_INT,
        @StringRes negativeButtonTextId: Int = UNDEFINED_INT,
        @StringRes neutralButtonTextId: Int = UNDEFINED_INT,
        requestCode: Int,
        cancelable: Boolean
    ) where T : AlertDialogClickListener, T : Fragment? =
        SimpleAlertDialogFragment.newInstance(
            titleId = titleId,
            messageId = messageId,
            positiveButtonTextId = positiveButtonTextId,
            negativeButtonTextId = negativeButtonTextId,
            neutralButtonTextId = neutralButtonTextId,
            requestCode = requestCode,
            isCancelable = cancelable
        ).apply {
            setTargetFragment(fragment, requestCode)
        }
}

interface AlertDialogClickListener {
    fun onPositiveClick(requestCode: Int) {}
    fun onNegativeClick(requestCode: Int) {}
    fun onNeutralClick(requestCode: Int) {}
}