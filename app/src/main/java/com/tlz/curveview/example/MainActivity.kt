package com.tlz.curveview.example

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.tlz.curveview.def.DefCurveRender
import com.tlz.curveview.def.DefData
import com.tlz.curveview.def.setupByDef
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var curveRender: DefCurveRender<Data>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化曲线为默认样式 动态添加模式
        curve_view.setupByDef<Data> {
            yaxis {
//                paddingTop = 10
//                paddingBot = 10
//                paddingLeft = 10
//                paddingRight = 10

                items = Array(6) {
                    it * 20
                }
            }

            curveRender {
                maxShownDataPoint = 40

                baseline = Data(60f)

                curveColor = Color.GREEN

                hintRectBg = Color.RED
                hintTextColor = Color.WHITE
                hintTextFormat = { data ->
                    "值:${data.value}\n时间:${data.time.toDate()}"
                }

                onDataLongPressed = { data ->
                    Toast.makeText(this@MainActivity, "长按数据:${data.value}", Toast.LENGTH_LONG).show()
                }
            }
        }

        var isStop = true

        btn_start.setOnClickListener {
            if (!isStop) return@setOnClickListener
            isStop = false
            (curve_view.curveRender<Data>() as? DefCurveRender<Data>)?.start()
            Thread {
                while (!isStop) {
                    Thread.sleep(2000)
                    runOnUiThread {
                        if (!isStop) {
                            curve_view.dataset<Data>()?.appendData(Data(Random().nextInt(100).toFloat()))
                        }
                    }
                }
            }.start()
        }

        btn_stop.setOnClickListener {
            isStop = true
            (curve_view.curveRender<Data>() as? DefCurveRender<Data>)?.stop()
        }


        // 初始化曲线为默认样式 静态模式
        curve_view_idle.setupByDef<Data> {
            yaxis {
                items = Array(6) {
                    it * 20
                }
            }

            curveRender = curveRender {
                isIdleMode = true

                baseline = Data(60f)

                curveColor = Color.GREEN

                hintRectBg = Color.RED
                hintTextColor = Color.WHITE
                hintTextFormat = { data ->
                    "值:${data.value}\n时间:${data.time.toDate()}"
                }

                markIcon = resources.getDrawable(R.drawable.ic_location).toBitmap()
                onMarkClicked = { d ->
                    Toast.makeText(this@MainActivity, "标记被点击啦:${d.value}", Toast.LENGTH_LONG).show()
                }
            }
        }

        curve_view_idle.dataset<Data>()?.setData(List(50) {
            Data(Random().nextInt(100).toFloat())
        })

        btn_scale.setOnClickListener {
            curveRender.scale()
        }

        btn_zoom.setOnClickListener {
            curveRender.zoom()
        }

        btn_change.setOnClickListener { _ ->
            curve_view_idle.dataset<Data>()?.setData(List(50) {
                Data(Random().nextInt(100).toFloat())
            })
        }
    }

    class Data(val value: Float, val time: Long = System.currentTimeMillis(), private val alert: String = "131") : DefData {

        override val yScale: Float
            get() = value / 100

        override val mark: String
            get() = alert
    }

    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss", Locale.CHINA)

    private fun Long.toDate() = dateFormat.format(Date(this))

    fun Drawable.toBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        draw(canvas)
        return bitmap
    }
}
