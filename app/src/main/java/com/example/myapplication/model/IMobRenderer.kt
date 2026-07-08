package com.example.myapplication.model

import android.graphics.Canvas
import com.example.myapplication.GameView

interface IMobRenderer {
    fun drawOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, sizeMultiplier: Float)
    fun drawInBattle(canvas: Canvas, x: Float, y: Float, mob: Mob, scale: Float, gameView: GameView)
    fun getAnimationName(action: MobAction): String
    fun getFallbackColor(): Int
    fun getSize(): Float
}

enum class MobAction {
    IDLE, ATTACK, RUN, DEATH
}
