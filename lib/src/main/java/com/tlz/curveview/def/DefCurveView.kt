package com.tlz.curveview.def

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import com.tlz.curveview.CurveRender
import com.tlz.curveview.TlzCurveView
import com.tlz.curveview.Yaxis
import android.view.Gravity
import android.text.TextPaint


/**
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 14:28.
 */

private fun Context.dp2px(dp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.displayMetrics)

private fun Context.dp2px(dp: Int) = dp2px(dp.toFloat()).toInt()

private fun Context.sp2px(sp: Float) = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, resources.displayMetrics)
private fun Context.sp2px(sp: Int) = sp2px(sp.toFloat()).toInt()

class DefCurveView<T : DefData> internal constructor(private val ctx: Context) {

  private var yaxis: Yaxis? = null
  private var curveRender: CurveRender<T>? = null

  fun yaxis(setup: YaxisBuilder.() -> Unit): DefYaxis {
    val builder = YaxisBuilder(ctx)
    setup.invoke(builder)
    return DefYaxis(builder).also { yaxis = it }
  }

  fun curveRender(setup: CurveRenderBuilder<T>.() -> Unit): DefCurveRender<T> {
    val builder = CurveRenderBuilder<T>(ctx)
    setup.invoke(builder)
    return DefCurveRender(DefDataset(), builder).also { curveRender = it }
  }

  internal fun create(curveView: TlzCurveView) {
    curveView.setup(yaxis = yaxis, curveRender = curveRender)
  }
}

class YaxisBuilder internal constructor(ctx: Context) {

  /** 文字大小. */
  var textSize: Float = ctx.dp2px(16f)
  /** 文字颜色. */
  var textColor = Color.DKGRAY
  /** 左边距. */
  var paddingLeft = ctx.dp2px(10)
  /** 右边距. */
  var paddingRight = ctx.dp2px(10)
  /** 上边距. */
  var paddingTop = paddingRight
  /** 下边距. */
  var paddingBot = paddingTop

  /** Y轴item. */
  var items: Array<Any> = arrayOf()

}

class CurveRenderBuilder<T> internal constructor(ctx: Context) {

  /** 是否是静态模式 静态模式不可追加数据. */
  var isIdleMode: Boolean = false

  /** 左边距. */
  var paddingLeft = 0
  /** 右边距. */
  var paddingRight = 0
  /** 上边距. */
  var paddingTop = ctx.dp2px(10)
  /** 下边距. */
  var paddingBot = paddingTop

  /** 曲线粗细. */
  var curveThickness: Float = ctx.dp2px(2f)
  /** 曲线颜色. */
  var curveColor = Color.RED
  /** 最大显示数据点. */
  var maxShownDataPoint = 10
  /** 平滑移动时间. */
  var smoothMoveDuration: Long = 2000L

  /** 是否绘制网格线. */
  var shownGridLine = true
  /** 网格粗细. */
  var gridThickness: Int = ctx.dp2px(1)
  /** 网格线颜色. */
  var gridColor = Color.GRAY

  /** 基线. */
  var baseline: DefData? = null
  /** 基线粗细 */
  var baselineThickness = ctx.dp2px(1f)
  /** 基线颜色. */
  var baselineColor = Color.BLUE

  /** 提示框方向. */
  var hintRectOrientation = DefCurueHintOrientation.H
  /** 提示框背景. */
  var hintRectBg = Color.WHITE
  /** 提示框圆角. */
  var hintRectRadius = ctx.dp2px(2f)
  /** 提示文字大小. */
  var hintTextSize = ctx.sp2px(14f)
  /** 提示文字颜色. */
  var hintTextColor = Color.GRAY
  /** 提示文字格式化. */
  var hintTextFormat: (T) -> String = { d -> d.toString() }
  /** 提示框内间距. */
  var hintRectPadding = ctx.dp2px(4)
  /** 提示框距离数据点的间距. */
  var hintRectTopDataSpace = ctx.dp2px(10)
  /** 提示点圆的半径. */
  var hintPointRadius = ctx.dp2px(4f)
  /** 提示点圆的颜色. */
//  var hintPointColor = Color.GREEN

  /** 平滑缩放的动画持续时间. */
  var smoothScaleOrZoomDuration = 1000L

  /** 触摸滑动阈值. */
//  var touchMoveThreshold = ctx.dp2px(8)

  /** 标记点击. */
  var onMarkClicked: ((T) -> Unit)? = null
  /** 标记图标. */
  var markerIcon: Bitmap? = null
  /** 标记数字文字大小. */
  var markNumTextSize = ctx.sp2px(12f)
  /** 标记数字文字颜色. */
  var markNumTextColor = Color.WHITE

  /** 数据点长按回调, 只有动态模式才会回调. */
  var onDataLongPressed: ((T) -> Unit)? = null

  /** Y轴点击时间半径. */
  var yClickEventRadius = ctx.dp2px(10f)
}

/**
 * 默认Y轴只支持Float 类型.
 * 默认曲线功能是根据自己规则中的业务需求开发.
 * @receiver TlzCurveView
 * @param setup (DefCurveView<T>) -> Unit
 */
fun <T : DefData> TlzCurveView.setupByDef(setup: DefCurveView<T>.() -> Unit) {
  val defCurveView = DefCurveView<T>(context)
  setup.invoke(defCurveView)
  defCurveView.create(this)
}

/**
 * Y轴偏移.
 * @receiver Paint
 * @param gravity Int
 * @return Int
 */
internal fun Paint.textOffsetY(gravity: Int = Gravity.CENTER): Int {
  val height = (fontMetrics.descent - fontMetrics.ascent).toInt()
  var offset = (fontMetrics.descent + fontMetrics.ascent).toInt() / 2
  if (gravity and Gravity.CENTER_VERTICAL != 0) {
    offset += height / 2
  } else if (gravity and Gravity.BOTTOM != 0) {
    offset += height
  }
  return offset
}

enum class DefCurueHintOrientation {
  H,
  v
}