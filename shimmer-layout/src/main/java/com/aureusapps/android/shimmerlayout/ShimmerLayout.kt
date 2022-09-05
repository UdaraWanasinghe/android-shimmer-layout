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
    private val colorPositions: FloatArray
    private val shimmerAnimator: ValueAnimator
    private val shaderMatrix = Matrix()
    private val stateListeners = mutableListOf<ShimmerStateListener>()
    private val animUpdateListener = ValueAnimator.AnimatorUpdateListener {
        invalidate()
    }

    @Suppress("unused")
    val isShimmerEnabled: Boolean
        get() = shimmerEnabled

    @Suppress("unused")
    val isShimmerPlaying: Boolean
        get() = !shimmerAnimator.isPaused
    
    init {
        context.obtainStyledAttributes(attrs, R.styleable.ShimmerLayout).apply {
            shimmerBaseColor = getColor(R.styleable.ShimmerLayout_shimmerBaseColor, 0x4cffffff)
            shimmerHighlightColor = getColor(R.styleable.ShimmerLayout_shimmerHighlightColor, Color.WHITE)
            shimmerTilt = getFloat(R.styleable.ShimmerLayout_shimmerTilt, 45f).toDouble()
            shimmerEnabled = getBoolean(R.styleable.ShimmerLayout_shimmerEnabled, true)
            recycle()
        }
        shimmerColors = intArrayOf(shimmerBaseColor, shimmerHighlightColor, shimmerHighlightColor, shimmerBaseColor)
        colorPositions = floatArrayOf(0f, 0.45f, 0.55f, 1f)
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

    @Suppress("unused")
    fun addShimmerStateListener(listener: ShimmerStateListener) {
        stateListeners.add(listener)
    }

    @Suppress("unused")
    fun removeShimmerStateListener(listener: ShimmerStateListener) {
        stateListeners.remove(listener)
    }

    @Suppress("unused")
    fun startShimmer() {
        shimmerAnimator.start()
        shimmerEnabled = true
        notifyShimmerListeners(ShimmerState.STARTED)
    }

    @Suppress("unused")
    fun pauseShimmer() {
        shimmerAnimator.pause()
        notifyShimmerListeners(ShimmerState.PAUSED)
    }

    @Suppress("unused")
    fun resumeShimmer() {
        shimmerAnimator.resume()
        notifyShimmerListeners(ShimmerState.RESUMED)
    }

    @Suppress("unused")
    fun stopShimmer() {
        shimmerAnimator.end()
        shimmerEnabled = false
        notifyShimmerListeners(ShimmerState.STOPPED)
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
            colorPositions,
            Shader.TileMode.CLAMP
        )
    }

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

}