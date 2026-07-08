package com.example.myapplication.renderer.mobs

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class SlimeGreenRenderer : BaseMobRenderer(
    spriteName = "slime_green",
    fallbackColor = Color.rgb(100, 200, 100),
    size = 25f
) {
    private val battleSpriteName = "slime_green_battle"

    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    // ИСПРАВЛЕНО: override val вместо fun
    override val spriteName: String
        get() = if (isAttacking) battleSpriteName else super.spriteName

    override fun getBattleScale(): Float = 2.6f

    override fun drawFallbackShape(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        super.drawFallbackShape(canvas, x, y, radius, color)

        // Улыбка слизня
        val smilePaint = Paint().apply {
            this.color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawArc(
            x - radius * 0.4f,
            y + radius * 0.2f,
            x + radius * 0.4f,
            y + radius * 0.7f,
            0f, 180f, false, smilePaint
        )
    }
}