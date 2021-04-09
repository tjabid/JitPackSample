package tg.sdk.sca.presentation.core.customview

import android.content.Context
import android.content.res.Resources
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import tg.sdk.sca.R
import timber.log.Timber
import kotlin.properties.Delegates

private const val MIN_PROGRESS = 0f
private const val MAX_PROGRESS = 1f
private const val MAX_ANGLE = 360f
private const val START_BACKGROUND_ANGLE = 0f
private const val START_PROGRESS_ANGLE = 270f
private const val DEFAULT_PROGRESS_WIDTH = 4f

class CircleProgressView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    View(context, attrs, defStyleAttr) {

    @ColorInt
    private var bgProgressColor: Int = ContextCompat.getColor(context, R.color.white)

    @ColorInt
    private var progressColor: Int = ContextCompat.getColor(context, R.color.dark_blue)

    private val progressPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val backgroundPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private val rectF = RectF()
    private var progressWidth by Delegates.notNull<Float>()
    private var progress = MIN_PROGRESS

    init {
        readAttrs(attrs)
    }

    private fun readAttrs(attrs: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressView, 0, 0)
        with(typedArray) {
            progressWidth = getDimension(
                R.styleable.CircleProgressView_cpv_progress_width,
                DEFAULT_PROGRESS_WIDTH
            )
            bgProgressColor =
                getColor(R.styleable.CircleProgressView_cpv_progress_bg_color, bgProgressColor)
            progressColor =
                getColor(R.styleable.CircleProgressView_cpv_progress_main_color, progressColor)
        }
        setupView()
        typedArray.recycle()
    }

    private fun setupView() {
        backgroundPaint.apply {
            strokeWidth = progressWidth
            color = bgProgressColor
        }
        progressPaint.apply {
            strokeWidth = progressWidth
            color = progressColor
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawArc(rectF, START_BACKGROUND_ANGLE, MAX_ANGLE, false, backgroundPaint)
        val progress = progress * MAX_ANGLE / MAX_PROGRESS
        canvas.drawArc(rectF, START_PROGRESS_ANGLE - progress, progress, false, progressPaint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val halfProgressWidth = progressWidth / 2
        rectF.set(
            paddingLeft.toFloat() + halfProgressWidth,
            paddingTop.toFloat() + halfProgressWidth,
            w - paddingRight - halfProgressWidth,
            h - paddingBottom - halfProgressWidth
        )
        invalidate()
    }

    fun setProgressColor(hexColor: String) {
        try {
            progressPaint.color = Color.parseColor(hexColor)
            invalidate()
        } catch (e: IllegalArgumentException) {
            Timber.e(e)
        }
    }

    fun setProgressColor(@ColorRes colorRes: Int) {
        try {
            progressPaint.color = ContextCompat.getColor(context, colorRes)
            invalidate()
        } catch (e: Resources.NotFoundException) {
            Timber.e(e)
        }
    }

    fun setBackgroundProgressColor(@ColorRes colorRes: Int) {
        try {
            backgroundPaint.color = ContextCompat.getColor(context, colorRes)
            invalidate()
        } catch (e: Resources.NotFoundException) {
            Timber.e(e)
        }
    }

    fun setProgress(progress: Float) {
        this.progress =
            when {
                progress > MAX_PROGRESS -> MAX_PROGRESS
                progress < MIN_PROGRESS -> MIN_PROGRESS
                else -> progress
            }
        invalidate()
    }
}