package com.example.myapplication.renderer

import android.graphics.*
import com.example.myapplication.manager.LocationManager

object GameRenderer {
    private var bgCity: Bitmap? = null
    private var bgForest: Bitmap? = null
    private var bgWasteland: Bitmap? = null
    private var bgRocks: Bitmap? = null
    private var bgCastle: Bitmap? = null
    private var bgDesert: Bitmap? = null

    fun loadBackgrounds(context: android.content.Context) {
        bgCity = loadBitmap(context, "bg_city")
        bgForest = loadBitmap(context, "bg_forest")
        bgWasteland = loadBitmap(context, "bg_wasteland")
        bgRocks = loadBitmap(context, "bg_rocks")
        bgCastle = loadBitmap(context, "bg_castle")
        bgDesert = loadBitmap(context, "bg_desert")
    }

    private fun loadBitmap(context: android.content.Context, name: String): Bitmap? {
        return try {
            val resId = context.resources.getIdentifier(name, "drawable", context.packageName)
            if (resId != 0) {
                android.graphics.BitmapFactory.decodeResource(context.resources, resId)
            } else {
                println("❌ Фон $name не найден!")
                null
            }
        } catch (e: Exception) {
            null
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
            LocationManager.Location.ROCKS -> bgRocks
            LocationManager.Location.CASTLE -> bgCastle
            LocationManager.Location.DESERT -> bgDesert
        }

        if (bitmap != null) {
            val dstRect = RectF(0f, 0f, width, height)
            canvas.drawBitmap(bitmap, null, dstRect, null)
        } else {
            drawFallbackColor(canvas, location, width, height)
        }
    }

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
            LocationManager.Location.ROCKS -> Color.rgb(100, 100, 120)
            LocationManager.Location.CASTLE -> Color.rgb(80, 80, 100)
            LocationManager.Location.DESERT -> Color.rgb(80, 80, 100)
        }
        canvas.drawColor(color)
    }
}