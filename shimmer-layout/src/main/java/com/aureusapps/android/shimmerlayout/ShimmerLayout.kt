package com.aureusapps.android.shimmerlayout

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.tan

class ShimmerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs), ValueAnimator.AnimatorUpdateListener {

    private val baseColor: Int
    private val highlightColor: Int
    private val tilt: Double

    private val shaderPaint: Paint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private val shimmerColors: IntArray
    private val colorPositions: FloatArray

    private val valueAnimator: ValueAnimator
    private val shaderMatrix: Matrix = Matrix()

    @Suppress("MemberVisibilityCanBePrivate")
    var isShimmerEnabled: Boolean = false
        private set

    @Suppress("unused")
    val isShimmerPlaying: Boolean get() = !valueAnimator.isPaused

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ShimmerLayout).apply {
            baseColor = getColor(R.styleable.ShimmerLayout_shimmerBaseColor, 0x4cffffff)
            highlightColor = getColor(R.styleable.ShimmerLayout_shimmerHighlightColor, Color.WHITE)
            tilt = getFloat(R.styleable.ShimmerLayout_shimmerTilt, 45f).toDouble()
            recycle()
        }
        shimmerColors = intArrayOf(baseColor, highlightColor, highlightColor, baseColor)
        colorPositions = floatArrayOf(0f, 0.45f, 0.55f, 1f)
        valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.addUpdateListener(this)
        valueAnimator.duration = 1000
        valueAnimator.repeatMode = ValueAnimator.RESTART
        valueAnimator.repeatCount = ValueAnimator.INFINITE
        setLayerType(LAYER_TYPE_HARDWARE, Paint())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createShader(width)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (isShimmerEnabled) {
            val tiltTan = tan(Math.toRadians(tilt)).toFloat()
            val translateWidth: Float = width + tiltTan * height
            val animatedValue = valueAnimator.animatedValue as Float
            val dx = offset(-translateWidth, translateWidth, animatedValue)
            val dy = 0f
            shaderMatrix.reset()
            shaderMatrix.setRotate(tilt.toFloat(), width / 2f, height / 2f)
            shaderMatrix.postTranslate(dx, dy)
            shaderPaint.shader.setLocalMatrix(shaderMatrix)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shaderPaint)
        }
    }

    fun startShimmer() {
        valueAnimator.start()
        isShimmerEnabled = true
    }

    fun pauseShimmer() {
        valueAnimator.pause()
    }

    fun resumeShimmer() {
        valueAnimator.resume()
    }

    fun stopShimmer() {
        valueAnimator.end()
        isShimmerEnabled = false
    }

    private fun createShader(width: Int) {
        shaderPaint.shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            0f,
            shimmerColors,
            colorPositions,
            Shader.TileMode.CLAMP
        )
    }

    override fun onAnimationUpdate(animation: ValueAnimator) {
        invalidate()
    }

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

}