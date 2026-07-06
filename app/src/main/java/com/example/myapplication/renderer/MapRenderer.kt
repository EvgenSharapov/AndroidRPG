package com.example.myapplication.renderer

import android.graphics.*
import com.example.myapplication.manager.LocationManager

object MapRenderer {
    private val paint = Paint()
    private val bgPaint = Paint().apply {
        color = Color.argb(220, 0, 0, 30)
    }

    fun drawMap(
        canvas: Canvas,
        screenWidth: Float,
        screenHeight: Float,
        locationManager: LocationManager
    ) {
        // --- ФОН ---
        canvas.drawPaint(bgPaint)

        // --- ЗАГОЛОВОК ---
        paint.apply {
            color = Color.WHITE
            textAlign = Paint.Align.CENTER
            textSize = 30f
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🗺️ КАРТА МИРА", screenWidth / 2, 80f, paint)

        // --- ПАРАМЕТРЫ КАРТЫ ---
        val mapScale = 0.8f
        val centerX = screenWidth / 2
        val centerY = screenHeight / 2 + 50f

        // ===== РИСУЕМ ЛОКАЦИИ =====

        // 1. Город (центр)
        val cityX = centerX
        val cityY = centerY
        paint.color = Color.rgb(180, 160, 120)
        canvas.drawCircle(cityX, cityY, 100f * mapScale, paint)
        paint.color = Color.WHITE
        paint.textSize = 22f
        canvas.drawText("🏙️ Город", cityX, cityY + 10f, paint)

        // 2. Лес (слева)
        val forestX = centerX - 250f * mapScale
        val forestY = centerY - 100f * mapScale
        paint.color = Color.rgb(40, 100, 40)
        canvas.drawCircle(forestX, forestY, 70f * mapScale, paint)
        paint.color = Color.WHITE
        paint.textSize = 18f
        canvas.drawText("🌲 Лес", forestX, forestY + 10f, paint)

        // 3. Пустошь (справа)
        val wasteX = centerX + 250f * mapScale
        val wasteY = centerY - 100f * mapScale
        paint.color = Color.rgb(140, 120, 80)
        canvas.drawCircle(wasteX, wasteY, 70f * mapScale, paint)
        paint.color = Color.WHITE
        paint.textSize = 18f
        canvas.drawText("🏜️ Пустошь", wasteX, wasteY + 10f, paint)

        // 4. ⭐ СКАЛЫ (слева от Леса)
        val rocksX = centerX - 400f * mapScale
        val rocksY = centerY - 200f * mapScale
        paint.color = Color.rgb(100, 100, 120)
        canvas.drawCircle(rocksX, rocksY, 70f * mapScale, paint)
        paint.color = Color.WHITE
        paint.textSize = 18f
        canvas.drawText("⛰️ Скалы", rocksX, rocksY + 10f, paint)

        // 5. ⭐ ЗАМОК (дальше слева от Скал)
        val castleX = centerX - 550f * mapScale
        val castleY = centerY - 300f * mapScale
        paint.color = Color.rgb(80, 80, 100)
        canvas.drawCircle(castleX, castleY, 70f * mapScale, paint)
        paint.color = Color.WHITE
        paint.textSize = 18f
        canvas.drawText("🏰 Замок", castleX, castleY + 10f, paint)

        // ⭐ 6. ПУСТЫНЯ (справа от Пустоши, чуть ниже)
        val desertX = centerX + 400f * mapScale
        val desertY = centerY - 30f * mapScale  // ← чуть ниже пустоши
        paint.color = Color.rgb(200, 180, 100)
        canvas.drawCircle(desertX, desertY, 70f * mapScale, paint)
        paint.color = Color.WHITE
        paint.textSize = 18f
        canvas.drawText("🏜️ Пустыня", desertX, desertY + 10f, paint)

        // ===== ПОДСВЕТКА ТЕКУЩЕЙ ЛОКАЦИИ =====
        val currentPos = when (locationManager.currentLocation) {
            LocationManager.Location.CITY -> cityX to cityY
            LocationManager.Location.FOREST -> forestX to forestY
            LocationManager.Location.WASTELAND -> wasteX to wasteY
            LocationManager.Location.ROCKS -> rocksX to rocksY
            LocationManager.Location.CASTLE -> castleX to castleY
            LocationManager.Location.DESERT -> desertX to desertY  // ← ДОБАВИЛИ
        }
        val markerPaint = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        canvas.drawCircle(currentPos.first, currentPos.second, 85f * mapScale, markerPaint)

        // ===== КНОПКИ ТЕЛЕПОРТАЦИИ =====
        val btnPaint = Paint().apply {
            color = Color.rgb(50, 150, 200)
            style = Paint.Style.FILL
        }
        val btnTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }

        // Кнопка "Город"
        val cityBtnX = centerX - 100f
        val cityBtnY = centerY + 150f
        canvas.drawRoundRect(
            RectF(cityBtnX, cityBtnY, cityBtnX + 200f, cityBtnY + 50f),
            10f, 10f, btnPaint
        )
        canvas.drawText("🏙️ Город", cityBtnX + 100f, cityBtnY + 35f, btnTextPaint)

        // Кнопка "Лес"
        val forestBtnX = centerX - 250f * mapScale - 100f
        val forestBtnY = centerY + 150f
        canvas.drawRoundRect(
            RectF(forestBtnX, forestBtnY, forestBtnX + 200f, forestBtnY + 50f),
            10f, 10f, btnPaint
        )
        canvas.drawText("🌲 Лес", forestBtnX + 100f, forestBtnY + 35f, btnTextPaint)

        // Кнопка "Пустошь"
        val wasteBtnX = centerX + 250f * mapScale - 100f
        val wasteBtnY = centerY + 150f
        canvas.drawRoundRect(
            RectF(wasteBtnX, wasteBtnY, wasteBtnX + 200f, wasteBtnY + 50f),
            10f, 10f, btnPaint
        )
        canvas.drawText("🏜️ Пустошь", wasteBtnX + 100f, wasteBtnY + 35f, btnTextPaint)

        // ⭐ Кнопка "Скалы"
        val rocksBtnX = centerX - 400f * mapScale - 100f
        val rocksBtnY = centerY + 150f
        canvas.drawRoundRect(
            RectF(rocksBtnX, rocksBtnY, rocksBtnX + 200f, rocksBtnY + 50f),
            10f, 10f, btnPaint
        )
        canvas.drawText("⛰️ Скалы", rocksBtnX + 100f, rocksBtnY + 35f, btnTextPaint)

        // ⭐ Кнопка "Замок"
        val castleBtnX = centerX - 550f * mapScale - 100f
        val castleBtnY = centerY + 150f
        canvas.drawRoundRect(
            RectF(castleBtnX, castleBtnY, castleBtnX + 200f, castleBtnY + 50f),
            10f, 10f, btnPaint
        )
        canvas.drawText("🏰 Замок", castleBtnX + 100f, castleBtnY + 35f, btnTextPaint)

        // ⭐ Кнопка "Пустыня" (справа от Пустоши, чуть ниже)
        val desertBtnX = centerX + 400f * mapScale - 100f
        val desertBtnY = centerY + 150f + 70f  // ← ниже пустоши
        canvas.drawRoundRect(
            RectF(desertBtnX, desertBtnY, desertBtnX + 200f, desertBtnY + 50f),
            10f, 10f, btnPaint
        )
        canvas.drawText("🏜️ Пустыня", desertBtnX + 100f, desertBtnY + 35f, btnTextPaint)

        // ===== ПОДПИСЬ =====
        val hintPaint = Paint().apply {
            color = Color.GRAY
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Нажмите на кнопку локации для телепортации",
            screenWidth / 2,
            screenHeight - 30f,
            hintPaint
        )

        // ===== ДЕКОРАТИВНЫЕ ТОЧКИ (звёзды) =====
        val starPaint = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
        }
        val random = java.util.Random()
        for (i in 0..30) {
            val x = random.nextInt(screenWidth.toInt()).toFloat()
            val y = random.nextInt((screenHeight * 0.8f).toInt()).toFloat()
            val size = 1f + random.nextFloat() * 3f
            canvas.drawCircle(x, y, size, starPaint)
        }
    }

    // ===== ОБРАБОТКА КЛИКОВ ПО КАРТЕ =====
    fun handleMapClick(
        x: Float,
        y: Float,
        screenWidth: Float,
        screenHeight: Float,
        locationManager: LocationManager,
        onTeleport: (LocationManager.Location) -> Unit
    ): Boolean {
        val mapScale = 0.8f
        val centerX = screenWidth / 2
        val centerY = screenHeight / 2 + 50f

        // Кнопка "Город"
        val cityBtnX = centerX - 100f
        val cityBtnY = centerY + 150f
        if (x > cityBtnX && x < cityBtnX + 200f &&
            y > cityBtnY && y < cityBtnY + 50f) {
            onTeleport(LocationManager.Location.CITY)
            return true
        }

        // Кнопка "Лес"
        val forestBtnX = centerX - 250f * mapScale - 100f
        val forestBtnY = centerY + 150f
        if (x > forestBtnX && x < forestBtnX + 200f &&
            y > forestBtnY && y < forestBtnY + 50f) {
            onTeleport(LocationManager.Location.FOREST)
            return true
        }

        // Кнопка "Пустошь"
        val wasteBtnX = centerX + 250f * mapScale - 100f
        val wasteBtnY = centerY + 150f
        if (x > wasteBtnX && x < wasteBtnX + 200f &&
            y > wasteBtnY && y < wasteBtnY + 50f) {
            onTeleport(LocationManager.Location.WASTELAND)
            return true
        }

        // Кнопка "Скалы"
        val rocksBtnX = centerX - 400f * mapScale - 100f
        val rocksBtnY = centerY + 150f
        if (x > rocksBtnX && x < rocksBtnX + 200f &&
            y > rocksBtnY && y < rocksBtnY + 50f) {
            onTeleport(LocationManager.Location.ROCKS)
            return true
        }

        // Кнопка "Замок"
        val castleBtnX = centerX - 550f * mapScale - 100f
        val castleBtnY = centerY + 150f
        if (x > castleBtnX && x < castleBtnX + 200f &&
            y > castleBtnY && y < castleBtnY + 50f) {
            onTeleport(LocationManager.Location.CASTLE)
            return true
        }

        // ⭐ Кнопка "Пустыня" (НОВАЯ)
        val desertBtnX = centerX + 400f * mapScale - 100f
        val desertBtnY = centerY + 150f + 70f
        if (x > desertBtnX && x < desertBtnX + 200f &&
            y > desertBtnY && y < desertBtnY + 50f) {
            onTeleport(LocationManager.Location.DESERT)
            return true
        }

        return false
    }
}
