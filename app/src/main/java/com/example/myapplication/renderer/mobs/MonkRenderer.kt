package com.example.myapplication.renderer.mobs

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class MonkRenderer : BaseMobRenderer(
    spriteName = "monk",
    fallbackColor = Color.rgb(200, 180, 100),
    size = 25f
) {
    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.WALK_LEFT -> "walk_left"
        MobAction.WALK_RIGHT -> "walk_right"
        else -> "idle"
    }

    override fun getBattleScale(): Float = 3.0f
    override fun getMapSize(): Float = 110f

    override fun drawFallbackShape(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        super.drawFallbackShape(canvas, x, y, radius, color)

        val dotPaint = Paint().apply {
            this.color = Color.rgb(255, 100, 50)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x, y - radius * 0.8f, radius * 0.15f, dotPaint)
    }
}
