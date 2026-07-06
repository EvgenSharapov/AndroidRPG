package com.example.myapplication.model

import android.graphics.Bitmap
import kotlin.math.min

enum class ItemRarity {
    COMMON,      // Обычный — белый
    UNCOMMON,    // Необычный — зелёный
    RARE,        // Редкий — синий
    EPIC,        // Эпический — фиолетовый
    LEGENDARY,   // Легендарный — оранжевый
    MYTHIC       // Мифический — золотой
}

data class Item(
    val id: String,
    val name: String,
    val type: ItemType,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val icon: Bitmap? = null,
    val description: String = "",
    val stats: ItemStats = ItemStats()
) {
    enum class ItemType {
        WEAPON,      // Оружие
        HELMET,      // Шлем
        CHEST,       // Броня/Нагрудник
        PANTS,       // Поножи/Штаны
        BOOTS,       // Сапоги
        GLOVES,      // Перчатки
        BRACERS,     // Наручи
        SHIELD,      // Щит
        RING,        // Кольцо
        NECKLACE,    // Амулет
        CONSUMABLE   // Расходник (зелье и т.д.)
    }

    fun getRarityColor(): Int {
        return when (rarity) {
            ItemRarity.COMMON -> android.graphics.Color.rgb(200, 200, 200)     // Белый
            ItemRarity.UNCOMMON -> android.graphics.Color.rgb(50, 200, 50)     // Зелёный
            ItemRarity.RARE -> android.graphics.Color.rgb(50, 150, 255)        // Синий
            ItemRarity.EPIC -> android.graphics.Color.rgb(200, 100, 255)       // Фиолетовый
            ItemRarity.LEGENDARY -> android.graphics.Color.rgb(255, 150, 50)   // Оранжевый
            ItemRarity.MYTHIC -> android.graphics.Color.rgb(255, 215, 0)       // Золотой
        }
    }

    fun getRarityName(): String {
        return when (rarity) {
            ItemRarity.COMMON -> "Обычный"
            ItemRarity.UNCOMMON -> "Необычный"
            ItemRarity.RARE -> "Редкий"
            ItemRarity.EPIC -> "Эпический"
            ItemRarity.LEGENDARY -> "Легендарный"
            ItemRarity.MYTHIC -> "Мифический"
        }
    }

    fun use(player: Player): Boolean {
        return when (id) {
            "cake_small" -> {
                player.hp = min(player.hp + 30f, player.calculateMaxHp())
                true
            }
            "cake_medium" -> {
                player.hp = min(player.hp + 60f, player.calculateMaxHp())
                true
            }
            "cake_large" -> {
                player.hp = min(player.hp + 120f, player.calculateMaxHp())
                true
            }
            else -> false
        }
    }
}

data class ItemStats(
    val attack: Int = 0,
    val defense: Int = 0,
    val health: Int = 0,
    val agility: Int = 0,
    val strength: Int = 0,
    val luck: Int = 0
)