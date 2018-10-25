package com.tlz.curveview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

/**
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 10:33.
 */
class TlzCurveView(ctx: Context, attrs: AttributeSet) : View(ctx, attrs) {

    /** 数据集. */
    private var dataset: Dataset<out Any>? = null
    /** X轴. */
    private var xaxis: Xaxis? = null
    /** Y轴. */
    private var yaxis: Yaxis? = null
    /** 曲线渲染器. */
    private var curveRender: CurveRender<out Any>? = null

    private val clearPaint = Paint()

    private val calcRect = Rect()

    init {
        clearPaint.style = Paint.Style.FILL
        clearPaint.color = Color.TRANSPARENT
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    /**
     * 初始化.
     * @param xaxis Xaxis?
     * @param yaxis Yaxis?
     * @param curveRender CurveRender<T>?
     */
    fun <T : Any> setup(xaxis: Xaxis? = null, yaxis: Yaxis? = null, curveRender: CurveRender<T>? = null) {
        this.xaxis = xaxis?.also { it.setupCurveView(this) }
        this.yaxis = yaxis?.also { it.setupCurveView(this) }
        this.dataset = curveRender?.dataset?.also { it.setupCurveView(this) }
        this.curveRender = curveRender?.also { it.setupCurveView(this) }
        postInvalidate()
    }

    /**
     * 刷新.
     */
    internal fun refresh() {
        postInvalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return event?.let {
            this.curveRender?.onTouchEvent(it) ?: false || this.xaxis?.onTouchEvent(it) ?: false || this.yaxis?.onTouchEvent(it) ?: false
        } ?: super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { cvs ->
            // 获取xy轴需要的宽高
            val xaxisHeight = this.xaxis?.getMeasureHeight() ?: 0
            val yaxisWidth = this.yaxis?.getMeasureWidth() ?: 0

            calcRect.left = paddingLeft
            calcRect.top = paddingTop
            calcRect.right = paddingLeft + yaxisWidth
            calcRect.bottom = height - paddingBottom - xaxisHeight
            this.yaxis?.onDraw(cvs, calcRect.left, calcRect.top, calcRect.right, calcRect.bottom)

            calcRect.left = calcRect.right
            calcRect.right = width - paddingRight
            calcRect.top = calcRect.bottom
            calcRect.bottom = height - paddingBottom - xaxisHeight
            this.xaxis?.onDraw(cvs, calcRect.left, calcRect.top, calcRect.right, calcRect.bottom)

            val layerId = cvs.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null, Canvas.ALL_SAVE_FLAG)
            calcRect.right = width - paddingRight
            calcRect.bottom = calcRect.top
            calcRect.top = paddingTop
            this.curveRender?.onDraw(cvs, calcRect.left, calcRect.top, calcRect.right, calcRect.bottom)

            // 清除超出曲线区域部分
            calcRect.left = 0
            calcRect.top = 0
            calcRect.right = paddingLeft + yaxisWidth
            calcRect.bottom = height - paddingBottom - xaxisHeight
            canvas.drawRect(calcRect, clearPaint)

            canvas.restoreToCount(layerId)
        }
    }

    fun <T> dataset() = dataset as? Dataset<T>
    fun <T> curveRender() = curveRender as? CurveRender<T>
    fun xaxis() = xaxis
    fun yaxis() = yaxis
}