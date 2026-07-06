package com.example.myapplication.model

import kotlin.random.Random

/**
 * Таблица дропа для мобов
 * Содержит список предметов и их шанс выпадения
 */
data class DropTable(
    val drops: MutableList<DropEntry> = mutableListOf()
) {

    data class DropEntry(
        val itemId: String,
        val itemName: String,
        val itemType: Item.ItemType,
        val chance: Double, // 0-100 (проценты)
        val minCount: Int = 1,
        val maxCount: Int = 1,
        val rarity: ItemRarity,
        val stats: ItemStats = ItemStats(),
        val description: String = ""
    )

    /**
     * Проверяет, выпал ли предмет
     * @param random Random для генерации
     * @return Список выпавших предметов
     */
    fun rollDrops(random: Random): List<Item> {
        val result = mutableListOf<Item>()
        for (entry in drops) {
            if (random.nextDouble() * 100 < entry.chance) {
                val count = random.nextInt(entry.minCount, entry.maxCount + 1)
                for (i in 0 until count) {
                    result.add(
                        Item(
                            id = entry.itemId + if (count > 1) "_${i}" else "",
                            name = entry.itemName + if (count > 1) " x${i+1}" else "",
                            type = entry.itemType,
                            icon = null,
                            description = entry.description,
                            stats = entry.stats
                        )
                    )
                }
            }
        }
        return result
    }

    companion object {

        fun createMixedDrop(vararg entries: DropEntry): DropTable {
            return DropTable().apply {
                drops.addAll(entries)
            }
        }
    }
}
