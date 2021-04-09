package tg.sdk.sca.presentation.utils.extensions.ui

import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import tg.sdk.sca.R

fun TextView.setTextColorId(@ColorRes colorIdRes: Int) {
    setTextColor(ContextCompat.getColor(context, colorIdRes))
}

fun TextView.addInitials(input: String) {
    val name = input.trim()
    val index = name.indexOf(" ")
    text = if (index > 0 && index + 2 <= name.length) {
        context.getString(R.string.name_initials, name.subSequence(0, 1), name.subSequence(index + 1, index + 2))
    } else {
        name.subSequence(0, 1)
    }
}