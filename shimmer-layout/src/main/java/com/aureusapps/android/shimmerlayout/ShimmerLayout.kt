package com.aureusapps.android.shimmerlayout

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.tan

class ShimmerLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private val shimmerBaseColor: Int
    private val shimmerHighlightColor: Int
    private val shimmerTilt: Double
    private var shimmerEnabled: Boolean

    private val shaderPaint = Paint().apply {
        isAntiAlias = true
        xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
    }
    private val shimmerColors: IntArray
    private val shimmerColorPositions: FloatArray = floatArrayOf(0f, 0.45f, 0.55f, 1f)
    private val shimmerAnimator: ValueAnimator
    private val shaderMatrix = Matrix()
    private val stateListeners = mutableListOf<ShimmerStateListener>()
    private val animUpdateListener = ValueAnimator.AnimatorUpdateListener {
        invalidate()
    }

    val isShimmerEnabled get() = shimmerEnabled

    val isShimmerPlaying get() = !shimmerAnimator.isPaused

    val shimmerGradientStart get() = shimmerColorPositions[1]

    val shimmerGradientEnd get() = shimmerColorPositions[2]

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ShimmerLayout).apply {
            shimmerBaseColor = getColor(R.styleable.ShimmerLayout_shimmerBaseColor, 0x4cffffff)
            shimmerHighlightColor = getColor(R.styleable.ShimmerLayout_shimmerHighlightColor, Color.WHITE)
            shimmerTilt = getFloat(R.styleable.ShimmerLayout_shimmerTilt, 45f).toDouble()
            shimmerEnabled = getBoolean(R.styleable.ShimmerLayout_shimmerEnabled, true)
            shimmerColorPositions[1] = getFloat(R.styleable.ShimmerLayout_shimmerGradientStart, 0.45f)
            shimmerColorPositions[2] = getFloat(R.styleable.ShimmerLayout_shimmerGradientEnd, 0.55f)
            recycle()
        }
        shimmerColors = intArrayOf(shimmerBaseColor, shimmerHighlightColor, shimmerHighlightColor, shimmerBaseColor)
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f)
        shimmerAnimator.addUpdateListener(animUpdateListener)
        shimmerAnimator.duration = 1000
        shimmerAnimator.repeatMode = ValueAnimator.RESTART
        shimmerAnimator.repeatCount = ValueAnimator.INFINITE
        setLayerType(LAYER_TYPE_HARDWARE, Paint())
        if (shimmerEnabled) {
            shimmerAnimator.start()
        }
    }

    fun addShimmerStateListener(listener: ShimmerStateListener) {
        stateListeners.add(listener)
    }

    fun removeShimmerStateListener(listener: ShimmerStateListener) {
        stateListeners.remove(listener)
    }

    fun startShimmer() {
        shimmerAnimator.start()
        shimmerEnabled = true
        notifyShimmerListeners(ShimmerState.STARTED)
    }

    fun pauseShimmer() {
        shimmerAnimator.pause()
        notifyShimmerListeners(ShimmerState.PAUSED)
    }

    fun resumeShimmer() {
        shimmerAnimator.resume()
        notifyShimmerListeners(ShimmerState.RESUMED)
    }

    fun stopShimmer() {
        shimmerAnimator.end()
        shimmerEnabled = false
        notifyShimmerListeners(ShimmerState.STOPPED)
    }

    fun setShimmerGradientStart(start: Float) {
        shimmerColorPositions[1] = start
        invalidate()
    }

    fun setShimmerGradientEnd(end: Float) {
        shimmerColorPositions[2] = end
        invalidate()
    }

    fun setShimmerGradientPositions(start: Float, end: Float) {
        shimmerColorPositions[1] = start
        shimmerColorPositions[2] = end
        invalidate()
    }

    fun setShaderXfermode(xfermode: PorterDuffXfermode) {
        shaderPaint.xfermode = xfermode
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        createShader(width)
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (shimmerEnabled) {
            val tiltTan = tan(Math.toRadians(shimmerTilt)).toFloat()
            val translateWidth: Float = width + tiltTan * height
            val animatedValue = shimmerAnimator.animatedValue as Float
            val dx = offset(-translateWidth, translateWidth, animatedValue)
            val dy = 0f
            shaderMatrix.reset()
            shaderMatrix.setRotate(shimmerTilt.toFloat(), width / 2f, height / 2f)
            shaderMatrix.postTranslate(dx, dy)
            shaderPaint.shader.setLocalMatrix(shaderMatrix)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shaderPaint)
        }
    }

    private fun notifyShimmerListeners(state: ShimmerState) {
        stateListeners.forEach { it.onStateChanged(state) }
    }

    private fun createShader(width: Int) {
        shaderPaint.shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            0f,
            shimmerColors,
            shimmerColorPositions,
            Shader.TileMode.CLAMP
        )
    }

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

}