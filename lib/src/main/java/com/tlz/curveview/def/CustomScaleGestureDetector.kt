package com.tlz.curveview.def

import android.content.Context
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.view.ViewConfiguration


/**
 * Created by Tomlezen.
 * Date: 2018/11/8.
 * Time: 9:40 PM.
 */
class CustomScaleGestureDetector(ctx: Context, private val listener: OnGestureListener) {

	private var activePointerId = INVALID_POINTER_ID
	private var mActivePointerIndex = 0

	private var velocityTracker: VelocityTracker? = null
	var isDragging: Boolean = false
		private set
	private var lastTouchX: Float = 0f
	private var lastTouchY: Float = 0f
	private var touchSlop: Float = 0f
	private var mMinimumVelocity: Float = 0f

	val isScaling: Boolean
		get() = detector.isInProgress

	private val detector = ScaleGestureDetector(ctx, object : ScaleGestureDetector.OnScaleGestureListener {
		override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean = true

		override fun onScaleEnd(detector: ScaleGestureDetector?) {
		}

		override
		fun onScale(detector: ScaleGestureDetector): Boolean {
			listener.onScale(detector.scaleFactor, detector.focusX, detector.focusY)
			return true
		}
	})

	init {
		val configuration = ViewConfiguration.get(ctx)
		mMinimumVelocity = configuration.scaledMinimumFlingVelocity.toFloat()
		touchSlop = configuration.scaledTouchSlop.toFloat()
	}

	fun onTouchEvent(ev: MotionEvent): Boolean =
			try {
				detector.onTouchEvent(ev)
				processTouchEvent(ev)
			} catch (e: IllegalArgumentException) {
				true
			}

	private fun processTouchEvent(ev: MotionEvent): Boolean {
		val action = ev.action
		when (action and MotionEvent.ACTION_MASK) {
			MotionEvent.ACTION_DOWN -> {
				activePointerId = ev.getPointerId(0)

				velocityTracker = VelocityTracker.obtain()
				velocityTracker?.addMovement(ev)

				lastTouchX = ev.activeX
				lastTouchY = ev.activeY
				isDragging = false
			}
			MotionEvent.ACTION_MOVE -> {
				val x = ev.activeX
				val y = ev.activeY
				val dx = x - lastTouchX
				val dy = y - lastTouchY

				if (!isDragging) {
					isDragging = Math.sqrt((dx * dx + dy * dy).toDouble()) >= touchSlop
				}

				if (isDragging) {
					listener.onDrag(dx, dy)
					lastTouchX = x
					lastTouchY = y

					velocityTracker?.addMovement(ev)
				}
			}
			MotionEvent.ACTION_CANCEL -> {
				activePointerId = INVALID_POINTER_ID
				if (null != velocityTracker) {
					velocityTracker?.recycle()
					velocityTracker = null
				}
			}
			MotionEvent.ACTION_UP -> {
				activePointerId = INVALID_POINTER_ID
				if (isDragging) {
					velocityTracker?.let {
						lastTouchX = ev.activeX
						lastTouchY = ev.activeY

						it.addMovement(ev)
						it.computeCurrentVelocity(1000)

						val vX = it.xVelocity
						val vY = it.yVelocity

						if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
							listener.onFling(lastTouchX, lastTouchY, -vX, -vY)
						}
					}
				}

				if (null != velocityTracker) {
					velocityTracker?.recycle()
					velocityTracker = null
				}
			}
			MotionEvent.ACTION_POINTER_UP -> {
				val pointerIndex = getPointerIndex(ev.action)
				val pointerId = ev.getPointerId(pointerIndex)
				if (pointerId == activePointerId) {
					val newPointerIndex = if (pointerIndex == 0) 1 else 0
					activePointerId = ev.getPointerId(newPointerIndex)
					lastTouchX = ev.getX(newPointerIndex)
					lastTouchY = ev.getY(newPointerIndex)
				}
			}
		}

		mActivePointerIndex = ev.findPointerIndex(if (activePointerId != INVALID_POINTER_ID) activePointerId else 0)
		return true
	}

	private fun getPointerIndex(action: Int): Int {
		return action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
	}

	private val MotionEvent.activeX
		get() =
			try {
				getX(mActivePointerIndex)
			} catch (e: Exception) {
				x
			}

	private val MotionEvent.activeY
		get() =
			try {
				getY(mActivePointerIndex)
			} catch (e: Exception) {
				y
			}

	companion object {
		private const val INVALID_POINTER_ID = -1
	}

	interface OnGestureListener {

		fun onDrag(dx: Float, dy: Float)

		fun onFling(startX: Float, startY: Float, velocityX: Float, velocityY: Float)

		fun onScale(scaleFactor: Float, focusX: Float, focusY: Float)

	}

}