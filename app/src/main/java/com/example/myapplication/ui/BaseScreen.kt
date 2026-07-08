package com.example.myapplication.ui

import android.graphics.Canvas
import com.example.myapplication.GameView
import com.example.myapplication.model.Inventory
import com.example.myapplication.model.Player

abstract class BaseScreen {
    protected lateinit var sizes: ScreenSizes

    open fun onSizeChanged(width: Float, height: Float) {
        sizes = calculateSizes(width, height)
    }

    abstract fun draw(canvas: Canvas, width: Float, height: Float, data: ScreenData)
    abstract fun handleTouch(x: Float, y: Float, width: Float, height: Float, data: ScreenData): Boolean

    protected fun calculateSizes(width: Float, height: Float): ScreenSizes {
        val scale = minOf(width / 1080f, height / 1920f).coerceIn(0.5f, 1.8f)
        return ScreenSizes(scale = scale, /* ... */)
    }
}

data class ScreenSizes(val scale: Float, /* все размеры */)
data class ScreenData(val player: Player, val inventory: Inventory, val gameView: GameView)
