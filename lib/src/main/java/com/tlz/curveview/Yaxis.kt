package com.tlz.curveview

import android.support.annotation.CallSuper
import android.view.MotionEvent

/**
 * Y轴.
 *
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 10:38.
 */
abstract class Yaxis : CurveView {

    internal lateinit var curveView: TlzCurveView

    open var items: Array<Any> = arrayOf()

    @CallSuper
    override fun setupCurveView(view: TlzCurveView) {
        this.curveView = view
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = false

    /** 获取测量宽度. */
    internal abstract fun getMeasureWidth(): Int

}