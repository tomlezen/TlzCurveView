package com.tlz.curveview

/**
 * 曲线渲染器.
 *
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 14:36.
 */
abstract class CurveRender<T>(internal val dataset: Dataset<T>) : CurveView {

    internal lateinit var curveView: TlzCurveView

    override fun setupCurveView(view: TlzCurveView) {
        this.curveView = view
    }

}