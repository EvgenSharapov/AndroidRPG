package com.example.myapplication.renderer

import android.graphics.*
import com.example.myapplication.GameView
import com.example.myapplication.model.Mob
import kotlin.math.cos
import kotlin.math.sin

object MobRenderer {
    private val paint = Paint()
    private val eyePaint = Paint().apply { color = Color.WHITE }
    private val pupilPaint = Paint().apply { color = Color.BLACK }

    // Для отрисовки Флаффи на карте
    fun drawMob(canvas: Canvas, mob: Mob, drawX: Float, drawY: Float, isSelected: Boolean, gameView: GameView? = null) {
        if (mob.isDead) return

        val sizeMultiplier = if (mob.isBoss) 3.0f else 1.0f
        val baseSize = 45f * sizeMultiplier

        // Тень
        val shadowPaint = Paint().apply {
            color = Color.argb(60, 0, 0, 0)
        }
        canvas.drawOval(drawX - baseSize - 10f, drawY + baseSize + 5f, drawX + baseSize + 10f, drawY + baseSize + 20f, shadowPaint)

        // Свечение для босса
        if (mob.isBoss) {
            val glowPaint = Paint().apply {
                shader = RadialGradient(
                    drawX, drawY, 180f,
                    Color.argb(80, 255, 215, 0),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(drawX, drawY, 120f, glowPaint)
        }

        // ⭐ РИСУЕМ ВСЕХ МОБОВ (ДОБАВЛЕНЫ ТИПЫ 3, 4, 5)
        when (mob.type) {
            0 -> drawFluffyOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            1 -> drawSpiderOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            2 -> drawManyEyesOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            3 -> drawRedKnightOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            4 -> drawSlimeGreenOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            5 -> drawSteelKnightOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            6 -> drawGoblinOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            7 -> drawMonkOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            8 -> drawOrkOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            9 -> drawTrollOnMap(canvas, drawX, drawY, mob, gameView, sizeMultiplier)
            else -> drawDefault(canvas, drawX, drawY, sizeMultiplier)
        }

        drawHPBar(canvas, drawX, drawY, mob, sizeMultiplier)

        if (mob.isBoss) {
            val crownPaint = Paint().apply {
                textSize = 50f * sizeMultiplier
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("👑", drawX, drawY - baseSize - 30f * sizeMultiplier, crownPaint)
        }

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
        canvas.drawText(mob.getTypeName() + if (mob.isBoss) " 👑" else "", drawX, drawY + baseSize + 30f * sizeMultiplier, typePaint)

        if (isSelected) {
            val selectPaint = Paint().apply {
                color = Color.argb(80, 255, 255, 0)
                style = Paint.Style.STROKE
                strokeWidth = 3f * sizeMultiplier
            }
            canvas.drawCircle(drawX, drawY, baseSize + 10f, selectPaint)
        }
    }

    private fun drawFluffyOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawFluffyFallbackMap(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("fluffy", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]

            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier

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
                drawFluffyFallbackMap(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFluffyFallbackMap(canvas, x, y, sizeMultiplier)
        }
    }

    // ===== ЗАПАСНОЙ ВАРИАНТ ФЛАФФИ =====
    private fun drawFluffyFallbackMap(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(255, 200, 200)
        canvas.drawCircle(x, y, radius, paint)

        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        canvas.drawCircle(x + 6f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)

        paint.color = Color.BLACK
        paint.strokeWidth = 2f * sizeMultiplier
        paint.style = Paint.Style.STROKE
        canvas.drawArc(x - 8f * sizeMultiplier, y + 4f * sizeMultiplier, x + 8f * sizeMultiplier, y + 12f * sizeMultiplier, 0f, 180f, false, paint)
    }

    // ===== ПАУК НА КАРТЕ =====
    private fun drawSpiderOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawSpiderFallback(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("spider", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]

            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier

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
                drawSpiderFallback(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawSpiderFallback(canvas, x, y, sizeMultiplier)
        }
    }

    // ===== ЗАПАСНОЙ ВАРИАНТ ПАУКА =====
    private fun drawSpiderFallback(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(100, 150, 50)
        canvas.drawCircle(x, y, radius, paint)

        paint.color = Color.rgb(80, 120, 40)
        paint.strokeWidth = 4f * sizeMultiplier
        for (i in 0..3) {
            val angle = i * 60f + 30f
            val endX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * 35f * sizeMultiplier
            val endY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * 35f * sizeMultiplier
            canvas.drawLine(x, y, endX, endY, paint)
        }
        for (i in 0..3) {
            val angle = i * 60f + 210f
            val endX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * 35f * sizeMultiplier
            val endY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * 35f * sizeMultiplier
            canvas.drawLine(x, y, endX, endY, paint)
        }

        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        paint.color = Color.RED
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        canvas.drawCircle(x + 6f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
    }

    // ===== МНОГОГЛАЗ НА КАРТЕ =====
    private fun drawManyEyesOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawManyEyesFallback(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("manyeyes", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]

            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier

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
                drawManyEyesFallback(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawManyEyesFallback(canvas, x, y, sizeMultiplier)
        }
    }

    // ===== ЗАПАСНОЙ ВАРИАНТ МНОГОГЛАЗА =====
    private fun drawManyEyesFallback(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(150, 50, 200)
        canvas.drawCircle(x, y, radius, paint)

        val eyePositions = listOf(
            -10f to -10f, 10f to -10f,
            -15f to 0f, 15f to 0f,
            -10f to 10f, 10f to 10f,
            0f to -15f, 0f to 15f
        )
        for ((ex, ey) in eyePositions) {
            paint.color = Color.WHITE
            canvas.drawCircle(x + ex * sizeMultiplier, y + ey * sizeMultiplier, 6f * sizeMultiplier, paint)
            paint.color = Color.RED
            canvas.drawCircle(x + (ex + 2f) * sizeMultiplier, y + (ey + 1f) * sizeMultiplier, 3f * sizeMultiplier, paint)
        }
    }

    // ============================================================
    // 🔴 КРАСНЫЙ РЫЦАРЬ (ТИП 3) — НОВЫЙ
    // ============================================================
    private fun drawRedKnightOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawFallbackRedKnight(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("red_knight", "idle")
        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]
            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier
            val dstRect = RectF(x - displayWidth / 2, y - displayHeight / 2, x + displayWidth / 2, y + displayHeight / 2)
            val spriteSheet = gameView.getMobSpriteSheet("red_knight")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackRedKnight(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFallbackRedKnight(canvas, x, y, sizeMultiplier)
        }
    }

    private fun drawFallbackRedKnight(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(200, 50, 50)
        canvas.drawCircle(x, y, radius, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        canvas.drawCircle(x + 6f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
    }

    // ============================================================
    // 🟢 ЗЕЛЁНЫЙ СЛИЗЕНЬ (ТИП 4) — НОВЫЙ
    // ============================================================
    private fun drawSlimeGreenOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawFallbackSlimeGreen(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("slime_green", "idle")
        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]
            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier
            val dstRect = RectF(x - displayWidth / 2, y - displayHeight / 2, x + displayWidth / 2, y + displayHeight / 2)
            val spriteSheet = gameView.getMobSpriteSheet("slime_green")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackSlimeGreen(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFallbackSlimeGreen(canvas, x, y, sizeMultiplier)
        }
    }

    private fun drawFallbackSlimeGreen(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(100, 200, 100)
        canvas.drawCircle(x, y, radius, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        canvas.drawCircle(x + 6f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        paint.strokeWidth = 2f * sizeMultiplier
        paint.style = Paint.Style.STROKE
        canvas.drawArc(x - 8f * sizeMultiplier, y + 4f * sizeMultiplier, x + 8f * sizeMultiplier, y + 12f * sizeMultiplier, 0f, 180f, false, paint)
    }

    // ============================================================
    // ⚪ СТАЛЬНОЙ РЫЦАРЬ (ТИП 5) — НОВЫЙ
    // ============================================================
    private fun drawSteelKnightOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawFallbackSteelKnight(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("steel_knight", "idle")
        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]
            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier
            val dstRect = RectF(x - displayWidth / 2, y - displayHeight / 2, x + displayWidth / 2, y + displayHeight / 2)
            val spriteSheet = gameView.getMobSpriteSheet("steel_knight")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackSteelKnight(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFallbackSteelKnight(canvas, x, y, sizeMultiplier)
        }
    }

    private fun drawFallbackSteelKnight(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(150, 150, 200)
        canvas.drawCircle(x, y, radius, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        canvas.drawCircle(x + 6f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
    }

    // ===== ГОБЛИН НА КАРТЕ =====
    private fun drawGoblinOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawFallbackGoblin(canvas, x, y, sizeMultiplier)
            return
        }

        val animName = if (mob.isBoss) "idle" else "idle"
        val animationFrames = gameView.getMobAnimationFrames("goblin", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]
            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier
            val dstRect = RectF(x - displayWidth / 2, y - displayHeight / 2, x + displayWidth / 2, y + displayHeight / 2)
            val spriteSheet = gameView.getMobSpriteSheet("goblin")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackGoblin(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFallbackGoblin(canvas, x, y, sizeMultiplier)
        }
    }

    // монах
    private fun drawMonkOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawFallbackMonk(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("monk", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]
            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = gameView.getMobSpriteSheet("monk")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackMonk(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFallbackMonk(canvas, x, y, sizeMultiplier)
        }
    }

    private fun drawFallbackMonk(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(200, 180, 100)
        canvas.drawCircle(x, y, radius, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        canvas.drawCircle(x + 6f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        paint.color = Color.rgb(255, 100, 50)
        canvas.drawCircle(x, y - 20f * sizeMultiplier, 4f * sizeMultiplier, paint)
    }

    private fun drawFallbackGoblin(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(50, 180, 50)
        canvas.drawCircle(x, y, radius, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        canvas.drawCircle(x + 6f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
    }

    // ===== ОРК НА КАРТЕ =====
    private fun drawOrkOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawFallbackOrk(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("ork", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]
            val displayWidth = 100f * sizeMultiplier
            val displayHeight = 100f * sizeMultiplier

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = gameView.getMobSpriteSheet("ork")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackOrk(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFallbackOrk(canvas, x, y, sizeMultiplier)
        }
    }

    private fun drawFallbackOrk(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 25f * sizeMultiplier
        paint.color = Color.rgb(100, 180, 80)
        canvas.drawCircle(x, y, radius, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(x - 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 6f * sizeMultiplier, 6f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        canvas.drawCircle(x + 6f * sizeMultiplier, y - 6f * sizeMultiplier, 3f * sizeMultiplier, paint)
        // Клыки орка
        paint.color = Color.WHITE
        paint.strokeWidth = 3f * sizeMultiplier
        canvas.drawLine(x - 6f * sizeMultiplier, y + 8f * sizeMultiplier, x - 10f * sizeMultiplier, y + 16f * sizeMultiplier, paint)
        canvas.drawLine(x + 6f * sizeMultiplier, y + 8f * sizeMultiplier, x + 10f * sizeMultiplier, y + 16f * sizeMultiplier, paint)
    }

    // ===== ТРОЛЛЬ НА КАРТЕ =====
    private fun drawTrollOnMap(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView?, sizeMultiplier: Float = 1f) {
        if (gameView == null) {
            drawFallbackTroll(canvas, x, y, sizeMultiplier)
            return
        }

        val animationFrames = gameView.getMobAnimationFrames("troll", "idle")

        if (animationFrames.isNotEmpty()) {
            val frameIndex = (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            val currentFrame = animationFrames[frameIndex]
            val displayWidth = 120f * sizeMultiplier  // Тролль чуть больше
            val displayHeight = 120f * sizeMultiplier

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = gameView.getMobSpriteSheet("troll")
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackTroll(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFallbackTroll(canvas, x, y, sizeMultiplier)
        }
    }

    private fun drawFallbackTroll(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
        val radius = 30f * sizeMultiplier
        paint.color = Color.rgb(150, 100, 200)
        canvas.drawCircle(x, y, radius, paint)
        paint.color = Color.WHITE
        canvas.drawCircle(x - 10f * sizeMultiplier, y - 8f * sizeMultiplier, 8f * sizeMultiplier, paint)
        canvas.drawCircle(x + 10f * sizeMultiplier, y - 8f * sizeMultiplier, 8f * sizeMultiplier, paint)
        paint.color = Color.BLACK
        canvas.drawCircle(x - 12f * sizeMultiplier, y - 8f * sizeMultiplier, 4f * sizeMultiplier, paint)
        canvas.drawCircle(x + 8f * sizeMultiplier, y - 8f * sizeMultiplier, 4f * sizeMultiplier, paint)
        // Рога тролля
        paint.color = Color.rgb(180, 180, 200)
        paint.strokeWidth = 4f * sizeMultiplier
        canvas.drawLine(x - 14f * sizeMultiplier, y - 20f * sizeMultiplier, x - 20f * sizeMultiplier, y - 40f * sizeMultiplier, paint)
        canvas.drawLine(x + 14f * sizeMultiplier, y - 20f * sizeMultiplier, x + 20f * sizeMultiplier, y - 40f * sizeMultiplier, paint)
    }

    // ===== HP BAR (УВЕЛИЧЕННЫЙ ДЛЯ БОССА) =====
    private fun drawHPBar(canvas: Canvas, x: Float, y: Float, mob: Mob, sizeMultiplier: Float = 1f) {
        val hpPercent = mob.hp / mob.maxHp
        val barWidth = 60f * sizeMultiplier
        val barHeight = 12f * sizeMultiplier
        val hpPaint = Paint().apply { color = Color.argb(180, 0, 0, 0) }
        canvas.drawRect(x - barWidth / 2 - 2f, y - 47f * sizeMultiplier - 2f, x + barWidth / 2 + 2f, y - 35f * sizeMultiplier + 2f, hpPaint)
        hpPaint.color = when {
            mob.isBoss -> Color.rgb(255, 215, 0)  // Босс — золотой HP бар
            hpPercent > 0.5f -> Color.GREEN
            hpPercent > 0.25f -> Color.YELLOW
            else -> Color.RED
        }
        canvas.drawRect(x - barWidth / 2, y - 47f * sizeMultiplier, x - barWidth / 2 + barWidth * hpPercent, y - 35f * sizeMultiplier, hpPaint)
    }

    // ===== ДЕФОЛТНЫЙ МОБ =====
    private fun drawDefault(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float = 1f) {
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