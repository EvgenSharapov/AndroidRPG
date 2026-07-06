package com.example.myapplication.renderer

import android.graphics.*
import android.os.Handler
import android.os.Looper
import com.example.myapplication.GameView
import com.example.myapplication.manager.BattleManager
import com.example.myapplication.model.Player
import kotlin.math.*

class BattleRenderer {
    companion object {
        private val paint = Paint()
        private val textPaint = Paint()
        private val bgPaint = Paint()
        private val random = java.util.Random()

        // Анимационные переменные
        private var hitEffectTimer = 0
        private var hitEffectX = 0f
        private var hitEffectY = 0f
        private var shakeTimer = 0
        private var shakeX = 0f
        private var shakeY = 0f
        private var damageNumbers = mutableListOf<DamageNumber>()
        private var attackAnimTimer = 0
        private var isPlayerAttacking = false
        private var isMobAttacking = false
        private var mobKnockbackX = 0f
        private var mobKnockbackY = 0f
        private var currentLocation: String = "Город"

        // ⭐ АНИМАЦИЯ АТАКИ МОБОВ
        private var isSpiderAttacking = false
        private var isManyEyesAttacking = false
        private var isRedKnightAttacking = false
        private var isSlimeGreenAttacking = false
        private var isSteelKnightAttacking = false
        private var isGoblinAttacking = false
        private var attackAnimTimerMob = 0
        private var attackFrameIndex = 0

        // Анимация Флаффи
        private var fluffyAttackTimer = 0
        private var isFluffyAttacking = false
        private var fluffyOffsetX = 0f
        private var fluffyFrameIndex = 0
        private var fluffyFrameTimer = 0
        private val fluffyFrames = listOf(
            Color.rgb(255, 220, 220),
            Color.rgb(255, 200, 200),
            Color.rgb(255, 180, 180),
            Color.rgb(255, 200, 200)
        )

        data class DamageNumber(
            var x: Float,
            var y: Float,
            var text: String,
            var life: Int,
            var maxLife: Int,
            var vy: Float,
            var color: Int
        )

        fun drawBattle(
            canvas: Canvas,
            width: Float,
            height: Float,
            manager: BattleManager,
            player: Player,
            location: String = "Город",
            gameView: GameView
        ) {
            val mob = manager.currentMob ?: return
            currentLocation = location

            // Обновляем анимации
            updateAnimations()

            // --- ФОН ---
            drawBattleBackground(canvas, width, height, location)

            // --- ТРЯСКА ---
            if (shakeTimer > 0) {
                shakeTimer--
                shakeX = (random.nextFloat() - 0.5f) * shakeTimer / 2
                shakeY = (random.nextFloat() - 0.5f) * shakeTimer / 2
            } else {
                shakeX = 0f
                shakeY = 0f
            }
            canvas.translate(shakeX, shakeY)

            // --- РАМКА ---
            drawBattleFrame(canvas, width, height)

            // --- ИГРОК ---
            val playerX = width * 0.25f
            val playerY = height * 0.55f
            gameView.drawBattlePlayer(canvas, playerX, playerY, 2.5f)

            // --- МОБ ---
            val mobX = width * 0.75f + mobKnockbackX
            val mobY = height * 0.55f + mobKnockbackY
            drawMobBattle(canvas, mobX, mobY, mob, gameView)

            // --- ЭФФЕКТЫ ---
            drawHitEffects(canvas)
            drawDamageNumbers(canvas)
            drawHPBars(canvas, width, height, player, mob)
            drawNames(canvas, width, height, player, mob)

            // --- КНОПКА АТАКИ ---
            if (manager.state == BattleManager.BattleState.PLAYER_TURN) {
                drawAttackButton(canvas, width, height)
            }

            // --- СТАТУС ---
            drawBattleStatus(canvas, width, height, manager)

            canvas.translate(-shakeX, -shakeY)
        }

        private fun updateAnimations() {
            // Анимация игрока
            if (isPlayerAttacking) {
                attackAnimTimer++
                if (attackAnimTimer > 25) {
                    isPlayerAttacking = false
                    attackAnimTimer = 0
                }
            }

            // ⭐ АНИМАЦИЯ АТАКИ МОБОВ - ИСПРАВЛЕНО
            if (isMobAttacking) {
                attackAnimTimerMob++

                // Меняем кадр каждые 3-4 тика
                if (attackAnimTimerMob % 2 == 0) {  // ← было 4, стало 3 (чуть быстрее)
                    attackFrameIndex++
                }

                // Определяем максимальное количество кадров для текущего моба
                val maxFrames = when {
                    isSpiderAttacking -> 8
                    isManyEyesAttacking -> 8
                    isRedKnightAttacking -> 8
                    isSlimeGreenAttacking -> 6
                    isSteelKnightAttacking -> 8
                    isGoblinAttacking -> 8
                    else -> 6
                }

                // ⭐ СБРАСЫВАЕМ, КОГДА ПОКАЗАНЫ ВСЕ КАДРЫ
                if (attackFrameIndex >= maxFrames) {
                    isMobAttacking = false
                    isSpiderAttacking = false
                    isManyEyesAttacking = false
                    isRedKnightAttacking = false
                    isSlimeGreenAttacking = false
                    isSteelKnightAttacking = false
                    isGoblinAttacking = false
                    attackAnimTimerMob = 0
                    attackFrameIndex = 0
                }
            }

            // Анимация Флаффи
            if (isFluffyAttacking) {
                fluffyAttackTimer++
                fluffyFrameTimer++
                if (fluffyFrameTimer > 6) {
                    fluffyFrameTimer = 0
                    fluffyFrameIndex = (fluffyFrameIndex + 1) % 4
                }

                if (fluffyAttackTimer < 20) {
                    fluffyOffsetX = (fluffyAttackTimer / 20f) * 120f
                } else if (fluffyAttackTimer < 40) {
                    fluffyOffsetX = (40f - fluffyAttackTimer) / 20f * 120f
                } else {
                    fluffyOffsetX = 0f
                    isFluffyAttacking = false
                    fluffyAttackTimer = 0
                    fluffyFrameIndex = 0
                }
            }

            // Отскок моба
            mobKnockbackX *= 0.9f
            mobKnockbackY *= 0.9f
            if (abs(mobKnockbackX) < 0.1f) mobKnockbackX = 0f
            if (abs(mobKnockbackY) < 0.1f) mobKnockbackY = 0f

            damageNumbers.removeAll { dn ->
                dn.y += dn.vy
                dn.vy += 0.3f
                dn.life--
                dn.life <= 0
            }

            if (hitEffectTimer > 0) hitEffectTimer--
        }

        // ===== ФОНЫ =====
        private fun drawBattleBackground(canvas: Canvas, width: Float, height: Float, location: String) {
            when (location) {
                "Город" -> drawCityBattleBg(canvas, width, height)
                "Лес" -> drawForestBattleBg(canvas, width, height)
                "Пустошь" -> drawWastelandBattleBg(canvas, width, height)
                else -> drawCityBattleBg(canvas, width, height)
            }
        }

        private fun drawCityBattleBg(canvas: Canvas, width: Float, height: Float) {
            val gradient = LinearGradient(
                0f, 0f, width * 0.5f, height,
                Color.rgb(80, 60, 40),
                Color.rgb(40, 30, 20),
                Shader.TileMode.CLAMP
            )
            bgPaint.shader = gradient
            canvas.drawRect(0f, 0f, width, height, bgPaint)

            paint.color = Color.argb(30, 255, 200, 100)
            for (i in 0..10) {
                val x = width * random.nextFloat()
                val y = height * random.nextFloat()
                canvas.drawCircle(x, y, 20f + random.nextFloat() * 50f, paint)
            }
        }

        private fun drawForestBattleBg(canvas: Canvas, width: Float, height: Float) {
            val gradient = LinearGradient(
                0f, 0f, width * 0.5f, height,
                Color.rgb(20, 50, 20),
                Color.rgb(10, 30, 10),
                Shader.TileMode.CLAMP
            )
            bgPaint.shader = gradient
            canvas.drawRect(0f, 0f, width, height, bgPaint)

            paint.color = Color.argb(50, 200, 255, 100)
            for (i in 0..15) {
                val x = width * random.nextFloat()
                val y = height * random.nextFloat()
                canvas.drawCircle(x, y, 3f + random.nextFloat() * 8f, paint)
            }
        }

        private fun drawWastelandBattleBg(canvas: Canvas, width: Float, height: Float) {
            val gradient = LinearGradient(
                0f, 0f, width * 0.5f, height,
                Color.rgb(80, 50, 30),
                Color.rgb(40, 25, 15),
                Shader.TileMode.CLAMP
            )
            bgPaint.shader = gradient
            canvas.drawRect(0f, 0f, width, height, bgPaint)

            paint.color = Color.argb(30, 200, 180, 150)
            for (i in 0..8) {
                val x = width * random.nextFloat()
                val y = height * random.nextFloat()
                canvas.drawCircle(x, y, 30f + random.nextFloat() * 60f, paint)
            }
        }

        private fun drawBattleFrame(canvas: Canvas, width: Float, height: Float) {
            paint.color = Color.argb(150, 255, 215, 0)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            canvas.drawRoundRect(20f, 100f, width - 20f, height - 30f, 20f, 20f, paint)

            paint.strokeWidth = 6f
            val cornerSize = 50f
            val corners = listOf(20f to 100f, width - 20f to 100f, 20f to height - 30f, width - 20f to height - 30f)
            for ((cx, cy) in corners) {
                val dx = if (cx > width / 2) -cornerSize else cornerSize
                val dy = if (cy > height / 2) -cornerSize else cornerSize
                canvas.drawLine(cx, cy, cx + dx, cy, paint)
                canvas.drawLine(cx, cy, cx, cy + dy, paint)
            }
        }

        // ===== РИСОВАНИЕ МОБА =====
        private fun drawMobBattle(canvas: Canvas, x: Float, y: Float, mob: com.example.myapplication.model.Mob, gameView: GameView) {
            println("⚔️ drawMobBattle() для ${mob.getTypeName()} (тип ${mob.type})")
            val bossScale = if (mob.isBoss) 1.8f else 1f
            val scale = 2.5f * bossScale

            var finalX = x
            var finalY = y
            if (mob.type == 0 && isFluffyAttacking) {
                finalX = x + fluffyOffsetX
                finalY = y - 20f * sin(fluffyAttackTimer / 20f * PI.toFloat())
            }

            // Тень
            paint.color = Color.argb(80, 0, 0, 0)
            canvas.drawOval(finalX - 80f * scale, finalY + 50f * scale, finalX + 80f * scale, finalY + 80f * scale, paint)

            // Свечение для босса
            if (mob.isBoss) {
                val glowPaint = Paint().apply {
                    shader = RadialGradient(
                        finalX, finalY, 200f * scale,
                        Color.argb(80, 255, 215, 0),
                        Color.TRANSPARENT,
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(finalX, finalY, 200f * scale, glowPaint)

                val pulse = (30 + sin(System.currentTimeMillis() / 150.0) * 15).toInt()
                val bossAura = Paint().apply {
                    color = Color.argb(pulse, 255, 215, 0)
                    style = Paint.Style.STROKE
                    strokeWidth = 8f
                }
                canvas.drawCircle(finalX, finalY, 100f * scale, bossAura)
            }

            // Аура атаки
            if (isMobAttacking && mob.type != 0) {
                val pulse = (30 + sin(System.currentTimeMillis() / 80.0) * 20).toInt()
                paint.color = Color.argb(pulse, 255, 0, 0)
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 6f * scale
                canvas.drawCircle(finalX, finalY, 80f * scale, paint)
            }

            // ⭐ РИСУЕМ ВСЕХ МОБОВ С АНИМАЦИЕЙ АТАКИ
            when (mob.type) {
                0 -> drawFluffyBattle(canvas, finalX, finalY, scale, gameView)
                1 -> drawSpiderBattle(canvas, finalX, finalY, scale, gameView)
                2 -> drawManyEyesBattle(canvas, finalX, finalY, scale, gameView)
                3 -> drawRedKnightBattle(canvas, finalX, finalY, scale, gameView)
                4 -> drawSlimeGreenBattle(canvas, finalX, finalY, scale, gameView)
                5 -> drawSteelKnightBattle(canvas, finalX, finalY, scale, gameView)
                6 -> drawGoblinBattle(canvas, finalX, finalY, scale, gameView)
            }

            // Корона для босса
            if (mob.isBoss) {
                val crownPaint = Paint().apply {
                    textSize = 80f * bossScale
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("👑", finalX, finalY - 90f * scale, crownPaint)
            }
        }

        // ===== ФЛАФФИ (БОЙ) =====
        private fun drawFluffyBattle(canvas: Canvas, x: Float, y: Float, scale: Float, gameView: GameView) {
            val spriteName = if (isFluffyAttacking) "fluffy" else "fluffy"
            val animName = if (isFluffyAttacking) "run" else "idle"

            val animationFrames = gameView.getMobAnimationFrames(spriteName, animName)

            if (animationFrames.isNotEmpty()) {
                val frameIndex = if (isFluffyAttacking) {
                    (fluffyAttackTimer / 6) % animationFrames.size
                } else {
                    0
                }

                val currentFrame = animationFrames[frameIndex.coerceAtMost(animationFrames.size - 1)]

                val displayWidth = 150f * scale
                val displayHeight = 150f * scale

                val dstRect = RectF(
                    x - displayWidth / 2,
                    y - displayHeight / 2,
                    x + displayWidth / 2,
                    y + displayHeight / 2
                )

                val spriteSheet = gameView.getMobSpriteSheet(spriteName)
                if (spriteSheet != null) {
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                } else {
                    drawFluffyFallback(canvas, x, y, scale)
                }
            } else {
                drawFluffyFallback(canvas, x, y, scale)
            }
        }

        private fun drawFluffyFallback(canvas: Canvas, x: Float, y: Float, scale: Float) {
            val color = fluffyFrames[fluffyFrameIndex]
            paint.color = color
            paint.style = Paint.Style.FILL

            canvas.drawCircle(x, y, 70f * scale, paint)
            for (i in 0..7) {
                val angle = i * 45f + fluffyFrameIndex * 10f
                val offsetX = cos(angle) * 25f * scale
                val offsetY = sin(angle) * 25f * scale
                canvas.drawCircle(x + offsetX, y + offsetY, 20f * scale, paint)
            }

            paint.color = Color.rgb(255, 235, 235)
            canvas.drawOval(x - 40f * scale, y - 50f * scale, x - 10f * scale, y - 20f * scale, paint)
            canvas.drawOval(x + 10f * scale, y - 50f * scale, x + 40f * scale, y - 20f * scale, paint)

            paint.color = Color.WHITE
            canvas.drawCircle(x - 30f * scale, y - 20f * scale, 18f * scale, paint)
            canvas.drawCircle(x + 30f * scale, y - 20f * scale, 18f * scale, paint)
            paint.color = Color.rgb(50, 50, 150)
            canvas.drawCircle(x - 28f * scale, y - 18f * scale, 12f * scale, paint)
            canvas.drawCircle(x + 32f * scale, y - 18f * scale, 12f * scale, paint)
            paint.color = Color.BLACK
            canvas.drawCircle(x - 25f * scale, y - 16f * scale, 6f * scale, paint)
            canvas.drawCircle(x + 35f * scale, y - 16f * scale, 6f * scale, paint)

            paint.color = Color.argb(80, 255, 100, 100)
            canvas.drawCircle(x - 50f * scale, y + 10f * scale, 15f * scale, paint)
            canvas.drawCircle(x + 50f * scale, y + 10f * scale, 15f * scale, paint)

            paint.color = Color.rgb(200, 100, 100)
            paint.strokeWidth = 4f * scale
            paint.style = Paint.Style.STROKE
            canvas.drawArc(x - 25f * scale, y + 10f * scale, x + 25f * scale, y + 35f * scale, 0f, -180f, false, paint)
        }

        // ===== ПАУК (БОЙ) С АНИМАЦИЕЙ АТАКИ =====
        private fun drawSpiderBattle(canvas: Canvas, x: Float, y: Float, scale: Float, gameView: GameView) {
            val animName = if (isSpiderAttacking) "attack" else "idle"
            val animationFrames = gameView.getMobAnimationFrames("spider_battle", animName)

            if (animationFrames.isNotEmpty()) {
                val frameIndex = if (isSpiderAttacking) {
                    attackFrameIndex % animationFrames.size
                } else {
                    (System.currentTimeMillis() / 200 % animationFrames.size).toInt()
                }
                val currentFrame = animationFrames[frameIndex % animationFrames.size]

                val displayWidth = 150f * scale
                val displayHeight = 150f * scale

                val dstRect = RectF(
                    x - displayWidth / 2,
                    y - displayHeight / 2,
                    x + displayWidth / 2,
                    y + displayHeight / 2
                )

                val spriteSheet = gameView.getMobSpriteSheet("spider_battle")
                if (spriteSheet != null) {
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                } else {
                    drawSpiderFallbackBattle(canvas, x, y, scale)
                }
            } else {
                drawSpiderFallbackBattle(canvas, x, y, scale)
            }
        }

        private fun drawSpiderFallbackBattle(canvas: Canvas, x: Float, y: Float, scale: Float) {
            paint.color = Color.rgb(100, 150, 50)
            canvas.drawCircle(x, y, 60f * scale, paint)

            paint.color = Color.rgb(80, 120, 40)
            paint.strokeWidth = 8f * scale
            for (i in 0..3) {
                val angle = i * 60f + 30f
                val endX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * 80f * scale
                val endY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * 80f * scale
                canvas.drawLine(x, y, endX, endY, paint)
            }
            for (i in 0..3) {
                val angle = i * 60f + 210f
                val endX = x + cos(Math.toRadians(angle.toDouble())).toFloat() * 80f * scale
                val endY = y + sin(Math.toRadians(angle.toDouble())).toFloat() * 80f * scale
                canvas.drawLine(x, y, endX, endY, paint)
            }

            paint.color = Color.WHITE
            canvas.drawCircle(x - 20f * scale, y - 15f * scale, 14f * scale, paint)
            canvas.drawCircle(x + 20f * scale, y - 15f * scale, 14f * scale, paint)
            paint.color = Color.RED
            canvas.drawCircle(x - 23f * scale, y - 15f * scale, 7f * scale, paint)
            canvas.drawCircle(x + 17f * scale, y - 15f * scale, 7f * scale, paint)
        }

        // ===== МНОГОГЛАЗ (БОЙ) С АНИМАЦИЕЙ АТАКИ =====
        private fun drawManyEyesBattle(canvas: Canvas, x: Float, y: Float, scale: Float, gameView: GameView) {
            val animName = if (isManyEyesAttacking) "attack" else "idle"
            val animationFrames = gameView.getMobAnimationFrames("manyeyes_battle", animName)

            if (animationFrames.isNotEmpty()) {
                val frameIndex = if (isManyEyesAttacking) {
                    attackFrameIndex % animationFrames.size
                } else {
                    (System.currentTimeMillis() / 200 % animationFrames.size).toInt()
                }
                val currentFrame = animationFrames[frameIndex % animationFrames.size]

                val displayWidth = 150f * scale
                val displayHeight = 150f * scale

                val dstRect = RectF(
                    x - displayWidth / 2,
                    y - displayHeight / 2,
                    x + displayWidth / 2,
                    y + displayHeight / 2
                )

                val spriteSheet = gameView.getMobSpriteSheet("manyeyes_battle")
                if (spriteSheet != null) {
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                } else {
                    drawManyEyesFallbackBattle(canvas, x, y, scale)
                }
            } else {
                drawManyEyesFallbackBattle(canvas, x, y, scale)
            }
        }

        private fun drawManyEyesFallbackBattle(canvas: Canvas, x: Float, y: Float, scale: Float) {
            paint.color = Color.rgb(150, 50, 200)
            canvas.drawCircle(x, y, 60f * scale, paint)

            val eyePositions = listOf(
                -25f to -25f, 25f to -25f,
                -35f to 0f, 35f to 0f,
                -25f to 25f, 25f to 25f,
                0f to -35f, 0f to 35f
            )
            for ((ex, ey) in eyePositions) {
                paint.color = Color.WHITE
                canvas.drawCircle(x + ex * scale, y + ey * scale, 14f * scale, paint)
                paint.color = Color.RED
                canvas.drawCircle(x + (ex + 3f) * scale, y + (ey + 2f) * scale, 7f * scale, paint)
            }
        }

        // ===== КРАСНЫЙ РЫЦАРЬ (БОЙ) С АНИМАЦИЕЙ АТАКИ =====
        private fun drawRedKnightBattle(canvas: Canvas, x: Float, y: Float, scale: Float, gameView: GameView) {
            val animName = if (isRedKnightAttacking) "attack" else "idle"
            val animationFrames = gameView.getMobAnimationFrames("red_knight_battle", animName)
            if (animationFrames.isNotEmpty()) {
                val frameIndex = if (isRedKnightAttacking) {
                    attackFrameIndex % animationFrames.size
                } else {
                    (System.currentTimeMillis() / 200 % animationFrames.size).toInt()
                }
                val currentFrame = animationFrames[frameIndex % animationFrames.size]
                val displayWidth = 150f * scale
                val displayHeight = 150f * scale
                val dstRect = RectF(x - displayWidth / 2, y - displayHeight / 2, x + displayWidth / 2, y + displayHeight / 2)
                val spriteSheet = gameView.getMobSpriteSheet("red_knight_battle")
                if (spriteSheet != null) {
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                } else {
                    drawFallbackRedKnightBattle(canvas, x, y, scale)
                }
            } else {
                drawFallbackRedKnightBattle(canvas, x, y, scale)
            }
        }

        private fun drawFallbackRedKnightBattle(canvas: Canvas, x: Float, y: Float, scale: Float) {
            paint.color = Color.rgb(200, 50, 50)
            canvas.drawCircle(x, y, 60f * scale, paint)
            paint.color = Color.WHITE
            canvas.drawCircle(x - 20f * scale, y - 15f * scale, 14f * scale, paint)
            canvas.drawCircle(x + 20f * scale, y - 15f * scale, 14f * scale, paint)
            paint.color = Color.BLACK
            canvas.drawCircle(x - 23f * scale, y - 15f * scale, 7f * scale, paint)
            canvas.drawCircle(x + 17f * scale, y - 15f * scale, 7f * scale, paint)
        }

        // ===== ЗЕЛЁНЫЙ СЛИЗЕНЬ (БОЙ) С АНИМАЦИЕЙ АТАКИ =====
        private fun drawSlimeGreenBattle(canvas: Canvas, x: Float, y: Float, scale: Float, gameView: GameView) {
            val animName = if (isSlimeGreenAttacking) "attack" else "idle"
            val animationFrames = gameView.getMobAnimationFrames("slime_green_battle", animName)
            if (animationFrames.isNotEmpty()) {
                val frameIndex = if (isSlimeGreenAttacking) {
                    attackFrameIndex % animationFrames.size
                } else {
                    (System.currentTimeMillis() / 200 % animationFrames.size).toInt()
                }
                val currentFrame = animationFrames[frameIndex % animationFrames.size]
                val displayWidth = 150f * scale
                val displayHeight = 150f * scale
                val dstRect = RectF(x - displayWidth / 2, y - displayHeight / 2, x + displayWidth / 2, y + displayHeight / 2)
                val spriteSheet = gameView.getMobSpriteSheet("slime_green_battle")
                if (spriteSheet != null) {
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                } else {
                    drawFallbackSlimeGreenBattle(canvas, x, y, scale)
                }
            } else {
                drawFallbackSlimeGreenBattle(canvas, x, y, scale)
            }
        }

        private fun drawFallbackSlimeGreenBattle(canvas: Canvas, x: Float, y: Float, scale: Float) {
            paint.color = Color.rgb(100, 200, 100)
            canvas.drawCircle(x, y, 60f * scale, paint)
            paint.color = Color.WHITE
            canvas.drawCircle(x - 20f * scale, y - 15f * scale, 14f * scale, paint)
            canvas.drawCircle(x + 20f * scale, y - 15f * scale, 14f * scale, paint)
            paint.color = Color.BLACK
            canvas.drawCircle(x - 23f * scale, y - 15f * scale, 7f * scale, paint)
            canvas.drawCircle(x + 17f * scale, y - 15f * scale, 7f * scale, paint)
            paint.color = Color.BLACK
            paint.strokeWidth = 4f * scale
            paint.style = Paint.Style.STROKE
            canvas.drawArc(x - 20f * scale, y + 10f * scale, x + 20f * scale, y + 30f * scale, 0f, 180f, false, paint)
        }

        // ===== СТАЛЬНОЙ РЫЦАРЬ (БОЙ) С АНИМАЦИЕЙ АТАКИ =====
        private fun drawSteelKnightBattle(canvas: Canvas, x: Float, y: Float, scale: Float, gameView: GameView) {
            val animName = if (isSteelKnightAttacking) "attack" else "idle"
            val animationFrames = gameView.getMobAnimationFrames("steel_knight_battle", animName)
            if (animationFrames.isNotEmpty()) {
                val frameIndex = if (isSteelKnightAttacking) {
                    attackFrameIndex % animationFrames.size
                } else {
                    (System.currentTimeMillis() / 200 % animationFrames.size).toInt()
                }
                val currentFrame = animationFrames[frameIndex % animationFrames.size]
                val displayWidth = 150f * scale
                val displayHeight = 150f * scale
                val dstRect = RectF(x - displayWidth / 2, y - displayHeight / 2, x + displayWidth / 2, y + displayHeight / 2)
                val spriteSheet = gameView.getMobSpriteSheet("steel_knight_battle")
                if (spriteSheet != null) {
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                } else {
                    drawFallbackSteelKnightBattle(canvas, x, y, scale)
                }
            } else {
                drawFallbackSteelKnightBattle(canvas, x, y, scale)
            }
        }

        private fun drawFallbackSteelKnightBattle(canvas: Canvas, x: Float, y: Float, scale: Float) {
            paint.color = Color.rgb(150, 150, 200)
            canvas.drawCircle(x, y, 60f * scale, paint)
            paint.color = Color.WHITE
            canvas.drawCircle(x - 20f * scale, y - 15f * scale, 14f * scale, paint)
            canvas.drawCircle(x + 20f * scale, y - 15f * scale, 14f * scale, paint)
            paint.color = Color.BLACK
            canvas.drawCircle(x - 23f * scale, y - 15f * scale, 7f * scale, paint)
            canvas.drawCircle(x + 17f * scale, y - 15f * scale, 7f * scale, paint)
            paint.color = Color.rgb(180, 180, 200)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f * scale
            canvas.drawArc(x - 25f * scale, y - 25f * scale, x + 25f * scale, y + 5f * scale, 0f, 180f, false, paint)
        }

        // ===== ГОБЛИН (БОЙ) С АНИМАЦИЕЙ АТАКИ =====
        private fun drawGoblinBattle(canvas: Canvas, x: Float, y: Float, scale: Float, gameView: GameView) {
            val animName = if (isGoblinAttacking) "attack" else "idle"
            val animationFrames = gameView.getMobAnimationFrames("goblin", animName)

            if (animationFrames.isNotEmpty()) {
                val frameIndex = if (isGoblinAttacking) {
                    attackFrameIndex % animationFrames.size
                } else {
                    (System.currentTimeMillis() / 200 % animationFrames.size).toInt()
                }
                val currentFrame = animationFrames[frameIndex % animationFrames.size]

                // ⭐ УВЕЛИЧЕННЫЙ РАЗМЕР ДЛЯ БОЯ
                val battleScale = 2.5f
                val displayWidth = 80f * scale * battleScale
                val displayHeight = 80f * scale * battleScale

                val dstRect = RectF(
                    x - displayWidth / 2,
                    y - displayHeight / 2,
                    x + displayWidth / 2,
                    y + displayHeight / 2
                )

                val spriteSheet = gameView.getMobSpriteSheet("goblin")
                if (spriteSheet != null) {
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                } else {
                    drawFallbackGoblinBattle(canvas, x, y, scale)
                }
            } else {
                drawFallbackGoblinBattle(canvas, x, y, scale)
            }
        }

        private fun drawFallbackGoblinBattle(canvas: Canvas, x: Float, y: Float, scale: Float) {
            paint.color = Color.rgb(50, 180, 50)
            canvas.drawCircle(x, y, 60f * scale, paint)
            paint.color = Color.WHITE
            canvas.drawCircle(x - 20f * scale, y - 15f * scale, 14f * scale, paint)
            canvas.drawCircle(x + 20f * scale, y - 15f * scale, 14f * scale, paint)
            paint.color = Color.BLACK
            canvas.drawCircle(x - 23f * scale, y - 15f * scale, 7f * scale, paint)
            canvas.drawCircle(x + 17f * scale, y - 15f * scale, 7f * scale, paint)
        }

        // ===== ЭФФЕКТЫ =====
        private fun drawHitEffects(canvas: Canvas) {
            if (hitEffectTimer > 0) {
                val flashPaint = Paint().apply {
                    color = Color.argb(hitEffectTimer * 12, 255, 255, 200)
                }
                canvas.drawCircle(hitEffectX, hitEffectY, hitEffectTimer * 5f, flashPaint)

                val ringPaint = Paint().apply {
                    color = Color.argb(hitEffectTimer * 6, 255, 200, 100)
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                }
                canvas.drawCircle(hitEffectX, hitEffectY, hitEffectTimer * 8f, ringPaint)
                ringPaint.color = Color.argb(hitEffectTimer * 4, 255, 255, 100)
                canvas.drawCircle(hitEffectX, hitEffectY, hitEffectTimer * 12f, ringPaint)
            }
        }

        private fun drawDamageNumbers(canvas: Canvas) {
            val numbersToDraw = damageNumbers.toList()
            for (dn in numbersToDraw) {
                val alpha = (255 * dn.life / dn.maxLife)
                textPaint.color = dn.color
                textPaint.alpha = alpha
                textPaint.textSize = 50f + (1 - dn.life / dn.maxLife.toFloat()) * 30f
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(dn.text, dn.x, dn.y, textPaint)
            }
        }

        private fun drawHPBars(canvas: Canvas, width: Float, height: Float, player: Player, mob: com.example.myapplication.model.Mob) {
            val hpPaint = Paint().apply { color = Color.argb(200, 0, 0, 0) }
            canvas.drawRoundRect(RectF(30f, 130f, 350f, 185f), 15f, 15f, hpPaint)

            val hpPercent = player.hp / player.calculateMaxHp()
            hpPaint.color = when {
                hpPercent > 0.5f -> Color.rgb(50, 220, 50)
                hpPercent > 0.25f -> Color.rgb(255, 200, 50)
                else -> Color.RED
            }
            canvas.drawRoundRect(RectF(35f, 135f, 35f + 305f * hpPercent, 180f), 12f, 12f, hpPaint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 28f
            textPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("❤️ ${player.hp.toInt()}/${player.calculateMaxHp().toInt()}", 45f, 168f, textPaint)

            hpPaint.color = Color.argb(200, 0, 0, 0)
            canvas.drawRoundRect(RectF(width - 350f, 130f, width - 30f, 185f), 15f, 15f, hpPaint)

            val mobHpPercent = mob.hp / mob.maxHp
            hpPaint.color = when {
                mobHpPercent > 0.5f -> Color.rgb(50, 220, 50)
                mobHpPercent > 0.25f -> Color.rgb(255, 200, 50)
                else -> Color.RED
            }
            canvas.drawRoundRect(RectF(width - 345f, 135f, width - 345f + 305f * mobHpPercent, 180f), 12f, 12f, hpPaint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 28f
            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("${mob.hp.toInt()}/${mob.maxHp.toInt()} ❤️", width - 40f, 168f, textPaint)
        }

        private fun drawNames(canvas: Canvas, width: Float, height: Float, player: Player, mob: com.example.myapplication.model.Mob) {
            textPaint.color = Color.argb(200, 255, 255, 255)
            textPaint.textSize = 22f
            textPaint.textAlign = Paint.Align.CENTER

            canvas.drawText("⚔️ Герой Ур.${player.level}", 190f, 115f, textPaint)
            val mobName = if (mob.type == 0) "🐑 Флаффи" else "👹 ${mob.getTypeName()}"
            canvas.drawText("$mobName Ур.${mob.level}", width - 190f, 115f, textPaint)
        }

        private fun drawAttackButton(canvas: Canvas, width: Float, height: Float) {
            val btnPaint = Paint().apply {
                shader = LinearGradient(
                    width - 200f, height - 120f,
                    width - 30f, height - 30f,
                    Color.rgb(255, 80, 30),
                    Color.rgb(200, 40, 10),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRoundRect(RectF(width - 200f, height - 120f, width - 30f, height - 30f), 20f, 20f, btnPaint)

            val glowPaint = Paint().apply {
                shader = RadialGradient(width - 115f, height - 75f, 120f, Color.argb(100, 255, 200, 100), Color.TRANSPARENT, Shader.TileMode.CLAMP)
            }
            canvas.drawCircle(width - 115f, height - 75f, 120f, glowPaint)

            paint.color = Color.argb(100, 255, 255, 255)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            canvas.drawRoundRect(RectF(width - 200f, height - 120f, width - 30f, height - 30f), 20f, 20f, paint)

            textPaint.color = Color.WHITE
            textPaint.textSize = 36f
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText("⚔️ АТАКА", width - 115f, height - 58f, textPaint)
        }

        private fun drawBattleStatus(canvas: Canvas, width: Float, height: Float, manager: BattleManager) {
            val statusText = when (manager.state) {
                BattleManager.BattleState.STARTED -> "⚔️ БОЙ НАЧАЛСЯ!"
                BattleManager.BattleState.PLAYER_TURN -> "👉 ВАШ ХОД!"
                BattleManager.BattleState.ENEMY_TURN -> "👹 ХОД ПРОТИВНИКА..."
                BattleManager.BattleState.VICTORY -> "🏆 ПОБЕДА! +20 ОПЫТА"
                BattleManager.BattleState.DEFEAT -> "💀 ПОРАЖЕНИЕ..."
                else -> ""
            }

            if (statusText.isNotEmpty()) {
                textPaint.color = when (manager.state) {
                    BattleManager.BattleState.VICTORY -> Color.rgb(255, 215, 0)
                    BattleManager.BattleState.DEFEAT -> Color.RED
                    BattleManager.BattleState.PLAYER_TURN -> Color.rgb(100, 255, 100)
                    else -> Color.YELLOW
                }
                textPaint.textSize = 44f
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.typeface = Typeface.DEFAULT_BOLD
                canvas.drawText(statusText, width / 2, 85f, textPaint)
            }
        }

        // ===== ПУБЛИЧНЫЕ МЕТОДЫ =====

        fun triggerPlayerAttack() {
            isPlayerAttacking = true
            attackAnimTimer = 0
        }

        fun triggerHitEffect(x: Float, y: Float) {
            hitEffectTimer = 20
            hitEffectX = x
            hitEffectY = y
            shakeTimer = 10
        }

        fun showDamageNumber(x: Float, y: Float, text: String, color: Int = Color.YELLOW) {
            damageNumbers.add(DamageNumber(x, y, text, 70, 70, -6f, color))
        }

        // ⭐ ОБНОВЛЁННЫЙ МЕТОД С ПЕРЕДАЧЕЙ ТИПА МОБА
        fun triggerMobAttack(mobType: Int = -1) {
            isMobAttacking = true
            attackAnimTimerMob = 0
            attackFrameIndex = 0

            // Устанавливаем флаг для конкретного моба
            when (mobType) {
                1 -> isSpiderAttacking = true
                2 -> isManyEyesAttacking = true
                3 -> isRedKnightAttacking = true
                4 -> isSlimeGreenAttacking = true
                5 -> isSteelKnightAttacking = true
                6 -> isGoblinAttacking = true
            }
        }

        fun triggerFluffyAttack() {
            isFluffyAttacking = true
            fluffyAttackTimer = 0
            fluffyOffsetX = 0f
            fluffyFrameIndex = 0
            fluffyFrameTimer = 0
        }

        fun knockbackMob(x: Float, y: Float) {
            mobKnockbackX = x
            mobKnockbackY = y
        }

        fun reset() {
            hitEffectTimer = 0
            shakeTimer = 0
            damageNumbers.clear()
            isPlayerAttacking = false
            isMobAttacking = false
            isFluffyAttacking = false
            isSpiderAttacking = false
            isManyEyesAttacking = false
            isRedKnightAttacking = false
            isSlimeGreenAttacking = false
            isSteelKnightAttacking = false
            isGoblinAttacking = false
            mobKnockbackX = 0f
            mobKnockbackY = 0f
            attackAnimTimer = 0
            attackAnimTimerMob = 0
            attackFrameIndex = 0
            fluffyOffsetX = 0f
            fluffyAttackTimer = 0
            fluffyFrameIndex = 0
        }
    }
}
