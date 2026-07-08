package com.example.myapplication.manager

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SaveManager(context: Context) {

    // ⭐ ОБЪЯВЛЯЕМ prefs
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
        private const val KEY_PLAYER_NAME = "player_name"
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
        editor.putString(KEY_PLAYER_NAME, player.name)

        // Характеристики
        editor.putInt(KEY_STRENGTH, player.strength)
        editor.putInt(KEY_ENDURANCE, player.endurance)
        editor.putInt(KEY_AGILITY, player.agility)
        editor.putInt(KEY_DEXTERITY, player.dexterity)
        editor.putInt(KEY_LUCK, player.luck)

        // Инвентарь
        val items = inventory.getItems()
        for (item in items) {
            if (item != null && item.runes == null) {
                item.runes = mutableListOf()
            }
        }
        val itemsJson = gson.toJson(items)
        editor.putString(KEY_INVENTORY, itemsJson)
        println("📦 Инвентарь сохранён: ${items.count { it != null }} предметов")

        // Экипировка
        val equipment = inventory.getAllEquipment()
        for ((_, item) in equipment) {
            if (item.runes == null) {
                item.runes = mutableListOf()
            }
        }
        val equipmentJson = gson.toJson(equipment)
        editor.putString(KEY_EQUIPMENT, equipmentJson)
        println("⚔️ Экипировка сохранена: ${equipment.size} предметов")

        editor.apply()
        println("✅ Полный прогресс сохранён!")
    }

    /**
     * ПОЛНАЯ ЗАГРУЗКА (инвентарь + экипировка)
     */
    fun loadGame(player: Player, inventory: Inventory): Boolean {
        if (!prefs.contains(KEY_GOLD)) {
            println("ℹ️ Нет сохранений")
            return false
        }

        try {
            // Основные параметры
            player.gold = prefs.getInt(KEY_GOLD, 0)
            player.level = prefs.getInt(KEY_LEVEL, 1)
            player.exp = prefs.getInt(KEY_EXP, 0)
            player.maxExp = prefs.getInt(KEY_MAX_EXP, 50)
            player.skillPoints = prefs.getInt(KEY_SKILL_POINTS, 0)
            player.hp = prefs.getFloat(KEY_HP, 100f)
            player.name = prefs.getString(KEY_PLAYER_NAME, "Герой") ?: "Герой"

            // Характеристики
            player.strength = prefs.getInt(KEY_STRENGTH, 5)
            player.endurance = prefs.getInt(KEY_ENDURANCE, 5)
            player.agility = prefs.getInt(KEY_AGILITY, 5)
            player.dexterity = prefs.getInt(KEY_DEXTERITY, 5)
            player.luck = prefs.getInt(KEY_LUCK, 5)

            // Инвентарь
            val itemsJson = prefs.getString(KEY_INVENTORY, null)
            if (itemsJson != null) {
                try {
                    val type = object : TypeToken<List<Item?>>() {}.type
                    val loadedItems: List<Item?> = gson.fromJson(itemsJson, type)

                    for (item in loadedItems) {
                        if (item != null && item.runes == null) {
                            item.runes = mutableListOf()
                        }
                    }

                    inventory.setItems(loadedItems)
                    println("📦 Загружено предметов: ${loadedItems.count { it != null }}")
                } catch (e: Exception) {
                    println("⚠️ Ошибка загрузки инвентаря: ${e.message}")
                }
            }

            // Экипировка
            val equipmentJson = prefs.getString(KEY_EQUIPMENT, null)
            if (equipmentJson != null) {
                try {
                    val type = object : TypeToken<Map<EquipmentSlot, Item>>() {}.type
                    val loadedEquipment: Map<EquipmentSlot, Item> = gson.fromJson(equipmentJson, type)

                    for ((_, item) in loadedEquipment) {
                        if (item.runes == null) {
                            item.runes = mutableListOf()
                        }
                    }

                    inventory.setEquipment(loadedEquipment)
                    println("⚔️ Загружено экипировки: ${loadedEquipment.size}")
                } catch (e: Exception) {
                    println("⚠️ Ошибка загрузки экипировки: ${e.message}")
                }
            }

            // ⭐ ПРИМЕНЯЕМ БОНУСЫ ОТ РУН ПРИ ЗАГРУЗКЕ
            // Обновляем максимальное HP с учетом рун
            val maxHp = player.getMaxHp(inventory)
            if (player.hp > maxHp) {
                player.hp = maxHp
            }

            // Обновляем скорость с учетом рун
            player.speed = player.getSpeed(inventory)

            // Проверяем, не нужно ли повысить уровень
            while (player.exp >= player.maxExp) {
                player.exp -= player.maxExp
                player.level++
                player.maxExp = (player.maxExp * 1.5f).toInt()
                player.skillPoints += 5
                println("🎉 УРОВЕНЬ ${player.level} восстановлен при загрузке!")
            }

            println("✅ Полный прогресс загружен! Золото: ${player.gold}")
            println("   HP: ${player.hp}/${player.getMaxHp(inventory)}")
            println("   Скорость: ${player.speed}")
            return true

        } catch (e: Exception) {
            println("❌ Ошибка загрузки: ${e.message}")
            return false
        }
    }
}
