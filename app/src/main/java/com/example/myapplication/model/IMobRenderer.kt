package com.example.myapplication.model

import android.graphics.Canvas
import com.example.myapplication.GameView

interface IMobRenderer {
    val spriteName: String
    val fallbackColor: Int
    val size: Float

    var isAttacking: Boolean  // ← свойство вместо метода
    var attackFrameIndex: Int // ← свойство вместо метода

    fun drawOnMap(
        canvas: Canvas,
        x: Float,
        y: Float,
        mob: Mob,
        sizeMultiplier: Float = 1f,
        gameView: GameView? = null
    )

    fun drawInBattle(
        canvas: Canvas,
        x: Float,
        y: Float,
        mob: Mob,
        scale: Float,
        gameView: GameView
    )

    fun getAnimationName(action: MobAction): String

    fun getBattleScale(): Float = 2.5f
    fun getMapSize(): Float = 100f
}

