package com.tlz.curveview.def

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.animation.LinearInterpolator
import com.tlz.curveview.CurveRender
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs

/**
 * 默认曲线渲染器.
 *
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 15:05.
 */
class DefCurveRender<T : DefData> internal constructor(dataset: DefDataset<T>, builder: CurveRenderBuilder<T>) : CurveRender<T>(dataset) {

  /** 网格画笔. */
  private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  /** 基线画笔. */
  private val baselinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
  /** 曲线路径画笔. */
  private val curvePathPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  /** 提示框画笔. */
  private val hintPaint = Paint(Paint.ANTI_ALIAS_FLAG)
  /** 提示框文字画笔. */
  private val hintTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)

  /** 是否是静态模式,静态模式不可添加数据. */
  private val isIdleMode: Boolean = builder.isIdleMode

  /** 左边距. */
  var paddingLeft = builder.paddingLeft
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 右边距. */
  var paddingRight = builder.paddingRight
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 上边距. */
  var paddingTop = builder.paddingTop
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 下边距. */
  var paddingBot = builder.paddingBot
    set(value) {
      field = value
      curveView.refresh()
    }

  /** 曲线粗细. */
  var curveThickness = builder.curveThickness
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 曲线颜色. */
  var curveColor = builder.curveColor
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 最大显示数据点. */
  var maxShownDataPoint = builder.maxShownDataPoint
    set(value) {
      // 至少3个点.
      field = if (value < 3) 3 else if (value > 50) 50 else value
      curveView.refresh()
    }
  /** 平滑移动时间. */
  var smoothMoveDuration = builder.smoothMoveDuration
    set(value) {
      field = value
      curveView.refresh()
    }

  /** 是否绘制网格线. */
  var shownGridLine = builder.shownGridLine
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 网格粗细. */
  var gridThickness: Int = builder.gridThickness
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 网格线颜色. */
  var gridColor = builder.gridColor
    set(value) {
      field = value
      curveView.refresh()
    }

  /** 基线. */
  var baseline: DefData? = builder.baseline
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 基线粗细 */
  var baselineThickness = builder.baselineThickness
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 基线颜色. */
  var baselineColor = builder.baselineColor
    set(value) {
      field = value
      curveView.refresh()
    }

  /** 提示框背景. */
  var hintRectBg = builder.hintRectBg
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 提示框圆角. */
  var hintRectRadius = builder.hintRectRadius
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 提示文字大小. */
  var hintTextSize = builder.hintTextSize
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 提示文字颜色. */
  var hintTextColor = builder.hintTextColor
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 提示文字格式化. */
  var hintTextFormat: (T) -> String = builder.hintTextFormat
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 提示框内间距. */
  var hintRectPadding = builder.hintRectPadding
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 提示框距离数据点的间距. */
  var hintRectTopDataSpace = builder.hintRectTopDataSpace
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 提示点圆的半径. */
  var hintPointRadius = builder.hintPointRadius
    set(value) {
      field = value
      curveView.refresh()
    }
  /** 提示点圆的颜色. */
  var hintPointColor = builder.hintPointColor
    set(value) {
      field = value
      curveView.refresh()
    }

  /** Y轴点击时间半径. */
  var yClickEventRadius = builder.yClickEventRadius

  /** 平滑缩放的动画持续时间. */
  var smoothScaleOrZoomDuration = builder.smoothScaleOrZoomDuration

  /** 触摸滑动阈值. */
  var touchMoveThreshold = builder.touchMoveThreshold

  /** 标记点击. */
  var onMarkClicked: ((T) -> Unit)? = builder.onMarkClicked
  /** 标记图标. */
  var markIcon: Bitmap? = builder.markIcon
    set(value) {
      field = value
      curveView.refresh()
    }

  /** 数据点长按. */
  var onDataLongPressed: ((T) -> Unit)? = builder.onDataLongPressed

  /** 等待绘制的数据队列. */
  private val waitDrawnDataQueue = ConcurrentLinkedQueue<T>()
  /** 绘制的数据 */
  private val drawnData = mutableListOf<DataWrapper<T>>()

  /** 曲线Path. */
  private val curvePath = Path()

  /** 绘制区域. */
  private val drawnRect = Rect()

  /** 平滑移动动画. */
  private var animator: ValueAnimator? = null
  private var animatorProgress = 0f
    set(value) {
      field = value
      curveView.refresh()
    }

  /** 每个数据点的有效宽度. */
  private var eachDataWidth = 0f

  /** 当前是否处于滑动中. */
  private var isScrolling = false
  /** 速度跟踪器. */
  private var velocityTracker: VelocityTracker? = null

  init {
    baselinePaint.strokeCap = Paint.Cap.ROUND
    baselinePaint.style = Paint.Style.STROKE
    baselinePaint.pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)

    curvePathPaint.style = Paint.Style.STROKE
    curvePathPaint.strokeCap = Paint.Cap.ROUND
    curvePathPaint.strokeJoin = Paint.Join.ROUND
  }

  /** Touch down时间. */
  private var motionDownTime = 0L
  /** Touch down X坐标. */
  private var motionStartX = 0f
  /** Touch down Y坐标. */
  private var motionStartY = 0f
  /** 手势触摸地方的数据. */
  private var motionData: DataWrapper<T>? = null
  /** 点击的数据 */
  private var motionClickedData: DataWrapper<T>? = null
  /** 是否显示提示框. */
  private var isShownHintRect = false

  /** 是否正在移动. */
  private var isMoving = false

  /**
   * 计算点击事件.
   * @param event MotionEvent
   */
  private fun calClickEvent(event: MotionEvent) {
    // 判断是否还是原来的点 是就触发点击操作 并显示提示框
    motionClickedData = calDataByMotionEvent(event)
    isShownHintRect = motionData != null && motionClickedData == motionData
    curveView.refresh()
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    if (!isScrolling) {
      curveView.parent?.requestDisallowInterceptTouchEvent(true)
      obtainVelocityTracker(event)
      when (event.action) {
        MotionEvent.ACTION_DOWN -> {
          motionDownTime = System.currentTimeMillis()
          motionStartX = event.x
          motionStartY = event.y
          motionData = calDataByMotionEvent(event)
        }
        MotionEvent.ACTION_MOVE -> {
          if (isIdleMode && scaleAnimator?.isRunning != true && moveAnimator?.isRunning != true) {
            var dis = event.x - motionStartX
            if (abs(dis) >= touchMoveThreshold) {
              isMoving = true
              // 判断左边界
              drawnData.first().apply {
                if (x + dis > drawnRect.left) {
                  dis = drawnRect.left - x
                }
              }
              // 判断右边边界
              drawnData.last().apply {
                if (x + dis < drawnRect.right) {
                  dis = drawnRect.right - x
                }
              }
              drawnData.forEach {
                it.x += dis
              }

              motionStartX = event.x
              motionStartY = event.y
              curveView.refresh()
              calCenterDataPosition()
            }
          }
        }
        MotionEvent.ACTION_UP -> {
          if (!isIdleMode) {
            // 判断时间是否是长按
            if (System.currentTimeMillis() - motionDownTime > 1000) {
              motionData?.let { onDataLongPressed?.invoke(it.data) }
            } else {
              calClickEvent(event)
            }
          } else if (!isMoving) {
            // 计算是否有标记被点击
            kotlin.run Break@{
              drawnData.forEach { d ->
                if (d.markRect.contains(event.x, event.y)) {
                  onMarkClicked?.invoke(d.data)
                  return@Break
                }
              }
              calClickEvent(event)
            }
          }
          curveView.parent?.requestDisallowInterceptTouchEvent(false)
          recycleVelocityTracker()
          isMoving = false
        }
        MotionEvent.ACTION_CANCEL -> {
          recycleVelocityTracker()
          curveView.parent?.requestDisallowInterceptTouchEvent(false)
          isMoving = false
        }
      }
    }
    return true
  }

  override fun onDraw(cvs: Canvas, left: Int, top: Int, right: Int, bottom: Int) {
    drawnRect.set(left + paddingLeft + 2, top + paddingTop, right - paddingRight - 2, bottom - paddingBot)
    val startX = drawnRect.left
    val endX = drawnRect.right
    val startY = drawnRect.bottom
    // 可绘制的内容总高度和宽度
    val drawnHeight = drawnRect.height()
    val drawnWidth = drawnRect.width()

    // 绘制网格
    drawGridLine(cvs, startX, startY, endX, drawnHeight)

    // 绘制基础线
    drawBaseLine(cvs, drawnHeight, startX, startY, endX)

    // 绘制曲线
    drawCurveLine(cvs, drawnWidth, drawnHeight, startX, startY)

    // 绘制标记.
    drawMark(cvs)

    // 绘制提示框.
    drawHintRect(cvs)
  }

  /**
   * 绘制网格线.
   * @param cvs Canvas
   * @param startX Int
   * @param startY Int
   * @param endX Int
   * @param drawnHeight Int
   */
  private fun drawGridLine(cvs: Canvas, startX: Int, startY: Int, endX: Int, drawnHeight: Int) {
    if (shownGridLine) {
      val items = curveView.yaxis()?.items ?: arrayOf()
      if (items.isNotEmpty()) {
        val itemCount = items.size
        val eachItemHeight = drawnHeight.toFloat() / (itemCount - 1)

        gridPaint.color = gridColor
        gridPaint.strokeWidth = gridThickness.toFloat()
        gridPaint.style = Paint.Style.FILL
        items.forEachIndexed { index, _ ->
          val y = startY - index * eachItemHeight
          cvs.drawLine(startX.toFloat(), y, endX.toFloat(), y, gridPaint)
        }
      }
    }
  }

  /**
   * 绘制基础线.
   * @param cvs Canvas
   * @param drawnHeight Int
   * @param startX Int
   * @param startY Int
   * @param endX Int
   */
  private fun drawBaseLine(cvs: Canvas, drawnHeight: Int, startX: Int, startY: Int, endX: Int) {
    baseline?.let {
      baselinePaint.strokeWidth = baselineThickness
      baselinePaint.color = baselineColor

      val y = startY - drawnHeight * it.yScale
      cvs.drawLine(startX.toFloat(), y, endX.toFloat(), y, baselinePaint)
    }
  }

  /**
   * 计算每个数据的数据宽度.
   * @param drawnWidth Int
   */
  private fun calEachDataWidth(drawnWidth: Int) {
    eachDataWidth = if (isIdleMode) {
      val dataSize = drawnData.count()
      val curEachDataWidth = drawnWidth.toFloat() / checkMin(1, dataSize / curScale - 1)
      val nextEachDataWidth = drawnWidth.toFloat() / checkMin(1, dataSize / nextScale - 1)
      curEachDataWidth + (nextEachDataWidth - curEachDataWidth) * scaleAnimateProgress
    } else {
      drawnWidth.toFloat() / (maxShownDataPoint - 1)
    }
  }

  /** 中心点位置. */
  private var centerDataPosition: Int = -1

  /**
   * 计算X Y 坐标.
   * @param startX Int
   * @param startY Int
   * @param drawnHeight Int
   */
  private fun calXAndY(startX: Int, startY: Int, drawnHeight: Int) {
    val size = drawnData.size
    // 先计算X,Y轴坐标.
    if (size == 1) {
      drawnData.first().let {
        it.x = startX.toFloat()
        it.y = startY - it.data.yScale * drawnHeight
      }
    } else {
      if (isIdleMode) {
        if (isMoving) return
        // 判断中心点的坐标是否计算.
        val centerData = drawnData[centerDataPosition].apply {
          if (x == -1f) {
            x = startX + eachDataWidth * centerDataPosition
          }
        }
        drawnData.forEachIndexed { index, dataWrapper ->
          dataWrapper.x = centerData.x - eachDataWidth * (centerDataPosition - index)
          dataWrapper.y = startY - dataWrapper.data.yScale * drawnHeight
        }
      } else {
        val firstShown = drawnData.first().isShown
        val offsetX = when{
          !firstShown && animatorProgress >= 1f -> 0f
          animator == null -> eachDataWidth.toFloat()
          else ->  eachDataWidth * animatorProgress
        }
        drawnData.forEachIndexed { index, dataWrapper ->
          if (index == 0) {
            dataWrapper.x = startX.toFloat() - eachDataWidth + offsetX
          } else {
            dataWrapper.x = startX.toFloat() + eachDataWidth * (index - 1) + offsetX
          }
          dataWrapper.y = startY - dataWrapper.data.yScale * drawnHeight
        }
      }
    }
  }

  /**
   * 绘制曲线.
   * @param cvs Canvas
   * @param drawnWidth Int
   * @param drawnHeight Int
   * @param startX Int
   * @param startY Int
   */
  private fun drawCurveLine(cvs: Canvas, drawnWidth: Int, drawnHeight: Int, startX: Int, startY: Int) {
    if (drawnData.isNotEmpty()) {
      curvePath.reset()
      val size = drawnData.size

      calEachDataWidth(drawnWidth)
      calXAndY(startX, startY, drawnHeight)

      var prePreviousPointX = Float.NaN
      var prePreviousPointY = Float.NaN
      var previousPointX = Float.NaN
      var previousPointY = Float.NaN
      var currentPointX = Float.NaN
      var currentPointY = Float.NaN
      var nextPointX: Float
      var nextPointY: Float

      drawnData.forEachIndexed { index, dataWrapper ->
        if (currentPointX.isNaN()) {
          currentPointX = dataWrapper.x
          currentPointY = dataWrapper.y
        }
        if (previousPointX.isNaN()) {
          //是第一个点?
          if (index > 0) {
            previousPointX = drawnData[index - 1].x
            previousPointY = drawnData[index - 1].y
          } else {
            //用当前点表示上一个点
            previousPointX = currentPointX
            previousPointY = currentPointY
          }
        }

        if (prePreviousPointX.isNaN()) {
          //是前两个点?
          if (index > 1) {
            prePreviousPointX = drawnData[index - 2].x
            prePreviousPointY = drawnData[index - 2].y
          } else {
            //当前点表示上上个点
            prePreviousPointX = previousPointX
            prePreviousPointY = previousPointY
          }
        }

        // 判断是不是最后一个点了
        if (index < size - 1) {
          nextPointX = drawnData[index + 1].x
          nextPointY = drawnData[index + 1].y
        } else {
          //用当前点表示下一个点
          nextPointX = currentPointX
          nextPointY = currentPointY
        }

        if (index == 0) {
          // 将Path移动到开始点
          curvePath.moveTo(currentPointX, currentPointY)
        } else {
          // 求出控制点坐标
          val firstDiffX = (currentPointX - prePreviousPointX)
          val firstDiffY = (currentPointY - prePreviousPointY)
          val secondDiffX = (nextPointX - previousPointX)
          val secondDiffY = (nextPointY - previousPointY)
          val firstControlPointX = previousPointX + (.05f * firstDiffX)
          val firstControlPointY = previousPointY + (.05f * firstDiffY)
          val secondControlPointX = currentPointX - (.05f * secondDiffX)
          val secondControlPointY = currentPointY - (.05f * secondDiffY)
          //画出曲线
          curvePath.cubicTo(firstControlPointX, firstControlPointY, secondControlPointX, secondControlPointY, currentPointX, currentPointY)
        }

        // 更新
        prePreviousPointX = previousPointX
        prePreviousPointY = previousPointY
        previousPointX = currentPointX
        previousPointY = currentPointY
        currentPointX = nextPointX
        currentPointY = nextPointY
      }

      curvePathPaint.color = curveColor
      curvePathPaint.strokeWidth = curveThickness
      cvs.drawPath(curvePath, curvePathPaint)
    }
  }

  /**
   * 绘制提示框.
   * @param cvs Canvas
   */
  private fun drawHintRect(cvs: Canvas) {
    motionClickedData?.let { data ->
      if (!isShownHintRect || !data.isShown || data.x > drawnRect.right) return
      val hintText = hintTextFormat.invoke(data.data)
      if (hintText.isEmpty()) return

      hintTextPaint.textSize = hintTextSize
      hintTextPaint.color = hintTextColor
      // 分割换行符 计算最大宽度
      val hints = hintText.split("\n")
      var width = 0f
      hints.forEach {
        val w = hintTextPaint.measureText(it)
        if (w > width) {
          width = w
        }
      }
      val staLayout = StaticLayout(
          hintText,
          hintTextPaint,
          (width + 2).toInt(),
          Layout.Alignment.ALIGN_NORMAL,
          1f,
          0f,
          true
      )
      val rectHeight = staLayout.height + hintRectPadding * 2
      val rectWidth = staLayout.width + hintRectPadding * 2

      var top = data.y - rectHeight - hintRectTopDataSpace
      if (top < drawnRect.top) {
        top = data.y + hintRectTopDataSpace
      }
      val bot = top + rectHeight
      var left = data.x - rectWidth / 2
      if (left < drawnRect.left) {
        left = drawnRect.left + 2f
      }
      var right = left + rectWidth
      if (right > drawnRect.right) {
        right = drawnRect.right - 2f
        left = right - rectWidth
      }

      // 绘制提示框
      hintPaint.color = hintRectBg
      cvs.drawRoundRect(RectF(left, top, right, bot), hintRectRadius, hintRectRadius, hintPaint)

      // 绘制提示语
      cvs.save()
      cvs.translate(left + hintRectPadding, top + hintRectPadding)
      staLayout.draw(cvs)
      cvs.restore()

      // 绘制提示点.
      hintPaint.color = hintPointColor
      cvs.drawCircle(data.x, data.y, hintPointRadius, hintPaint)
    }
  }

  /**
   * 绘制标记.
   * @param cvs Canvas
   */
  private fun drawMark(cvs: Canvas) {
    markIcon?.let {
      drawnData.filter { d -> d.data.mark.isNotEmpty() }.forEach { data ->
        data.markRect.set(data.x - it.width / 2, data.y - it.height, data.x + it.width / 2, data.y)
        cvs.drawBitmap(it, null, data.markRect, hintPaint)
      }
    } ?: return
  }

  /**
   * 设置数据.
   * @param data List<T>
   */
  internal fun setData(data: List<T>) {
    this.motionData = null
    this.motionClickedData = null
    this.isShownHintRect = false
    this.scaleAnimator?.cancel()
    this.moveAnimator?.cancel()
    this.curScale = 1
    this.nextScale = 1
    this.scaleAnimateProgress = 0f
    this.drawnData.clear()
    data.mapTo(this.drawnData) {
      DataWrapper(it, isShown = true)
    }
    this.centerDataPosition = data.size / 2
    curveView.refresh()
  }

  /**
   * 添加一个数据.
   * @param data T
   */
  internal fun addData(data: T) {
    if (isIdleMode) return
    start()
    waitDrawnDataQueue.offer(data)
    checkMinWaitQueue()
  }

  /**
   * 检查等待队列.
   */
  private fun checkMinWaitQueue() {
    if (waitDrawnDataQueue.isNotEmpty() && (drawnData.size < 2 || drawnData.firstOrNull()?.isShown == true)) {
      drawnData.add(0, DataWrapper(waitDrawnDataQueue.poll(), isShown = drawnData.size == 0))
      // 1个点不执行动画
      if (drawnData.size > 1 && animator?.isRunning != true) {
        startAnimator()
      }
      return
    }
    if (waitDrawnDataQueue.isEmpty()) {
      stopAnimator()
    }
  }

  /**
   * 开启动画.
   */
  private fun startAnimator() {
    if (animator?.duration != smoothMoveDuration) {
      stopAnimator()
      animatorProgress = 0f
      animator = ObjectAnimator.ofFloat(0f, 1f).apply {
        duration = smoothMoveDuration
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()

        addUpdateListener {
          animatorProgress = it.animatedValue as Float
        }

        addListener(object : AnimatorListenerAdapter() {
          override fun onAnimationRepeat(animation: Animator?) {
            drawnData.first().isShown = true
            if (drawnData.size - maxShownDataPoint > 2) {
              drawnData.removeAt(drawnData.size - 1)
            }
            checkMinWaitQueue()
          }
        })
      }
    }

    if (animator?.isRunning != true) {
      animator?.start()
    }
  }

  /**
   * 停止动画.
   */
  private fun stopAnimator() {
    animator?.cancel()
    animator = null
    curveView.refresh()
  }

  /**
   * 获取速度跟踪器.
   * @param event MotionEvent
   */
  private fun obtainVelocityTracker(event: MotionEvent) {
    if (!isScrolling) {
      return
    }
//    (velocityTracker ?: VelocityTracker.obtain().also { velocityTracker = it }).addMovement(event)
  }

  /**
   * 回收速度跟踪器.
   */
  private fun recycleVelocityTracker() {
    velocityTracker?.recycle()
  }

  /**
   * 计算触摸点的数据.
   * @param event MotionEvent
   * @return DataWrapper<T>?
   */
  private fun calDataByMotionEvent(event: MotionEvent): DataWrapper<T>? {
    val x = event.x
    val y = event.y
    val range = eachDataWidth / 2
    return drawnData.find {
      it.isShown && it.x >= x - range && it.x <= x + range && it.y >= y - yClickEventRadius && it.y <= y + yClickEventRadius
    }?.apply {
      println("id是: $id")
    }
  }

  /** 是否停止. */
  private var isStop = true

  /**
   * 停止.
   */
  fun stop() {
    if (isIdleMode) return
    isStop = true
  }

  /**
   * 开始.
   */
  fun start() {
    if (!isStop) return
    reset()
    isStop = false
    curveView.refresh()
  }

    /**
     * 关闭提示.
     */
    fun closeHint(){
        isShownHintRect = false
        curveView.refresh()
    }

  /**
   * 重置.
   */
  private fun reset() {
    isStop = true
    waitDrawnDataQueue.clear()
    stopAnimator()
    animatorProgress = 0f
    isShownHintRect = false
    motionData = null
    motionClickedData = null
    drawnData.clear()
  }

  /** 当前放大倍数. */
  private var curScale = 1
  /** 下一次放大系数. */
  private var nextScale = 1
  /** 放缩动画进度. */
  private var scaleAnimateProgress = 0f
  /** 放缩动画. */
  private var scaleAnimator: ValueAnimator? = null
  /** 移动动画. */
  private var moveAnimator: ValueAnimator? = null

  /**
   * 放大.
   */
  fun scale() {
    val maxScale = drawnData.size / maxShownDataPoint + 1
    if (curScale >= maxScale) return
    smoothScaleOrZoom(curScale + 1)
  }

  /**
   * 缩小.
   */
  fun zoom() {
    if (curScale <= 1) return
    smoothScaleOrZoom(curScale - 1)
  }

  /**
   * 平滑缩放.
   * @param scale Int
   */
  private fun smoothScaleOrZoom(scale: Int) {
    // 在执行中, 不再次执行
    if (scaleAnimator?.isRunning == true || moveAnimator?.isRunning == true) return
    nextScale = scale
    scaleAnimator = ObjectAnimator.ofFloat(0f, 1f).apply {
      duration = smoothScaleOrZoomDuration

      addUpdateListener {
        scaleAnimateProgress = it.animatedValue as Float
        curveView.refresh()
      }

      addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
          curScale = nextScale
          scaleAnimateProgress = 0f
          curveView.refresh()
          moveToRightPosition()
        }
      })
      start()
    }
  }

  /**
   * 移动到正确的位置.
   */
  private fun moveToRightPosition() {
    // 先检查左边 再检查右边
    var isMove = true
    var dis = drawnRect.left - drawnData.first().x
    if (dis > 0f) {
      isMove = false
      dis = drawnRect.right - drawnData.last().x
      if (dis > 0f) {
        isMove = true
      }
    }
    if (!isMove) return
    isMoving = true
    moveAnimator = ObjectAnimator.ofFloat(0f, 1f).apply {
      duration = 1000L

      addUpdateListener {
        val value = it.animatedValue as Float
        drawnData.forEach { d ->
          d.x += value * dis
        }
        dis *= (1 - value)
        curveView.refresh()
      }

      addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
          calCenterDataPosition()
          isMoving = false
        }
      })

      start()
    }
  }

  /**
   * 计算中心点的位置.
   */
  private fun calCenterDataPosition() {
    kotlin.run Break@{
      drawnData.forEachIndexed { index, dataWrapper ->
        if (dataWrapper.x > drawnRect.centerX() - eachDataWidth) {
          centerDataPosition = index
          return@Break
        }
      }
    }
  }

  /**
   * 检查最小值.
   * @param min Int
   * @param value Int
   * @return Int
   */
  private fun checkMin(min: Int, value: Int): Int {
    if (min > value) return min
    return value
  }

  private class DataWrapper<T>(
      val data: T,
      var x: Float = -1f,
      var y: Float = -1f,
      var isShown: Boolean = false) {
    val id = System.currentTimeMillis()
    val markRect = RectF()

    override fun equals(other: Any?): Boolean = id == (other as? DataWrapper<T>)?.id

    override fun hashCode(): Int {
      var result = data?.hashCode() ?: 0
      result = 31 * result + x.hashCode()
      result = 31 * result + y.hashCode()
      result = 31 * result + isShown.hashCode()
      result = 31 * result + id.hashCode()
      return result
    }
  }
}