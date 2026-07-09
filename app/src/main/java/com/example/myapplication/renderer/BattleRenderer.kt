package com.example.myapplication.renderer

import android.graphics.*
import com.example.myapplication.GameView
import com.example.myapplication.manager.BattleManager
import com.example.myapplication.model.IMobRenderer
import com.example.myapplication.model.Mob
import com.example.myapplication.model.Player
import kotlin.math.sin

object BattleRenderer {
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

    // Экраные координаты
    private var battlePlayerX = 0f
    private var battlePlayerY = 0f
    private var battleMobX = 0f
    private var battleMobY = 0f

    private val mobRenderers = mutableMapOf<Int, IMobRenderer>()

    init {
        for (type in 0..9) {
            mobRenderers[type] = MobRendererFactory.getRenderer(type)
        }
    }

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

        updateAnimations()

        drawBattleBackground(canvas, width, height, location)
        applyShake()

        battlePlayerX = width * 0.25f
        battlePlayerY = height * 0.55f
        battleMobX = width * 0.75f + mobKnockbackX
        battleMobY = height * 0.55f + mobKnockbackY

        canvas.translate(shakeX, shakeY)

        drawBattleFrame(canvas, width, height)
        gameView.drawBattlePlayer(canvas, battlePlayerX, battlePlayerY, 2.5f)
        drawMobInBattle(canvas, battleMobX, battleMobY, mob, gameView)

        drawHitEffects(canvas)
        drawDamageNumbers(canvas)
        drawHPBars(canvas, width, height, player, mob)
        drawNames(canvas, width, height, player, mob)

        if (manager.state == BattleManager.BattleState.PLAYER_TURN) {
            drawAttackButton(canvas, width, height)
        }

        drawBattleStatus(canvas, width, height, manager)

        canvas.translate(-shakeX, -shakeY)
    }

    // ===== ФОНЫ БИТВЫ =====
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

    // ===== РАМКА БИТВЫ =====
    private fun drawBattleFrame(canvas: Canvas, width: Float, height: Float) {
        paint.color = Color.argb(150, 255, 215, 0)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawRoundRect(20f, 100f, width - 20f, height - 30f, 20f, 20f, paint)

        paint.strokeWidth = 6f
        val cornerSize = 50f
        val corners = listOf(
            20f to 100f,
            width - 20f to 100f,
            20f to height - 30f,
            width - 20f to height - 30f
        )
        for ((cx, cy) in corners) {
            val dx = if (cx > width / 2) -cornerSize else cornerSize
            val dy = if (cy > height / 2) -cornerSize else cornerSize
            canvas.drawLine(cx, cy, cx + dx, cy, paint)
            canvas.drawLine(cx, cy, cx, cy + dy, paint)
        }
    }

    // ===== МОБ В БИТВЕ =====
    private fun drawMobInBattle(canvas: Canvas, x: Float, y: Float, mob: Mob, gameView: GameView) {
        val bossScale = if (mob.isBoss) 1.8f else 1f
        val scale = 2.5f * bossScale

        var finalX = x
        var finalY = y

        drawMobShadow(canvas, finalX, finalY, scale)

        if (mob.isBoss) {
            drawBossBattleGlow(canvas, finalX, finalY, scale)
        }

        val renderer = mobRenderers[mob.type] ?: MobRendererFactory.getRenderer(mob.type)

        if (mob.type != 0) {
            renderer.isAttacking = isMobAttacking
        }

        renderer.drawInBattle(canvas, finalX, finalY, mob, scale, gameView)

        if (mob.isBoss) {
            drawBossCrown(canvas, finalX, finalY, bossScale)
        }
    }

    private fun drawMobShadow(canvas: Canvas, x: Float, y: Float, scale: Float) {
        paint.color = Color.argb(80, 0, 0, 0)
        canvas.drawOval(x - 80f * scale, y + 50f * scale, x + 80f * scale, y + 80f * scale, paint)
    }

    private fun drawBossBattleGlow(canvas: Canvas, x: Float, y: Float, scale: Float) {
        val glowPaint = Paint().apply {
            shader = RadialGradient(
                x, y, 200f * scale,
                Color.argb(80, 255, 215, 0),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(x, y, 200f * scale, glowPaint)

        val pulse = (30 + sin(System.currentTimeMillis() / 150.0) * 15).toInt()
        val bossAura = Paint().apply {
            color = Color.argb(pulse, 255, 215, 0)
            style = Paint.Style.STROKE
            strokeWidth = 8f
        }
        canvas.drawCircle(x, y, 100f * scale, bossAura)
    }

    private fun drawBossCrown(canvas: Canvas, x: Float, y: Float, bossScale: Float) {
        val crownPaint = Paint().apply {
            textSize = 80f * bossScale
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("👑", x, y - 90f * bossScale, crownPaint)
    }

    // ===== ЭФФЕКТЫ =====
    private fun drawHitEffects(canvas: Canvas) {
        // Эффекты удара (можно оставить пустым или добавить визуальные эффекты)
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
        }
    }

    private fun drawDamageNumbers(canvas: Canvas) {
        // Создаем копию списка для безопасной итерации
        val numbersToDraw = damageNumbers.toList()

        for (dn in numbersToDraw) {
            val progress = 1f - dn.life / dn.maxLife.toFloat()
            val alpha = (255 * (1f - progress * 0.5f)).toInt()

            val baseSize = when {
                dn.text.contains("💥") -> 80f
                dn.text.contains("💰") -> 55f
                dn.text.contains("EXP") -> 55f
                else -> 60f
            }
            val size = baseSize + (1f - progress) * 20f

            textPaint.color = Color.BLACK
            textPaint.alpha = (alpha * 0.5f).toInt()
            textPaint.textSize = size
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(dn.text, dn.x + 3f, dn.y + 3f, textPaint)

            textPaint.color = dn.color
            textPaint.alpha = alpha
            textPaint.textSize = size
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.typeface = Typeface.DEFAULT_BOLD
            canvas.drawText(dn.text, dn.x, dn.y, textPaint)

            if (dn.text.contains("💥") || dn.text.contains("💀") || dn.text.contains("👑")) {
                val glowPaint = Paint().apply {
                    color = Color.argb((alpha * 0.2f).toInt(), 255, 200, 50)
                    textSize = size * 1.3f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText(dn.text, dn.x, dn.y, glowPaint)
            }
        }
    }

    // ===== HP БАРЫ =====
    private fun drawHPBars(canvas: Canvas, width: Float, height: Float, player: Player, mob: Mob) {
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

    // ===== ИМЕНА =====
    private fun drawNames(canvas: Canvas, width: Float, height: Float, player: Player, mob: Mob) {
        textPaint.color = Color.argb(200, 255, 255, 255)
        textPaint.textSize = 22f
        textPaint.textAlign = Paint.Align.CENTER

        canvas.drawText("⚔️ Герой Ур.${player.level}", 190f, 115f, textPaint)
        val mobName = if (mob.type == 0) "🐑 Флаффи" else "👹 ${mob.getTypeName()}"
        canvas.drawText("$mobName Ур.${mob.level}", width - 190f, 115f, textPaint)
    }

    // ===== КНОПКА АТАКИ =====
    private fun drawAttackButton(canvas: Canvas, width: Float, height: Float) {
        val btnWidth = width * 0.4f
        val btnHeight = 140f
        val btnX = (width - btnWidth) / 2
        val btnY = height - btnHeight - 30f

        val shadowPaint = Paint().apply {
            color = Color.argb(60, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(btnX + 8f, btnY + 8f, btnX + btnWidth + 8f, btnY + btnHeight + 8f),
            25f, 25f, shadowPaint
        )

        val btnPaint = Paint().apply {
            shader = LinearGradient(
                btnX, btnY,
                btnX, btnY + btnHeight,
                Color.rgb(255, 70, 30),
                Color.rgb(200, 40, 10),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(
            RectF(btnX, btnY, btnX + btnWidth, btnY + btnHeight),
            25f, 25f, btnPaint
        )

        val pulse = (150 + sin(System.currentTimeMillis() / 200.0) * 80).toInt()
        val borderPaint = Paint().apply {
            color = Color.argb(pulse, 255, 255, 200)
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        canvas.drawRoundRect(
            RectF(btnX, btnY, btnX + btnWidth, btnY + btnHeight),
            25f, 25f, borderPaint
        )

        val glowPaint = Paint().apply {
            shader = RadialGradient(
                btnX + btnWidth / 2, btnY + btnHeight / 2, btnWidth * 0.7f,
                Color.argb(80, 255, 200, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(btnX + btnWidth / 2, btnY + btnHeight / 2, btnWidth * 0.7f, glowPaint)

        val swordPaint = Paint().apply {
            color = Color.WHITE
            textSize = 70f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("⚔️", btnX + btnWidth / 2 - 60f, btnY + btnHeight / 2 + 25f, swordPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 38f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("АТАКА", btnX + btnWidth / 2 + 10f, btnY + btnHeight / 2 + 25f, textPaint)
    }

    // ===== СТАТУС БИТВЫ =====
    private fun drawBattleStatus(canvas: Canvas, width: Float, height: Float, manager: BattleManager) {
        val statusText = when (manager.state) {
            BattleManager.BattleState.STARTED -> "⚔️ БОЙ НАЧАЛСЯ!"
            BattleManager.BattleState.PLAYER_TURN -> "👉 ВАШ ХОД!"
            BattleManager.BattleState.ENEMY_TURN -> "👹 ХОД ПРОТИВНИКА..."
            BattleManager.BattleState.VICTORY -> "🏆 ПОБЕДА!"
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

    // ===== ОБНОВЛЕНИЕ АНИМАЦИЙ =====
    private fun updateAnimations() {
        for ((type, renderer) in mobRenderers) {
            if (renderer.isAttacking) {
                renderer.attackFrameIndex = renderer.attackFrameIndex + 1

                val maxFrames = when (type) {
                    1 -> 8  // Паук
                    2 -> 8  // Многоглаз
                    3 -> 8  // Красный рыцарь
                    4 -> 6  // Слизень
                    5 -> 8  // Стальной рыцарь
                    6 -> 8  // Гоблин
                    7 -> 6  // Монах
                    8 -> 8  // Орк
                    9 -> 8  // Тролль
                    else -> 6
                }

                if (renderer.attackFrameIndex >= maxFrames) {
                    renderer.isAttacking = false
                    renderer.attackFrameIndex = 0
                    isMobAttacking = false
                }
            }
        }

        mobKnockbackX *= 0.9f
        mobKnockbackY *= 0.9f
        if (kotlin.math.abs(mobKnockbackX) < 0.1f) mobKnockbackX = 0f
        if (kotlin.math.abs(mobKnockbackY) < 0.1f) mobKnockbackY = 0f

        val iterator = damageNumbers.iterator()
        while (iterator.hasNext()) {
            val dn = iterator.next()
            dn.y += dn.vy
            dn.vy += 0.4f
            dn.life--
            dn.x += kotlin.math.sin(dn.life / 10f) * 0.3f

            if (dn.life <= 0) {
                iterator.remove()
            }
        }

        if (hitEffectTimer > 0) hitEffectTimer--
    }

    private fun applyShake() {
        if (shakeTimer > 0) {
            shakeTimer--
            shakeX = (random.nextFloat() - 0.5f) * shakeTimer / 2
            shakeY = (random.nextFloat() - 0.5f) * shakeTimer / 2
        } else {
            shakeX = 0f
            shakeY = 0f
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

    fun showBattleDamageNumber(target: String, text: String, color: Int = Color.YELLOW, offsetY: Float = 0f) {
        val x = if (target == "player") battlePlayerX else battleMobX
        val y = if (target == "player") battlePlayerY else battleMobY

        damageNumbers.add(
            DamageNumber(
                x = x,
                y = y + offsetY,
                text = text,
                life = 50,
                maxLife = 50,
                vy = -12f,
                color = color
            )
        )
    }

    fun showDamageNumber(x: Float, y: Float, text: String, color: Int = Color.YELLOW) {
        damageNumbers.add(
            DamageNumber(
                x = x,
                y = y,
                text = text,
                life = 80,
                maxLife = 80,
                vy = -8f,
                color = color
            )
        )
    }

    fun triggerMobAttack(mobType: Int = -1) {
        isMobAttacking = true
        val renderer = mobRenderers[mobType] ?: return
        renderer.isAttacking = true
        renderer.attackFrameIndex = 0
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
        mobKnockbackX = 0f
        mobKnockbackY = 0f
        attackAnimTimer = 0

        for (renderer in mobRenderers.values) {
            renderer.isAttacking = false
            renderer.attackFrameIndex = 0
        }
    }
}
