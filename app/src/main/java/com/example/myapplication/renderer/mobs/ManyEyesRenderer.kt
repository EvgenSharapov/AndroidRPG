package com.example.myapplication.renderer.mobs

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class ManyEyesRenderer : BaseMobRenderer(
    spriteName = "manyeyes",
    fallbackColor = Color.rgb(150, 50, 200),
    size = 25f
) {
    private val battleSpriteName = "manyeyes_battle"

    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    // Переопределяем свойство spriteName вместо создания метода
    override val spriteName: String
        get() = if (isAttacking) battleSpriteName else super.spriteName

    override fun getBattleScale(): Float = 2.8f

    override fun drawFallbackShape(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        super.drawFallbackShape(canvas, x, y, radius, color)

        // Дополнительные глаза для многоглаза
        val eyePaint = Paint()
        eyePaint.color = Color.WHITE

        val pupilPaint = Paint()
        pupilPaint.color = Color.RED

        val eyePositions = listOf(
            -radius * 0.6f to -radius * 0.6f,
            radius * 0.6f to -radius * 0.6f,
            -radius * 0.8f to 0f,
            radius * 0.8f to 0f,
            -radius * 0.6f to radius * 0.6f,
            radius * 0.6f to radius * 0.6f,
            0f to -radius * 0.8f,
            0f to radius * 0.8f
        )

        val eyeRadius = radius * 0.25f
        val pupilRadius = eyeRadius * 0.5f

        for ((ex, ey) in eyePositions) {
            canvas.drawCircle(x + ex, y + ey, eyeRadius, eyePaint)
            canvas.drawCircle(x + ex + pupilRadius * 0.5f, y + ey, pupilRadius, pupilPaint)
        }
    }
}
