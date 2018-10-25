package com.tlz.curveview.example

import android.graphics.Color
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
                items = Array(6) {
                    it * 20
                }
            }

            curveRender {
                maxShownDataPoint = 20

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

        Thread {
            while (true) {
                Thread.sleep(2000)
                runOnUiThread {
                    curve_view.dataset<Data>()?.appendData(Data(Random().nextInt(100).toFloat()))
                }
            }
        }.start()

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
    }

    class Data(val value: Float, val time: Long = System.currentTimeMillis(), private val alert: String = "") : DefData {

        override val yScale: Float
            get() = value / 100

        override val mark: String
            get() = alert
    }

    private val dateFormat = SimpleDateFormat("yyyy年MM月dd日 hh:mm:ss", Locale.CHINA)

    private fun Long.toDate() = dateFormat.format(Date(this))
}
