package com.example.myapplication.ui

import android.content.Context
import android.graphics.*
import com.example.myapplication.GameView
import com.example.myapplication.SpriteManager
import com.example.myapplication.model.*
import kotlin.math.cos
import kotlin.math.sin

class InventoryScreen(
    private val context: Context,
    private val spriteManager: SpriteManager
) {

    private val slotSize = 180f
    private val padding = 20f
    private var animFrameIndex = 0
    private var animFrameTimer = 0

    private var lastClickTime = 0L
    private var lastClickSlot = -1
    private val DOUBLE_CLICK_DELAY = 300L

    // ДЛЯ ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
    private var showDeleteConfirm = false
    private var deleteItemIndex = -1
    private var deleteItemName = ""


    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        inventory: Inventory,
        player: Player,
        gameView: GameView,
        onClose: () -> Unit
    ) {
        // --- ФОН ---
        val bgPaint = Paint().apply {
            color = Color.argb(240, 20, 15, 30)
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // --- ЗАГОЛОВОК ---
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🎒 ИНВЕНТАРЬ", width / 2, 75f, titlePaint)

        // --- ЗОЛОТО (СЛЕВА СВЕРХУ) ---
        val goldPaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            textSize = 32f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("💰 ${player.gold}", 30f, 75f, goldPaint)

        // --- КНОПКА ЗАКРЫТИЯ (КРЕСТИК СПРАВА СВЕРХУ) ---
        drawCloseButton(canvas, width)

        // --- ПЕРСОНАЖ ---
        drawCharacterPreview(canvas, width, height, player, inventory, gameView)

        // --- СЛОТЫ ЭКИПИРОВКИ ---
        drawEquipmentSlots(canvas, width, height, inventory)

        // --- ЯЧЕЙКИ ИНВЕНТАРЯ ---
        drawInventoryGrid(canvas, width, height, inventory)

        // --- ИНФОРМАЦИЯ О ВЫБРАННОМ ПРЕДМЕТЕ ---
        if (inventory.selectedSlot >= 0 && !showDeleteConfirm) {
            val items = inventory.getItems()
            if (inventory.selectedSlot < items.size) {
                val item = items[inventory.selectedSlot]
                if (item != null) {
                    drawItemInfo(canvas, width, height, item, inventory.selectedSlot)
                }
            }
        }

        // ⭐ ОКНО ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
        if (showDeleteConfirm) {
            drawDeleteConfirm(canvas, width, height)
        }
    }

    // ⭐ КНОПКА ЗАКРЫТИЯ (вынесена в отдельный метод)
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

    // ⭐ ОКНО ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
    private fun drawDeleteConfirm(canvas: Canvas, width: Float, height: Float) {
        // Затемнение фона
        val dimPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width, height, dimPaint)

        // ⭐ ОКНО ПОДТВЕРЖДЕНИЯ (УВЕЛИЧЕНО В 3 РАЗА)
        val dialogPaint = Paint().apply {
            color = Color.argb(240, 30, 20, 40)
        }
        val dialogWidth = 600f   // ← было 400
        val dialogHeight = 350f  // ← было 200
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
        canvas.drawText("🗑️", width / 2, dialogY + 75f, iconPaint)

        // Текст
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 34f  // ← увеличена
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Удалить предмет?", width / 2, dialogY + 130f, textPaint)

        // Название предмета
        val itemPaint = Paint().apply {
            color = Color.rgb(255, 200, 100)
            textSize = 38f  // ← увеличена
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(deleteItemName, width / 2, dialogY + 180f, itemPaint)

        // ⭐ КНОПКА "ДА" (увеличена)
        val btnWidth = 160f   // ← было 120
        val btnHeight = 60f   // ← было 45
        val btnY = dialogY + 230f

        val yesPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + 50f, btnY, dialogX + 50f + btnWidth, btnY + btnHeight),
            15f, 15f, yesPaint
        )

        // Свечение кнопки "Да"
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
            textSize = 28f  // ← увеличена
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("ДА", dialogX + 50f + btnWidth / 2, btnY + btnHeight / 2 + 10f, yesText)

        // ⭐ КНОПКА "НЕТ" (увеличена)
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
            textSize = 28f  // ← увеличена
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("НЕТ", dialogX + dialogWidth - btnWidth / 2 - 50f, btnY + btnHeight / 2 + 10f, noText)
    }

    // ⭐ ИНФОРМАЦИЯ О ПРЕДМЕТЕ С КНОПКОЙ УДАЛЕНИЯ
    private fun drawItemInfo(canvas: Canvas, width: Float, height: Float, item: Item, index: Int) {
        val infoPaint = Paint().apply {
            color = Color.argb(220, 0, 0, 0)
        }
        val infoX = 25f
        val infoY = height * 0.45f

        canvas.drawRoundRect(
            RectF(infoX, infoY, infoX + 500f, infoY + 280f),
            15f, 15f, infoPaint
        )

        // Название
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📦 ${item.name}", infoX + 20f, infoY + 45f, textPaint)

        // Редкость
        val rarityPaint = Paint().apply {
            color = item.getRarityColor()
            textSize = 20f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📌 ${item.getRarityName()}", infoX + 20f, infoY + 75f, rarityPaint)

        // Статы
        val statsPaint = Paint().apply {
            color = Color.argb(200, 200, 200, 200)
            textSize = 20f
            textAlign = Paint.Align.LEFT
        }
        var lineY = infoY + 110f
        if (item.stats.attack > 0) {
            canvas.drawText("⚔️ +${item.stats.attack} атака", infoX + 20f, lineY, statsPaint)
            lineY += 28f
        }
        if (item.stats.defense > 0) {
            canvas.drawText("🛡️ +${item.stats.defense} защита", infoX + 20f, lineY, statsPaint)
            lineY += 28f
        }
        if (item.stats.health > 0) {
            canvas.drawText("❤️ +${item.stats.health} HP", infoX + 20f, lineY, statsPaint)
            lineY += 28f
        }
        if (item.stats.strength > 0) {
            canvas.drawText("💪 +${item.stats.strength} сила", infoX + 20f, lineY, statsPaint)
            lineY += 28f
        }
        if (item.stats.agility > 0) {
            canvas.drawText("💨 +${item.stats.agility} ловкость", infoX + 20f, lineY, statsPaint)
            lineY += 28f
        }
        if (item.stats.luck > 0) {
            canvas.drawText("🍀 +${item.stats.luck} удача", infoX + 20f, lineY, statsPaint)
            lineY += 28f
        }

        // Если это расходник — показываем восстановление
        if (item.type == Item.ItemType.CONSUMABLE) {
            val healAmount = when (item.id) {
                "cake_small" -> "30 HP"
                "cake_medium" -> "60 HP"
                "cake_large" -> "120 HP"
                else -> ""
            }
            if (healAmount.isNotEmpty()) {
                val healPaint = Paint().apply {
                    color = Color.rgb(100, 255, 100)
                    textSize = 20f
                    textAlign = Paint.Align.LEFT
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("❤️ Восстанавливает $healAmount", infoX + 20f, lineY, healPaint)
                lineY += 28f
            }
        }

        // Кнопка удаления
        val deleteBtnX = infoX + 440f
        val deleteBtnY = infoY + 20f
        val deleteBtnSize = 90f

        val deleteBgPaint = Paint().apply {
            color = Color.rgb(180, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(deleteBtnX, deleteBtnY, deleteBtnSize / 2 + 8f, deleteBgPaint)

        val deleteGlowPaint = Paint().apply {
            shader = RadialGradient(
                deleteBtnX, deleteBtnY, deleteBtnSize + 20f,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(deleteBtnX, deleteBtnY, deleteBtnSize + 20f, deleteGlowPaint)

        val deleteCross = Paint().apply {
            color = Color.WHITE
            strokeWidth = 8f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        val crossSize2 = 25f
        canvas.drawLine(
            deleteBtnX - crossSize2, deleteBtnY - crossSize2,
            deleteBtnX + crossSize2, deleteBtnY + crossSize2,
            deleteCross
        )
        canvas.drawLine(
            deleteBtnX + crossSize2, deleteBtnY - crossSize2,
            deleteBtnX - crossSize2, deleteBtnY + crossSize2,
            deleteCross
        )

        val deleteHint = Paint().apply {
            color = Color.argb(200, 255, 150, 150)
            textSize = 16f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🗑️ Удалить", deleteBtnX, deleteBtnY + 75f, deleteHint)
    }

    private fun drawCharacterPreview(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        inventory: Inventory,
        gameView: GameView
    ) {
        val centerX = width / 2
        val centerY = height * 0.30f

        val glowPaint = Paint().apply {
            shader = RadialGradient(
                centerX, centerY, 280f,
                Color.argb(60, 100, 200, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, 280f, glowPaint)

        val animationFrames = gameView.getCharacterIdleFrames()
        val spriteSheet = gameView.getCharacterIdleSpriteSheet()

        if (animationFrames.isNotEmpty() && spriteSheet != null) {
            animFrameTimer++
            if (animFrameTimer > 3) {
                animFrameTimer = 0
                animFrameIndex = (animFrameIndex + 1) % animationFrames.size
            }

            val currentFrame = animationFrames[animFrameIndex % animationFrames.size]
            val displaySize = 300f
            val dstRect = RectF(
                centerX - displaySize / 2,
                centerY - displaySize / 2,
                centerX + displaySize / 2,
                centerY + displaySize / 2
            )

            canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)

            val weapon = inventory.getEquipment(EquipmentSlot.WEAPON)
            if (weapon != null) {
                val weaponIcon = loadItemIcon(weapon)

                if (weaponIcon != null) {
                    val iconSize = 120f
                    val dstRectWeapon = RectF(
                        centerX + 160f - iconSize / 2,
                        centerY - 10f - iconSize / 2,
                        centerX + 160f + iconSize / 2,
                        centerY - 10f + iconSize / 2
                    )
                    canvas.drawBitmap(weaponIcon, null, dstRectWeapon, null)
                } else {
                    val weaponEmoji = when (weapon.id) {
                        "sword_01" -> "🗡️"
                        else -> "⚔️"
                    }
                    val weaponPaint = Paint().apply {
                        color = Color.YELLOW
                        textSize = 100f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(weaponEmoji, centerX + 160f, centerY + 35f, weaponPaint)
                }

                val weaponNamePaint = Paint().apply {
                    color = Color.argb(200, 255, 215, 0)
                    textSize = 20f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val shortName = if (weapon.name.length > 12) weapon.name.take(12) + ".." else weapon.name
                canvas.drawText(shortName, centerX + 160f, centerY + 85f, weaponNamePaint)

                if (weapon.stats.attack > 0) {
                    val statPaint = Paint().apply {
                        color = Color.argb(150, 255, 200, 100)
                        textSize = 16f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText(
                        "⚔️ +${weapon.stats.attack} атаки",
                        centerX + 160f,
                        centerY + 110f,
                        statPaint
                    )
                }
            } else {
                val hintPaint = Paint().apply {
                    color = Color.argb(80, 255, 255, 255)
                    textSize = 16f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🗡️ слот оружия пуст", centerX + 160f, centerY + 50f, hintPaint)
            }

            val shield = inventory.getEquipment(EquipmentSlot.SHIELD)
            if (shield != null) {
                val shieldIcon = loadItemIcon(shield)
                if (shieldIcon != null) {
                    val iconSize = 100f
                    val dstRectShield = RectF(
                        centerX - 160f - iconSize / 2,
                        centerY - 10f - iconSize / 2,
                        centerX - 160f + iconSize / 2,
                        centerY - 10f + iconSize / 2
                    )
                    canvas.drawBitmap(shieldIcon, null, dstRectShield, null)
                } else {
                    val shieldPaint = Paint().apply {
                        color = Color.CYAN
                        textSize = 80f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("🛡️", centerX - 160f, centerY + 30f, shieldPaint)
                }
            }

            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = 34f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(
                "Герой Ур.${player.level}",
                centerX,
                centerY + displaySize / 2 + 50f,
                namePaint
            )

            val stats = inventory.getTotalStats()
            val statsPaint = Paint().apply {
                color = Color.argb(200, 255, 255, 200)
                textSize = 26f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "⚔️${stats.attack}  🛡️${stats.defense}  ❤️${stats.health}  💨${stats.agility}  💪${stats.strength}  🍀${stats.luck}",
                centerX,
                centerY + displaySize / 2 + 95f,
                statsPaint
            )

        } else {
            val avatarPaint = Paint().apply {
                color = Color.rgb(50, 150, 255)
            }
            canvas.drawCircle(centerX, centerY, 220f, avatarPaint)

            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = 80f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("⚔️", centerX, centerY + 70f, namePaint)

            val namePaint2 = Paint().apply {
                color = Color.WHITE
                textSize = 28f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("Герой Ур.${player.level}", centerX, centerY + 200f, namePaint2)
        }
    }

    private fun drawEquipmentSlots(
        canvas: Canvas,
        width: Float,
        height: Float,
        inventory: Inventory
    ) {
        val centerX = width / 2
        val centerY = height * 0.30f
        val radius = 400f

        val slots = listOf(
            EquipmentSlot.HELMET to -90f,
            EquipmentSlot.WEAPON to -150f,
            EquipmentSlot.SHIELD to -30f,
            EquipmentSlot.CHEST to 0f,
            EquipmentSlot.GLOVES to 60f,
            EquipmentSlot.BRACERS to 120f,
            EquipmentSlot.PANTS to 180f,
            EquipmentSlot.BOOTS to 240f,
            EquipmentSlot.NECKLACE to 300f
        )

        for ((slot, angleDeg) in slots) {
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val x = centerX + (radius * cos(angleRad)).toFloat()
            val y = centerY + (radius * sin(angleRad)).toFloat()

            val slotPaint = Paint().apply {
                color = if (inventory.isSlotEquipped(slot)) {
                    Color.argb(180, 100, 200, 100)
                } else {
                    Color.argb(120, 50, 50, 80)
                }
            }
            canvas.drawRoundRect(
                RectF(x - 70f, y - 70f, x + 70f, y + 70f),
                15f, 15f, slotPaint
            )

            val item = inventory.getEquipment(slot)

            if (item == null) {
                val iconPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText(getSlotIcon(slot), x, y + 14f, iconPaint)
            } else {
                val borderPaint = Paint().apply {
                    color = Color.YELLOW
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                }
                canvas.drawRoundRect(
                    RectF(x - 70f, y - 70f, x + 70f, y + 70f),
                    15f, 15f, borderPaint
                )

                val icon = loadItemIcon(item)
                if (icon != null) {
                    val iconSize = 80f
                    val dstRect = RectF(
                        x - iconSize / 2,
                        y - iconSize / 2,
                        x + iconSize / 2,
                        y + iconSize / 2
                    )
                    canvas.drawBitmap(icon, null, dstRect, null)
                } else {
                    val emojiPaint = Paint().apply {
                        color = Color.WHITE
                        textSize = 55f
                        textAlign = Paint.Align.CENTER
                    }
                    val emoji = when (item.id) {
                        "sword_01" -> "🗡️"
                        "crusader_shield" -> "🛡️"
                        else -> "📦"
                    }
                    canvas.drawText(emoji, x, y + 20f, emojiPaint)
                }

                val namePaint = Paint().apply {
                    color = Color.GREEN
                    textSize = 16f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val shortName = if (item.name.length > 8) item.name.take(8) + ".." else item.name
                canvas.drawText(shortName, x, y + 95f, namePaint)
            }
        }
    }

    private fun drawInventoryGrid(
        canvas: Canvas,
        width: Float,
        height: Float,
        inventory: Inventory,
    ) {
        val startY = height * 0.58f
        val cols = 5
        val rows = 4
        val totalWidth = cols * (slotSize + padding) - padding
        val startX = (width - totalWidth) / 2

        val items = inventory.getItems()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val x = startX + col * (slotSize + padding)
                val y = startY + row * (slotSize + padding)

                val cellPaint = Paint().apply {
                    color = if (inventory.selectedSlot == index) {
                        Color.argb(180, 255, 255, 100)
                    } else {
                        Color.argb(100, 80, 80, 100)
                    }
                }
                canvas.drawRoundRect(
                    RectF(x, y, x + slotSize, y + slotSize),
                    12f, 12f, cellPaint
                )

                if (index < items.size) {
                    val item = items[index]
                    if (item != null) {
                        val icon = loadItemIcon(item)
                        if (icon != null) {
                            val iconSize = 100f
                            val dstRect = RectF(
                                x + (slotSize - iconSize) / 2,
                                y + (slotSize - iconSize) / 2 - 10f,
                                x + (slotSize + iconSize) / 2,
                                y + (slotSize + iconSize) / 2 - 10f
                            )
                            drawItemRarity(canvas, x, y, slotSize, slotSize, item.rarity)
                            canvas.drawBitmap(icon, null, dstRect, null)
                        } else {
                            val emojiPaint = Paint().apply {
                                color = Color.WHITE
                                textSize = 50f
                                textAlign = Paint.Align.CENTER
                            }
                            val emoji = when (item.type) {
                                Item.ItemType.WEAPON -> "🗡️"
                                Item.ItemType.SHIELD -> "🛡️"
                                Item.ItemType.CONSUMABLE -> "🍰"
                                else -> "📦"
                            }
                            canvas.drawText(emoji, x + slotSize / 2, y + slotSize / 2 + 15f, emojiPaint)
                        }

                        val textPaint = Paint().apply {
                            color = when (item.type) {
                                Item.ItemType.WEAPON -> Color.rgb(255, 200, 100)
                                Item.ItemType.SHIELD -> Color.rgb(100, 200, 255)
                                Item.ItemType.HELMET -> Color.rgb(100, 200, 255)
                                Item.ItemType.CHEST -> Color.rgb(200, 100, 255)
                                Item.ItemType.CONSUMABLE -> Color.rgb(100, 255, 100)
                                else -> Color.WHITE
                            }
                            textSize = 16f
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        val displayName = if (item.name.length > 8) item.name.take(8) + ".." else item.name
                        canvas.drawText(displayName, x + slotSize / 2, y + slotSize - 10f, textPaint)

                        // Подсказка для расходников
                        if (item.type == Item.ItemType.CONSUMABLE) {
                            val hintPaint = Paint().apply {
                                color = Color.argb(150, 100, 255, 100)
                                textSize = 12f
                                textAlign = Paint.Align.CENTER
                            }
                            canvas.drawText("🍴 Использовать", x + slotSize / 2, y + slotSize - 28f, hintPaint)
                        }

                        val borderPaint = Paint().apply {
                            color = Color.argb(80, 255, 255, 255)
                            style = Paint.Style.STROKE
                            strokeWidth = 2f
                        }
                        canvas.drawRoundRect(
                            RectF(x, y, x + slotSize, y + slotSize),
                            12f, 12f, borderPaint
                        )
                    }
                }
            }
        }
    }



    // ОБРАБОТКА КАСАНИЙ
    fun handleTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        inventory: Inventory,
        onEquip: (Int) -> Unit,
        onUnequip: (EquipmentSlot) -> Unit,
        onClose: () -> Unit,
        onDeleteItem: (Int) -> Unit,
        onUseItem: (Int) -> Unit
    ): Boolean {
        // Кнопка закрытия
        val closeX = width - 40f
        val closeY = 45f
        val halfSize = 40f

        if (x > closeX - halfSize && x < closeX + halfSize &&
            y > closeY - halfSize && y < closeY + halfSize) {
            if (showDeleteConfirm) {
                showDeleteConfirm = false
                deleteItemIndex = -1
            } else {
                onClose()
            }
            return true
        }

        // Окно подтверждения удаления
        if (showDeleteConfirm) {
            val dialogWidth = 600f
            val dialogHeight = 350f
            val dialogX = (width - dialogWidth) / 2
            val dialogY = (height - dialogHeight) / 2
            val btnWidth = 160f
            val btnHeight = 60f
            val btnY = dialogY + 230f

            if (x > dialogX + 50f && x < dialogX + 50f + btnWidth &&
                y > btnY && y < btnY + btnHeight) {
                onDeleteItem(deleteItemIndex)
                showDeleteConfirm = false
                deleteItemIndex = -1
                return true
            }

            if (x > dialogX + dialogWidth - btnWidth - 50f && x < dialogX + dialogWidth - 50f &&
                y > btnY && y < btnY + btnHeight) {
                showDeleteConfirm = false
                deleteItemIndex = -1
                return true
            }
            return true
        }

        // Кнопка удаления в информации о предмете
        if (inventory.selectedSlot >= 0) {
            val infoX = 25f
            val infoY = height * 0.45f
            val deleteBtnX = infoX + 440f
            val deleteBtnY = infoY + 20f
            val deleteBtnSize = 90f

            if (x > deleteBtnX - deleteBtnSize / 2 - 8f &&
                x < deleteBtnX + deleteBtnSize / 2 + 8f &&
                y > deleteBtnY - deleteBtnSize / 2 - 8f &&
                y < deleteBtnY + deleteBtnSize / 2 + 8f) {
                val items = inventory.getItems()
                if (inventory.selectedSlot < items.size) {
                    val item = items[inventory.selectedSlot]
                    if (item != null) {
                        showDeleteConfirm = true
                        deleteItemIndex = inventory.selectedSlot
                        deleteItemName = item.name
                        return true
                    }
                }
            }
        }

        // Клик по слоту экипировки (для снятия)
        if (checkEquipmentSlotClick(x, y, width, height, inventory, onUnequip)) {
            return true
        }

        // Клик по ячейке инвентаря
        val startY = height * 0.58f
        val cols = 5
        val rows = 4
        val totalWidth = cols * (slotSize + padding) - padding
        val startX = (width - totalWidth) / 2

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val cellX = startX + col * (slotSize + padding)
                val cellY = startY + row * (slotSize + padding)

                if (x > cellX && x < cellX + slotSize &&
                    y > cellY && y < cellY + slotSize) {
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastClickTime < DOUBLE_CLICK_DELAY && lastClickSlot == index) {
                        val items = inventory.getItems()
                        if (index < items.size) {
                            val item = items[index]
                            if (item != null) {
                                if (item.type == Item.ItemType.CONSUMABLE) {
                                    // ⭐ ИСПОЛЬЗУЕМ РАСХОДНИК
                                    onUseItem(index)
                                } else {
                                    onEquip(index)
                                }
                            }
                        }
                        lastClickTime = 0
                        lastClickSlot = -1
                    } else {
                        lastClickTime = currentTime
                        lastClickSlot = index
                        inventory.selectedSlot = if (inventory.selectedSlot == index) -1 else index
                    }
                    return true
                }
            }
        }

        return false
    }

    private fun checkEquipmentSlotClick(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        inventory: Inventory,
        onUnequip: (EquipmentSlot) -> Unit
    ): Boolean {
        val centerX = width / 2
        val centerY = height * 0.30f
        val radius = 400f

        val slots = listOf(
            EquipmentSlot.HELMET to -90f,
            EquipmentSlot.WEAPON to -150f,
            EquipmentSlot.SHIELD to -30f,
            EquipmentSlot.CHEST to 0f,
            EquipmentSlot.GLOVES to 60f,
            EquipmentSlot.BRACERS to 120f,
            EquipmentSlot.PANTS to 180f,
            EquipmentSlot.BOOTS to 240f,
            EquipmentSlot.NECKLACE to 300f
        )

        for ((slot, angleDeg) in slots) {
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val slotX = centerX + (radius * cos(angleRad)).toFloat()
            val slotY = centerY + (radius * sin(angleRad)).toFloat()

            if (x > slotX - 70f && x < slotX + 70f &&
                y > slotY - 70f && y < slotY + 70f) {
                if (inventory.isSlotEquipped(slot)) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastClickTime < DOUBLE_CLICK_DELAY && lastClickSlot == -2) {
                        onUnequip(slot)
                        lastClickTime = 0
                        lastClickSlot = -1
                    } else {
                        lastClickTime = currentTime
                        lastClickSlot = -2
                    }
                    return true
                }
            }
        }
        return false
    }

    private fun loadItemIcon(item: Item): Bitmap? {
        return when (item.id) {
            // Мечи
            "sword_rusty" -> loadIcon("sword_rusty")
            "sword_iron" -> loadIcon("sword_iron")
            "sword_steel" -> loadIcon("sword_steel")
            "sword_flame" -> loadIcon("sword_flame")
            "sword_venom" -> loadIcon("sword_venom")
            "sword_arachnid" -> loadIcon("sword_arachnid")
            "sword_venomous" -> loadIcon("sword_venomous")
            "sword_moonlight" -> loadIcon("sword_moonlight")
            "sword_legendary" -> loadIcon("sword_legendary")
            "dagger_bone" -> loadIcon("dagger_bone")
            // Топоры
            "axe_wooden" -> loadIcon("axe_wooden")
            "axe_iron" -> loadIcon("axe_iron")
            "axe_battle" -> loadIcon("axe_battle")
            // Щит
            "crusader_shield" -> loadIcon("crusader_shield")
            // Торты
            "cake_small" -> loadIcon("cake_small")
            "cake_medium" -> loadIcon("cake_medium")
            "cake_large" -> loadIcon("cake_large")
            // Шлемы
            "helmet_2" -> loadItemIconFromSprite("armor_items", "helmet_2")
            "helmet_3" -> loadItemIconFromSprite("armor_items", "helmet_3")
            "helmet_4" -> loadItemIconFromSprite("armor_items", "helmet_4")
            "helmet_5" -> loadItemIconFromSprite("armor_items", "helmet_5")
            // Броня
            "chest_1" -> loadItemIconFromSprite("armor_items", "chest_1")
            "chest_2" -> loadItemIconFromSprite("armor_items", "chest_2")
            "chest_3" -> loadItemIconFromSprite("armor_items", "chest_3")
            "chest_4" -> loadItemIconFromSprite("armor_items", "chest_4")
            "chest_5" -> loadItemIconFromSprite("armor_items", "chest_5")
            // Перчатки
            "gloves_1" -> loadItemIconFromSprite("armor_items", "gloves_1")
            "gloves_2" -> loadItemIconFromSprite("armor_items", "gloves_2")
            "gloves_3" -> loadItemIconFromSprite("armor_items", "gloves_3")
            "gloves_4" -> loadItemIconFromSprite("armor_items", "gloves_4")
            "gloves_5" -> loadItemIconFromSprite("armor_items", "gloves_5")
            // Поножи
            "pants_1" -> loadItemIconFromSprite("armor_items", "pants_1")
            "pants_2" -> loadItemIconFromSprite("armor_items", "pants_2")
            "pants_3" -> loadItemIconFromSprite("armor_items", "pants_3")
            "pants_4" -> loadItemIconFromSprite("armor_items", "pants_4")
            "pants_5" -> loadItemIconFromSprite("armor_items", "pants_5")
            // Ботинки
            "boots_1" -> loadItemIconFromSprite("armor_items", "boots_1")
            "boots_2" -> loadItemIconFromSprite("armor_items", "boots_2")
            "boots_3" -> loadItemIconFromSprite("armor_items", "boots_3")
            "boots_4" -> loadItemIconFromSprite("armor_items", "boots_4")
            "boots_5" -> loadItemIconFromSprite("armor_items", "boots_5")
            else -> null
        }
    }

    private fun loadIcon(name: String): Bitmap? {
        return try {
            val resId = context.resources.getIdentifier(
                name, "drawable", context.packageName
            )
            if (resId != 0) {
                BitmapFactory.decodeResource(context.resources, resId)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // ===== ОТРИСОВКА РАМКИ РЕДКОСТИ =====
    private fun drawItemRarity(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, rarity: ItemRarity) {
        var color = when (rarity) {
            ItemRarity.COMMON -> Color.rgb(200, 200, 200)
            ItemRarity.UNCOMMON -> Color.rgb(50, 200, 50)
            ItemRarity.RARE -> Color.rgb(50, 150, 255)
            ItemRarity.EPIC -> Color.rgb(200, 100, 255)
            ItemRarity.LEGENDARY -> Color.rgb(255, 150, 50)
            ItemRarity.MYTHIC -> Color.rgb(255, 215, 0)
        }

        val glowPaint = Paint().apply {
            color = color
            alpha = 40
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(x - 4f, y - 4f, x + width + 4f, y + height + 4f),
            16f, 16f, glowPaint
        )

        val borderPaint = Paint().apply {
            color = color
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(
            RectF(x - 2f, y - 2f, x + width + 2f, y + height + 2f),
            14f, 14f, borderPaint
        )
    }

    // ⭐ НОВЫЙ МЕТОД ДЛЯ ЗАГРУЗКИ ИКОНОК ИЗ СПРАЙТ-ЛИСТА
    private fun loadItemIconFromSprite(sheetName: String, frameName: String): Bitmap? {
        return try {
            val spriteSheet = getSpriteSheet(sheetName) ?: return null
            val frameRect = getFrame(sheetName, frameName) ?: return null

            // Вырезаем нужный кадр из спрайт-листа
            Bitmap.createBitmap(
                spriteSheet,
                frameRect.left,
                frameRect.top,
                frameRect.width(),
                frameRect.height()
            )
        } catch (e: Exception) {
            println("❌ Ошибка загрузки иконки $frameName: ${e.message}")
            null
        }
    }

    // Вспомогательные методы для доступа к SpriteManager
    private fun getSpriteSheet(name: String): Bitmap? {
        return (context as? android.app.Activity)?.let {
            spriteManager?.getSpriteSheet(name)
        }
    }

    private fun getFrame(sheetName: String, frameName: String): Rect? {
        return (context as? android.app.Activity)?.let {
            spriteManager?.getFrame(sheetName, frameName)
        }
    }
}
