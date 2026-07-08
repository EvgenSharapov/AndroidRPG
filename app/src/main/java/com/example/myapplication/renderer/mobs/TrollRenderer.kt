package com.example.myapplication.renderer.mobs

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class TrollRenderer : BaseMobRenderer(
    spriteName = "troll",
    fallbackColor = Color.rgb(150, 100, 200),
    size = 30f
) {
    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.WALK_LEFT -> "walk_left"
        MobAction.WALK_RIGHT -> "walk_right"
        else -> "idle"
    }

    override fun getBattleScale(): Float = 3.8f
    override fun getMapSize(): Float = 140f

    override fun drawFallbackShape(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        super.drawFallbackShape(canvas, x, y, radius, color)

        val hornPaint = Paint().apply {
            this.color = Color.rgb(180, 180, 200)
            strokeWidth = 5f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        canvas.drawLine(
            x - radius * 0.6f, y - radius * 0.8f,
            x - radius * 0.9f, y - radius * 1.5f,
            hornPaint
        )
        canvas.drawLine(
            x + radius * 0.6f, y - radius * 0.8f,
            x + radius * 0.9f, y - radius * 1.5f,
            hornPaint
        )
    }
}
