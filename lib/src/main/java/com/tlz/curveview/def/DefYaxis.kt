package com.tlz.curveview.def

import android.graphics.Canvas
import android.graphics.Paint
import com.tlz.curveview.Yaxis

/**
 * 默认Y轴.
 *
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 14:26.
 */
class DefYaxis internal constructor(builder: YaxisBuilder) : Yaxis() {

    /** 文字画笔. */
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** 已测量的宽度. */
    private var measureWidth = 0
    /** Y轴偏移. */
    private var textOffsetY = 0
    /** 是否需要重新计算. */
    private var isNeedCalculate = true

    /** 文字大小. */
    var textSize: Float = builder.textSize
        set(value) {
            field = value
            isNeedCalculate = true
            curveView.refresh()
        }
    /** 文字颜色. */
    var textColor = builder.textColor
        set(value) {
            field = value
            curveView.refresh()
        }
    /** 左边距. */
    var paddingLeft = builder.paddingLeft
        set(value) {
            field = value
            isNeedCalculate = true
            curveView.refresh()
        }
    /** 右边距. */
    var paddingRight = builder.paddingRight
        set(value) {
            field = value
            isNeedCalculate = true
            curveView.refresh()
        }
    /** 上边距. */
    var paddingTop = builder.paddingTop
        set(value) {
            field = value
            isNeedCalculate = true
            curveView.refresh()
        }
    /** 下边距. */
    var paddingBot = builder.paddingBot
        set(value) {
            field = value
            isNeedCalculate = true
            curveView.refresh()
        }

    /** Y轴item. */
    override var items: Array<Any> = builder.items
        set(value) {
            field = value
            isNeedCalculate = true
            curveView.refresh()
        }

    override fun getMeasureWidth(): Int =
            if (isNeedCalculate) calculateWidth().also { measureWidth = it } else measureWidth

    override fun onDraw(cvs: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
        if (items.isNotEmpty()) {
            textPaint.textSize = textSize
            val drawnHeight = bottom - top - paddingTop - paddingBot
            val itemCount = items.size
            val eachHeight = drawnHeight.toFloat() / (itemCount - 1)
            val startY = bottom - paddingBot

            val drawnR = right - paddingRight.toFloat()
            textPaint.color = textColor
            textPaint.textAlign = Paint.Align.RIGHT
            items.forEachIndexed { index, item ->
                // 绘制文字
                cvs.drawText(item.toString(), drawnR, startY - eachHeight * index + textOffsetY, textPaint)
            }
        }
    }

    /**
     * 计算宽度.
     * @return Int
     */
    private fun calculateWidth(): Int {
        textPaint.textSize = textSize
        var width = 0f
        items.forEach {
            val wid = textPaint.measureText(it.toString())
            if (width < wid) {
                width = wid
            }
        }
        textOffsetY = textPaint.textOffsetY() + 2
        return (paddingLeft + paddingRight + width).toInt()
    }
}