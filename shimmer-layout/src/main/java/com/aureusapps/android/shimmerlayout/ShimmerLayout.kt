package com.aureusapps.android.shimmerlayout

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import kotlin.math.tan

open class ShimmerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val shimmerBaseColor: Int
    private val shimmerHighlightColor: Int
    private val shimmerTilt: Double
    private var shimmerEnabled: Boolean

    private val shaderPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val shimmerColors: IntArray
    private val shimmerColorPositions: FloatArray = floatArrayOf(0f, 0.33f, 0.66f, 1f)
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

    private var hardPaused: Boolean = false

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ShimmerLayout).apply {
            shimmerBaseColor = getColor(R.styleable.ShimmerLayout_shimmerBaseColor, Color.LTGRAY)
            shimmerHighlightColor =
                getColor(R.styleable.ShimmerLayout_shimmerHighlightColor, Color.BLACK)
            shimmerTilt = getFloat(R.styleable.ShimmerLayout_shimmerTilt, 45f).toDouble()
            shimmerEnabled = getBoolean(R.styleable.ShimmerLayout_shimmerEnabled, true)
            shimmerColorPositions[1] =
                getFloat(R.styleable.ShimmerLayout_shimmerGradientStart, 0.33f)
            shimmerColorPositions[2] = getFloat(R.styleable.ShimmerLayout_shimmerGradientEnd, 0.66f)
            shaderPaint.xfermode = PorterDuffXfermode(getXfermode())
            recycle()
        }
        shimmerColors = intArrayOf(
            shimmerBaseColor, shimmerHighlightColor, shimmerHighlightColor, shimmerBaseColor
        )
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f)
        shimmerAnimator.addUpdateListener(animUpdateListener)
        shimmerAnimator.duration = 1000
        shimmerAnimator.repeatMode = ValueAnimator.RESTART
        shimmerAnimator.repeatCount = ValueAnimator.INFINITE
        setLayerType(LAYER_TYPE_HARDWARE, Paint())
        if (shimmerEnabled) {
            shimmerAnimator.start()
        }
        addOnAttachStateChangeListener(
            object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    if (shimmerAnimator.isPaused && !hardPaused) {
                        shimmerAnimator.resume()
                    }
                }

                override fun onViewDetachedFromWindow(v: View) {
                    if (shimmerAnimator.isRunning) {
                        shimmerAnimator.pause()
                    }
                }
            }
        )
    }

    fun addShimmerStateListener(listener: ShimmerStateListener) {
        stateListeners.add(listener)
    }

    fun removeShimmerStateListener(listener: ShimmerStateListener) {
        stateListeners.remove(listener)
    }

    fun startShimmer() {
        if (shimmerAnimator.isRunning) return
        shimmerAnimator.start()
        shimmerEnabled = true
        notifyShimmerListeners(ShimmerState.STARTED)
    }

    fun pauseShimmer() {
        if (shimmerAnimator.isPaused) return
        shimmerAnimator.pause()
        notifyShimmerListeners(ShimmerState.PAUSED)
        hardPaused = true
    }

    fun resumeShimmer() {
        if (!shimmerAnimator.isPaused) return
        shimmerAnimator.resume()
        notifyShimmerListeners(ShimmerState.RESUMED)
        hardPaused = false
    }

    fun stopShimmer() {
        if (!shimmerAnimator.isRunning) return
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
        shaderPaint.shader = createShader(width)
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

    private fun createShader(width: Int): Shader {
        return LinearGradient(
            0f, 0f, width.toFloat(), 0f, shimmerColors, shimmerColorPositions, Shader.TileMode.CLAMP
        )
    }

    private fun offset(start: Float, end: Float, percent: Float): Float {
        return start + (end - start) * percent
    }

    private fun TypedArray.getXfermode(): PorterDuff.Mode {
        return when (getInt(R.styleable.ShimmerLayout_shimmerXfermode, 5)) {
            0 -> PorterDuff.Mode.CLEAR
            1 -> PorterDuff.Mode.SRC
            2 -> PorterDuff.Mode.DST
            3 -> PorterDuff.Mode.SRC_OVER
            4 -> PorterDuff.Mode.DST_OVER
            5 -> PorterDuff.Mode.SRC_IN
            6 -> PorterDuff.Mode.DST_IN
            7 -> PorterDuff.Mode.SRC_OUT
            8 -> PorterDuff.Mode.DST_OUT
            9 -> PorterDuff.Mode.SRC_ATOP
            10 -> PorterDuff.Mode.DST_ATOP
            11 -> PorterDuff.Mode.XOR
            12 -> PorterDuff.Mode.ADD
            13 -> PorterDuff.Mode.MULTIPLY
            14 -> PorterDuff.Mode.SCREEN
            15 -> PorterDuff.Mode.OVERLAY
            16 -> PorterDuff.Mode.DARKEN
            else -> PorterDuff.Mode.LIGHTEN
        }
    }

}