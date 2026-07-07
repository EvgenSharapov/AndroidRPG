package com.example.myapplication.ui

import android.graphics.*
import com.example.myapplication.model.*

class ShopScreen {

    enum class ShopTab {
        CONSUMABLES,
        CHARACTER
    }

    private var currentTab = ShopTab.CONSUMABLES
    private var selectedItemIndex = -1
    private var showBuyConfirm = false
    private var buyingItem: ShopItem? = null

    data class ShopItem(
        val id: String,
        val name: String,
        val price: Int,
        val icon: String,
        val type: Item.ItemType,
        val rarity: ItemRarity = ItemRarity.COMMON
    )

    private val consumables = listOf(
        ShopItem(
            id = "cake_small",
            name = "Маленький торт",
            price = 50,
            icon = "🍰",
            type = Item.ItemType.CONSUMABLE,
            rarity = ItemRarity.COMMON
        ),
        ShopItem(
            id = "cake_medium",
            name = "Средний торт",
            price = 100,
            icon = "🎂",
            type = Item.ItemType.CONSUMABLE,
            rarity = ItemRarity.UNCOMMON
        ),
        ShopItem(
            id = "cake_large",
            name = "Большой торт",
            price = 200,
            icon = "🍰",
            type = Item.ItemType.CONSUMABLE,
            rarity = ItemRarity.RARE
        )
    )

    private val characterItems = listOf(
        ShopItem(
            id = "rename",
            name = "Смена имени",
            price = 500,
            icon = "✏️",
            type = Item.ItemType.CONSUMABLE,
            rarity = ItemRarity.UNCOMMON
        ),
        ShopItem(
            id = "reset_stats",
            name = "Сброс характеристик",
            price = 1000,
            icon = "🔄",
            type = Item.ItemType.CONSUMABLE,
            rarity = ItemRarity.RARE
        )
    )

    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        onClose: () -> Unit,
        onBuyItem: (ShopItem) -> Unit,
        onRename: () -> Unit,
        onResetStats: () -> Unit
    ) {
        val dimPaint = Paint().apply {
            color = Color.argb(220, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width, height, dimPaint)

        val shopPaint = Paint().apply {
            color = Color.argb(240, 30, 25, 40)
        }
        val margin = 40f
        val shopX = margin
        val shopY = margin
        val shopWidth = width - margin * 2
        val shopHeight = height - margin * 2

        canvas.drawRoundRect(
            RectF(shopX, shopY, shopX + shopWidth, shopY + shopHeight),
            25f, 25f, shopPaint
        )

        val borderPaint = Paint().apply {
            color = Color.argb(150, 255, 215, 0)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(
            RectF(shopX, shopY, shopX + shopWidth, shopY + shopHeight),
            25f, 25f, borderPaint
        )

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🏪 МАГАЗИН", width / 2, shopY + 55f, titlePaint)

        val goldPaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            textSize = 28f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("💰 ${player.gold}", shopX + 30f, shopY + 50f, goldPaint)

        drawCloseButton(canvas, shopX, shopY, shopWidth)
        drawTabs(canvas, shopX, shopY, shopWidth)

        val tabStartY = shopY + 130f
        val items = when (currentTab) {
            ShopTab.CONSUMABLES -> consumables
            ShopTab.CHARACTER -> characterItems
        }
        drawItems(canvas, shopX, tabStartY, shopWidth, shopHeight - 150f, items, player)

        if (showBuyConfirm && buyingItem != null) {
            drawBuyConfirmDialog(canvas, width, height, player, onBuyItem, onRename, onResetStats)
        }
    }

    private fun drawCloseButton(canvas: Canvas, shopX: Float, shopY: Float, shopWidth: Float) {
        val closeX = shopX + shopWidth - 35f
        val closeY = shopY + 35f
        val closeSize = 30f

        val closeBgPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
        }
        canvas.drawCircle(closeX, closeY, closeSize, closeBgPaint)

        val closeIconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("✕", closeX, closeY + 10f, closeIconPaint)
    }

    private fun drawTabs(canvas: Canvas, shopX: Float, shopY: Float, shopWidth: Float) {
        val tabWidth = 200f
        val tabHeight = 50f
        val tabY = shopY + 75f
        val tabSpacing = 10f
        val startX = (shopWidth - tabWidth * 2 - tabSpacing) / 2 + shopX

        val tabs = listOf(
            ShopTab.CONSUMABLES to "🧪 Расходники",
            ShopTab.CHARACTER to "👤 Персонаж"
        )

        for ((index, pair) in tabs.withIndex()) {
            val tab = pair.first
            val label = pair.second
            val x = startX + index * (tabWidth + tabSpacing)
            val isActive = currentTab == tab

            val tabPaint = Paint().apply {
                color = if (isActive) Color.rgb(200, 150, 50) else Color.argb(100, 80, 80, 100)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(
                RectF(x, tabY, x + tabWidth, tabY + tabHeight),
                12f, 12f, tabPaint
            )

            if (isActive) {
                val activeBorder = Paint().apply {
                    color = Color.argb(150, 255, 215, 0)
                    style = Paint.Style.STROKE
                    strokeWidth = 2f
                }
                canvas.drawRoundRect(
                    RectF(x, tabY, x + tabWidth, tabY + tabHeight),
                    12f, 12f, activeBorder
                )
            }

            val textPaint = Paint().apply {
                color = if (isActive) Color.WHITE else Color.argb(150, 200, 200, 200)
                textSize = 22f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(label, x + tabWidth / 2, tabY + tabHeight / 2 + 8f, textPaint)
        }
    }

    private fun drawItems(
        canvas: Canvas,
        shopX: Float,
        startY: Float,
        shopWidth: Float,
        maxHeight: Float,
        items: List<ShopItem>,
        player: Player
    ) {
        val itemHeight = 80f
        val itemSpacing = 10f
        val padding = 20f
        val maxItems = (maxHeight / (itemHeight + itemSpacing)).toInt()
        val displayItems = items.take(maxItems)

        for ((index, item) in displayItems.withIndex()) {
            val y = startY + index * (itemHeight + itemSpacing)
            val x = shopX + padding
            val width = shopWidth - padding * 2

            val itemPaint = Paint().apply {
                color = if (selectedItemIndex == index) {
                    Color.argb(150, 255, 215, 0)
                } else {
                    Color.argb(80, 50, 50, 80)
                }
            }
            canvas.drawRoundRect(
                RectF(x, y, x + width, y + itemHeight),
                12f, 12f, itemPaint
            )

            val rarityColor = when (item.rarity) {
                ItemRarity.COMMON -> Color.rgb(200, 200, 200)
                ItemRarity.UNCOMMON -> Color.rgb(50, 200, 50)
                ItemRarity.RARE -> Color.rgb(50, 150, 255)
                ItemRarity.EPIC -> Color.rgb(200, 100, 255)
                ItemRarity.LEGENDARY -> Color.rgb(255, 150, 50)
                ItemRarity.MYTHIC -> Color.rgb(255, 215, 0)
            }
            val rarityPaint = Paint().apply {
                color = rarityColor
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(
                RectF(x, y, x + width, y + itemHeight),
                12f, 12f, rarityPaint
            )

            // Иконка
            val iconPaint = Paint().apply {
                color = Color.WHITE
                textSize = 40f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(item.icon, x + 45f, y + itemHeight / 2 + 14f, iconPaint)

            // Название
            val namePaint = Paint().apply {
                color = Color.WHITE
                textSize = 24f
                textAlign = Paint.Align.LEFT
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(item.name, x + 75f, y + 32f, namePaint)


            // Цена
            val canAfford = player.gold >= item.price
            val pricePaint = Paint().apply {
                color = if (canAfford) Color.rgb(255, 215, 0) else Color.RED
                textSize = 22f
                textAlign = Paint.Align.RIGHT
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(
                "💰 ${item.price}",
                x + width - 320f,
                y + itemHeight / 2 + 8f,
                pricePaint
            )

            if (canAfford) {
                val buyBtnPaint = Paint().apply {
                    color = Color.rgb(50, 200, 50)
                    style = Paint.Style.FILL
                }
                val buyBtnX = x + width - 160f
                val buyBtnY = y + 15f
                val buyBtnW = 120f
                val buyBtnH = 50f
                canvas.drawRoundRect(
                    RectF(buyBtnX, buyBtnY, buyBtnX + buyBtnW, buyBtnY + buyBtnH),
                    10f, 10f, buyBtnPaint
                )

                val buyTextPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 20f
                    textAlign = Paint.Align.CENTER
                    typeface = Typeface.DEFAULT_BOLD
                }
                canvas.drawText(
                    "Купить",
                    buyBtnX + buyBtnW / 2,
                    buyBtnY + buyBtnH / 2 + 7f,
                    buyTextPaint
                )
            }
        }
    }

    private fun drawBuyConfirmDialog(
        canvas: Canvas,
        width: Float,
        height: Float,
        player: Player,
        onBuyItem: (ShopItem) -> Unit,
        onRename: () -> Unit,
        onResetStats: () -> Unit
    ) {
        val item = buyingItem ?: return

        val dimPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, width, height, dimPaint)

        val dialogPaint = Paint().apply {
            color = Color.argb(240, 30, 20, 40)
        }
        val dialogWidth = 500f
        val dialogHeight = 300f
        val dialogX = (width - dialogWidth) / 2
        val dialogY = (height - dialogHeight) / 2

        canvas.drawRoundRect(
            RectF(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight),
            25f, 25f, dialogPaint
        )

        val borderPaint = Paint().apply {
            color = Color.argb(100, 255, 215, 0)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(
            RectF(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight),
            25f, 25f, borderPaint
        )

        val iconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(item.icon, width / 2, dialogY + 80f, iconPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("Купить ${item.name}?", width / 2, dialogY + 140f, textPaint)

        val pricePaint = Paint().apply {
            color = Color.rgb(255, 215, 0)
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("💰 ${item.price}", width / 2, dialogY + 175f, pricePaint)

        val btnWidth = 150f
        val btnHeight = 50f
        val btnY = dialogY + dialogHeight - 70f

        val yesPaint = Paint().apply {
            color = Color.rgb(50, 200, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + 50f, btnY, dialogX + 50f + btnWidth, btnY + btnHeight),
            12f, 12f, yesPaint
        )
        val yesText = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("✅ Да", dialogX + 50f + btnWidth / 2, btnY + btnHeight / 2 + 8f, yesText)

        val noPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(
                dialogX + dialogWidth - btnWidth - 50f,
                btnY,
                dialogX + dialogWidth - 50f,
                btnY + btnHeight
            ),
            12f, 12f, noPaint
        )
        val noText = Paint().apply {
            color = Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(
            "❌ Нет",
            dialogX + dialogWidth - btnWidth / 2 - 50f,
            btnY + btnHeight / 2 + 8f,
            noText
        )
    }

    fun handleTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        player: Player,
        onClose: () -> Unit,
        onBuyItem: (ShopItem) -> Unit,
        onRename: () -> Unit,
        onResetStats: () -> Unit
    ): Boolean {
        val margin = 40f
        val shopX = margin
        val shopY = margin
        val shopWidth = width - margin * 2
        val shopHeight = height - margin * 2

        // Подтверждение покупки
        if (showBuyConfirm && buyingItem != null) {
            val dialogWidth = 500f
            val dialogHeight = 300f
            val dialogX = (width - dialogWidth) / 2
            val dialogY = (height - dialogHeight) / 2
            val btnWidth = 150f
            val btnHeight = 50f
            val btnY = dialogY + dialogHeight - 70f

            if (x > dialogX + 50f && x < dialogX + 50f + btnWidth &&
                y > btnY && y < btnY + btnHeight
            ) {
                val item = buyingItem!!
                when (item.id) {
                    "rename" -> {
                        showBuyConfirm = false
                        buyingItem = null
                        onRename()
                    }

                    "reset_stats" -> {
                        showBuyConfirm = false
                        buyingItem = null
                        onResetStats()
                    }

                    else -> {
                        onBuyItem(item)
                        showBuyConfirm = false
                        buyingItem = null
                    }
                }
                return true
            }

            if (x > dialogX + dialogWidth - btnWidth - 50f && x < dialogX + dialogWidth - 50f &&
                y > btnY && y < btnY + btnHeight
            ) {
                showBuyConfirm = false
                buyingItem = null
                return true
            }

            if (x < dialogX || x > dialogX + dialogWidth || y < dialogY || y > dialogY + dialogHeight) {
                showBuyConfirm = false
                buyingItem = null
                return true
            }
            return true
        }

        // Кнопка закрытия
        val closeX = shopX + shopWidth - 35f
        val closeY = shopY + 35f
        val closeSize = 30f
        if (x > closeX - closeSize && x < closeX + closeSize &&
            y > closeY - closeSize && y < closeY + closeSize
        ) {
            onClose()
            return true
        }

        // Вкладки
        val tabWidth = 200f
        val tabHeight = 50f
        val tabY = shopY + 75f
        val tabSpacing = 10f
        val startX = (shopWidth - tabWidth * 2 - tabSpacing) / 2 + shopX

        if (x > startX && x < startX + tabWidth &&
            y > tabY && y < tabY + tabHeight
        ) {
            currentTab = ShopTab.CONSUMABLES
            selectedItemIndex = -1
            return true
        }

        if (x > startX + tabWidth + tabSpacing && x < startX + tabWidth + tabSpacing + tabWidth &&
            y > tabY && y < tabY + tabHeight
        ) {
            currentTab = ShopTab.CHARACTER
            selectedItemIndex = -1
            return true
        }

        // Товары
        val items = when (currentTab) {
            ShopTab.CONSUMABLES -> consumables
            ShopTab.CHARACTER -> characterItems
        }

        val itemHeight = 80f
        val itemSpacing = 10f
        val padding = 20f
        val startItemY = shopY + 130f
        val maxHeight = shopHeight - 150f
        val maxItems = (maxHeight / (itemHeight + itemSpacing)).toInt()
        val displayItems = items.take(maxItems)

        for ((index, item) in displayItems.withIndex()) {
            val yPos = startItemY + index * (itemHeight + itemSpacing)
            val xPos = shopX + padding
            val widthItem = shopWidth - padding * 2

            // ⭐ 1. СНАЧАЛА КНОПКА "КУПИТЬ"
            if (player.gold >= item.price) {
                val buyBtnX = xPos + widthItem - 160f
                val buyBtnY = yPos + 15f
                val buyBtnW = 120f
                val buyBtnH = 50f

                // Проверяем клик по кнопке
                if (x >= buyBtnX && x <= buyBtnX + buyBtnW &&
                    y >= buyBtnY && y <= buyBtnY + buyBtnH) {

                    println("🛒 КУПИТЬ: ${item.name} за ${item.price}💰")

                    if (currentTab == ShopTab.CONSUMABLES) {
                        onBuyItem(item)
                    } else {
                        buyingItem = item
                        showBuyConfirm = true
                    }
                    return true
                }
            }

            // ⭐ 2. ПОТОМ КЛИК ПО СТРОКЕ ТОВАРА (ВЫДЕЛЕНИЕ)
            // Добавляем проверку, что клик НЕ по кнопке "Купить"
            val isNotBuyButton = if (player.gold >= item.price) {
                val buyBtnX = xPos + widthItem - 160f
                val buyBtnY = yPos + 15f
                val buyBtnW = 120f
                val buyBtnH = 50f
                !(x >= buyBtnX && x <= buyBtnX + buyBtnW && y >= buyBtnY && y <= buyBtnY + buyBtnH)
            } else {
                true
            }

            if (isNotBuyButton && x > xPos && x < xPos + widthItem &&
                y > yPos && y < yPos + itemHeight) {
                selectedItemIndex = if (selectedItemIndex == index) -1 else index
                println("🖱️ Выделен товар: ${item.name}, index=$index")
                return true
            }
        }

        // Клик вне магазина
        if (x < shopX || x > shopX + shopWidth || y < shopY || y > shopY + shopHeight) {
            onClose()
            return true
        }

        return true
    }
}
