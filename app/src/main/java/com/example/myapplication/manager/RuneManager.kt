package com.example.myapplication.manager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.google.gson.Gson
import java.io.FileNotFoundException

class RuneManager(private val context: Context) {

    private var runeSheet: Bitmap? = null
    private val runeFrames = mutableMapOf<String, Rect>()

    data class RuneSpriteData(
        val frames: Map<String, FrameEntry>,
        val meta: MetaData
    )

    data class FrameEntry(
        val frame: RectData,
        val rotated: Boolean = false,
        val trimmed: Boolean = false
    )

    data class RectData(
        val x: Int,
        val y: Int,
        val w: Int,
        val h: Int
    )

    data class MetaData(
        val image: String,
        val size: SizeData
    )

    data class SizeData(
        val w: Int,
        val h: Int
    )

    init {
        loadRunes()
    }

    private fun loadRunes() {
        try {
            // Загружаем изображение с рунами
            val resId = context.resources.getIdentifier("runes", "drawable", context.packageName)
            if (resId == 0) {
                println("❌ Ресурс runes не найден!")
                return
            }

            runeSheet = BitmapFactory.decodeResource(context.resources, resId)
            val realWidth = runeSheet?.width ?: 0
            val realHeight = runeSheet?.height ?: 0
            println("✅ runes загружен: ${realWidth}x${realHeight}")

            // Загружаем JSON с координатами
            val jsonString = context.assets.open("runes.json").bufferedReader().use { it.readText() }
            val gson = Gson()
            val spriteData: RuneSpriteData = gson.fromJson(jsonString, RuneSpriteData::class.java)

            val jsonWidth = spriteData.meta.size.w
            val jsonHeight = spriteData.meta.size.h
            val scaleX = realWidth.toFloat() / jsonWidth.toFloat()
            val scaleY = realHeight.toFloat() / jsonHeight.toFloat()

            for ((name, entry) in spriteData.frames) {
                val rect = entry.frame
                val scaledX = (rect.x * scaleX).toInt()
                val scaledY = (rect.y * scaleY).toInt()
                val scaledW = (rect.w * scaleX).toInt()
                val scaledH = (rect.h * scaleY).toInt()

                runeFrames[name] = Rect(
                    scaledX,
                    scaledY,
                    scaledX + scaledW,
                    scaledY + scaledH
                )
                println("📐 Руна $name: x=$scaledX, y=$scaledY, w=$scaledW, h=$scaledH")
            }

            println("✅ Загружено ${runeFrames.size} рун")

        } catch (e: FileNotFoundException) {
            println("❌ Файл runes.json НЕ НАЙДЕН!")
            e.printStackTrace()
        } catch (e: Exception) {
            println("❌ Ошибка при загрузке рун: ${e.message}")
            e.printStackTrace()
        }
    }

    fun getRuneBitmap(runeId: String): Bitmap? {
        val sheet = runeSheet ?: return null
        val frame = runeFrames[runeId] ?: return null

        return try {
            Bitmap.createBitmap(sheet, frame.left, frame.top, frame.width(), frame.height())
        } catch (e: Exception) {
            println("❌ Ошибка создания руны $runeId: ${e.message}")
            null
        }
    }

    fun getRuneFrame(runeId: String): Rect? {
        return runeFrames[runeId]
    }

    fun getAllRuneIds(): List<String> {
        return runeFrames.keys.toList()
    }
}
