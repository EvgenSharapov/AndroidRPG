package com.example.myapplication.ui

import android.graphics.*
import com.example.myapplication.model.Inventory
import com.example.myapplication.model.Player
import kotlin.math.max

class StatsScreen {

    data class StatInfo(
        val name: String,
        val icon: String,
        val value: Int,
        val description: String,
        val maxLevel: Int = 99
    )

    private val STATS_START_Y = 220f
    private val STAT_ITEM_HEIGHT = 70f
    private val STATS_COUNT = 5
    private val RESET_BTN_WIDTH = 300f
    private val RESET_BTN_HEIGHT = 60f

    private var showResetConfirm = false

    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        inventory: Inventory,
        damage: Int,
        defense: Int,
        onUpgrade: (Player.StatType) -> Unit,
        onClose: () -> Unit,
        onResetStats: () -> Unit
    ) {
        // --- ФОН (затемнение) ---
        val bgPaint = Paint().apply {
            color = Color.argb(220, 0, 0, 30)
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // ЗОЛОТО (СЛЕВА СВЕРХУ)
        val goldPaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            textSize = 32f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("💰 ${player.gold}", 30f, 75f, goldPaint)

        // КНОПКА ЗАКРЫТИЯ
        drawCloseButton(canvas, width)

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
            "❤️ HP: ${player.hp.toInt()}/${player.getMaxHp(inventory).toInt()}  ⚔️ Урон: $damage",
            width / 2,
            155f,
            statsPaint
        )

        canvas.drawText(
            "🛡️ Защита: $defense  💨 Уворот: ${player.getDodgeChance(inventory).toInt()}%  🎯 Точность: ${player.getHitChance(inventory).toInt()}%  💥 Крит: ${player.getCritChance(inventory).toInt()}% (x${String.format("%.1f", player.getCritDamage(inventory))})",
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

        var currentY = startY
        for ((index, stat) in stats.withIndex()) {
            val y = startY + index * itemHeight
            currentY = y + itemHeight

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

        // ⭐ ДОБАВЛЯЕМ ОТОБРАЖЕНИЕ БОНУСОВ ОТ РУН
        currentY = drawRuneBonuses(canvas, width, height, player, inventory, currentY + 20f)

        // КНОПКА СБРОСА ХАРАКТЕРИСТИК (ВНИЗУ)
        val resetY = max(currentY + 30f, startY + stats.size * itemHeight + 30f)

        // Фон для кнопки
        val resetBgPaint = Paint().apply {
            color = Color.argb(80, 0, 0, 0)
        }
        canvas.drawRoundRect(
            RectF(30f, resetY, width - 30f, resetY + 70f),
            15f, 15f, resetBgPaint
        )

        // Кнопка сброса
        val canReset = player.gold >= 1000
        val resetBtnPaint = Paint().apply {
            color = if (canReset) Color.rgb(200, 50, 50) else Color.argb(100, 100, 100, 100)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(width / 2 - 150f, resetY + 5f, width / 2 + 150f, resetY + 65f),
            12f, 12f, resetBtnPaint
        )

        // Свечение кнопки (если можно сбросить)
        if (canReset) {
            val glowResetPaint = Paint().apply {
                shader = RadialGradient(
                    width / 2, resetY + 35f, 200f,
                    Color.argb(60, 255, 100, 100),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(width / 2, resetY + 35f, 200f, glowResetPaint)
        }

        val resetTextPaint = Paint().apply {
            color = if (canReset) Color.WHITE else Color.argb(150, 200, 200, 200)
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val resetText = if (canReset) {
            "🔄 Сбросить характеристики (1000💰)"
        } else {
            "❌ Недостаточно золота (нужно 1000💰)"
        }
        canvas.drawText(resetText, width / 2, resetY + 48f, resetTextPaint)

        // ОКНО ПОДТВЕРЖДЕНИЯ СБРОСА
        if (showResetConfirm) {
            drawResetConfirmDialog(canvas, width, height)
        }
    }



    // ⭐ КНОПКА ЗАКРЫТИЯ
    private fun drawCloseButton(canvas: Canvas, width: Float) {
        val closeBtnSize = 60f
        val closeX = width - 40f
        val closeY = 45f
        val halfSize = closeBtnSize / 2

        val closeBgPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(closeX, closeY, halfSize + 10f, closeBgPaint)

        val glowPaint = Paint().apply {
            shader = RadialGradient(
                closeX, closeY, halfSize + 30f,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(closeX, closeY, halfSize + 30f, glowPaint)

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

        val borderPaint = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawCircle(closeX, closeY, halfSize + 10f, borderPaint)
    }

    // ⭐ ОКНО ПОДТВЕРЖДЕНИЯ СБРОСА
    private fun drawResetConfirmDialog(canvas: Canvas, width: Float, height: Float) {
        // Затемнение фона
        val dimPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width, height, dimPaint)

        // Окно подтверждения
        val dialogPaint = Paint().apply {
            color = Color.argb(240, 30, 20, 40)
        }
        val dialogWidth = 600f
        val dialogHeight = 350f
        val dialogX = (width - dialogWidth) / 2
        val dialogY = (height - dialogHeight) / 2

        canvas.drawRoundRect(
            RectF(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight),
            25f, 25f, dialogPaint
        )

        // Рамка
        val borderPaint = Paint().apply {
            color = Color.argb(100, 255, 100, 100)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(
            RectF(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight),
            25f, 25f, borderPaint
        )

        // Свечение
        val glowPaint = Paint().apply {
            shader = RadialGradient(
                width / 2, dialogY + 60f, 300f,
                Color.argb(40, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width / 2, dialogY + 60f, 300f, glowPaint)

        // Иконка
        val iconPaint = Paint().apply {
            color = Color.rgb(255, 200, 100)
            textSize = 60f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🔄", width / 2, dialogY + 75f, iconPaint)

        // Текст
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Сбросить характеристики?", width / 2, dialogY + 130f, textPaint)

        // Информация
        val infoPaint = Paint().apply {
            color = Color.argb(200, 255, 215, 0)
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Стоимость: 1000 💰", width / 2, dialogY + 175f, infoPaint)

        val warningPaint = Paint().apply {
            color = Color.argb(200, 255, 150, 150)
            textSize = 22f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Все очки характеристик будут сброшены!", width / 2, dialogY + 210f, warningPaint)

        // Кнопка "ДА"
        val btnWidth = 160f
        val btnHeight = 60f
        val btnY = dialogY + 240f

        val yesPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + 50f, btnY, dialogX + 50f + btnWidth, btnY + btnHeight),
            15f, 15f, yesPaint
        )

        val yesGlow = Paint().apply {
            shader = RadialGradient(
                dialogX + 50f + btnWidth / 2, btnY + btnHeight / 2, 80f,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(dialogX + 50f + btnWidth / 2, btnY + btnHeight / 2, 80f, yesGlow)

        val yesText = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("ДА", dialogX + 50f + btnWidth / 2, btnY + btnHeight / 2 + 10f, yesText)

        // Кнопка "НЕТ"
        val noPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + dialogWidth - btnWidth - 50f, btnY, dialogX + dialogWidth - 50f, btnY + btnHeight),
            15f, 15f, noPaint
        )

        val noText = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("НЕТ", dialogX + dialogWidth - btnWidth / 2 - 50f, btnY + btnHeight / 2 + 10f, noText)
    }

    // Обработка касаний на экране характеристик
    fun handleTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        player: Player,
        onUpgrade: (Player.StatType) -> Unit,
        onClose: () -> Unit,
        onResetStats: () -> Unit,
        onFullReset: () -> Unit
    ): Boolean {
        // ⭐ ЕСЛИ ОТКРЫТО ОКНО ПОДТВЕРЖДЕНИЯ — ОБРАБАТЫВАЕМ ЕГО
        if (showResetConfirm) {
            val dialogWidth = 600f
            val dialogHeight = 350f
            val dialogX = (width - dialogWidth) / 2
            val dialogY = (height - dialogHeight) / 2
            val btnWidth = 160f
            val btnHeight = 60f
            val btnY = dialogY + 240f

            // Кнопка "ДА"
            if (x > dialogX + 50f && x < dialogX + 50f + btnWidth &&
                y > btnY && y < btnY + btnHeight) {
                showResetConfirm = false
                onResetStats()  // ← вызываем сброс
                return true
            }

            // Кнопка "НЕТ"
            if (x > dialogX + dialogWidth - btnWidth - 50f && x < dialogX + dialogWidth - 50f &&
                y > btnY && y < btnY + btnHeight) {
                showResetConfirm = false
                return true
            }
            return true
        }

        // ⭐ КНОПКА ЗАКРЫТИЯ
        val closeX = width - 40f
        val closeY = 45f
        val halfSize = 40f

        if (x > closeX - halfSize && x < closeX + halfSize &&
            y > closeY - halfSize && y < closeY + halfSize) {
            onClose()
            return true
        }

        // ⭐ КНОПКА СБРОСА (внизу экрана)
        val resetY = STATS_START_Y + STATS_COUNT * STAT_ITEM_HEIGHT + 150f
        val btnWidth = 400f
        val btnHeight = 55f
        val btnX = (width - btnWidth) / 2
        val btnY = resetY

        if (x > btnX && x < btnX + btnWidth &&
            y > btnY && y < btnY + btnHeight) {
            onFullReset()  // ← вызываем полный сброс
            return true
        }

        // Проверяем кнопки "+" для каждой характеристики
        if (player.skillPoints > 0) {
            val stats = listOf(
                Player.StatType.STRENGTH,
                Player.StatType.ENDURANCE,
                Player.StatType.AGILITY,
                Player.StatType.DEXTERITY,
                Player.StatType.LUCK
            )

            for ((index, statType) in stats.withIndex()) {
                val btnY = STATS_START_Y + index * STAT_ITEM_HEIGHT
                if (x > width - 100f && x < width - 45f &&
                    y > btnY + 10f && y < btnY + 60f) {
                    onUpgrade(statType)
                    return true
                }
            }
        }
        return false
    }

    private fun drawRuneBonuses(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        inventory: Inventory,
        startY: Float
    ): Float {
        val totalStats = inventory.getTotalStats()

        // Проверяем, есть ли вообще бонусы
        val hasBonuses = totalStats.attack > 0 || totalStats.defense > 0 ||
                totalStats.health > 0 || totalStats.dodge > 0 ||
                totalStats.crit > 0 || totalStats.critDamage > 0 ||
                totalStats.hpRegen > 0 || totalStats.moveSpeed > 0 ||
                totalStats.goldBonus > 0 || totalStats.expBonus > 0

        if (!hasBonuses) {
            return startY
        }

        // Заголовок
        val titlePaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            textSize = 24f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("✨ Активные бонусы от рун и вещей:", 30f, startY + 30f, titlePaint)

        var currentY = startY + 50f
        val bonusPaint = Paint().apply {
            color = Color.argb(200, 200, 255, 200)
            textSize = 20f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        val valuePaint = Paint().apply {
            color = Color.rgb(100, 255, 100)
            textSize = 20f
            textAlign = Paint.Align.RIGHT
            typeface = Typeface.DEFAULT_BOLD
        }

        // Список бонусов
        val bonuses = mutableListOf<Pair<String, Int>>()

        if (totalStats.attack > 0) {
            bonuses.add("⚔️ Атака" to totalStats.attack)
        }
        if (totalStats.defense > 0) {
            bonuses.add("🛡️ Защита" to totalStats.defense)
        }
        if (totalStats.health > 0) {
            bonuses.add("❤️ HP" to totalStats.health)
        }
        if (totalStats.dodge > 0) {
            bonuses.add("💨 Уворот" to totalStats.dodge)
        }
        if (totalStats.crit > 0) {
            bonuses.add("💥 Крит" to totalStats.crit)
        }
        if (totalStats.critDamage > 0) {
            bonuses.add("⚡ Крит. урон" to totalStats.critDamage)
        }
        if (totalStats.hpRegen > 0) {
            bonuses.add("🔄 Регенерация" to totalStats.hpRegen)
        }
        if (totalStats.moveSpeed > 0) {
            bonuses.add("🏃 Скорость" to totalStats.moveSpeed)
        }
        if (totalStats.goldBonus > 0) {
            bonuses.add("💰 Золото" to totalStats.goldBonus)
        }
        if (totalStats.expBonus > 0) {
            bonuses.add("⭐ Опыт" to totalStats.expBonus)
        }

        // Если бонусов много, разбиваем на 2 колонки
        if (bonuses.size > 5) {
            val midIndex = (bonuses.size + 1) / 2
            val leftBonuses = bonuses.subList(0, midIndex)
            val rightBonuses = bonuses.subList(midIndex, bonuses.size)

            val maxWidth = 250f
            val spacing = 35f

            for (i in leftBonuses.indices) {
                val y = currentY + i * spacing
                val (name, value) = leftBonuses[i]

                val bgPaint = Paint().apply {
                    color = Color.argb(40, 255, 255, 255)
                }
                canvas.drawRoundRect(
                    RectF(30f, y - 15f, 30f + maxWidth, y + 15f),
                    8f, 8f, bgPaint
                )

                canvas.drawText(name, 40f, y + 7f, bonusPaint)
                canvas.drawText("+$value", 30f + maxWidth - 10f, y + 7f, valuePaint)
            }

            for (i in rightBonuses.indices) {
                val y = currentY + i * spacing
                val (name, value) = rightBonuses[i]

                val bgPaint = Paint().apply {
                    color = Color.argb(40, 255, 255, 255)
                }
                val xOffset = width / 2 + 20f
                canvas.drawRoundRect(
                    RectF(xOffset, y - 15f, xOffset + maxWidth, y + 15f),
                    8f, 8f, bgPaint
                )

                canvas.drawText(name, xOffset + 10f, y + 7f, bonusPaint)
                canvas.drawText("+$value", xOffset + maxWidth - 10f, y + 7f, valuePaint)
            }

            currentY += leftBonuses.size * spacing + 20f
        } else {
            // Все бонусы в одну колонку
            for ((name, value) in bonuses) {
                val bgPaint = Paint().apply {
                    color = Color.argb(40, 255, 255, 255)
                }
                canvas.drawRoundRect(
                    RectF(30f, currentY - 15f, width - 30f, currentY + 15f),
                    8f, 8f, bgPaint
                )

                canvas.drawText(name, 40f, currentY + 7f, bonusPaint)
                canvas.drawText("+$value", width - 40f, currentY + 7f, valuePaint)

                currentY += 35f
            }
            currentY += 10f
        }

        return currentY
    }

    private fun drawResetFullButton(
        canvas: Canvas,
        width: Float,
        height: Float,
        startY: Float
    ) {
        val btnWidth = 400f
        val btnHeight = 55f
        val btnX = (width - btnWidth) / 2
        val btnY = startY + 20f

        // Фон кнопки (красный)
        val btnPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(btnX, btnY, btnX + btnWidth, btnY + btnHeight),
            15f, 15f, btnPaint
        )

        // Свечение
        val glowPaint = Paint().apply {
            shader = RadialGradient(
                width / 2, btnY + btnHeight / 2, 200f,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width / 2, btnY + btnHeight / 2, 200f, glowPaint)

        // Текст
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🗑️ ПОЛНЫЙ СБРОС ПЕРСОНАЖА", width / 2, btnY + btnHeight / 2 + 9f, textPaint)
    }

}
