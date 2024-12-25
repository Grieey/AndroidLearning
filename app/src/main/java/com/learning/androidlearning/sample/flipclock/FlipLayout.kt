package com.learning.androidlearning.sample.flipclock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.Scroller
import android.widget.TextView
import com.learning.androidlearning.R
import java.util.Calendar

class FlipLayout : FrameLayout {

    private lateinit var mVisibleTextView: TextView
    private lateinit var mInvisibleTextView: TextView

    private var layoutWidth: Int = 0
    private var layoutHeight: Int = 0
    private lateinit var mScroller: Scroller
    private val tag = "FlipLayout"
    private var timetag: String = ""
    private val mCamera = Camera()
    private val mMatrix = Matrix()
    private val mTopRect = Rect()
    private val mBottomRect = Rect()
    private var isUp = true
    private val mminutenePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val mShadePaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private var isFlipping = false

    private var maxNumber: Int = 0
    private var flipTimes = 0
    private var timesCount = 0

    private var mFlipOverListener: FlipOverListener? = null

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.FlipLayout)
        val resId = array.getResourceId(R.styleable.FlipLayout_flipTextBackground, -1)
        val color = if (resId == -1) {
            array.getColor(R.styleable.FlipLayout_flipTextBackground, Color.WHITE)
        } else {
            Color.WHITE
        }
        var size = array.getDimension(R.styleable.FlipLayout_flipTextSize, 36f)
        size = px2dip(context, size)
        val textColor = array.getColor(R.styleable.FlipLayout_flipTextColor, Color.BLACK)
        array.recycle()
        init(context, resId, color, size, textColor)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @SuppressLint("SetTextI18n")
    private fun init(context: Context, resId: Int, color: Int, size: Float, textColor: Int) {
        mScroller = Scroller(context, DecelerateInterpolator())
        val tf = Typeface.createFromAsset(context.assets, "fonts/Aura.otf")

        mInvisibleTextView = TextView(context).apply {
            textSize = size
            text = "00"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(textColor)
            typeface = tf
            if (resId == -1) {
                setBackgroundColor(color)
            } else {
                setBackgroundResource(resId)
            }
        }
        addView(mInvisibleTextView)

        mVisibleTextView = TextView(context).apply {
            textSize = size
            text = "00"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextColor(textColor)
            typeface = tf
            if (resId == -1) {
                setBackgroundColor(color)
            } else {
                setBackgroundResource(resId)
            }
        }
        addView(mVisibleTextView)
    }

    companion object {
        fun px2dip(context: Context, pxValue: Float): Float {
            val scale = context.resources.displayMetrics.density
            return pxValue / scale + 0.5f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        layoutWidth = MeasureSpec.getSize(widthMeasureSpec)
        layoutHeight = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(layoutWidth, layoutHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        for (i in 0 until childCount) {
            getChildAt(i).layout(0, 0, layoutWidth, layoutHeight)
        }

        mTopRect.apply {
            this.top = 0
            this.left = 0
            this.right = width
            this.bottom = height / 2
        }

        mBottomRect.apply {
            this.top = height / 2
            this.left = 0
            this.right = width
            this.bottom = height
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (!mScroller.isFinished && mScroller.computeScrollOffset()) {
            drawTopHalf(canvas)
            drawBottomHalf(canvas)
            drawFlipHalf(canvas)
            postInvalidate()
        } else {
            if (isFlipping) {
                showViews(canvas)
            }

            if (mScroller.isFinished && !mScroller.computeScrollOffset()) {
                isFlipping = false
            }

            if (timesCount < flipTimes) {
                timesCount += 1
                initTextView()
                isFlipping = true
                mScroller.startScroll(0, 0, 0, layoutHeight, getAnimDuration(flipTimes - timesCount))
                postInvalidate()
            } else {
                timesCount = 0
                flipTimes = 0
                if (mFlipOverListener != null && !isFlipping()) {
                    mFlipOverListener?.onFLipOver(this)
                }
            }
        }
    }

    private fun showViews(canvas: Canvas) {
        var current = mVisibleTextView.text.toString()
        if (current.length < 2) {
            current = "0$current"
        }
        var past = mInvisibleTextView.text.toString()
        if (past.length < 2) {
            past = "0$past"
        }

        mVisibleTextView.text = past
        mInvisibleTextView.text = current
        drawChild(canvas, mVisibleTextView, 0)
    }

    private fun drawBottomHalf(canvas: Canvas) {
        canvas.save()
        canvas.clipRect(mBottomRect)
        val drawView = if (isUp) mInvisibleTextView else mVisibleTextView
        drawChild(canvas, drawView, 0)
        canvas.restore()
    }

    private fun drawTopHalf(canvas: Canvas) {
        canvas.save()
        canvas.clipRect(mTopRect)
        val drawView = if (isUp) mVisibleTextView else mInvisibleTextView
        drawChild(canvas, drawView, 0)
        canvas.restore()
    }

    private fun drawFlipHalf(canvas: Canvas) {
        canvas.save()
        mCamera.save()

        val deg = getDeg()
        val view: View?
        if (deg > 90) {
            canvas.clipRect(if (isUp) mTopRect else mBottomRect)
            mCamera.rotateX(if (isUp) deg - 180 else -(deg - 180))
            view = mInvisibleTextView
        } else {
            canvas.clipRect(if (isUp) mBottomRect else mTopRect)
            mCamera.rotateX(if (isUp) deg else -deg)
            view = mVisibleTextView
        }

        mCamera.getMatrix(mMatrix)
        positionMatrix()
        canvas.concat(mMatrix)

        drawChild(canvas, view, 0)
        drawFlippingShademinutene(canvas)

        mCamera.restore()
        canvas.restore()
    }

    private fun getDeg(): Float = mScroller.currY * 180f / layoutHeight

    private fun drawFlippingShademinutene(canvas: Canvas) {
        val degreesFlipped = getDeg()
        Log.d(tag, "deg: $degreesFlipped")
        if (degreesFlipped < 90) {
            val alpha = getAlpha(degreesFlipped)
            Log.d(tag, "小于90度时的透明度-------------------> $alpha")
            mminutenePaint.alpha = alpha
            mShadePaint.alpha = alpha
            canvas.drawRect(if (isUp) mBottomRect else mTopRect, if (isUp) mminutenePaint else mShadePaint)
        } else {
            val alpha = getAlpha(Math.abs(degreesFlipped - 180))
            Log.d(tag, "大于90度时的透明度-------------> $alpha")
            mShadePaint.alpha = alpha
            mminutenePaint.alpha = alpha
            canvas.drawRect(if (isUp) mTopRect else mBottomRect, if (isUp) mShadePaint else mminutenePaint)
        }
    }

    private fun getAlpha(degreesFlipped: Float): Int = (degreesFlipped / 90f * 100).toInt()

    private fun positionMatrix() {
        mMatrix.apply {
            preScale(0.25f, 0.25f)
            postScale(4.0f, 4.0f)
            preTranslate(-width / 2f, -height / 2f)
            postTranslate(width / 2f, height / 2f)
        }
    }

    private fun initTextView() {
        val visibleValue = getTime()
        var invisibleValue = if (isUp) visibleValue - 1 else visibleValue

        if (invisibleValue < 0) {
            invisibleValue += maxNumber
        }

        if (invisibleValue >= maxNumber) {
            invisibleValue -= maxNumber
        }

        var value = invisibleValue.toString()
        if (value.length < 2) {
            value = "0$value"
        }
        mInvisibleTextView.text = value
    }

    private fun getAnimDuration(times: Int): Int {
        val actualTimes = if (times <= 0) 1 else times
        return 500 - (500 - 100) / 9 * actualTimes
    }

    interface FlipOverListener {
        fun onFLipOver(flipLayout: FlipLayout)
    }

    fun smoothFlip(value: Int, maxnumber: Int, timeTAG: String, isMinus: Boolean) {
        timetag = timeTAG
        maxNumber = maxnumber
        if (value <= 0) {
            mFlipOverListener?.onFLipOver(this)
            return
        }
        flipTimes = value
        isUp = isMinus

        initTextView()
        isFlipping = true
        mScroller.startScroll(0, 0, 0, layoutHeight, getAnimDuration(flipTimes - timesCount))
        timesCount = 1
        postInvalidate()
    }

    fun flip(value: Int, maxnumber: Int, timeTAG: String) {
        timetag = timeTAG
        maxNumber = maxnumber
        var text = value.toString()
        if (text.length < 2) {
            text = "0$text"
        }
        mVisibleTextView.text = text
    }

    fun addFlipOverListener(flipOverListener: FlipOverListener) {
        this.mFlipOverListener = flipOverListener
    }

    fun isFlipping(): Boolean = isFlipping && !mScroller.isFinished && mScroller.computeScrollOffset()

    fun getCurrentValue(): Int = mVisibleTextView.text.toString().toInt()

    private fun getTime(): Int {
        val now = Calendar.getInstance()
        return when (timetag) {
            "SECOND" -> now[Calendar.SECOND]
            "MINUTE" -> now[Calendar.MINUTE]
            "HOUR" -> now[Calendar.HOUR_OF_DAY]
            else -> 0
        }
    }

    fun setFlipTextBackground(resId: Int) {
        for (i in 0 until childCount) {
            getChildAt(i)?.setBackgroundResource(resId)
        }
    }

    fun setFLipTextSize(size: Float) {
        for (i in 0 until childCount) {
            (getChildAt(i) as? TextView)?.textSize = size
        }
    }

    fun setFLipTextColor(color: Int) {
        for (i in 0 until childCount) {
            (getChildAt(i) as? TextView)?.setTextColor(color)
        }
    }
}