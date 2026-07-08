package com.example.myapplication.model

enum class EquipmentSlot {
    WEAPON,      // Оружие (правая рука)
    SHIELD,      // Щит (левая рука)
    HELMET,      // Шлем
    CHEST,       // Броня
    PANTS,       // Поножи
    BOOTS,       // Сапоги
    GLOVES,      // Перчатки
    BRACERS,     // Наручи
    NECKLACE,    // Амулет
    RING1,       // Кольцо 1
    RING2
}

fun getSlotDisplayName(slot: EquipmentSlot): String = when (slot) {
    EquipmentSlot.WEAPON -> "⚔️ Оружие"
    EquipmentSlot.SHIELD -> "🛡️ Щит"
    EquipmentSlot.HELMET -> "⛑️ Шлем"
    EquipmentSlot.CHEST -> "👕 Броня"
    EquipmentSlot.PANTS -> "👖 Поножи"
    EquipmentSlot.BOOTS -> "👢 Сапоги"
    EquipmentSlot.GLOVES -> "🧤 Перчатки"
    EquipmentSlot.BRACERS -> "💪 Наручи"
    EquipmentSlot.NECKLACE -> "📿 Амулет"
    EquipmentSlot.RING1 -> "💍 Кольцо 1"
    EquipmentSlot.RING2 -> "💍 Кольцо 2"
}

fun getSlotIcon(slot: EquipmentSlot): String = when (slot) {
    EquipmentSlot.WEAPON -> "⚔️"
    EquipmentSlot.SHIELD -> "🛡️"
    EquipmentSlot.HELMET -> "⛑️"
    EquipmentSlot.CHEST -> "👕"
    EquipmentSlot.PANTS -> "👖"
    EquipmentSlot.BOOTS -> "👢"
    EquipmentSlot.GLOVES -> "🧤"
    EquipmentSlot.BRACERS -> "💪"
    EquipmentSlot.NECKLACE -> "📿"
    EquipmentSlot.RING1 -> "💍"
    EquipmentSlot.RING2 -> "💍"
}