package com.tlz.curveview

import android.graphics.Canvas
import android.view.MotionEvent

/**
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 11:21.
 */
internal interface CurveView {

    fun setupCurveView(view: TlzCurveView)

    fun onTouchEvent(event: MotionEvent): Boolean

    fun onDraw(cvs: Canvas, left: Int, top: Int, right: Int, bottom: Int)

}