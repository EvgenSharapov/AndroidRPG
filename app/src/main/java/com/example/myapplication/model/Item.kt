package com.example.myapplication.model

import android.graphics.Bitmap

data class Item(
    val id: String,
    val name: String,
    val type: ItemType,
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
}

data class ItemStats(
    val attack: Int = 0,
    val defense: Int = 0,
    val health: Int = 0,
    val agility: Int = 0,
    val strength: Int = 0,
    val luck: Int = 0
)