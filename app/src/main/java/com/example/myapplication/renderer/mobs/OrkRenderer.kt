package com.example.myapplication.renderer.mobs

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class OrkRenderer : BaseMobRenderer(
    spriteName = "lizard",
    fallbackColor = Color.rgb(100, 180, 80),
    size = 25f
) {
    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.WALK_LEFT -> "walk_left"
        MobAction.WALK_RIGHT -> "walk_right"
        else -> "idle"
    }

    override fun getBattleScale(): Float = 3.2f
    override fun getMapSize(): Float = 115f

    override fun drawFallbackShape(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        super.drawFallbackShape(canvas, x, y, radius, color)

        val tuskPaint = Paint().apply {
            this.color = Color.WHITE
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(
            x - radius * 0.3f, y + radius * 0.3f,
            x - radius * 0.5f, y + radius * 0.7f,
            tuskPaint
        )
        canvas.drawLine(
            x + radius * 0.3f, y + radius * 0.3f,
            x + radius * 0.5f, y + radius * 0.7f,
            tuskPaint
        )
    }
}
