package com.example.myapplication.manager

import com.example.myapplication.model.DropTable
import com.example.myapplication.model.Mob
import kotlin.random.Random
import com.example.myapplication.model.Item

/**
 * Данные о дропе для конкретного моба
 */
data class MobDrop(
    val mobType: Int,                    // Тип моба (0, 1, 2...)
    val minLevel: Int = 1,               // Минимальный уровень моба для этого дропа
    val maxLevel: Int = 99,              // Максимальный уровень моба для этого дропа
    val dropTable: DropTable,            // Таблица дропа
    val goldMin: Int = 1,                // Минимальное золото
    val goldMax: Int = 4,                // Максимальное золото
    val goldChance: Int = 100            // Шанс выпадения золота (0-100)
) {
    /**
     * Проверяет, подходит ли этот дроп для моба
     */
    fun matches(mob: Mob): Boolean {
        return mob.type == mobType && mob.level in minLevel..maxLevel
    }

    /**
     * Получить дроп для моба
     * @return Пара (предметы, золото)
     */
    fun getDrop(random: Random): Pair<List<Item>, Int>{
        val items = dropTable.rollDrops(random)

        // Расчёт золота
        var gold = 0
        if (random.nextInt(100) < goldChance) {
            gold = random.nextInt(goldMin, goldMax + 1)
        }

        return Pair(items, gold)
    }
}