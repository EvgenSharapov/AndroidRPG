package com.example.myapplication.renderer

import android.graphics.*
import com.example.myapplication.manager.LocationManager

object GameRenderer {
    private var bgCity: Bitmap? = null
    private var bgForest: Bitmap? = null
    private var bgWasteland: Bitmap? = null

    // Загружаем фоновые картинки
    fun loadBackgrounds(context: android.content.Context) {
        try {
            val resIdCity = context.resources.getIdentifier("bg_city", "drawable", context.packageName)
            val resIdForest = context.resources.getIdentifier("bg_forest", "drawable", context.packageName)
            val resIdWasteland = context.resources.getIdentifier("bg_wasteland", "drawable", context.packageName)

            if (resIdCity != 0) {
                bgCity = android.graphics.BitmapFactory.decodeResource(context.resources, resIdCity)
                println("✅ bg_city загружен")
            } else {
                println("❌ bg_city.png НЕ НАЙДЕН в res/drawable/")
            }

            if (resIdForest != 0) {
                bgForest = android.graphics.BitmapFactory.decodeResource(context.resources, resIdForest)
                println("✅ bg_forest загружен")
            } else {
                println("❌ bg_forest.png НЕ НАЙДЕН в res/drawable/")
            }

            if (resIdWasteland != 0) {
                bgWasteland = android.graphics.BitmapFactory.decodeResource(context.resources, resIdWasteland)
                println("✅ bg_wasteland загружен")
            } else {
                println("❌ bg_wasteland.png НЕ НАЙДЕН в res/drawable/")
            }
        } catch (e: Exception) {
            println("❌ Ошибка загрузки фонов: ${e.message}")
        }
    }

    fun drawBackground(
        canvas: Canvas,
        location: LocationManager.Location,
        width: Float,
        height: Float
    ) {
        val bitmap = when (location) {
            LocationManager.Location.CITY -> bgCity
            LocationManager.Location.FOREST -> bgForest
            LocationManager.Location.WASTELAND -> bgWasteland
        }

        if (bitmap != null) {
            // Рисуем картинку на весь экран (растягиваем)
            val dstRect = RectF(0f, 0f, width, height)
            canvas.drawBitmap(bitmap, null, dstRect, null)
        } else {
            // Если картинка не загружена — используем цветной фон
            drawFallbackColor(canvas, location, width, height)
        }
    }

    // Запасной вариант, если картинок нет
    private fun drawFallbackColor(
        canvas: Canvas,
        location: LocationManager.Location,
        width: Float,
        height: Float
    ) {
        val color = when (location) {
            LocationManager.Location.CITY -> Color.rgb(180, 160, 120)
            LocationManager.Location.FOREST -> Color.rgb(40, 100, 40)
            LocationManager.Location.WASTELAND -> Color.rgb(140, 120, 80)
        }
        canvas.drawColor(color)
    }
}