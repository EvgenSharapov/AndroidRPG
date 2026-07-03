package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.google.gson.Gson
import java.io.FileNotFoundException

class SpriteManager(private val context: Context) {

    // Храним все спрайт-листы по имени
    private val spriteSheets: MutableMap<String, Bitmap> = mutableMapOf()
    // Кадры: имя спрайта → имя кадра → Rect
    private val frames: MutableMap<String, MutableMap<String, Rect>> = mutableMapOf()
    // Анимации: имя спрайта → имя анимации → Animation
    private val animations: MutableMap<String, MutableMap<String, Animation>> = mutableMapOf()

    data class Animation(
        val frames: List<String>,
        val speed: Int = 8
    )

    data class SpriteData(
        val frames: Map<String, FrameEntry>,
        val meta: MetaData
    )

    data class FrameEntry(
        val frame: RectData,
        val duration: Int
    )

    data class RectData(
        val x: Int,
        val y: Int,
        val w: Int,
        val h: Int
    )

    data class MetaData(
        val image: String,
        val size: SizeData,
        val fps: Int
    )

    data class SizeData(
        val w: Int,
        val h: Int
    )

    // Отступы для обрезки (подбери под свои спрайты)
    private val leftPadding = 10
    private val rightPadding = 10
    private val topPadding = 1
    private val bottomPadding = 0

    init {
        // Загружаем все спрайты
        loadSpriteSheet("character", "character.json")
        loadSpriteSheet("battle_idle", "character_battle_idle.json")
        loadSpriteSheet("battle_attack", "character_battle_attack.json")
        loadSpriteSheet("fluffy", "fluffy.json")
        loadSpriteSheet("spider", "spider.json")
        loadSpriteSheet("spider_battle", "spider_battle.json")

        loadSpriteSheet("manyeyes", "manyeyes.json")
        loadSpriteSheet("manyeyes_battle", "manyeyes_battle.json")
    }

    private fun loadSpriteSheet(spriteName: String, jsonFileName: String) {
        try {
            // 1. Загружаем PNG
            val resId = context.resources.getIdentifier(spriteName, "drawable", context.packageName)
            if (resId == 0) {
                println("❌ Ресурс $spriteName не найден!")
                return
            }

            val spriteSheet = BitmapFactory.decodeResource(context.resources, resId)
            spriteSheets[spriteName] = spriteSheet
            val realWidth = spriteSheet.width
            val realHeight = spriteSheet.height
            println("✅ $spriteName загружен: ${realWidth}x${realHeight}")

            // 2. Загружаем JSON
            val jsonString = context.assets.open(jsonFileName).bufferedReader().use { it.readText() }
            val gson = Gson()
            val spriteData: SpriteData = gson.fromJson(jsonString, SpriteData::class.java)

            // 3. Масштабируем координаты
            val jsonWidth = spriteData.meta.size.w
            val jsonHeight = spriteData.meta.size.h
            val scaleX = realWidth.toFloat() / jsonWidth.toFloat()
            val scaleY = realHeight.toFloat() / jsonHeight.toFloat()

            // 4. Сохраняем кадры
            val frameMap = mutableMapOf<String, Rect>()
            for ((name, entry) in spriteData.frames) {
                val rect = entry.frame
                val scaledX = (rect.x * scaleX).toInt()
                val scaledY = (rect.y * scaleY).toInt()
                val scaledW = (rect.w * scaleX).toInt()
                val scaledH = (rect.h * scaleY).toInt()

                frameMap[name] = Rect(
                    scaledX + leftPadding,
                    scaledY + topPadding,
                    scaledX + scaledW - rightPadding,
                    scaledY + scaledH - bottomPadding
                )
                println("📐 $spriteName - $name: x=$scaledX, y=$scaledY, w=$scaledW, h=$scaledH")
            }
            frames[spriteName] = frameMap

            // 5. Настраиваем анимации в зависимости от спрайта
            val allFrames = spriteData.frames.keys.toList()
            val animMap = mutableMapOf<String, Animation>()

            when (spriteName) {
                "character" -> {
                    // Для карты
                    animMap["idle"] = Animation(listOf(allFrames[0]), 10)
                    animMap["run"] = Animation(allFrames, 8)
                    animMap["attack"] = Animation(allFrames.take(3), 6)
                }
                "battle_idle" -> {
                    // Для боя: просто стоит (1 кадр)
                    animMap["idle"] = Animation(allFrames, 3)
                }
                "battle_attack" -> {
                    // Для боя: атака (несколько кадров)
                    animMap["attack"] = Animation(allFrames, 2)
                }
                "fluffy" -> {
                    // Для карты: все 4 кадра
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["run"] = Animation(allFrames, 8)
                }
                "spider" -> {  // ← ДЛЯ КАРТЫ
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["run"] = Animation(allFrames, 8)
                }
                "spider_battle" -> {  // ← ДЛЯ БОЯ
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["attack"] = Animation(allFrames, 6)
                }
                "manyeyes" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["run"] = Animation(allFrames, 8)
                }
                "manyeyes_battle" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["attack"] = Animation(allFrames, 6)
                }
            }
            animations[spriteName] = animMap

            println("✅ $spriteName загружено кадров: ${frameMap.size}")
            println("   Анимации: ${animMap.keys}")

        } catch (e: FileNotFoundException) {
            println("❌ Файл $jsonFileName НЕ НАЙДЕН!")
            e.printStackTrace()
        } catch (e: Exception) {
            println("❌ Ошибка при загрузке $spriteName: ${e.message}")
            e.printStackTrace()
        }
    }

    // ===== ПУБЛИЧНЫЕ МЕТОДЫ =====

    fun getSpriteSheet(name: String): Bitmap? = spriteSheets[name]

    fun getFrame(spriteName: String, frameName: String): Rect {
        return frames[spriteName]?.get(frameName) ?: Rect(0, 0, 47, 47)
    }

    fun getAnimation(spriteName: String, animName: String): Animation? {
        return animations[spriteName]?.get(animName)
    }

    fun getAnimationFrames(spriteName: String, animName: String): List<Rect> {
        val anim = animations[spriteName]?.get(animName) ?: return emptyList()
        val frameMap = frames[spriteName] ?: return emptyList()
        return anim.frames.mapNotNull { frameMap[it] }
    }

    fun getAllFrameNames(spriteName: String): Set<String> {
        return frames[spriteName]?.keys ?: emptySet()
    }
}
