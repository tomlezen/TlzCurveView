package com.tlz.curveview.def

import android.graphics.Canvas
import com.tlz.curveview.Xaxis

/**
 * 默认X轴.
 * 无X轴.
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 14:25.
 */
class DefXaxis internal constructor() : Xaxis() {

  override fun getMeasureHeight(): Int = 0

  override fun onDraw(cvs: Canvas, left: Int, top: Int, right: Int, bottom: Int) {

  }

}