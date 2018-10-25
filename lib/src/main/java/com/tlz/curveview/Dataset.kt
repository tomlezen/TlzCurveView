package com.tlz.curveview

import android.graphics.Canvas
import android.view.MotionEvent

/**
 * 数据集.
 *
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 10:43.
 */
abstract class Dataset<T> : CurveView {

    internal lateinit var curveView: TlzCurveView

    final override fun setupCurveView(view: TlzCurveView) {
        this.curveView = view
    }

    /**
     * 通知数据更改.
     */
    fun notifyDatasetChange() {
        this.curveView.postInvalidate()
    }

    /** 获取本次需要绘制的数据. */
    abstract fun getDrawnData(): List<T>

    /** 追加数据. */
    abstract fun appendData(data: T)
    abstract fun appendData(data: List<T>)

    /** 设置数据. */
    abstract fun setData(vararg data: T)
    abstract fun setData(data: List<T>)

    final override fun onTouchEvent(event: MotionEvent): Boolean = false

    final override fun onDraw(cvs: Canvas, left: Int, top: Int, right: Int, bottom: Int) {}

}