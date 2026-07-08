package com.example.myapplication.ui

import android.graphics.Canvas
import com.example.myapplication.model.ScreenData
import com.example.myapplication.model.ScreenSizes

abstract class BaseScreen {
    protected var screenSizes: ScreenSizes? = null

    open fun onSizeChanged(width: Float, height: Float) {
        screenSizes = ScreenSizes.calculate(width, height)
    }

    abstract fun draw(canvas: Canvas, width: Float, height: Float, data: ScreenData)
    abstract fun handleTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        data: ScreenData
    ): Boolean

    protected fun sizes(): ScreenSizes {
        return screenSizes ?: throw IllegalStateException("Sizes not calculated! Call onSizeChanged first.")
    }
}
