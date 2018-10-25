package com.tlz.curveview

import android.support.annotation.CallSuper
import android.view.MotionEvent

/**
 * X轴.
 *
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 10:37.
 */

abstract class Xaxis : CurveView {

    internal lateinit var curveView: TlzCurveView

    @CallSuper
    override fun setupCurveView(view: TlzCurveView) {
        this.curveView = view
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    /** 获取测量高度. */
    internal abstract fun getMeasureHeight(): Int
}