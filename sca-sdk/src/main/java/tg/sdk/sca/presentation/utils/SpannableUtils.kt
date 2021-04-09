package tg.sdk.sca.presentation.utils

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import tg.sdk.sca.R
import tg.sdk.sca.presentation.utils.extensions.UNDEFINED_INT

fun CharSequence.applyClickSpan(
    givenString: String?,
    action: () -> Unit,
    source: SpannableString = SpannableString(this)
): CharSequence {
    givenString?.let {
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                action()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = true
            }
        }

        val startIndex = source.indexOf(givenString, ignoreCase = true)
        val endIndex = startIndex + givenString.length
        source.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return source
}

fun CharSequence.applyFontSpan(
    context: Context,
    @FontRes font: Int,
    givenString: String?,
    source: SpannableString = SpannableString(this)
): CharSequence {
    givenString?.let {
        val typeface = ResourcesCompat.getFont(context, font)
        val startIndex = source.indexOf(givenString, ignoreCase = true)
        val endIndex = startIndex + givenString.length
        source.setSpan(
            CustomTypefaceSpan(typeface),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return source
}

fun CharSequence.applyColorSpan(
    context: Context,
    @ColorRes color: Int,
    givenString: String?,
    source: SpannableString = SpannableString(this)
): CharSequence {
    givenString?.let {
        val startIndex = source.indexOf(givenString, ignoreCase = true)
        val endIndex = startIndex + givenString.length
        source.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, color)),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return source
}

fun CharSequence.applySelectedTextSpan(
    textToSelect: String,
    context: Context,
    @FontRes fontId: Int = R.font.sca_bold
): CharSequence {
    if (textToSelect.isEmpty()) {
        return this
    }
    val subSearchQuery = textToSelect.split(" ").toTypedArray()
    val spannableText = SpannableString(this)
    var nStartPosition = UNDEFINED_INT
    var nEndPosition: Int
    for (subQuery in subSearchQuery) {
        nStartPosition = this.indexOf(subQuery, nStartPosition + 1, true)
        if (nStartPosition != -1) {
            nEndPosition = nStartPosition + subQuery.length
            val typeface = ResourcesCompat.getFont(context, fontId)
            typeface?.let {
                spannableText.setSpan(
                    CustomTypefaceSpan(it),
                    nStartPosition,
                    nEndPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }
    }
    return spannableText
}

class CustomTypefaceSpan(private val newType: Typeface?) : TypefaceSpan("") {

    override fun updateDrawState(ds: TextPaint) {
        applyCustomTypeFace(ds, newType)
    }

    override fun updateMeasureState(paint: TextPaint) {
        applyCustomTypeFace(paint, newType)
    }

    private fun applyCustomTypeFace(paint: Paint, tf: Typeface?) {
        tf?.let {
            val oldStyle: Int
            val old = paint.typeface
            oldStyle = old?.style ?: 0

            val fake = oldStyle and tf.style.inv()
            if (fake and Typeface.BOLD != 0) {
                paint.isFakeBoldText = true
            }

            if (fake and Typeface.ITALIC != 0) {
                paint.textSkewX = -0.25f
            }

            paint.typeface = tf
        }
    }
}