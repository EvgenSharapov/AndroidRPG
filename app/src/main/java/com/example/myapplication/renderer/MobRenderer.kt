package com.example.myapplication.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import com.example.myapplication.GameView
import com.example.myapplication.model.Mob

object MobRenderer {
    private val paint = Paint()

    fun drawMob(
        canvas: Canvas,
        mob: Mob,
        drawX: Float,
        drawY: Float,
        isSelected: Boolean,
        gameView: GameView?
    ) {
        if (mob.isDead) return

        val sizeMultiplier = if (mob.isBoss) 2.0f else 1.2f
        val renderer = MobRendererFactory.getRenderer(mob.type)

        // Тень
        drawShadow(canvas, drawX, drawY, renderer.size * sizeMultiplier)

        // Свечение для босса
        if (mob.isBoss) {
            drawBossGlow(canvas, drawX, drawY)
        }

        // Рисуем моба через рендерер
        renderer.drawOnMap(canvas, drawX, drawY, mob, sizeMultiplier, gameView)

        // HP бар
        drawHPBar(canvas, drawX, drawY, mob, sizeMultiplier)

        // Имя и уровень
        drawMobInfo(canvas, drawX, drawY, mob, sizeMultiplier, gameView)

        // Выделение
        if (isSelected) {
            drawSelection(canvas, drawX, drawY, renderer.size * sizeMultiplier)
        }
    }

    private fun drawShadow(canvas: Canvas, x: Float, y: Float, size: Float) {
        val shadowPaint = Paint().apply {
            color = Color.argb(60, 0, 0, 0)
        }
        canvas.drawOval(
            x - size - 10f,
            y + size + 5f,
            x + size + 10f,
            y + size + 20f,
            shadowPaint
        )
    }

    private fun drawBossGlow(canvas: Canvas, x: Float, y: Float) {
        val glowPaint = Paint().apply {
            shader = android.graphics.RadialGradient(
                x, y, 180f,
                Color.argb(80, 255, 215, 0),
                Color.TRANSPARENT,
                android.graphics.Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(x, y, 120f, glowPaint)
    }

    private fun drawHPBar(canvas: Canvas, x: Float, y: Float, mob: Mob, sizeMultiplier: Float) {
        val hpPercent = mob.hp / mob.maxHp
        val barWidth = 60f * sizeMultiplier
        val barHeight = 12f * sizeMultiplier

        val hpPaint = Paint().apply { color = Color.argb(180, 0, 0, 0) }
        canvas.drawRect(
            x - barWidth / 2 - 2f,
            y - 47f * sizeMultiplier - 2f,
            x + barWidth / 2 + 2f,
            y - 35f * sizeMultiplier + 2f,
            hpPaint
        )

        hpPaint.color = when {
            mob.isBoss -> Color.rgb(255, 215, 0)
            hpPercent > 0.5f -> Color.GREEN
            hpPercent > 0.25f -> Color.YELLOW
            else -> Color.RED
        }
        canvas.drawRect(
            x - barWidth / 2,
            y - 47f * sizeMultiplier,
            x - barWidth / 2 + barWidth * hpPercent,
            y - 35f * sizeMultiplier,
            hpPaint
        )
    }

    private fun drawMobInfo(
        canvas: Canvas,
        x: Float,
        y: Float,
        mob: Mob,
        sizeMultiplier: Float,
        gameView: GameView?
    ) {
        val playerLevel = gameView?.getPlayerLevel() ?: 1
        val levelDiff = mob.level - playerLevel

        val nameColor = when {
            mob.isBoss -> Color.rgb(255, 215, 0)
            levelDiff >= 2 -> Color.RED
            levelDiff >= 1 -> Color.YELLOW
            levelDiff <= -2 -> Color.GREEN
            else -> Color.WHITE
        }

        val typePaint = Paint().apply {
            color = nameColor
            textSize = if (mob.isBoss) 22f * sizeMultiplier else 14f * sizeMultiplier
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        val baseSize = MobRendererFactory.getRenderer(mob.type).size
        canvas.drawText(
            mob.getTypeName() + if (mob.isBoss) " 👑" else "",
            x,
            y + baseSize * sizeMultiplier + 30f * sizeMultiplier,
            typePaint
        )
    }

    private fun drawSelection(canvas: Canvas, x: Float, y: Float, size: Float) {
        val selectPaint = Paint().apply {
            color = Color.argb(80, 255, 255, 0)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(x, y, size + 10f, selectPaint)
    }
}
