package com.example.myapplication.model

import kotlin.random.Random

/**
 * Данные о дропе для конкретного моба
 */
data class MobDrop(
    val mobType: Int,
    val minLevel: Int = 1,
    val maxLevel: Int = 99,
    val dropTable: DropTable,
    val goldMin: Int = 1,
    val goldMax: Int = 4,
    val goldChance: Int = 100
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
    fun getDrop(random: Random): Pair<List<Item>, Int> {
        val items = dropTable.rollDrops(random)

        // Расчёт золота
        var gold = 0
        if (random.nextInt(100) < goldChance) {
            gold = random.nextInt(goldMin, goldMax + 1)
        }

        return Pair(items, gold)
    }
}
