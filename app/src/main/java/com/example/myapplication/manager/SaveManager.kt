package com.example.myapplication.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SaveManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("game_save", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_GOLD = "player_gold"
        private const val KEY_LEVEL = "player_level"
        private const val KEY_EXP = "player_exp"
        private const val KEY_MAX_EXP = "player_max_exp"
        private const val KEY_SKILL_POINTS = "player_skill_points"
        private const val KEY_STRENGTH = "player_strength"
        private const val KEY_ENDURANCE = "player_endurance"
        private const val KEY_AGILITY = "player_agility"
        private const val KEY_DEXTERITY = "player_dexterity"
        private const val KEY_LUCK = "player_luck"
        private const val KEY_HP = "player_hp"
        private const val KEY_INVENTORY = "inventory_data"
        private const val KEY_EQUIPMENT = "equipment_data"
    }

    /**
     * ПОЛНОЕ СОХРАНЕНИЕ (инвентарь + экипировка)
     */
    fun saveGame(player: Player, inventory: Inventory) {
        val editor = prefs.edit()

        // Основные параметры
        editor.putInt(KEY_GOLD, player.gold)
        editor.putInt(KEY_LEVEL, player.level)
        editor.putInt(KEY_EXP, player.exp)
        editor.putInt(KEY_MAX_EXP, player.maxExp)
        editor.putInt(KEY_SKILL_POINTS, player.skillPoints)
        editor.putFloat(KEY_HP, player.hp)

        // Характеристики
        editor.putInt(KEY_STRENGTH, player.strength)
        editor.putInt(KEY_ENDURANCE, player.endurance)
        editor.putInt(KEY_AGILITY, player.agility)
        editor.putInt(KEY_DEXTERITY, player.dexterity)
        editor.putInt(KEY_LUCK, player.luck)

        // ⭐ ИНВЕНТАРЬ (сериализуем в JSON)
        val items = inventory.getItems()
        val itemsJson = gson.toJson(items)
        editor.putString(KEY_INVENTORY, itemsJson)
        println("📦 Инвентарь сохранён: ${items.count { it != null }} предметов")

        // ⭐ ЭКИПИРОВКА (сериализуем в JSON)
        val equipment = inventory.getAllEquipment()
        val equipmentJson = gson.toJson(equipment)
        editor.putString(KEY_EQUIPMENT, equipmentJson)
        println("⚔️ Экипировка сохранена: ${equipment.size} предметов")

        editor.apply()
        println("✅ Полный прогресс сохранён!")
    }

    /**
     * ПОЛНАЯ ЗАГРУЗКА (инвентарь + экипировка)
     */
    /**
     * ПОЛНАЯ ЗАГРУЗКА (инвентарь + экипировка)
     */
    fun loadGame(player: Player, inventory: Inventory): Boolean {
        if (!prefs.contains(KEY_GOLD)) {
            println("ℹ️ Нет сохранений")
            return false
        }

        try {
            // ⭐ ОСНОВНЫЕ ПАРАМЕТРЫ
            player.gold = prefs.getInt(KEY_GOLD, 0)
            player.level = prefs.getInt(KEY_LEVEL, 1)
            player.exp = prefs.getInt(KEY_EXP, 0)
            player.maxExp = prefs.getInt(KEY_MAX_EXP, 50)
            player.skillPoints = prefs.getInt(KEY_SKILL_POINTS, 0)
            player.hp = prefs.getFloat(KEY_HP, 100f)
            println("💰 Загружено золото: ${player.gold}")

            // ⭐ ХАРАКТЕРИСТИКИ
            player.strength = prefs.getInt(KEY_STRENGTH, 5)
            player.endurance = prefs.getInt(KEY_ENDURANCE, 5)
            player.agility = prefs.getInt(KEY_AGILITY, 5)
            player.dexterity = prefs.getInt(KEY_DEXTERITY, 5)
            player.luck = prefs.getInt(KEY_LUCK, 5)

            // ⭐ ИНВЕНТАРЬ
            val itemsJson = prefs.getString(KEY_INVENTORY, null)
            if (itemsJson != null) {
                try {
                    val type = object : TypeToken<List<Item?>>() {}.type
                    val loadedItems: List<Item?> = gson.fromJson(itemsJson, type)
                    inventory.setItems(loadedItems)
                    println("📦 Загружено предметов: ${loadedItems.count { it != null }}")
                } catch (e: Exception) {
                    println("⚠️ Ошибка загрузки инвентаря: ${e.message}")
                }
            }

            // ⭐ ЭКИПИРОВКА
            val equipmentJson = prefs.getString(KEY_EQUIPMENT, null)
            if (equipmentJson != null) {
                try {
                    val type = object : TypeToken<Map<EquipmentSlot, Item>>() {}.type
                    val loadedEquipment: Map<EquipmentSlot, Item> = gson.fromJson(equipmentJson, type)
                    inventory.setEquipment(loadedEquipment)
                    println("⚔️ Загружено экипировки: ${loadedEquipment.size}")
                } catch (e: Exception) {
                    println("⚠️ Ошибка загрузки экипировки: ${e.message}")
                }
            }

            println("✅ Полный прогресс загружен! Золото: ${player.gold}")
            return true

        } catch (e: Exception) {
            println("❌ Ошибка загрузки: ${e.message}")
            return false
        }
    }

}
