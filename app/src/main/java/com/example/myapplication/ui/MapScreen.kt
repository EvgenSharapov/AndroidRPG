package com.example.myapplication.ui

import android.graphics.*
import com.example.myapplication.manager.LocationManager

class MapScreen {

    data class LocationInfo(
        val location: LocationManager.Location,
        val name: String,
        val emoji: String,
        val color: Int,
        val iconEmoji: String
    )

    private val locations = listOf(
        LocationInfo(
            LocationManager.Location.CITY,
            "Город",
            "🏙️",
            Color.rgb(180, 160, 120),
            "🏘️"
        ),
        LocationInfo(
            LocationManager.Location.FOREST,
            "Лес",
            "🌲",
            Color.rgb(40, 120, 40),
            "🌳"
        ),
        LocationInfo(
            LocationManager.Location.WASTELAND,
            "Пустошь",
            "🏜️",
            Color.rgb(180, 150, 80),
            "🏜️"
        ),
        LocationInfo(
            LocationManager.Location.ROCKS,
            "Скалы",
            "⛰️",
            Color.rgb(130, 130, 150),
            "🗻"
        ),
        LocationInfo(
            LocationManager.Location.CASTLE,
            "Замок",
            "🏰",
            Color.rgb(100, 100, 130),
            "🏰"
        ),
        LocationInfo(
            LocationManager.Location.DESERT,
            "Пустыня",
            "🏜️",
            Color.rgb(200, 180, 100),
            "🐪"
        )
    )

    fun draw(
        canvas: Canvas,
        width: Float,
        height: Float,
        locationManager: LocationManager,
        onTeleport: (LocationManager.Location) -> Unit
    ) {
        // Фон
        val bgPaint = Paint().apply {
            color = Color.argb(240, 15, 10, 30)
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Заголовок
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🗺️ КАРТА МИРА", width / 2, 70f, titlePaint)

        // Текущая локация
        val currentLoc = locationManager.currentLocation
        val currentName = locations.find { it.location == currentLoc }?.name ?: "Неизвестно"
        val currentPaint = Paint().apply {
            color = Color.argb(150, 255, 215, 0)
            textSize = 22f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📍 Текущая: $currentName", width / 2, 110f, currentPaint)

        // Рисуем локации в виде сетки
        val cols = 2
        val rows = 3
        val spacing = 20f
        val totalWidth = width - 80f
        val cardWidth = (totalWidth - spacing * (cols - 1)) / cols
        val cardHeight = 180f
        val startX = 40f
        val startY = 140f
        val cardSpacingX = spacing
        val cardSpacingY = 25f

        for ((index, location) in locations.withIndex()) {
            val row = index / cols
            val col = index % cols
            val x = startX + col * (cardWidth + cardSpacingX)
            val y = startY + row * (cardHeight + cardSpacingY)

            val isCurrent = location.location == currentLoc

            drawLocationCard(
                canvas,
                x, y,
                cardWidth, cardHeight,
                location,
                isCurrent,
                onTeleport
            )
        }
    }

    private fun drawLocationCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        location: LocationInfo,
        isCurrent: Boolean,
        onTeleport: (LocationManager.Location) -> Unit
    ) {
        // Тень
        val shadowPaint = Paint().apply {
            color = Color.argb(40, 0, 0, 0)
        }
        canvas.drawRoundRect(
            RectF(x + 4f, y + 4f, x + width + 4f, y + height + 4f),
            20f, 20f, shadowPaint
        )

        // Основной фон
        val bgPaint = Paint().apply {
            color = if (isCurrent) Color.argb(200, 255, 215, 0) else Color.argb(180, 30, 25, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(x, y, x + width, y + height),
            20f, 20f, bgPaint
        )

        // Рамка
        val borderPaint = Paint().apply {
            color = if (isCurrent) Color.rgb(255, 215, 0) else Color.argb(100, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = if (isCurrent) 4f else 2f
        }
        canvas.drawRoundRect(
            RectF(x, y, x + width, y + height),
            20f, 20f, borderPaint
        )

        // Свечение для текущей локации
        if (isCurrent) {
            val glowPaint = Paint().apply {
                shader = RadialGradient(
                    x + width / 2, y + height / 2, width * 0.8f,
                    Color.argb(40, 255, 215, 0),
                    Color.TRANSPARENT,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(x + width / 2, y + height / 2, width * 0.8f, glowPaint)
        }

        // Фон-превью локации (имитация)
        drawLocationPreview(canvas, x, y, width, height, location)

        // Эмодзи локации (крупно)
        val iconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(location.emoji, x + width / 2, y + 85f, iconPaint)

        // Название
        val namePaint = Paint().apply {
            color = if (isCurrent) Color.rgb(255, 215, 0) else Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(location.name, x + width / 2, y + 125f, namePaint)

        // Индикатор "ТЕКУЩАЯ"
        if (isCurrent) {
            val currentTagPaint = Paint().apply {
                color = Color.rgb(255, 215, 0)
                textSize = 16f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("📍 ТЕКУЩАЯ", x + width / 2, y + 160f, currentTagPaint)
        }

        // Кнопка "Перейти"
        if (!isCurrent) {
            val btnPaint = Paint().apply {
                color = Color.rgb(50, 150, 200)
                style = Paint.Style.FILL
            }
            val btnX = x + width / 2 - 50f
            val btnY = y + height - 35f
            val btnW = 100f
            val btnH = 28f
            canvas.drawRoundRect(
                RectF(btnX, btnY, btnX + btnW, btnY + btnH),
                10f, 10f, btnPaint
            )

            val btnTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 16f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("Перейти ➜", x + width / 2, btnY + 20f, btnTextPaint)
        }
    }

    private fun drawLocationPreview(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        location: LocationInfo
    ) {
        // Верхняя часть карточки — цвет фона локации
        val previewPaint = Paint().apply {
            color = location.color
            style = Paint.Style.FILL
        }
        val previewHeight = height * 0.45f
        canvas.drawRoundRect(
            RectF(x + 4f, y + 4f, x + width - 4f, y + previewHeight),
            16f, 16f, previewPaint
        )

        // Декоративные элементы на превью
        val decorPaint = Paint().apply {
            color = Color.argb(60, 255, 255, 255)
            style = Paint.Style.FILL
        }

        // Несколько кружков для имитации ландшафта
        when (location.location) {
            LocationManager.Location.CITY -> {
                // Домики
                canvas.drawRect(x + 20f, y + 20f, x + 40f, y + 45f, decorPaint)
                canvas.drawRect(x + 50f, y + 25f, x + 70f, y + 45f, decorPaint)
                canvas.drawRect(x + 80f, y + 15f, x + 100f, y + 45f, decorPaint)
            }
            LocationManager.Location.FOREST -> {
                // Ёлочки
                drawTree(canvas, x + 30f, y + 35f, 25f, decorPaint)
                drawTree(canvas, x + 65f, y + 40f, 20f, decorPaint)
                drawTree(canvas, x + 95f, y + 30f, 30f, decorPaint)
            }
            LocationManager.Location.WASTELAND -> {
                // Холмы
                canvas.drawCircle(x + 30f, y + 45f, 25f, decorPaint)
                canvas.drawCircle(x + 70f, y + 40f, 30f, decorPaint)
                canvas.drawCircle(x + 100f, y + 45f, 20f, decorPaint)
            }
            LocationManager.Location.ROCKS -> {
                // Камни
                canvas.drawCircle(x + 25f, y + 35f, 18f, decorPaint)
                canvas.drawCircle(x + 55f, y + 30f, 22f, decorPaint)
                canvas.drawCircle(x + 85f, y + 40f, 15f, decorPaint)
                canvas.drawCircle(x + 105f, y + 35f, 20f, decorPaint)
            }
            LocationManager.Location.CASTLE -> {
                // Замок
                canvas.drawRect(x + 25f, y + 20f, x + 45f, y + 45f, decorPaint)
                canvas.drawRect(x + 45f, y + 10f, x + 65f, y + 45f, decorPaint)
                canvas.drawRect(x + 65f, y + 20f, x + 85f, y + 45f, decorPaint)
            }
            LocationManager.Location.DESERT -> {
                // Дюны
                canvas.drawCircle(x + 35f, y + 45f, 28f, decorPaint)
                canvas.drawCircle(x + 75f, y + 42f, 25f, decorPaint)
                canvas.drawCircle(x + 105f, y + 45f, 22f, decorPaint)
            }
        }
    }

    private fun drawTree(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        // Ёлочка из трёх треугольников
        val path = Path()
        path.moveTo(x, y - size)
        path.lineTo(x - size * 0.6f, y - size * 0.3f)
        path.lineTo(x - size * 0.3f, y - size * 0.3f)
        path.lineTo(x - size * 0.5f, y + size * 0.2f)
        path.lineTo(x - size * 0.2f, y + size * 0.2f)
        path.lineTo(x, y + size * 0.5f)
        path.lineTo(x + size * 0.2f, y + size * 0.2f)
        path.lineTo(x + size * 0.5f, y + size * 0.2f)
        path.lineTo(x + size * 0.3f, y - size * 0.3f)
        path.lineTo(x + size * 0.6f, y - size * 0.3f)
        path.close()
        canvas.drawPath(path, paint)
    }

    fun handleTouch(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        locationManager: LocationManager,
        onTeleport: (LocationManager.Location) -> Unit
    ): Boolean {
        val cols = 2
        val rows = 3
        val spacing = 20f
        val totalWidth = width - 80f
        val cardWidth = (totalWidth - spacing * (cols - 1)) / cols
        val cardHeight = 180f
        val startX = 40f
        val startY = 140f
        val cardSpacingX = spacing
        val cardSpacingY = 25f

        for ((index, location) in locations.withIndex()) {
            val row = index / cols
            val col = index % cols
            val cardX = startX + col * (cardWidth + cardSpacingX)
            val cardY = startY + row * (cardHeight + cardSpacingY)

            val isCurrent = location.location == locationManager.currentLocation

            // Проверяем клик по карточке
            if (x >= cardX && x <= cardX + cardWidth &&
                y >= cardY && y <= cardY + cardHeight) {

                // Если это не текущая локация — телепортируем
                if (!isCurrent) {
                    onTeleport(location.location)
                }
                return true
            }
        }

        return false
    }
}
