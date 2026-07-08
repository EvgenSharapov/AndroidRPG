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

    // ⭐ ВКЛАДКИ ИНВЕНТАРЯ
    enum class InventoryTab {
        ALL,        // Все предметы
        EQUIPMENT,  // Вещи (оружие, броня, аксессуары)
        CONSUMABLES,// Расходники (торты, материалы)
        RUNES       // Руны
    }

    // ⭐ АДАПТИВНЫЕ РАЗМЕРЫ — ВЫЧИСЛЯЮТСЯ НА ОСНОВЕ ЭКРАНА
    private data class ScreenSizes(
        val scale: Float,
        val slotSize: Float,
        val padding: Float,
        val charDisplaySize: Float,
        val charGlowRadius: Float,
        val equipmentRadius: Float,
        val equipmentSlotSize: Float,
        val gridSlotSize: Float,
        val gridPadding: Float,
        val gridStartY: Float,
        val gridCols: Int,
        val gridRows: Int,
        val infoWidth: Float,
        val infoHeight: Float,
        val fontSizeTitle: Float,
        val fontSizeLarge: Float,
        val fontSizeMedium: Float,
        val fontSizeSmall: Float,
        val fontSizeTiny: Float,
        val closeBtnSize: Float,
        val editBtnSize: Float,
        val dialogWidth: Float,
        val dialogHeight: Float,
        val btnWidth: Float,
        val btnHeight: Float,
        val keyboardKeyWidth: Float,
        val keyboardKeyHeight: Float,
        val keyboardSpacing: Float
    )

    // ⭐ ДЛЯ ВСТАВКИ РУН
    private var isRuneInjectionMode = false  // Режим вставки руны
    private var selectedRuneIndex = -1       // Индекс выбранной руны для вставки
    private var targetItemIndex = -1         // Индекс предмета, в который вставляем

    private var sizes: ScreenSizes? = null

    private var animFrameIndex = 0
    private var animFrameTimer = 0

    private var lastClickTime = 0L
    private var lastClickSlot = -1
    private val DOUBLE_CLICK_DELAY = 300L

    private var showDeleteConfirm = false
    private var deleteItemIndex = -1
    private var deleteItemName = ""

    private var showRenameDialog = false
    private var newNameInput = ""
    private var renameError = ""
    private var isNameInputActive = false

    private var editButtonRect = RectF()

    // ⭐ ВЫЧИСЛЕНИЕ РАЗМЕРОВ
    private fun calculateSizes(width: Float, height: Float) {
        // Базовый размер — 1080p, от него считаем масштаб
        val baseWidth = 1080f
        val baseHeight = 1920f
        val scaleX = width / baseWidth
        val scaleY = height / baseHeight
        val scale = minOf(scaleX, scaleY).coerceIn(0.5f, 1.8f)  // Ограничиваем масштаб

        sizes = ScreenSizes(
            scale = scale,
            slotSize = 160f * scale,
            padding = 16f * scale,
            charDisplaySize = 280f * scale,
            charGlowRadius = 280f * scale,
            equipmentRadius = 360f * scale,
            equipmentSlotSize = 120f * scale,
            gridSlotSize = 140f * scale,
            gridPadding = 12f * scale,
            gridStartY = height * 0.55f,
            gridCols = 5,
            gridRows = 4,
            infoWidth = 680f * scale,
            infoHeight = 560f * scale,
            fontSizeTitle = 44f * scale,
            fontSizeLarge = 32f * scale,
            fontSizeMedium = 24f * scale,
            fontSizeSmall = 18f * scale,
            fontSizeTiny = 14f * scale,
            closeBtnSize = 55f * scale,
            editBtnSize = 38f * scale,
            dialogWidth = 600f * scale,
            dialogHeight = 680f * scale,
            btnWidth = 150f * scale,
            btnHeight = 50f * scale,
            keyboardKeyWidth = 65f * scale,
            keyboardKeyHeight = 65f * scale,
            keyboardSpacing = 6f * scale
        )
    }

    // ⭐ БЕЗОПАСНОЕ ПОЛУЧЕНИЕ РАЗМЕРОВ
    private fun getSizes(): ScreenSizes {
        return sizes ?: throw IllegalStateException("Sizes not calculated! Call calculateSizes first.")
    }

    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        inventory: Inventory,
        player: Player,
        gameView: GameView,
        onClose: () -> Unit,
        onRename: (String) -> Unit,
        onShowMessage: (String) -> Unit
    ) {
        calculateSizes(width, height)
        val s = getSizes()

        // --- ФОН ---
        val bgPaint = Paint().apply {
            color = Color.argb(240, 20, 15, 30)
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // --- ЗАГОЛОВОК ---
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = s.fontSizeTitle
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🎒 ИНВЕНТАРЬ", width / 2, 75f * s.scale, titlePaint)

        // --- ЗОЛОТО ---
        val goldPaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            textSize = s.fontSizeLarge
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("💰 ${player.gold}", 30f * s.scale, 75f * s.scale, goldPaint)

        // --- КНОПКА ЗАКРЫТИЯ ---
        drawCloseButton(canvas, width, s)

        // --- ПЕРСОНАЖ ---
        drawCharacterPreview(canvas, width, height, player, inventory, gameView, s)

        // --- СЛОТЫ ЭКИПИРОВКИ ---
        drawEquipmentSlots(canvas, width, height, inventory, s)

        // --- ЯЧЕЙКИ ИНВЕНТАРЯ ---
        drawInventoryGrid(canvas, width, height, inventory, s)

        // --- ИНФОРМАЦИЯ О ПРЕДМЕТЕ ---
        if (inventory.selectedSlot >= 0 && !showDeleteConfirm && !showRenameDialog) {
            val items = inventory.getItems()
            if (inventory.selectedSlot < items.size) {
                val item = items[inventory.selectedSlot]
                if (item != null) {
                    drawItemInfo(canvas, width, height, item, inventory.selectedSlot, s)
                }
            }
        }

        if (showDeleteConfirm) {
            drawDeleteConfirm(canvas, width, height, s)
        }

        if (showRenameDialog) {
            drawRenameDialog(canvas, width, height, player, onRename, s)
        }
    }

    // ⭐ КНОПКА ЗАКРЫТИЯ
    private fun drawCloseButton(canvas: Canvas, width: Float, s: ScreenSizes) {
        // ⭐ НОВЫЕ КООРДИНАТЫ: левее и ниже
        val closeX = width - 80f * s.scale  // ← было 40f, стало 80f (левее)
        val closeY = 70f * s.scale          // ← было 45f, стало 70f (ниже)
        val halfSize = s.closeBtnSize / 2 * 1.5f  // ← УВЕЛИЧИВАЕМ В 1.5 РАЗА

        val closeBgPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(closeX, closeY, halfSize + 8f * s.scale, closeBgPaint)

        val glowPaint = Paint().apply {
            shader = RadialGradient(
                closeX, closeY, halfSize + 30f * s.scale,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(closeX, closeY, halfSize + 30f * s.scale, glowPaint)

        val crossPaint = Paint().apply {
            color = Color.WHITE
            strokeWidth = 9f * s.scale  // ← было 7f, стало 9f
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        val crossSize = halfSize * 0.55f
        canvas.drawLine(closeX - crossSize, closeY - crossSize, closeX + crossSize, closeY + crossSize, crossPaint)
        canvas.drawLine(closeX + crossSize, closeY - crossSize, closeX - crossSize, closeY + crossSize, crossPaint)

        val borderPaint = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f * s.scale  // ← было 2f, стало 3f
        }
        canvas.drawCircle(closeX, closeY, halfSize + 8f * s.scale, borderPaint)
    }

    // ⭐ ОКНО ПОДТВЕРЖДЕНИЯ УДАЛЕНИЯ
    private fun drawDeleteConfirm(canvas: Canvas, width: Float, height: Float, s: ScreenSizes) {
        val dimPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width, height, dimPaint)

        val dialogPaint = Paint().apply {
            color = Color.argb(240, 30, 20, 40)
        }
        val dialogX = (width - s.dialogWidth) / 2
        val dialogY = (height - s.dialogHeight * 0.5f) / 2
        val dialogW = s.dialogWidth
        val dialogH = s.dialogHeight * 0.5f

        canvas.drawRoundRect(RectF(dialogX, dialogY, dialogX + dialogW, dialogY + dialogH), 25f * s.scale, 25f * s.scale, dialogPaint)

        val borderPaint = Paint().apply {
            color = Color.argb(100, 255, 100, 100)
            style = Paint.Style.STROKE
            strokeWidth = 3f * s.scale
        }
        canvas.drawRoundRect(RectF(dialogX, dialogY, dialogX + dialogW, dialogY + dialogH), 25f * s.scale, 25f * s.scale, borderPaint)

        val glowPaint = Paint().apply {
            shader = RadialGradient(
                width / 2, dialogY + 60f * s.scale, 300f * s.scale,
                Color.argb(40, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width / 2, dialogY + 60f * s.scale, 300f * s.scale, glowPaint)

        val iconPaint = Paint().apply {
            color = Color.rgb(255, 200, 100)
            textSize = 55f * s.scale
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("🗑️", width / 2, dialogY + 75f * s.scale, iconPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = s.fontSizeLarge
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Удалить предмет?", width / 2, dialogY + 130f * s.scale, textPaint)

        val itemPaint = Paint().apply {
            color = Color.rgb(255, 200, 100)
            textSize = s.fontSizeMedium * 1.4f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(deleteItemName, width / 2, dialogY + 175f * s.scale, itemPaint)

        val btnY = dialogY + dialogH - s.btnHeight - 20f * s.scale

        val yesPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + 50f * s.scale, btnY, dialogX + 50f * s.scale + s.btnWidth, btnY + s.btnHeight),
            15f * s.scale, 15f * s.scale, yesPaint
        )

        val yesText = Paint().apply {
            color = Color.WHITE
            textSize = s.fontSizeMedium
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("ДА", dialogX + 50f * s.scale + s.btnWidth / 2, btnY + s.btnHeight / 2 + 9f * s.scale, yesText)

        val noPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + s.dialogWidth - s.btnWidth - 50f * s.scale, btnY, dialogX + s.dialogWidth - 50f * s.scale, btnY + s.btnHeight),
            15f * s.scale, 15f * s.scale, noPaint
        )

        val noText = Paint().apply {
            color = Color.WHITE
            textSize = s.fontSizeMedium
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("НЕТ", dialogX + s.dialogWidth - s.btnWidth / 2 - 50f * s.scale, btnY + s.btnHeight / 2 + 9f * s.scale, noText)
    }

    // ⭐ ИНФОРМАЦИЯ О ПРЕДМЕТЕ
    private fun drawItemInfo(canvas: Canvas, width: Float, height: Float, item: Item, index: Int, s: ScreenSizes) {
        val infoPaint = Paint().apply {
            color = Color.argb(220, 0, 0, 0)
        }
        val infoX = 25f * s.scale
        val infoY = height * 0.20f  // ← поднял ещё выше
        val infoW = s.infoWidth
        val infoH = s.infoHeight

        canvas.drawRoundRect(RectF(infoX, infoY, infoX + infoW, infoY + infoH), 15f * s.scale, 15f * s.scale, infoPaint)

        // --- НАЗВАНИЕ (УВЕЛИЧЕНО В 2 РАЗА) ---
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = s.fontSizeMedium * 2f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📦 ${item.getDisplayName()}", infoX + 25f * s.scale, infoY + 65f * s.scale, textPaint)

        // --- КОЛИЧЕСТВО (для стакающихся предметов) ---
        var currentY = infoY + 110f * s.scale
        if (item.isStackable() && item.quantity > 1) {
            val countPaint = Paint().apply {
                color = Color.rgb(255, 215, 0)
                textSize = s.fontSizeMedium * 1.8f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("📦 Количество: ${item.quantity}", infoX + 25f * s.scale, currentY, countPaint)
            currentY += 45f * s.scale
        }

        // --- УРОВЕНЬ ЗАТОЧКИ ---
        if (item.refineLevel > 0) {
            val refinePaint = Paint().apply {
                color = Color.rgb(255, 215, 0)
                textSize = s.fontSizeMedium * 1.8f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("⭐ Уровень: +${item.refineLevel} ${item.getRefineStars()}", infoX + 25f * s.scale, currentY, refinePaint)
            currentY += 45f * s.scale
        }

        // --- РЕДКОСТЬ (УВЕЛИЧЕНА В 2 РАЗА) ---
        val rarityPaint = Paint().apply {
            color = item.getRarityColor()
            textSize = s.fontSizeMedium * 1.8f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📌 ${item.getRarityName()}", infoX + 25f * s.scale, currentY, rarityPaint)
        currentY += 50f * s.scale

        // ⭐ СТАТЫ С УЧЁТОМ РУН (УВЕЛИЧЕНЫ В 2 РАЗА)
        val finalStats = item.getFinalStats()
        val runeStats = item.getRuneStats()

        // --- ДЛЯ ОРУЖИЯ ---
        if (item.type == Item.ItemType.WEAPON) {
            val attackPaint = Paint().apply {
                color = Color.rgb(255, 200, 100)
                textSize = s.fontSizeMedium * 1.8f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }
            val baseAttack = item.stats.attack
            val refineBonus = if (item.refineLevel > 0) {
                val percent = (ItemStats.getRefineMultiplier(item.refineLevel) * 100).toInt()
                " (+${percent}% от заточки)"
            } else ""
            val runeText = if (runeStats.attack > 0) " (+${runeStats.attack} от рун)" else ""
            canvas.drawText("⚔️ Урон: ${finalStats.attack}$refineBonus$runeText", infoX + 25f * s.scale, currentY, attackPaint)
            currentY += 42f * s.scale

            if (item.refineLevel > 0 || runeStats.attack > 0) {
                val basePaint = Paint().apply {
                    color = Color.argb(150, 200, 200, 200)
                    textSize = s.fontSizeMedium * 1.4f
                    textAlign = Paint.Align.LEFT
                }
                canvas.drawText("   Базовый: $baseAttack", infoX + 25f * s.scale, currentY, basePaint)
                currentY += 35f * s.scale
            }
        }

        // --- ДЛЯ БРОНИ И ЩИТОВ ---
        else if (item.type == Item.ItemType.HELMET || item.type == Item.ItemType.CHEST ||
            item.type == Item.ItemType.PANTS || item.type == Item.ItemType.BOOTS ||
            item.type == Item.ItemType.GLOVES || item.type == Item.ItemType.BRACERS ||
            item.type == Item.ItemType.SHIELD) {

            val defensePaint = Paint().apply {
                color = Color.rgb(100, 200, 255)
                textSize = s.fontSizeMedium * 1.8f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }
            val baseDefense = item.stats.defense
            val refineBonus = if (item.refineLevel > 0) {
                val percent = (ItemStats.getRefineMultiplier(item.refineLevel) * 100).toInt()
                " (+${percent}% от заточки)"
            } else ""
            val runeText = if (runeStats.defense > 0) " (+${runeStats.defense} от рун)" else ""
            canvas.drawText("🛡️ Защита: ${finalStats.defense}$refineBonus$runeText", infoX + 25f * s.scale, currentY, defensePaint)
            currentY += 42f * s.scale

            if (item.refineLevel > 0 || runeStats.defense > 0) {
                val basePaint = Paint().apply {
                    color = Color.argb(150, 200, 200, 200)
                    textSize = s.fontSizeMedium * 1.4f
                    textAlign = Paint.Align.LEFT
                }
                canvas.drawText("   Базовый: $baseDefense", infoX + 25f * s.scale, currentY, basePaint)
                currentY += 35f * s.scale
            }
        }

        // --- ДЛЯ АКСЕССУАРОВ ---
        else if (item.type == Item.ItemType.RING || item.type == Item.ItemType.NECKLACE) {
            val statsPaint = Paint().apply {
                color = Color.rgb(200, 200, 255)
                textSize = s.fontSizeMedium * 1.6f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }

            if (finalStats.health > 0) {
                val runeText = if (runeStats.health > 0) " (+${runeStats.health} от рун)" else ""
                canvas.drawText("❤️ +${finalStats.health} HP$runeText", infoX + 25f * s.scale, currentY, statsPaint)
                currentY += 38f * s.scale
            }
            if (finalStats.dodge > 0) {
                val runeText = if (runeStats.dodge > 0) " (+${runeStats.dodge}% от рун)" else ""
                canvas.drawText("💨 +${finalStats.dodge}% уклонения$runeText", infoX + 25f * s.scale, currentY, statsPaint)
                currentY += 38f * s.scale
            }
            if (finalStats.crit > 0) {
                val runeText = if (runeStats.crit > 0) " (+${runeStats.crit}% от рун)" else ""
                canvas.drawText("💥 +${finalStats.crit}% крита$runeText", infoX + 25f * s.scale, currentY, statsPaint)
                currentY += 38f * s.scale
            }
            if (finalStats.critDamage > 0) {
                val runeText = if (runeStats.critDamage > 0) " (+${runeStats.critDamage}% от рун)" else ""
                canvas.drawText("⚡ +${finalStats.critDamage}% крит. урона$runeText", infoX + 25f * s.scale, currentY, statsPaint)
                currentY += 38f * s.scale
            }
            if (finalStats.hpRegen > 0) {
                val runeText = if (runeStats.hpRegen > 0) " (+${runeStats.hpRegen} от рун)" else ""
                canvas.drawText("🔄 +${finalStats.hpRegen} регенерации$runeText", infoX + 25f * s.scale, currentY, statsPaint)
                currentY += 38f * s.scale
            }
            if (finalStats.goldBonus > 0) {
                val runeText = if (runeStats.goldBonus > 0) " (+${runeStats.goldBonus}% от рун)" else ""
                canvas.drawText("💰 +${finalStats.goldBonus}% золота$runeText", infoX + 25f * s.scale, currentY, statsPaint)
                currentY += 38f * s.scale
            }
            if (finalStats.expBonus > 0) {
                val runeText = if (runeStats.expBonus > 0) " (+${runeStats.expBonus}% от рун)" else ""
                canvas.drawText("⭐ +${finalStats.expBonus}% опыта$runeText", infoX + 25f * s.scale, currentY, statsPaint)
                currentY += 38f * s.scale
            }

            if (finalStats.health == 0 && finalStats.dodge == 0 && finalStats.crit == 0 &&
                finalStats.critDamage == 0 && finalStats.hpRegen == 0 &&
                finalStats.goldBonus == 0 && finalStats.expBonus == 0) {
                val emptyPaint = Paint().apply {
                    color = Color.argb(150, 200, 200, 200)
                    textSize = s.fontSizeMedium * 1.6f
                    textAlign = Paint.Align.LEFT
                }
                canvas.drawText("📖 Нет дополнительных статов", infoX + 25f * s.scale, currentY, emptyPaint)
                currentY += 38f * s.scale
            }
        }

        // --- ДЛЯ РАСХОДНИКОВ ---
        else if (item.type == Item.ItemType.CONSUMABLE) {
            val healAmount = when (item.id) {
                "cake_small" -> "30 HP"
                "cake_medium" -> "60 HP"
                "cake_large" -> "120 HP"
                "oridecon" -> "Материал для заточки оружия"
                "elunium" -> "Материал для заточки брони"
                else -> ""
            }
            if (healAmount.isNotEmpty()) {
                val healPaint = Paint().apply {
                    color = if (item.id == "oridecon" || item.id == "elunium") {
                        Color.rgb(200, 200, 100)
                    } else {
                        Color.rgb(100, 255, 100)
                    }
                    textSize = s.fontSizeMedium * 1.8f
                    textAlign = Paint.Align.LEFT
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("📖 $healAmount", infoX + 25f * s.scale, currentY, healPaint)
                currentY += 45f * s.scale
            }

            // Для рун
            if (item.id.startsWith("rune_")) {
                val runePaint = Paint().apply {
                    color = Color.rgb(255, 215, 0)
                    textSize = s.fontSizeMedium * 1.6f
                    textAlign = Paint.Align.LEFT
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("💎 Можно вставить в предмет с слотами", infoX + 25f * s.scale, currentY, runePaint)
                currentY += 42f * s.scale
            }
        }

        // ⭐ СЛОТЫ ДЛЯ РУН (УВЕЛИЧЕНЫ И СМЕЩЕНЫ НИЖЕ)
        if (item.type != Item.ItemType.CONSUMABLE) {
            val maxSlots = item.getMaxRuneSlots()
            if (maxSlots > 0) {
                // Добавляем небольшой отступ перед слотами
                currentY += 10f * s.scale

                val slotPaint = Paint().apply {
                    color = Color.argb(150, 200, 200, 200)
                    style = Paint.Style.STROKE
                    strokeWidth = 3f * s.scale
                }
                val filledSlotPaint = Paint().apply {
                    color = Color.rgb(255, 215, 0)
                    style = Paint.Style.FILL
                }
                val slotTextPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = s.fontSizeMedium * 1.6f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }

                val slotStartX = infoX + 25f * s.scale
                val slotY = currentY + 10f * s.scale
                val slotSize = 45f * s.scale
                val slotSpacing = 15f * s.scale

                // Заголовок
                val titlePaint = Paint().apply {
                    color = Color.argb(200, 200, 200, 200)
                    textSize = s.fontSizeMedium * 1.6f
                    textAlign = Paint.Align.LEFT
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText("💎 Слоты для рун:", slotStartX, slotY + slotSize * 0.7f, titlePaint)

                // Рисуем каждый слот
                for (i in 0 until maxSlots) {
                    val x = slotStartX + 160f * s.scale + i * (slotSize + slotSpacing)
                    val y = slotY

                    if (i < item.runes.size) {
                        canvas.drawRoundRect(
                            RectF(x, y, x + slotSize, y + slotSize),
                            8f * s.scale, 8f * s.scale, filledSlotPaint
                        )
                        val rune = item.runes[i]
                        canvas.drawText(rune.icon, x + slotSize / 2, y + slotSize * 0.75f, slotTextPaint)

                        // Название руны под слотом
                        val runeNamePaint = Paint().apply {
                            color = Color.argb(180, 200, 200, 200)
                            textSize = s.fontSizeMedium * 1.0f
                            textAlign = Paint.Align.CENTER
                        }
                        val shortName = if (rune.name.length > 10) rune.name.take(10) + ".." else rune.name
                        canvas.drawText(shortName, x + slotSize / 2, y + slotSize + 22f * s.scale, runeNamePaint)
                    } else {
                        canvas.drawRoundRect(
                            RectF(x, y, x + slotSize, y + slotSize),
                            8f * s.scale, 8f * s.scale, slotPaint
                        )
                        val emptyPaint = Paint().apply {
                            color = Color.argb(80, 255, 255, 255)
                            textSize = s.fontSizeMedium * 1.4f
                            textAlign = Paint.Align.CENTER
                        }
                        canvas.drawText("○", x + slotSize / 2, y + slotSize * 0.75f, emptyPaint)
                    }
                }

                // Показываем количество занятых слотов
                val countPaint = Paint().apply {
                    color = Color.argb(180, 200, 200, 200)
                    textSize = s.fontSizeMedium * 1.4f
                    textAlign = Paint.Align.RIGHT
                    typeface = Typeface.DEFAULT_BOLD
                }
                val countX = infoX + infoW - 25f * s.scale
                canvas.drawText("${item.runes.size}/$maxSlots", countX, slotY + slotSize * 0.7f, countPaint)

                currentY += slotSize + 55f * s.scale
            }
        }

        // --- ⭐ КНОПКИ (УВЕЛИЧЕНЫ В 2 РАЗА) ---

        // КНОПКА УДАЛЕНИЯ (справа сверху)
        val deleteBtnX = infoX + infoW - 75f * s.scale
        val deleteBtnY = infoY + 30f * s.scale
        val deleteSize = 100f * s.scale

        val deleteBgPaint = Paint().apply {
            color = Color.rgb(180, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(deleteBtnX, deleteBtnY, deleteSize / 2 + 8f * s.scale, deleteBgPaint)

        val deleteCross = Paint().apply {
            color = Color.WHITE
            strokeWidth = 8f * s.scale
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        val crossSize = deleteSize * 0.3f
        canvas.drawLine(deleteBtnX - crossSize, deleteBtnY - crossSize, deleteBtnX + crossSize, deleteBtnY + crossSize, deleteCross)
        canvas.drawLine(deleteBtnX + crossSize, deleteBtnY - crossSize, deleteBtnX - crossSize, deleteBtnY + crossSize, deleteCross)

        val deleteHint = Paint().apply {
            color = Color.argb(200, 255, 150, 150)
            textSize = s.fontSizeMedium * 1.2f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🗑️", deleteBtnX, deleteBtnY + deleteSize * 0.45f + 10f * s.scale, deleteHint)

        // ⭐ КНОПКА "ИНЖЕКТ" (слева, увеличена в 2 раза)
        val injectBtnSize = 140f * s.scale
        val injectBtnX = infoX + infoW - 320f * s.scale
        val injectBtnY = infoY + infoH - 90f * s.scale
        val injectBtnSpacing = 30f * s.scale  // ← отступ между кнопками

        val canInject = item.getMaxRuneSlots() > 0 && item.runes.size < item.getMaxRuneSlots()

        if (canInject) {
            val injectBgPaint = Paint().apply {
                color = Color.rgb(100, 200, 255)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(injectBtnX, injectBtnY, injectBtnX + injectBtnSize, injectBtnY + injectBtnSize),
                15f * s.scale, 15f * s.scale, injectBgPaint
            )

            val injectGlowPaint = Paint().apply {
                shader = RadialGradient(
                    injectBtnX + injectBtnSize / 2, injectBtnY + injectBtnSize / 2, injectBtnSize,
                    Color.argb(60, 100, 200, 255),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(injectBtnX + injectBtnSize / 2, injectBtnY + injectBtnSize / 2, injectBtnSize, injectGlowPaint)

            val injectPaint = Paint().apply {
                color = Color.WHITE
                textSize = 50f * s.scale
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("💉", injectBtnX + injectBtnSize / 2, injectBtnY + injectBtnSize / 2 + 25f * s.scale, injectPaint)

            val injectTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = s.fontSizeMedium * 1.0f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("Инжект", injectBtnX + injectBtnSize / 2, injectBtnY + injectBtnSize + 28f * s.scale, injectTextPaint)
        } else {
            val injectBgPaint = Paint().apply {
                color = Color.argb(100, 100, 100, 100)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(injectBtnX, injectBtnY, injectBtnX + injectBtnSize, injectBtnY + injectBtnSize),
                15f * s.scale, 15f * s.scale, injectBgPaint
            )
            val injectPaint = Paint().apply {
                color = Color.argb(100, 200, 200, 200)
                textSize = 50f * s.scale
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("💉", injectBtnX + injectBtnSize / 2, injectBtnY + injectBtnSize / 2 + 25f * s.scale, injectPaint)
        }

        // ⭐ КНОПКА ЗАТОЧКИ (справа, увеличена в 2 раза)
        val refineBtnSize = 140f * s.scale
        val refineBtnX = infoX + infoW - 150f * s.scale
        val refineBtnY = infoY + infoH - 90f * s.scale

        val canRefine = item.isRefinable() && item.refineLevel < 10

        if (canRefine) {
            val refineBgPaint = Paint().apply {
                color = Color.rgb(255, 180, 50)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(refineBtnX, refineBtnY, refineBtnX + refineBtnSize, refineBtnY + refineBtnSize),
                15f * s.scale, 15f * s.scale, refineBgPaint
            )

            val refineGlowPaint = Paint().apply {
                shader = RadialGradient(
                    refineBtnX + refineBtnSize / 2, refineBtnY + refineBtnSize / 2, refineBtnSize,
                    Color.argb(60, 255, 200, 100),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(refineBtnX + refineBtnSize / 2, refineBtnY + refineBtnSize / 2, refineBtnSize, refineGlowPaint)

            val hammerPaint = Paint().apply {
                color = Color.WHITE
                textSize = 60f * s.scale
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("🔨", refineBtnX + refineBtnSize / 2, refineBtnY + refineBtnSize / 2 + 28f * s.scale, hammerPaint)

            val refineTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = s.fontSizeMedium * 1.0f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("Заточка", refineBtnX + refineBtnSize / 2, refineBtnY + refineBtnSize + 28f * s.scale, refineTextPaint)
        } else {
            val refineBgPaint = Paint().apply {
                color = Color.argb(100, 100, 100, 100)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(refineBtnX, refineBtnY, refineBtnX + refineBtnSize, refineBtnY + refineBtnSize),
                15f * s.scale, 15f * s.scale, refineBgPaint
            )

            val hammerPaint = Paint().apply {
                color = Color.argb(100, 200, 200, 200)
                textSize = 60f * s.scale
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("🔨", refineBtnX + refineBtnSize / 2, refineBtnY + refineBtnSize / 2 + 28f * s.scale, hammerPaint)
        }
    }

    // ⭐ ПРЕДПРОСМОТР ПЕРСОНАЖА
    private fun drawCharacterPreview(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        inventory: Inventory,
        gameView: GameView,
        s: ScreenSizes
    ) {
        val centerX = width / 2
        val centerY = height * 0.30f
        val displaySize = s.charDisplaySize

        val glowPaint = Paint().apply {
            shader = RadialGradient(
                centerX, centerY, s.charGlowRadius,
                Color.argb(60, 100, 200, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(centerX, centerY, s.charGlowRadius, glowPaint)

        val animationFrames = gameView.getCharacterIdleFrames()
        val spriteSheet = gameView.getCharacterIdleSpriteSheet()

        if (animationFrames.isNotEmpty() && spriteSheet != null) {
            animFrameTimer++
            if (animFrameTimer > 3) {
                animFrameTimer = 0
                animFrameIndex = (animFrameIndex + 1) % animationFrames.size
            }

            val currentFrame = animationFrames[animFrameIndex % animationFrames.size]
            val dstRect = RectF(
                centerX - displaySize / 2,
                centerY - displaySize / 2,
                centerX + displaySize / 2,
                centerY + displaySize / 2
            )

            canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)

            // Оружие
            val weapon = inventory.getEquipment(EquipmentSlot.WEAPON)
            if (weapon != null) {
                val weaponIcon = loadItemIcon(weapon)
                val iconSize = displaySize * 0.4f
                if (weaponIcon != null) {
                    val dstRectWeapon = RectF(
                        centerX + displaySize * 0.55f - iconSize / 2,
                        centerY - iconSize / 2,
                        centerX + displaySize * 0.55f + iconSize / 2,
                        centerY + iconSize / 2
                    )
                    canvas.drawBitmap(weaponIcon, null, dstRectWeapon, null)
                } else {
                    val weaponPaint = Paint().apply {
                        color = Color.YELLOW
                        textSize = iconSize
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("⚔️", centerX + displaySize * 0.55f, centerY + iconSize * 0.35f, weaponPaint)
                }

                val weaponNamePaint = Paint().apply {
                    color = Color.argb(200, 255, 215, 0)
                    textSize = s.fontSizeTiny
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val shortName = if (weapon.name.length > 10) weapon.name.take(10) + ".." else weapon.name
                canvas.drawText(shortName, centerX + displaySize * 0.55f, centerY + iconSize * 0.75f, weaponNamePaint)
            }

            // ⭐ ИМЯ С КНОПКОЙ РЕДАКТИРОВАНИЯ
            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = s.fontSizeLarge
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            val nameText = "${player.name} Ур.${player.level}"
            canvas.drawText(nameText, centerX, centerY + displaySize / 2 + 45f * s.scale, namePaint)

            // Кнопка редактирования
            val editX = centerX + displaySize * 0.5f
            val editY = centerY + displaySize / 2 + 45f * s.scale - 5f * s.scale
            val editSize = s.editBtnSize

            editButtonRect.set(
                editX - editSize / 2,
                editY - editSize / 2,
                editX + editSize / 2,
                editY + editSize / 2
            )

            val editBgPaint = Paint().apply {
                color = Color.argb(150, 255, 200, 50)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(editButtonRect, 10f * s.scale, 10f * s.scale, editBgPaint)

            val editBorderPaint = Paint().apply {
                color = Color.argb(200, 255, 215, 0)
                style = Paint.Style.STROKE
                strokeWidth = 2f * s.scale
            }
            canvas.drawRoundRect(editButtonRect, 10f * s.scale, 10f * s.scale, editBorderPaint)

            val editIconPaint = Paint().apply {
                color = Color.WHITE
                textSize = editSize * 0.65f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("✏️", editX, editY + editSize * 0.3f, editIconPaint)

            // Статы
            val stats = inventory.getTotalStats()
            val statsPaint = Paint().apply {
                color = Color.argb(200, 255, 255, 200)
                textSize = s.fontSizeMedium
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "⚔️${stats.attack}  🛡️${stats.defense}  ❤️${stats.health}",
                centerX,
                centerY + displaySize / 2 + 95f * s.scale,
                statsPaint
            )

        } else {
            // Fallback
            val avatarPaint = Paint().apply {
                color = Color.rgb(50, 150, 255)
            }
            val fallbackSize = displaySize * 0.7f
            canvas.drawCircle(centerX, centerY, fallbackSize, avatarPaint)

            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = displaySize * 0.25f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("⚔️", centerX, centerY + displaySize * 0.2f, namePaint)

            val namePaint2 = Paint().apply {
                color = Color.WHITE
                textSize = s.fontSizeMedium
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("${player.name} Ур.${player.level}", centerX, centerY + fallbackSize + 30f * s.scale, namePaint2)
        }
    }

    // ⭐ ОКНО ПЕРЕИМЕНОВАНИЯ
    private fun drawRenameDialog(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        onRename: (String) -> Unit,
        s: ScreenSizes
    ) {
        val dimPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width, height, dimPaint)

        val dialogPaint = Paint().apply {
            color = Color.argb(240, 30, 20, 40)
        }
        val dialogX = (width - s.dialogWidth) / 2
        val dialogY = (height - s.dialogHeight) / 2

        canvas.drawRoundRect(RectF(dialogX, dialogY, dialogX + s.dialogWidth, dialogY + s.dialogHeight), 25f * s.scale, 25f * s.scale, dialogPaint)

        val borderPaint = Paint().apply {
            color = Color.argb(100, 255, 215, 0)
            style = Paint.Style.STROKE
            strokeWidth = 3f * s.scale
        }
        canvas.drawRoundRect(RectF(dialogX, dialogY, dialogX + s.dialogWidth, dialogY + s.dialogHeight), 25f * s.scale, 25f * s.scale, borderPaint)

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = s.fontSizeLarge
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("✏️ Переименовать", width / 2, dialogY + 60f * s.scale, titlePaint)

        val costPaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            textSize = s.fontSizeMedium
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Стоимость: 500 💰", width / 2, dialogY + 100f * s.scale, costPaint)

        val currentNamePaint = Paint().apply {
            color = Color.argb(150, 200, 200, 200)
            textSize = s.fontSizeSmall
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Текущее имя: ${player.name}", width / 2, dialogY + 140f * s.scale, currentNamePaint)

        // Поле ввода
        val inputBgPaint = Paint().apply {
            color = Color.argb(150, 255, 255, 255)
        }
        val inputX = dialogX + 50f * s.scale
        val inputY = dialogY + 170f * s.scale
        val inputWidth = s.dialogWidth - 100f * s.scale
        val inputHeight = 50f * s.scale
        canvas.drawRoundRect(RectF(inputX, inputY, inputX + inputWidth, inputY + inputHeight), 12f * s.scale, 12f * s.scale, inputBgPaint)

        if (isNameInputActive) {
            val activePaint = Paint().apply {
                color = Color.argb(80, 255, 215, 0)
                style = Paint.Style.STROKE
                strokeWidth = 3f * s.scale
            }
            canvas.drawRoundRect(RectF(inputX, inputY, inputX + inputWidth, inputY + inputHeight), 12f * s.scale, 12f * s.scale, activePaint)
        }

        val inputTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = s.fontSizeMedium
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        val displayText = if (newNameInput.isEmpty()) "Введите новое имя..." else newNameInput
        inputTextPaint.color = if (newNameInput.isEmpty()) Color.argb(150, 100, 100, 100) else Color.BLACK
        canvas.drawText(displayText, width / 2, inputY + 38f * s.scale, inputTextPaint)

        if (renameError.isNotEmpty()) {
            val errorPaint = Paint().apply {
                color = Color.RED
                textSize = s.fontSizeSmall
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(renameError, width / 2, inputY + inputHeight + 30f * s.scale, errorPaint)
        }

        // ⭐ КЛАВИАТУРА
        val keyboardStartY = inputY + inputHeight + 80f * s.scale
        drawKeyboard(canvas, dialogX, keyboardStartY, s)

        // ⭐ КНОПКИ ДЕЙСТВИЙ
        val btnY = keyboardStartY + (3 * (s.keyboardKeyHeight + s.keyboardSpacing) + 30f * s.scale) + 20f * s.scale

        val canRename = newNameInput.isNotEmpty() && newNameInput.length >= 2 && player.gold >= 500
        val acceptPaint = Paint().apply {
            color = if (canRename) Color.rgb(50, 200, 50) else Color.argb(100, 100, 100, 100)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + 50f * s.scale, btnY, dialogX + 50f * s.scale + s.btnWidth, btnY + s.btnHeight),
            12f * s.scale, 12f * s.scale, acceptPaint
        )
        val acceptTextPaint = Paint().apply {
            color = if (canRename) Color.WHITE else Color.argb(150, 200, 200, 200)
            textSize = s.fontSizeMedium
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("✅ Принять", dialogX + 50f * s.scale + s.btnWidth / 2, btnY + s.btnHeight / 2 + 8f * s.scale, acceptTextPaint)

        val cancelPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + s.dialogWidth - s.btnWidth - 50f * s.scale, btnY, dialogX + s.dialogWidth - 50f * s.scale, btnY + s.btnHeight),
            12f * s.scale, 12f * s.scale, cancelPaint
        )
        val cancelTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = s.fontSizeMedium
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("❌ Отмена", dialogX + s.dialogWidth - s.btnWidth / 2 - 50f * s.scale, btnY + s.btnHeight / 2 + 8f * s.scale, cancelTextPaint)
    }

    // ⭐ КЛАВИАТУРА
    private fun drawKeyboard(canvas: Canvas, startX: Float, startY: Float, s: ScreenSizes) {
        val keyWidth = s.keyboardKeyWidth
        val keyHeight = s.keyboardKeyHeight
        val keySpacing = s.keyboardSpacing
        val keyboardWidth = 10 * (keyWidth + keySpacing) - keySpacing
        val offsetX = (s.dialogWidth - keyboardWidth) / 2
        val x = startX + offsetX

        val rows = listOf(
            "ЙЦУКЕНГШЩЗХЪ",
            "ФЫВАПРОЛДЖЭ",
            "ЯЧСМИТЬБЮ"
        )

        val keyPaint = Paint().apply {
            color = Color.rgb(60, 60, 80)
            style = Paint.Style.FILL
        }
        val keyBorderPaint = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f * s.scale
        }
        val keyTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = s.fontSizeMedium * 0.85f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        for ((rowIndex, row) in rows.withIndex()) {
            val y = startY + rowIndex * (keyHeight + keySpacing) + 15f * s.scale
            val rowWidth = row.length * (keyWidth + keySpacing) - keySpacing
            val rowOffsetX = (s.dialogWidth - rowWidth) / 2
            val xPos = startX + rowOffsetX

            for ((charIndex, char) in row.withIndex()) {
                val keyXPos = xPos + charIndex * (keyWidth + keySpacing)

                canvas.drawRoundRect(RectF(keyXPos, y, keyXPos + keyWidth, y + keyHeight), 10f * s.scale, 10f * s.scale, keyPaint)
                canvas.drawRoundRect(RectF(keyXPos, y, keyXPos + keyWidth, y + keyHeight), 10f * s.scale, 10f * s.scale, keyBorderPaint)

                if (rowIndex == 0 && charIndex == row.length - 1) {
                    keyTextPaint.textSize = s.fontSizeMedium * 0.7f
                    canvas.drawText("⌫", keyXPos + keyWidth / 2, y + keyHeight / 2 + 8f * s.scale, keyTextPaint)
                    keyTextPaint.textSize = s.fontSizeMedium * 0.85f
                } else if (rowIndex == 2 && charIndex == row.length - 1) {
                    val spaceWidth = keyWidth * 3 + keySpacing * 2
                    val spacePaint = Paint().apply {
                        color = Color.rgb(60, 60, 80)
                        style = Paint.Style.FILL
                    }
                    canvas.drawRoundRect(RectF(keyXPos, y, keyXPos + spaceWidth, y + keyHeight), 10f * s.scale, 10f * s.scale, spacePaint)
                    canvas.drawRoundRect(RectF(keyXPos, y, keyXPos + spaceWidth, y + keyHeight), 10f * s.scale, 10f * s.scale, keyBorderPaint)
                    keyTextPaint.textSize = s.fontSizeMedium * 0.7f
                    canvas.drawText("ПРОБЕЛ", keyXPos + spaceWidth / 2, y + keyHeight / 2 + 8f * s.scale, keyTextPaint)
                    keyTextPaint.textSize = s.fontSizeMedium * 0.85f
                    break
                } else {
                    canvas.drawText(char.toString(), keyXPos + keyWidth / 2, y + keyHeight / 2 + 10f * s.scale, keyTextPaint)
                }
            }
        }
    }

    // ⭐ СЛОТЫ ЭКИПИРОВКИ
    private fun drawEquipmentSlots(
        canvas: Canvas,
        width: Float,
        height: Float,
        inventory: Inventory,
        s: ScreenSizes
    ) {
        val centerX = width / 2
        val centerY = height * 0.30f
        val radius = s.equipmentRadius
        val slotSize = s.equipmentSlotSize

        val slots = listOf(
            EquipmentSlot.HELMET to -90f,
            EquipmentSlot.WEAPON to -150f,
            EquipmentSlot.SHIELD to -30f,
            EquipmentSlot.CHEST to 0f,
            EquipmentSlot.GLOVES to 240f,
            EquipmentSlot.BRACERS to 120f,
            EquipmentSlot.PANTS to 180f,
            EquipmentSlot.BOOTS to 60f,
            EquipmentSlot.NECKLACE to 300f,
            EquipmentSlot.RING1 to -210f,
            EquipmentSlot.RING2 to -270f
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
                RectF(x - slotSize / 2, y - slotSize / 2, x + slotSize / 2, y + slotSize / 2),
                15f * s.scale, 15f * s.scale, slotPaint
            )

            val item = inventory.getEquipment(slot)

            if (item == null) {
                val iconPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = slotSize * 0.5f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText(getSlotIcon(slot), x, y + slotSize * 0.2f, iconPaint)
            } else {
                val borderPaint = Paint().apply {
                    color = Color.YELLOW
                    style = Paint.Style.STROKE
                    strokeWidth = 4f * s.scale
                }
                canvas.drawRoundRect(
                    RectF(x - slotSize / 2, y - slotSize / 2, x + slotSize / 2, y + slotSize / 2),
                    15f * s.scale, 15f * s.scale, borderPaint
                )

                val icon = loadItemIcon(item)
                if (icon != null) {
                    val iconSize = slotSize * 0.65f
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
                        textSize = slotSize * 0.55f
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.drawText("📦", x, y + slotSize * 0.25f, emojiPaint)
                }

                val namePaint = Paint().apply {
                    color = Color.GREEN
                    textSize = s.fontSizeTiny
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                val shortName = if (item.name.length > 6) item.name.take(6) + ".." else item.name
                canvas.drawText(shortName, x, y + slotSize / 2 + s.fontSizeTiny * 0.6f, namePaint)
            }
        }
    }

    // ⭐ ЯЧЕЙКИ ИНВЕНТАРЯ
    // ⭐ ЯЧЕЙКИ ИНВЕНТАРЯ
    private fun drawInventoryGrid(
        canvas: Canvas,
        width: Float,
        height: Float,
        inventory: Inventory,
        s: ScreenSizes
    ) {
        val slotSize = s.gridSlotSize
        val padding = s.gridPadding
        val startY = s.gridStartY
        val cols = s.gridCols
        val rows = s.gridRows
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
                canvas.drawRoundRect(RectF(x, y, x + slotSize, y + slotSize), 12f * s.scale, 12f * s.scale, cellPaint)

                if (index < items.size) {
                    val item = items[index]
                    if (item != null) {
                        // ⭐ ПРОВЕРЯЕМ, В РЕЖИМЕ ЛИ ИНЖЕКТА И ЭТО РУНА
                        val isRune = item.id.startsWith("rune_")
                        val isSelectableRune = isRuneInjectionMode && isRune

                        // Если в режиме инжекта — подсвечиваем руны
                        if (isSelectableRune) {
                            val glowPaint = Paint().apply {
                                color = Color.argb(80, 255, 215, 0)
                                style = Paint.Style.FILL
                            }
                            canvas.drawRoundRect(
                                RectF(x - 4f * s.scale, y - 4f * s.scale,
                                    x + slotSize + 4f * s.scale, y + slotSize + 4f * s.scale),
                                16f * s.scale, 16f * s.scale, glowPaint
                            )

                            val borderPaint = Paint().apply {
                                color = Color.rgb(255, 215, 0)
                                style = Paint.Style.STROKE
                                strokeWidth = 3f * s.scale
                            }
                            canvas.drawRoundRect(
                                RectF(x - 2f * s.scale, y - 2f * s.scale,
                                    x + slotSize + 2f * s.scale, y + slotSize + 2f * s.scale),
                                14f * s.scale, 14f * s.scale, borderPaint
                            )
                        }

                        // Иконка предмета
                        val icon = loadItemIcon(item)
                        if (icon != null) {
                            val iconSize = slotSize * 0.6f
                            val dstRect = RectF(
                                x + (slotSize - iconSize) / 2,
                                y + (slotSize - iconSize) / 2 - slotSize * 0.05f,
                                x + (slotSize + iconSize) / 2,
                                y + (slotSize + iconSize) / 2 - slotSize * 0.05f
                            )
                            drawItemRarity(canvas, x, y, slotSize, slotSize, item.rarity, s)
                            canvas.drawBitmap(icon, null, dstRect, null)
                        } else {
                            // Эмодзи если нет иконки
                            val emojiPaint = Paint().apply {
                                color = Color.WHITE
                                textSize = slotSize * 0.45f
                                textAlign = Paint.Align.CENTER
                            }
                            val emoji = when {
                                item.id.startsWith("rune_") -> "💎"
                                item.type == Item.ItemType.WEAPON -> "🗡️"
                                item.type == Item.ItemType.SHIELD -> "🛡️"
                                item.type == Item.ItemType.CONSUMABLE -> {
                                    when {
                                        item.id.startsWith("cake_") -> "🍰"
                                        item.id == "oridecon" || item.id == "elunium" -> "⛏️"
                                        else -> "🍽️"
                                    }
                                }
                                item.type == Item.ItemType.HELMET -> "⛑️"
                                item.type == Item.ItemType.CHEST -> "👕"
                                item.type == Item.ItemType.PANTS -> "👖"
                                item.type == Item.ItemType.BOOTS -> "👢"
                                item.type == Item.ItemType.GLOVES -> "🧤"
                                item.type == Item.ItemType.BRACERS -> "💪"
                                item.type == Item.ItemType.RING -> "💍"
                                item.type == Item.ItemType.NECKLACE -> "📿"
                                else -> "📦"
                            }
                            canvas.drawText(emoji, x + slotSize / 2, y + slotSize / 2 + slotSize * 0.15f, emojiPaint)
                        }

                        // ⭐ ОТОБРАЖЕНИЕ КОЛИЧЕСТВА (для стакающихся предметов)
                        if (item.isStackable() && item.quantity > 1) {
                            val countPaint = Paint().apply {
                                color = Color.rgb(255, 215, 0)
                                textSize = s.fontSizeSmall * 1.5f
                                textAlign = Paint.Align.RIGHT
                                typeface = Typeface.DEFAULT_BOLD
                                isAntiAlias = true
                            }
                            val bgCountPaint = Paint().apply {
                                color = Color.argb(200, 0, 0, 0)
                                style = Paint.Style.FILL
                            }
                            val countText = "×${item.quantity}"
                            val textWidth = countPaint.measureText(countText)
                            val countX = x + slotSize - 8f * s.scale
                            val countY = y + slotSize - 8f * s.scale

                            val bgPadding = 6f * s.scale
                            canvas.drawRoundRect(
                                RectF(
                                    countX - textWidth - bgPadding - 4f * s.scale,
                                    countY - s.fontSizeMedium * 1.2f,
                                    countX + bgPadding + 4f * s.scale,
                                    countY + 6f * s.scale
                                ),
                                8f * s.scale, 8f * s.scale, bgCountPaint
                            )
                            canvas.drawText(countText, countX, countY, countPaint)
                        }

                        // Название предмета
                        val textPaint = Paint().apply {
                            color = when {
                                item.id.startsWith("rune_") -> Color.rgb(255, 215, 0)
                                item.type == Item.ItemType.WEAPON -> Color.rgb(255, 200, 100)
                                item.type == Item.ItemType.SHIELD -> Color.rgb(100, 200, 255)
                                item.type == Item.ItemType.HELMET -> Color.rgb(100, 200, 255)
                                item.type == Item.ItemType.CHEST -> Color.rgb(200, 100, 255)
                                item.type == Item.ItemType.CONSUMABLE -> {
                                    when {
                                        item.id.startsWith("cake_") -> Color.rgb(100, 255, 100)
                                        item.id == "oridecon" || item.id == "elunium" -> Color.rgb(200, 200, 100)
                                        else -> Color.rgb(100, 255, 100)
                                    }
                                }
                                item.type == Item.ItemType.RING -> Color.rgb(255, 200, 200)
                                item.type == Item.ItemType.NECKLACE -> Color.rgb(200, 200, 255)
                                else -> Color.WHITE
                            }
                            textSize = s.fontSizeTiny
                            textAlign = Paint.Align.CENTER
                            typeface = Typeface.DEFAULT_BOLD
                        }
                        val displayName = if (item.name.length > 8) item.name.take(8) + ".." else item.name
                        if (item.refineLevel > 0) {
                            canvas.drawText("+${item.refineLevel} $displayName", x + slotSize / 2, y + slotSize - s.fontSizeTiny * 0.6f, textPaint)
                        } else {
                            canvas.drawText(displayName, x + slotSize / 2, y + slotSize - s.fontSizeTiny * 0.6f, textPaint)
                        }

                        // Подсказка для расходников
                        if (item.type == Item.ItemType.CONSUMABLE && !item.id.startsWith("rune_") && !item.id.startsWith("oridecon") && !item.id.startsWith("elunium")) {
                            val hintPaint = Paint().apply {
                                color = Color.argb(150, 100, 255, 100)
                                textSize = s.fontSizeTiny * 0.8f
                                textAlign = Paint.Align.CENTER
                            }
                            canvas.drawText("🍴 Использовать", x + slotSize / 2, y + slotSize - s.fontSizeTiny * 1.1f, hintPaint)
                        }

                        // ⭐ ПОДСКАЗКА ДЛЯ РУН (в режиме инжекта)
                        if (isSelectableRune) {
                            val hintPaint = Paint().apply {
                                color = Color.argb(200, 255, 215, 0)
                                textSize = s.fontSizeTiny * 0.8f
                                textAlign = Paint.Align.CENTER
                                typeface = Typeface.DEFAULT_BOLD
                            }
                            canvas.drawText("👆 Нажмите для вставки", x + slotSize / 2, y + slotSize - s.fontSizeTiny * 1.8f, hintPaint)
                        }

                        // Рамка ячейки
                        val borderPaint = Paint().apply {
                            color = Color.argb(80, 255, 255, 255)
                            style = Paint.Style.STROKE
                            strokeWidth = 2f * s.scale
                        }
                        canvas.drawRoundRect(RectF(x, y, x + slotSize, y + slotSize), 12f * s.scale, 12f * s.scale, borderPaint)
                    }
                }
            }
        }
    }

    // ⭐ ОТРИСОВКА РАМКИ РЕДКОСТИ
    private fun drawItemRarity(canvas: Canvas, x: Float, y: Float, width: Float, height: Float, rarity: ItemRarity, s: ScreenSizes) {
        val color = when (rarity) {
            ItemRarity.COMMON -> Color.rgb(200, 200, 200)
            ItemRarity.UNCOMMON -> Color.rgb(50, 200, 50)
            ItemRarity.RARE -> Color.rgb(50, 150, 255)
            ItemRarity.EPIC -> Color.rgb(200, 100, 255)
            ItemRarity.LEGENDARY -> Color.rgb(255, 150, 50)
            ItemRarity.MYTHIC -> Color.rgb(255, 215, 0)
        }

        val glowPaint = Paint().apply {
            this.color = color
            alpha = 40
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(x - 4f * s.scale, y - 4f * s.scale, x + width + 4f * s.scale, y + height + 4f * s.scale),
            16f * s.scale, 16f * s.scale, glowPaint
        )

        val borderPaint = Paint().apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = 3f * s.scale
        }
        canvas.drawRoundRect(
            RectF(x - 2f * s.scale, y - 2f * s.scale, x + width + 2f * s.scale, y + height + 2f * s.scale),
            14f * s.scale, 14f * s.scale, borderPaint
        )
    }

    // ⭐ ОБРАБОТКА КАСАНИЙ
    fun handleTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        inventory: Inventory,
        player: Player,
        onEquip: (Int) -> Unit,
        onUnequip: (EquipmentSlot) -> Unit,
        onClose: () -> Unit,
        onDeleteItem: (Int) -> Unit,
        onUseItem: (Int) -> Unit,
        onRename: (String) -> Unit,
        onRefineItem: (Int) -> Unit,
        onShowMessage: (String) -> Unit
    ): Boolean {
        // Если размеры не вычислены — вычисляем
        if (sizes == null) calculateSizes(width, height)
        val s = getSizes()

        if (showRenameDialog) {
            return handleRenameDialogTouch(x, y, width, height, player, onRename, s)
        }

        // Кнопка закрытия
        val closeX = width - 80f * s.scale
        val closeY = 70f * s.scale
        val halfSize = s.closeBtnSize / 2 * 1.5f
        val clickPadding = 15f * s.scale

        if (x > closeX - halfSize - clickPadding && x < closeX + halfSize + clickPadding &&
            y > closeY - halfSize - clickPadding && y < closeY + halfSize + clickPadding) {
            if (showDeleteConfirm) {
                showDeleteConfirm = false
                deleteItemIndex = -1
            } else {
                onClose()
            }
            return true
        }

        if (showDeleteConfirm) {
            val dialogX = (width - s.dialogWidth) / 2
            val dialogY = (height - s.dialogHeight * 0.5f) / 2
            val btnY = dialogY + s.dialogHeight * 0.5f - s.btnHeight - 20f * s.scale

            if (x > dialogX + 50f * s.scale && x < dialogX + 50f * s.scale + s.btnWidth &&
                y > btnY && y < btnY + s.btnHeight) {
                onDeleteItem(deleteItemIndex)
                showDeleteConfirm = false
                deleteItemIndex = -1
                return true
            }

            if (x > dialogX + s.dialogWidth - s.btnWidth - 50f * s.scale && x < dialogX + s.dialogWidth - 50f * s.scale &&
                y > btnY && y < btnY + s.btnHeight) {
                showDeleteConfirm = false
                deleteItemIndex = -1
                return true
            }
            return true
        }

        // Кнопка редактирования имени
        if (x > editButtonRect.left && x < editButtonRect.right &&
            y > editButtonRect.top && y < editButtonRect.bottom) {
            if (player.gold >= 500) {
                showRenameDialog = true
                newNameInput = player.name
                renameError = ""
                isNameInputActive = true
            } else {
                onShowMessage("❌ Недостаточно золота! Нужно 500 💰")
            }
            return true
        }

        // ⭐ КНОПКА ЗАТОЧКИ
        if (inventory.selectedSlot >= 0) {
            val infoX = 25f * s.scale
            val infoY = height * 0.20f
            val infoW = s.infoWidth
            val infoH = s.infoHeight

            val refineBtnSize = 140f * s.scale
            val refineBtnX = infoX + infoW - 150f * s.scale
            val refineBtnY = infoY + infoH - 90f * s.scale

            val clickPadding = 20f * s.scale
            if (x > refineBtnX - clickPadding && x < refineBtnX + refineBtnSize + clickPadding &&
                y > refineBtnY - clickPadding && y < refineBtnY + refineBtnSize + clickPadding) {
                val items = inventory.getItems()
                if (inventory.selectedSlot < items.size) {
                    val item = items[inventory.selectedSlot]
                    if (item != null && item.isRefinable() && item.refineLevel < 10) {
                        onRefineItem(inventory.selectedSlot)
                        return true
                    }
                }
            }
        }

        // ⭐ КНОПКА "ИНЖЕКТ" (вставка руны)
        if (inventory.selectedSlot >= 0) {
            val infoX = 25f * s.scale
            val infoY = height * 0.20f
            val infoW = s.infoWidth
            val infoH = s.infoHeight

            val injectBtnSize = 140f * s.scale
            val injectBtnX = infoX + infoW - 320f * s.scale
            val injectBtnY = infoY + infoH - 90f * s.scale

            val clickPadding = 20f * s.scale
            if (x > injectBtnX - clickPadding && x < injectBtnX + injectBtnSize + clickPadding &&
                y > injectBtnY - clickPadding && y < injectBtnY + injectBtnSize + clickPadding) {
                val items = inventory.getItems()
                if (inventory.selectedSlot < items.size) {
                    val item = items[inventory.selectedSlot]
                    if (item != null && item.getMaxRuneSlots() > 0 && item.runes.size < item.getMaxRuneSlots()) {
                        isRuneInjectionMode = true
                        targetItemIndex = inventory.selectedSlot
                        onShowMessage("💎 Выберите руну для вставки в ${item.name}")
                        return true
                    } else {
                        onShowMessage("❌ В этот предмет нельзя вставить руну (нет свободных слотов)")
                    }
                }
            }
        }

        // Кнопка удаления в информации о предмете
        if (inventory.selectedSlot >= 0) {
            val infoX = 25f * s.scale
            val infoY = height * 0.45f
            val infoW = s.infoWidth
            val deleteBtnX = infoX + infoW - 60f * s.scale
            val deleteBtnY = infoY + 20f * s.scale
            val deleteSize = 80f * s.scale

            if (x > deleteBtnX - deleteSize / 2 - 6f * s.scale &&
                x < deleteBtnX + deleteSize / 2 + 6f * s.scale &&
                y > deleteBtnY - deleteSize / 2 - 6f * s.scale &&
                y < deleteBtnY + deleteSize / 2 + 6f * s.scale) {
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

        if (checkEquipmentSlotClick(x, y, width, height, inventory, onUnequip, s)) {
            return true
        }

        // Клик по ячейке инвентаря
        val startY = s.gridStartY
        val cols = s.gridCols
        val rows = s.gridRows
        val slotSize = s.gridSlotSize
        val padding = s.gridPadding
        val totalWidth = cols * (slotSize + padding) - padding
        val startX = (width - totalWidth) / 2

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val index = row * cols + col
                val cellX = startX + col * (slotSize + padding)
                val cellY = startY + row * (slotSize + padding)

                if (x > cellX && x < cellX + slotSize &&
                    y > cellY && y < cellY + slotSize) {

                    // ⭐ ЕСЛИ В РЕЖИМЕ ИНЖЕКТА
                    if (isRuneInjectionMode) {
                        val items = inventory.getItems()
                        if (index < items.size) {
                            val item = items[index]
                            if (item != null && item.id.startsWith("rune_")) {
                                // Пробуем вставить руну
                                val success = insertRuneIntoItem(inventory, targetItemIndex, index)
                                if (success) {
                                    onShowMessage("✅ Руна ${item.name} вставлена!")
                                    isRuneInjectionMode = false
                                    targetItemIndex = -1
                                    return true
                                } else {
                                    onShowMessage("❌ Нельзя вставить эту руну!")
                                    isRuneInjectionMode = false
                                    targetItemIndex = -1
                                    return true
                                }
                            }
                        }
                        // Если кликнули не по руне — выходим из режима
                        isRuneInjectionMode = false
                        targetItemIndex = -1
                        return true
                    }

                    // Обычный клик
                    val currentTime = System.currentTimeMillis()

                    if (currentTime - lastClickTime < DOUBLE_CLICK_DELAY && lastClickSlot == index) {
                        val items = inventory.getItems()
                        if (index < items.size) {
                            val item = items[index]
                            if (item != null) {
                                if (item.type == Item.ItemType.CONSUMABLE) {
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

    // ⭐ ОБРАБОТКА КАСАНИЙ В ДИАЛОГЕ ПЕРЕИМЕНОВАНИЯ
    private fun handleRenameDialogTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        player: Player,
        onRename: (String) -> Unit,
        s: ScreenSizes
    ): Boolean {
        val dialogX = (width - s.dialogWidth) / 2
        val dialogY = (height - s.dialogHeight) / 2

        val inputY = dialogY + 170f * s.scale
        val inputHeight = 50f * s.scale

        val keyboardStartY = inputY + inputHeight + 80f * s.scale
        val keyboardHeight = 3 * (s.keyboardKeyHeight + s.keyboardSpacing) + 30f * s.scale

        val btnY = keyboardStartY + keyboardHeight + 20f * s.scale

        if (x > dialogX + 50f * s.scale && x < dialogX + 50f * s.scale + s.btnWidth &&
            y > btnY && y < btnY + s.btnHeight) {
            if (newNameInput.isNotEmpty() && newNameInput.length >= 2 && player.gold >= 500) {
                onRename(newNameInput)
                showRenameDialog = false
                newNameInput = ""
                renameError = ""
                isNameInputActive = false
            } else if (player.gold < 500) {
                renameError = "⚠️ Недостаточно золота! Нужно 500 💰"
            } else {
                renameError = "⚠️ Минимум 2 символа!"
            }
            return true
        }

        if (x > dialogX + s.dialogWidth - s.btnWidth - 50f * s.scale && x < dialogX + s.dialogWidth - 50f * s.scale &&
            y > btnY && y < btnY + s.btnHeight) {
            showRenameDialog = false
            newNameInput = ""
            renameError = ""
            isNameInputActive = false
            return true
        }

        // ⭐ КЛАВИАТУРА
        val rows = listOf("ЙЦУКЕНГШЩЗХЪ", "ФЫВАПРОЛДЖЭ", "ЯЧСМИТЬБЮ")
        val keyWidth = s.keyboardKeyWidth
        val keyHeight = s.keyboardKeyHeight
        val keySpacing = s.keyboardSpacing

        for ((rowIndex, row) in rows.withIndex()) {
            val yPos = keyboardStartY + rowIndex * (keyHeight + keySpacing) + 15f * s.scale
            val rowWidth = row.length * (keyWidth + keySpacing) - keySpacing
            val rowOffsetX = (s.dialogWidth - rowWidth) / 2
            val xPos = dialogX + rowOffsetX

            for ((charIndex, char) in row.withIndex()) {
                val keyXPos = xPos + charIndex * (keyWidth + keySpacing)

                if (rowIndex == 0 && charIndex == row.length - 1) {
                    val backspaceRect = RectF(keyXPos, yPos, keyXPos + keyWidth, yPos + keyHeight)
                    if (x > backspaceRect.left && x < backspaceRect.right &&
                        y > backspaceRect.top && y < backspaceRect.bottom) {
                        if (newNameInput.isNotEmpty()) {
                            newNameInput = newNameInput.dropLast(1)
                            renameError = ""
                        }
                        return true
                    }
                    continue
                }

                if (rowIndex == 2 && charIndex == row.length - 1) {
                    val spaceWidth = keyWidth * 3 + keySpacing * 2
                    val spaceRect = RectF(keyXPos, yPos, keyXPos + spaceWidth, yPos + keyHeight)
                    if (x > spaceRect.left && x < spaceRect.right &&
                        y > spaceRect.top && y < spaceRect.bottom) {
                        if (newNameInput.length < 15) {
                            newNameInput += " "
                            renameError = ""
                        }
                        return true
                    }
                    break
                }

                val keyRect = RectF(keyXPos, yPos, keyXPos + keyWidth, yPos + keyHeight)
                if (x > keyRect.left && x < keyRect.right &&
                    y > keyRect.top && y < keyRect.bottom) {
                    if (newNameInput.length < 15) {
                        newNameInput += char
                        renameError = ""
                    }
                    return true
                }
            }
        }

        if (x < dialogX || x > dialogX + s.dialogWidth || y < dialogY || y > dialogY + s.dialogHeight) {
            showRenameDialog = false
            newNameInput = ""
            renameError = ""
            isNameInputActive = false
            return true
        }

        return true
    }

    // ⭐ ПРОВЕРКА КЛИКА ПО СЛОТУ ЭКИПИРОВКИ
    private fun checkEquipmentSlotClick(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        inventory: Inventory,
        onUnequip: (EquipmentSlot) -> Unit,
        s: ScreenSizes
    ): Boolean {
        val centerX = width / 2
        val centerY = height * 0.30f
        val radius = s.equipmentRadius
        val slotSize = s.equipmentSlotSize

        // ⭐ ДОБАВЛЯЕМ КОЛЬЦА
        val slots = listOf(
            EquipmentSlot.HELMET to -90f,
            EquipmentSlot.WEAPON to -150f,
            EquipmentSlot.SHIELD to -30f,
            EquipmentSlot.CHEST to 0f,
            EquipmentSlot.GLOVES to 240f,
            EquipmentSlot.BRACERS to 120f,
            EquipmentSlot.PANTS to 180f,
            EquipmentSlot.BOOTS to 60f,
            EquipmentSlot.NECKLACE to 300f,
            EquipmentSlot.RING1 to -210f,
            EquipmentSlot.RING2 to -270f
        )

        for ((slot, angleDeg) in slots) {
            val angleRad = Math.toRadians(angleDeg.toDouble())
            val slotX = centerX + (radius * cos(angleRad)).toFloat()
            val slotY = centerY + (radius * sin(angleRad)).toFloat()

            if (x > slotX - slotSize / 2 && x < slotX + slotSize / 2 &&
                y > slotY - slotSize / 2 && y < slotY + slotSize / 2) {
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

    // ⭐ ЗАГРУЗКА ИКОНОК
    private fun loadItemIcon(item: Item): Bitmap? {
        return when (item.id) {
            // Мечи
            "sword_rusty", "sword_iron", "sword_steel", "sword_flame",
            "sword_venom", "sword_arachnid", "sword_venomous", "sword_moonlight",
            "sword_legendary", "sword_mythic", "dagger_bone" -> loadIcon(item.id)
            // Топоры
            "axe_wooden", "axe_iron", "axe_battle" -> loadIcon(item.id)
            // Щит
            "crusader_shield" -> loadIcon("crusader_shield")
            "shield_1" -> loadIcon("shield_1")
            "shield_2" -> loadIcon("shield_2")
            "shield_3" -> loadIcon("shield_3")
            // Торты
            "cake_small", "cake_medium", "cake_large" -> loadIcon(item.id)
            // Шлемы
            "helmet_1", "helmet_2", "helmet_3", "helmet_4", "helmet_5" ->
                loadItemIconFromSprite("armor_items", item.id)
            // Броня
            "chest_1", "chest_2", "chest_3", "chest_4", "chest_5" ->
                loadItemIconFromSprite("armor_items", item.id)
            // Перчатки
            "gloves_1", "gloves_2", "gloves_3", "gloves_4", "gloves_5" ->
                loadItemIconFromSprite("armor_items", item.id)
            // Поножи
            "pants_1", "pants_2", "pants_3", "pants_4", "pants_5" ->
                loadItemIconFromSprite("armor_items", item.id)
            // Ботинки
            "boots_1", "boots_2", "boots_3", "boots_4", "boots_5" ->
                loadItemIconFromSprite("armor_items", item.id)
            // кольца
            "ring_1" -> loadIcon("ring_1")
            // ожерелья
            "necklace_1" -> loadIcon("necklace_1")
            "necklace_2" -> loadIcon("necklace_2")
            "necklace_3" -> loadIcon("necklace_3")

            // ⭐ МАТЕРИАЛЫ ДЛЯ ЗАТОЧКИ
            "oridecon" -> loadIcon("oridecon")
            "elunium" -> loadIcon("elunium")

            // ⭐ РУНЫ
            "rune_strength" -> loadIcon("rune_1")

            else -> null
        }
    }

    private fun loadIcon(name: String): Bitmap? {
        return try {
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun loadItemIconFromSprite(sheetName: String, frameName: String): Bitmap? {
        return try {
            val spriteSheet = spriteManager.getSpriteSheet(sheetName) ?: return null
            val frameRect = spriteManager.getFrame(sheetName, frameName) ?: return null
            Bitmap.createBitmap(spriteSheet, frameRect.left, frameRect.top, frameRect.width(), frameRect.height())
        } catch (e: Exception) {
            null
        }
    }

    private fun getSlotIcon(slot: EquipmentSlot): String = when (slot) {
        EquipmentSlot.WEAPON -> "⚔️"
        EquipmentSlot.SHIELD -> "🛡️"
        EquipmentSlot.HELMET -> "⛑️"
        EquipmentSlot.CHEST -> "👕"
        EquipmentSlot.PANTS -> "👖"
        EquipmentSlot.BOOTS -> "👢"
        EquipmentSlot.GLOVES -> "🧤"
        EquipmentSlot.BRACERS -> "💪"
        EquipmentSlot.NECKLACE -> "📿"
        EquipmentSlot.RING1, EquipmentSlot.RING2 -> "💍"
    }

    // ⭐ МЕТОД ВСТАВКИ РУНЫ В ПРЕДМЕТ
    private fun insertRuneIntoItem(inventory: Inventory, itemIndex: Int, runeIndex: Int): Boolean {
        val items = inventory.getItems()
        if (itemIndex >= items.size || runeIndex >= items.size) return false

        val targetItem = items[itemIndex] ?: return false
        val runeItem = items[runeIndex] ?: return false

        // Проверяем, что это руна
        if (!runeItem.id.startsWith("rune_")) return false

        // Проверяем, что предмет может принять руну
        if (!targetItem.canAddRune()) return false

        // Создаём руну из предмета
        val rune = Rune(
            id = runeItem.id,
            name = runeItem.name,
            description = runeItem.description,
            stats = runeItem.stats,
            rarity = runeItem.rarity
        )

        // Добавляем руну в предмет
        if (targetItem.addRune(rune)) {
            // Удаляем руну из инвентаря
            inventory.removeItem(runeIndex)
            return true
        }

        return false
    }
}