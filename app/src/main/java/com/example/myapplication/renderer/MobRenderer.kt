package com.example.myapplication.renderer

import android.graphics.*
import com.example.myapplication.GameView
import com.example.myapplication.model.Mob
import com.example.myapplication.SpriteManager
import kotlin.math.cos
import kotlin.math.sin

object MobRenderer {
    private val paint = Paint()
    private val eyePaint = Paint().apply { color = Color.WHITE }
    private val pupilPaint = Paint().apply { color = Color.BLACK }

    // Для отрисовки Флаффи на карте
    fun drawMob(canvas: Canvas, mob: Mob, drawX: Float, drawY: Float, isSelected: Boolean, gameView: GameView? = null) {
        if (mob.isDead) return

        val shadowPaint = Paint().apply {
            color = Color.argb(60, 0, 0, 0)
        }
        canvas.drawOval(drawX - 35f, drawY + 25f, drawX + 35f, drawY + 40f, shadowPaint)

        when (mob.type) {
            0 -> drawFluffyOnMap(canvas, drawX, drawY, mob, gameView)
            1 -> drawSpiderOnMap(canvas, drawX, drawY, mob, gameView)  // ← ПАУК
            2 -> drawManyEyesOnMap(canvas, drawX, drawY, mob, gameView)
            else -> drawDefault(canvas, drawX, drawY)
        }

        drawHPBar(canvas, drawX, drawY, mob)

        val playerLevel = gameView?.getPlayerLevel() ?: 1
        val levelDiff = mob.level - playerLevel

        val nameColor = when {
            levelDiff >= 2 -> Color.RED
            levelDiff >= 1 -> Color.YELLOW
            levelDiff <= -2 -> Color.GREEN
            else -> Color.WHITE
        }

        val typePaint = Paint().apply {
            color = nameColor
            textSize = 14f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("${mob.getTypeName()} (ур.${mob.level})", drawX, drawY + 55f, typePaint)

        if (isSelected) {
            val selectPaint = Paint().apply {
                color = Color.argb(80, 255, 255, 0)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(drawX, drawY, 35f, selectPaint)
        }
    }

    // ===== ФЛАФФИ НА КАРТЕ (используем спрайт) =====
    private fun drawFluffyOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?) {
        if (gameView == null) {
            // Если GameView не передан — рисуем упрощённую версию
            drawFluffyFallbackMap(canvas, x, y)
            return
        }

        // Получаем кадры для анимации Флаффи
        val animationFrames = gameView.getMobAnimationFrames("fluffy", "idle")

        if (animationFrames.isNotEmpty()) {
            // Используем первый кадр (или анимируем медленно)
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]

            // Маленький размер для карты
            val displayWidth = 60f
            val displayHeight = 60f

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = gameView.getMobSpriteSheet("fluffy")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFluffyFallbackMap(canvas, x, y)
            }
        } else {
            drawFluffyFallbackMap(canvas, x, y)
        }
    }

    // ===== ЗАПАСНОЙ ВАРИАНТ ФЛАФФИ НА КАРТЕ =====
    private fun drawFluffyFallbackMap(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(255, 200, 200)
        canvas.drawCircle(x, y, 25f, paint)

        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f, y - 6f, 6f, paint)
        canvas.drawCircle(x + 8f, y - 6f, 6f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 10f, y - 6f, 3f, paint)
        canvas.drawCircle(x + 6f, y - 6f, 3f, paint)

        paint.color = Color.BLACK
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawArc(x - 8f, y + 4f, x + 8f, y + 12f, 0f, 180f, false, paint)
    }

    // ===== ПАУК НА КАРТЕ =====
    private fun drawSpiderOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?) {
        if (gameView == null) {
            drawSpiderFallback(canvas, x, y)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("spider", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]

            val displayWidth = 60f
            val displayHeight = 60f

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = gameView.getMobSpriteSheet("spider")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawSpiderFallback(canvas, x, y)
            }
        } else {
            drawSpiderFallback(canvas, x, y)
        }
    }

    // ===== ЗАПАСНОЙ ВАРИАНТ ПАУКА =====
    private fun drawSpiderFallback(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(100, 150, 50)
        canvas.drawCircle(x, y, 25f, paint)

        // Ножки
        paint.color = Color.rgb(80, 120, 40)
        paint.strokeWidth = 4f
        for (i in 0..3) {
            val angle = i * 60f + 30f
            val endX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * 35f
            val endY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * 35f
            canvas.drawLine(x, y, endX, endY, paint)
        }
        for (i in 0..3) {
            val angle = i * 60f + 210f
            val endX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * 35f
            val endY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * 35f
            canvas.drawLine(x, y, endX, endY, paint)
        }

        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f, y - 6f, 6f, paint)
        canvas.drawCircle(x + 8f, y - 6f, 6f, paint)
        paint.color = Color.RED
        canvas.drawCircle(x - 10f, y - 6f, 3f, paint)
        canvas.drawCircle(x + 6f, y - 6f, 3f, paint)
    }

    // ===== МНОГОГЛАЗ НА КАРТЕ =====
    private fun drawManyEyesOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?) {
        if (gameView == null) {
            drawManyEyesFallback(canvas, x, y)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("manyeyes", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]

            val displayWidth = 60f
            val displayHeight = 60f

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = gameView.getMobSpriteSheet("manyeyes")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawManyEyesFallback(canvas, x, y)
            }
        } else {
            drawManyEyesFallback(canvas, x, y)
        }
    }

    // ===== ЗАПАСНОЙ ВАРИАНТ МНОГОГЛАЗА =====
    private fun drawManyEyesFallback(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.rgb(150, 50, 200)
        canvas.drawCircle(x, y, 25f, paint)

        // Много глаз
        val eyePositions = listOf(
            -10f to -10f, 10f to -10f,
            -15f to 0f, 15f to 0f,
            -10f to 10f, 10f to 10f,
            0f to -15f, 0f to 15f
        )
        for ((ex, ey) in eyePositions) {
            paint.color = Color.WHITE
            canvas.drawCircle(x + ex, y + ey, 6f, paint)
            paint.color = Color.RED
            canvas.drawCircle(x + ex + 2f, y + ey + 1f, 3f, paint)
        }
    }

    private fun drawHPBar(canvas: Canvas, x: Float, y: Float, mob: Mob) {
        val hpPercent = mob.hp / mob.maxHp
        val hpPaint = Paint().apply { color = Color.argb(180, 0, 0, 0) }
        canvas.drawRect(x - 32f, y - 47f, x + 32f, y - 35f, hpPaint)
        hpPaint.color = when {
            hpPercent > 0.5f -> Color.GREEN
            hpPercent > 0.25f -> Color.YELLOW
            else -> Color.RED
        }
        canvas.drawRect(x - 30f, y - 45f, x - 30f + 60f * hpPercent, y - 37f, hpPaint)

        val typePaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(mob.getTypeName(), x, y + 50f, typePaint)
    }

    // ===== ДЕФОЛТНЫЙ МОБ =====
    private fun drawDefault(canvas: Canvas, x: Float, y: Float) {
        paint.color = Color.GRAY
        canvas.drawCircle(x, y, 25f, paint)

        paint.color = Color.WHITE
        canvas.drawCircle(x - 10f, y - 8f, 8f, paint)
        canvas.drawCircle(x + 10f, y - 8f, 8f, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 12f, y - 8f, 4f, paint)
        canvas.drawCircle(x + 8f, y - 8f, 4f, paint)
    }
}