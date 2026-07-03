package com.example.myapplication.ui

import android.graphics.*
import com.example.myapplication.GameView
import com.example.myapplication.model.*
import kotlin.math.cos
import kotlin.math.sin

class InventoryScreen {

    private val slotSize = 180f  // ← было 140, +30%
    private val padding = 20f    // ← было 16, +25%
    private var animFrameIndex = 0
    private var animFrameTimer = 0

    private var lastClickTime = 0L
    private var lastClickSlot = -1
    private val DOUBLE_CLICK_DELAY = 300L  // 300 мс для двойного клика


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

        // --- ПЕРСОНАЖ (по центру сверху) ---
        drawCharacterPreview(canvas, width, height, player, inventory, gameView)

        // --- СЛОТЫ ЭКИПИРОВКИ (вокруг персонажа) ---
        drawEquipmentSlots(canvas, width, height, inventory)

        // --- ЯЧЕЙКИ ИНВЕНТАРЯ (внизу) ---
        drawInventoryGrid(canvas, width, height, inventory)

        // --- КНОПКА ЗАКРЫТЬ ---
        val closePaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
        }
        canvas.drawRoundRect(
            RectF(width / 2 - 120f, height - 90f, width / 2 + 120f, height - 35f),
            18f, 18f, closePaint
        )
        val closeText = Paint().apply {
            color = Color.WHITE
            textSize = 32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("✖ Закрыть", width / 2, height - 48f, closeText)

        // --- ИНФОРМАЦИЯ О ВЫБРАННОМ ПРЕДМЕТЕ ---
        if (inventory.selectedSlot >= 0) {
            val items = inventory.getItems()
            if (inventory.selectedSlot < items.size) {
                val item = items[inventory.selectedSlot]
                if (item != null) {
                    drawItemInfo(canvas, width, height, item)
                }
            }
        }
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

        // --- ФОН ДЛЯ ПЕРСОНАЖА (свечение) ---
        val glowPaint = Paint().apply {
            shader = RadialGradient(
                centerX, centerY, 280f,
                Color.argb(60, 100, 200, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, 280f, glowPaint)

        // --- РИСУЕМ ПЕРСОНАЖА С АНИМАЦИЕЙ IDLE ---
        val animationFrames = gameView.getCharacterIdleFrames()
        val spriteSheet = gameView.getCharacterIdleSpriteSheet()

        if (animationFrames.isNotEmpty() && spriteSheet != null) {
            animFrameTimer++
            if (animFrameTimer > 6) {
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

            // ⭐ РИСУЕМ ЭКИПИРОВАННОЕ ОРУЖИЕ (ПОВЕРХ ПЕРСОНАЖА)
            val weapon = inventory.getEquipment(EquipmentSlot.WEAPON)
            if (weapon != null) {
                // Загружаем картинку меча
                val weaponIcon = loadItemIcon(weapon)

                if (weaponIcon != null) {
                    // Рисуем PNG-картинку меча
                    val iconSize = 120f
                    val dstRectWeapon = RectF(
                        centerX + 160f - iconSize / 2,
                        centerY - 10f - iconSize / 2,
                        centerX + 160f + iconSize / 2,
                        centerY - 10f + iconSize / 2
                    )
                    canvas.drawBitmap(weaponIcon, null, dstRectWeapon, null)
                } else {
                    // Если картинка не загружена — используем эмодзи
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

                // Название оружия
                val weaponNamePaint = Paint().apply {
                    color = Color.argb(200, 255, 215, 0)
                    textSize = 20f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val shortName = if (weapon.name.length > 12) weapon.name.take(12) + ".." else weapon.name
                canvas.drawText(shortName, centerX + 160f, centerY + 85f, weaponNamePaint)

                // Дополнительная статистика оружия
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
                // Если оружия нет — показываем подсказку
                val hintPaint = Paint().apply {
                    color = Color.argb(80, 255, 255, 255)
                    textSize = 16f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🗡️ слот оружия пуст", centerX + 160f, centerY + 50f, hintPaint)
            }

            // ⭐ РИСУЕМ ЩИТ (если экипирован)
            val shield = inventory.getEquipment(EquipmentSlot.SHIELD)
            if (shield != null) {
                val shieldPaint = Paint().apply {
                    color = Color.CYAN
                    textSize = 80f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🛡️", centerX - 160f, centerY + 30f, shieldPaint)
            }

            // --- Имя и уровень ---
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

            // --- Суммарные статы ---
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
            // Запасной вариант — если спрайт не загружен
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

            // Фон слота
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
                // ⭐ СЛОТ ПУСТ — ПОКАЗЫВАЕМ ИКОНКУ СЛОТА
                val iconPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 40f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText(getSlotIcon(slot), x, y + 14f, iconPaint)
            } else {
                // ⭐ СЛОТ ЗАНЯТ — ПОКАЗЫВАЕМ ПРЕДМЕТ
                val borderPaint = Paint().apply {
                    color = Color.YELLOW
                    style = Paint.Style.STROKE
                    strokeWidth = 5f
                }
                canvas.drawRoundRect(
                    RectF(x - 70f, y - 70f, x + 70f, y + 70f),
                    15f, 15f, borderPaint
                )

// ⭐ КАРТИНКА ПРЕДМЕТА
                val icon = loadItemIcon(item)
                if (icon != null) {
                    // Рисуем картинку
                    val iconSize = 80f
                    val dstRect = RectF(
                        x - iconSize / 2,
                        y - iconSize / 2,
                        x + iconSize / 2,
                        y + iconSize / 2
                    )
                    canvas.drawBitmap(icon, null, dstRect, null)
                } else {
                    // Если картинки нет — используем эмодзи
                    val emojiPaint = Paint().apply {
                        color = Color.WHITE
                        textSize = 55f
                        textAlign = Paint.Align.CENTER
                    }
                    val emoji = when (item.id) {
                        "sword_01" -> "🗡️"
                        else -> "📦"
                    }
                    canvas.drawText(emoji, x, y + 20f, emojiPaint)
                }

                // Название предмета
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
        inventory: Inventory
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

                // Фон ячейки
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

                // Если есть предмет
                if (index < items.size) {
                    val item = items[index]
                    if (item != null) {
                        // ⭐ ЭМОДЗИ ПРЕДМЕТА
                        val emojiPaint = Paint().apply {
                            color = Color.WHITE
                            textSize = 50f
                            textAlign = Paint.Align.CENTER
                        }
                        // ⭐ КАРТИНКА ПРЕДМЕТА В ИНВЕНТАРЕ
                        val icon = loadItemIcon(item)
                        if (icon != null) {
                            val iconSize = 100f
                            val dstRect = RectF(
                                x + (slotSize - iconSize) / 2,
                                y + (slotSize - iconSize) / 2 - 10f,
                                x + (slotSize + iconSize) / 2,
                                y + (slotSize + iconSize) / 2 - 10f
                            )
                            canvas.drawBitmap(icon, null, dstRect, null)
                        } else {
                            // Эмодзи если картинки нет
                            val emojiPaint = Paint().apply {
                                color = Color.WHITE
                                textSize = 50f
                                textAlign = Paint.Align.CENTER
                            }
                            val emoji = when (item.id) {
                                "sword_01" -> "🗡️"
                                else -> "📦"
                            }
                            canvas.drawText(emoji, x + slotSize / 2, y + slotSize / 2 + 15f, emojiPaint)
                        }
                        // Название предмета
                        val textPaint = Paint().apply {
                            color = when (item.type) {
                                Item.ItemType.WEAPON -> Color.rgb(255, 200, 100)
                                Item.ItemType.HELMET -> Color.rgb(100, 200, 255)
                                Item.ItemType.CHEST -> Color.rgb(200, 100, 255)
                                Item.ItemType.CONSUMABLE -> Color.rgb(100, 255, 100)
                                else -> Color.WHITE
                            }
                            textSize = 16f
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        val displayName =
                            if (item.name.length > 8) item.name.take(8) + ".." else item.name
                        canvas.drawText(
                            displayName,
                            x + slotSize / 2,
                            y + slotSize - 10f,
                            textPaint
                        )

                        // Рамка
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

    private fun drawItemInfo(canvas: Canvas, width: Float, height: Float, item: Item) {
        val infoPaint = Paint().apply {
            color = Color.argb(220, 0, 0, 0)
        }
        val infoX = 25f
        val infoY = height * 0.45f
        canvas.drawRoundRect(
            RectF(infoX, infoY, infoX + 380f, infoY + 240f),  // ← увеличен
            15f, 15f, infoPaint
        )

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📦 ${item.name}", infoX + 20f, infoY + 45f, textPaint)

        val statsPaint = Paint().apply {
            color = Color.argb(200, 200, 200, 200)
            textSize = 22f
            textAlign = Paint.Align.LEFT
        }
        var lineY = infoY + 90f
        if (item.stats.attack > 0) {
            canvas.drawText("⚔️ +${item.stats.attack} атака", infoX + 20f, lineY, statsPaint)
            lineY += 30f
        }
        if (item.stats.defense > 0) {
            canvas.drawText("🛡️ +${item.stats.defense} защита", infoX + 20f, lineY, statsPaint)
            lineY += 30f
        }
        if (item.stats.health > 0) {
            canvas.drawText("❤️ +${item.stats.health} HP", infoX + 20f, lineY, statsPaint)
            lineY += 30f
        }
        if (item.stats.strength > 0) {
            canvas.drawText("💪 +${item.stats.strength} сила", infoX + 20f, lineY, statsPaint)
            lineY += 30f
        }
        if (item.stats.agility > 0) {
            canvas.drawText("💨 +${item.stats.agility} ловкость", infoX + 20f, lineY, statsPaint)
            lineY += 30f
        }
        if (item.stats.luck > 0) {
            canvas.drawText("🍀 +${item.stats.luck} удача", infoX + 20f, lineY, statsPaint)
        }

        val hintPaint = Paint().apply {
            color = Color.argb(150, 200, 200, 200)
            textSize = 18f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("👆 Тап для экипировки", infoX + 20f, infoY + 215f, hintPaint)
    }

    // ===== ОБРАБОТКА КАСАНИЙ =====
    fun handleTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        inventory: Inventory,
        onEquip: (Int) -> Unit,
        onUnequip: (EquipmentSlot) -> Unit,
        onClose: () -> Unit
    ): Boolean {
        // Кнопка "Закрыть"
        if (x > width / 2 - 120f && x < width / 2 + 120f &&
            y > height - 90f && y < height - 35f
        ) {
            onClose()
            return true
        }

        // ⭐ СНАЧАЛА ПРОВЕРЯЕМ КЛИК ПО СЛОТАМ ЭКИПИРОВКИ
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
                    y > cellY && y < cellY + slotSize
                ) {

                    val currentTime = System.currentTimeMillis()

                    // Проверяем двойной клик
                    if (currentTime - lastClickTime < DOUBLE_CLICK_DELAY && lastClickSlot == index) {
                        // ⭐ ДВОЙНОЙ КЛИК — экипируем
                        val items = inventory.getItems()
                        if (index < items.size) {
                            val item = items[index]
                            if (item != null && item.type != Item.ItemType.CONSUMABLE) {
                                onEquip(index)
                            }
                        }
                        lastClickTime = 0
                        lastClickSlot = -1
                    } else {
                        // Одиночный клик — просто выделяем
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

    // Проверка клика по слотам экипировки
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

            // Проверяем попадание в слот
            if (x > slotX - 70f && x < slotX + 70f &&
                y > slotY - 70f && y < slotY + 70f
            ) {

                // Если в слоте есть предмет
                if (inventory.isSlotEquipped(slot)) {
                    val currentTime = System.currentTimeMillis()
                    // ⭐ ДВОЙНОЙ КЛИК ДЛЯ СНЯТИЯ
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

    // Добавь в класс InventoryScreen
    private fun loadItemIcon(item: Item): Bitmap? {
        return when (item.id) {
            "sword_01" -> {
                try {
                    // Загружаем из drawable
                    val resId = android.content.res.Resources.getSystem().getIdentifier(
                        "sword_icon", "drawable", "com.example.myapplication"
                    )
                    if (resId != 0) {
                        BitmapFactory.decodeResource(
                            android.content.res.Resources.getSystem(), resId
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }
    }
}