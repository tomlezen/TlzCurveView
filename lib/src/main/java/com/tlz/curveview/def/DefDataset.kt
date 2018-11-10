package com.tlz.curveview.def

import com.tlz.curveview.Dataset

/**
 * 默认数据集.
 *
 * Created by Tomlezen.
 * Data: 2018/10/22.
 * Time: 14:24.
 */
class DefDataset<T : DefData> internal constructor() : Dataset<T>() {

  override fun getDrawnData(): List<T> = listOf()

  override fun appendData(data: T) {
    (this.curveView.curveRender<T>() as? DefCurveRender<T>)?.addData(data)
  }

  override fun appendData(data: List<T>) {

  }

  override fun setData(vararg data: T) {
  }

  override fun setData(data: List<T>) {
    (this.curveView.curveRender<T>() as? DefCurveRender<T>)?.setData(data)
  }
}