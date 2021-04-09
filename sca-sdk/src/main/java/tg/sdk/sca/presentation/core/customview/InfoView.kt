package tg.sdk.sca.presentation.core.customview

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.Px
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.isVisible
import tg.sdk.sca.R
import tg.sdk.sca.presentation.utils.dpToPx

private const val LAYOUT_APPEARANCE_TIME = 3000L
private const val ANIMATION_DURATION = 250L
private const val MIN_PROGRESS = 0f
private const val MAX_PROGRESS = 1f

class InfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    @Px
    private val halfPadding = dpToPx(context, 8f)

    @Px
    private val padding = dpToPx(context, 16f)

    private val animator = ValueAnimator.ofFloat(MIN_PROGRESS, MAX_PROGRESS).apply {
        addUpdateListener {
            val value = it.animatedValue as Float
            findViewById<CircleProgressView>(R.id.progressView).setProgress(value)
            if (value == MAX_PROGRESS) hideWithAnimation()
        }
        duration = LAYOUT_APPEARANCE_TIME
    }

    init {
        View.inflate(context, R.layout.layout_info_view, this)
        setupView()
    }

    private fun setupView() {
        layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setPadding(halfPadding, halfPadding, halfPadding, padding)
            }
        clipToPadding = false
        alpha = 0f
        isVisible = false
        findViewById<AppCompatImageButton>(R.id.closeBtn)?.setOnClickListener {
            hideWithAnimation()
        }
    }

    fun showSuccessMessage(title: CharSequence, subtitle: CharSequence? = null) =
        show(title, subtitle, true)

    fun showErrorMessage(title: CharSequence, subtitle: CharSequence? = null) =
        show(title, subtitle, false)

    private fun show(title: CharSequence, subtitle: CharSequence?, isSuccess: Boolean) {
        if (alpha == 0f) {
            findViewById<AppCompatImageView>(R.id.infoViewIv).setBackgroundResource(if (isSuccess) R.drawable.ic_tick_teal else R.drawable.ic_cross_red)
            findViewById<AppCompatTextView>(R.id.titleTv).text = title
            findViewById<AppCompatTextView>(R.id.messageTv)?.apply {
                visibility = if (subtitle.isNullOrEmpty()) GONE else VISIBLE
                text = subtitle
            }
            animate()
                .withStartAction {
                    isVisible = true
                    animator.start()
                }
                .alpha(1f)
                .duration = ANIMATION_DURATION
        }
    }

    private fun hideWithAnimation() {
        animate()
            .withEndAction {
                isVisible = false
                animator.cancel()
            }
            .alpha(0f)
            .duration = ANIMATION_DURATION
    }
}