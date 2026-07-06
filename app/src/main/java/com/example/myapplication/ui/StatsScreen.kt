package com.example.myapplication.ui

import android.graphics.*
import com.example.myapplication.model.Player

class StatsScreen {

    data class StatInfo(
        val name: String,
        val icon: String,
        val value: Int,
        val description: String,
        val maxLevel: Int = 99
    )

    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        onUpgrade: (Player.StatType) -> Unit,
        onClose: () -> Unit
    ) {
        // --- ФОН (затемнение) ---
        val bgPaint = Paint().apply {
            color = Color.argb(220, 0, 0, 30)
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // ⭐ ЗОЛОТО (СЛЕВА СВЕРХУ)
        val goldPaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            textSize = 32f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("💰 ${player.gold}", 30f, 75f, goldPaint)

        // ⭐ КНОПКА ЗАКРЫТИЯ (КРЕСТИК СПРАВА СВЕРХУ)
        val closeBtnSize = 60f
        val closeX = width - 40f
        val closeY = 45f
        val halfSize = closeBtnSize / 2

        // Фон кнопки (круг)
        val closeBgPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(closeX, closeY, halfSize + 10f, closeBgPaint)

        // Свечение кнопки
        val glowPaint = Paint().apply {
            shader = RadialGradient(
                closeX, closeY, halfSize + 30f,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(closeX, closeY, halfSize + 30f, glowPaint)

        // Крестик (X)
        val crossPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 8f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        val crossSize = halfSize * 0.6f
        canvas.drawLine(
            closeX - crossSize, closeY - crossSize,
            closeX + crossSize, closeY + crossSize,
            crossPaint
        )
        canvas.drawLine(
            closeX + crossSize, closeY - crossSize,
            closeX - crossSize, closeY + crossSize,
            crossPaint
        )

        // Рамка кнопки
        val borderPaint = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawCircle(closeX, closeY, halfSize + 10f, borderPaint)

        // --- ЗАГОЛОВОК ---
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📊 ХАРАКТЕРИСТИКИ", width / 2, 80f, titlePaint)

        // --- ИНФОРМАЦИЯ О ПЕРСОНАЖЕ ---
        val infoPaint = Paint().apply {
            color = Color.argb(200, 255, 255, 255)
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Уровень ${player.level}  |  Очки прокачки: ${player.skillPoints}  |  Опыт: ${player.exp}/${player.maxExp}",
            width / 2,
            125f,
            infoPaint
        )

        // --- СТАТИСТИКИ ПЕРСОНАЖА ---
        val statsPaint = Paint().apply {
            color = Color.argb(150, 255, 255, 200)
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "❤️ HP: ${player.hp.toInt()}/${player.calculateMaxHp().toInt()}  ⚔️ Урон: ${player.getDamage().toInt()}",
            width / 2,
            155f,
            statsPaint
        )
        canvas.drawText(
            "🛡️ Уворот: ${player.getDodgeChance().toInt()}%  🎯 Точность: ${player.getHitChance().toInt()}%  💥 Крит: ${player.getCritChance().toInt()}% (x${String.format("%.1f", player.getCritDamage())})",
            width / 2,
            180f,
            statsPaint
        )

        // --- ХАРАКТЕРИСТИКИ ---
        val startY = 220f
        val itemHeight = 70f
        val stats = listOf(
            Triple(Player.StatType.STRENGTH, "💪 Сила", "Увеличивает урон"),
            Triple(Player.StatType.ENDURANCE, "❤️ Выносливость", "Увеличивает HP"),
            Triple(Player.StatType.AGILITY, "💨 Ловкость", "Увеличивает уворот"),
            Triple(Player.StatType.DEXTERITY, "🎯 Сноровка", "Увеличивает точность"),
            Triple(Player.StatType.LUCK, "🍀 Удача", "Увеличивает крит")
        )

        for ((index, stat) in stats.withIndex()) {
            val y = startY + index * itemHeight

            // Фон строки
            val rowPaint = Paint().apply {
                color = Color.argb(80, 255, 255, 255)
            }
            canvas.drawRoundRect(
                RectF(30f, y, width - 30f, y + itemHeight),
                12f, 12f, rowPaint
            )

            // Название и значение
            val value = when (stat.first) {
                Player.StatType.STRENGTH -> player.strength
                Player.StatType.ENDURANCE -> player.endurance
                Player.StatType.AGILITY -> player.agility
                Player.StatType.DEXTERITY -> player.dexterity
                Player.StatType.LUCK -> player.luck
            }

            val textPaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("${stat.second}", 50f, y + 45f, textPaint)

            textPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText("$value", width - 120f, y + 45f, textPaint)

            // Кнопка "+" (если есть очки)
            if (player.skillPoints > 0) {
                val btnPaint = Paint().apply {
                    color = Color.rgb(50, 200, 50)
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    RectF(width - 100f, y + 10f, width - 45f, y + 60f),
                    10f, 10f, btnPaint
                )

                val plusPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 28f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("+", width - 72f, y + 48f, plusPaint)
            } else {
                // Если нет очков — кнопка неактивна
                val btnPaint = Paint().apply {
                    color = Color.argb(80, 100, 100, 100)
                    style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    RectF(width - 100f, y + 10f, width - 45f, y + 60f),
                    10f, 10f, btnPaint
                )
            }

            // Подсказка
            val descPaint = Paint().apply {
                color = Color.argb(150, 200, 200, 200)
                textSize = 14f
                textAlign = Paint.Align.LEFT
            }
            canvas.drawText(stat.third, 50f, y + 65f, descPaint)
        }
    }

    // Обработка касаний на экране характеристик
    fun handleTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        player: Player,
        onUpgrade: (Player.StatType) -> Unit,
        onClose: () -> Unit
    ): Boolean {
        // ⭐ КНОПКА ЗАКРЫТИЯ (КРЕСТИК СПРАВА СВЕРХУ)
        val closeX = width - 40f
        val closeY = 45f
        val halfSize = 40f

        if (x > closeX - halfSize && x < closeX + halfSize &&
            y > closeY - halfSize && y < closeY + halfSize) {
            onClose()
            return true
        }

        // Проверяем кнопки "+" для каждой характеристики
        if (player.skillPoints > 0) {
            val startY = 220f
            val itemHeight = 70f
            val stats = listOf(
                Player.StatType.STRENGTH,
                Player.StatType.ENDURANCE,
                Player.StatType.AGILITY,
                Player.StatType.DEXTERITY,
                Player.StatType.LUCK
            )

            for ((index, statType) in stats.withIndex()) {
                val btnY = startY + index * itemHeight
                // Кнопка "+" находится справа
                if (x > width - 100f && x < width - 45f &&
                    y > btnY + 10f && y < btnY + 60f) {
                    onUpgrade(statType)
                    return true
                }
            }
        }

        return false
    }
}
