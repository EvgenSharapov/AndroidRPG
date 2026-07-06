package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import com.google.gson.Gson
import java.io.FileNotFoundException

class SpriteManager(private val context: Context) {

    private val spriteSheets: MutableMap<String, Bitmap> = mutableMapOf()
    private val frames: MutableMap<String, MutableMap<String, Rect>> = mutableMapOf()
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

    private val leftPadding = 0
    private val rightPadding = 0
    private val topPadding = 0
    private val bottomPadding = 0

    init {
        loadSpriteSheet("character", "character.json")
        loadSpriteSheet("battle_idle", "character_battle_idle.json")
        loadSpriteSheet("battle_attack", "character_battle_attack.json")

        loadSpriteSheet("fluffy", "fluffy.json")
        loadSpriteSheet("spider", "spider.json")
        loadSpriteSheet("spider_battle", "spider_battle.json")
        loadSpriteSheet("manyeyes", "manyeyes.json")
        loadSpriteSheet("manyeyes_battle", "manyeyes_battle.json")

        loadSpriteSheet("red_knight", "red_knight.json")
        loadSpriteSheet("red_knight_battle", "red_knight.json")
        loadSpriteSheet("slime_green", "slime_green.json")
        loadSpriteSheet("slime_green_battle", "slime_green.json")
        loadSpriteSheet("steel_knight", "steel_knight.json")
        loadSpriteSheet("steel_knight_battle", "steel_knight.json")
        loadSpriteSheet("goblin", "goblin.json")

        loadSpriteSheet("character_zombie", "character_zombie.json")
    }

    private fun loadSpriteSheet(spriteName: String, jsonFileName: String) {
        try {
            val resId = context.resources.getIdentifier(spriteName, "drawable", context.packageName)
            println("🔍 Ищем ресурс: $spriteName, resId=$resId")
            if (resId == 0) {
                println("❌ Ресурс $spriteName не найден!")
                return
            }

            val spriteSheet = BitmapFactory.decodeResource(context.resources, resId)
            spriteSheets[spriteName] = spriteSheet
            val realWidth = spriteSheet.width
            val realHeight = spriteSheet.height
            println("✅ $spriteName загружен: ${realWidth}x${realHeight}")

            val jsonString = context.assets.open(jsonFileName).bufferedReader().use { it.readText() }
            val gson = Gson()
            val spriteData: SpriteData = gson.fromJson(jsonString, SpriteData::class.java)

            val jsonWidth = spriteData.meta.size.w
            val jsonHeight = spriteData.meta.size.h
            val scaleX = realWidth.toFloat() / jsonWidth.toFloat()
            val scaleY = realHeight.toFloat() / jsonHeight.toFloat()

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

            val allFrames = spriteData.frames.keys.toList()
            val animMap = mutableMapOf<String, Animation>()

            when (spriteName) {
                "character" -> {
                    animMap["idle"] = Animation(listOf(allFrames[0]), 10)
                    animMap["run"] = Animation(allFrames, 8)
                    animMap["attack"] = Animation(allFrames.take(3), 6)
                }
                "battle_idle" -> {
                    animMap["idle"] = Animation(allFrames, 3)
                }
                "battle_attack" -> {
                    animMap["attack"] = Animation(allFrames, 2)
                }
                "fluffy" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["run"] = Animation(allFrames, 8)
                }
                "spider" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["run"] = Animation(allFrames, 8)
                }
                "spider_battle" -> {
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
                "red_knight" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["run"] = Animation(allFrames, 8)
                }
                "red_knight_battle" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["attack"] = Animation(allFrames, 6)
                }
                "slime_green" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["run"] = Animation(allFrames, 8)
                }
                "slime_green_battle" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["attack"] = Animation(allFrames, 6)
                }
                "steel_knight" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["run"] = Animation(allFrames, 8)
                }
                "steel_knight_battle" -> {
                    animMap["idle"] = Animation(allFrames, 10)
                    animMap["attack"] = Animation(allFrames, 6)
                }
                "goblin" -> {
                    val idleFrames = allFrames.filter { it.startsWith("idle_") }
                    val walkLeftFrames = allFrames.filter { it.startsWith("walk_left_") }
                    val walkRightFrames = allFrames.filter { it.startsWith("walk_right_") }
                    val attackFrames = allFrames.filter { it.startsWith("attack_") }

                    animMap["idle"] = Animation(idleFrames, 10)
                    animMap["walk_left"] = Animation(walkLeftFrames, 8)
                    animMap["walk_right"] = Animation(walkRightFrames, 8)
                    animMap["attack"] = Animation(attackFrames, 2)
                }
                "character_zombie" -> {
                    val idleFrames = allFrames.filter { it.startsWith("idle_") }
                    val attackFrames = allFrames.filter { it.startsWith("attack_") }
                    val walkLeftFrames = allFrames.filter { it.startsWith("walk_left_") }
                    val walkRightFrames = allFrames.filter { it.startsWith("walk_right_") }
                    val runFrames = allFrames.filter { it.startsWith("run_") }

                    animMap["idle"] = Animation(if (idleFrames.isNotEmpty()) idleFrames else listOf(allFrames[0]), 4)
                    animMap["attack"] = Animation(if (attackFrames.isNotEmpty()) attackFrames else allFrames.take(4), 1)
                    animMap["walk_left"] = Animation(walkLeftFrames, 6)
                    animMap["walk_right"] = Animation(walkRightFrames, 6)
                    animMap["run"] = Animation(if (runFrames.isNotEmpty()) runFrames else walkRightFrames, 5)
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

    fun getSpriteSheet(name: String): Bitmap? = spriteSheets[name]

    fun getFrame(spriteName: String, frameName: String): Rect {
        return frames[spriteName]?.get(frameName) ?: Rect(0, 0, 64, 64)
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
