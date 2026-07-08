package com.example.myapplication.model

data class Rune(
    val id: String,
    val name: String,
    val description: String,
    val stats: ItemStats = ItemStats(),
    val rarity: ItemRarity = ItemRarity.COMMON,
    val icon: String = "💎"  // Эмодзи для отображения
) {
    fun getRarityColor(): Int {
        return when (rarity) {
            ItemRarity.COMMON -> android.graphics.Color.rgb(200, 200, 200)
            ItemRarity.UNCOMMON -> android.graphics.Color.rgb(50, 200, 50)
            ItemRarity.RARE -> android.graphics.Color.rgb(50, 150, 255)
            ItemRarity.EPIC -> android.graphics.Color.rgb(200, 100, 255)
            ItemRarity.LEGENDARY -> android.graphics.Color.rgb(255, 150, 50)
            ItemRarity.MYTHIC -> android.graphics.Color.rgb(255, 215, 0)
        }
    }
}
