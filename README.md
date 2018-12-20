# 一个简单的曲线图

### 使用

1. 默认样式

初始化**setupByDef<T>**
 ```
 TlzCurveView.setupByDef<T> {
  ///
 }
 ```
 T为数据类型，必须实现**DefData**
 ```
 class Data(val value: Float, val time: Long = System.currentTimeMillis()) : DefData {

    override val yScale: Float
      get() = value / 100

    override val mark: String
      get() = ""
  }
 ```
初始化显示样式
```
TlzCurveView.setupByDef<T> {
  // Y轴
  yaxis {
  /** 文字大小. */
   textSize: Float = ctx.dp2px(16f)
  /** 文字颜色. */
   textColor = Color.DKGRAY
  /** 左边距. */
   paddingLeft = ctx.dp2px(10)
  /** 右边距. */
   paddingRight = ctx.dp2px(10)
  /** 上边距. */
   paddingTop = paddingRight
  /** 下边距. */
   paddingBot = paddingTop

  /** Y轴刻度. */
   items: Array<Any> = arrayOf()
  }
  
  // 曲线
  curveRender {
  /** 是否是静态模式 静态模式不可追加数据. */
   isIdleMode: Boolean = false

  /** 左边距. */
   paddingLeft = 0
  /** 右边距. */
   paddingRight = 0
  /** 上边距. */
   paddingTop = ctx.dp2px(10)
  /** 下边距. */
   paddingBot = paddingTop

  /** 曲线粗细. */
   curveThickness: Float = ctx.dp2px(2f)
  /** 曲线颜色. */
   curveColor = Color.RED
  /** 最大显示数据点. */
   maxShownDataPoint = 10
  /** 平滑移动时间. */
   smoothMoveDuration: Long = 2000L

  /** 是否绘制网格线. */
   shownGridLine = true
  /** 网格粗细. */
   gridThickness: Int = ctx.dp2px(1)
  /** 网格线颜色. */
   gridColor = Color.GRAY

  /** 基线. */
   baseline: DefData? = null
  /** 基线粗细 */
   baselineThickness = ctx.dp2px(1f)
  /** 基线颜色. */
   baselineColor = Color.BLUE

  /** 提示框方向. */
   hintRectOrientation = DefCurueHintOrientation.H
  /** 提示框背景. */
   hintRectBg = Color.WHITE
  /** 提示框圆角. */
   hintRectRadius = ctx.dp2px(2f)
  /** 提示文字大小. */
   hintTextSize = ctx.sp2px(14f)
  /** 提示文字颜色. */
   hintTextColor = Color.GRAY
  /** 提示文字格式化. */
   hintTextFormat: (T) -> String = { d -> d.toString() }
  /** 提示框内间距. */
   hintRectPadding = ctx.dp2px(4)
  /** 提示框距离数据点的间距. */
   hintRectTopDataSpace = ctx.dp2px(10)
  /** 提示点圆的半径. */
   hintPointRadius = ctx.dp2px(4f)

  /** 平滑缩放的动画持续时间. */
   smoothScaleOrZoomDuration = 1000L
   
  /** 标记点击. */
   onMarkClicked: ((T) -> Unit)? = null
  /** 标记图标. */
   markerIcon: Bitmap? = null
  /** 标记数字文字大小. */
   markNumTextSize = ctx.sp2px(12f)
  /** 标记数字文字颜色. */
   markNumTextColor = Color.WHITE

  /** 数据点长按回调, 只有动态模式才会回调. */
   onDataLongPressed: ((T) -> Unit)? = null

  /** Y轴点击时间半径. */
   yClickEventRadius = ctx.dp2px(10f)
  }
 }
```
