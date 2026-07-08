package com.example.myapplication.manager

import com.example.myapplication.model.*
import kotlin.random.Random

class RefineManager {

    companion object {
        // ⭐ ШАНСЫ УСПЕХА ДЛЯ КАЖДОГО УРОВНЯ
        private val SUCCESS_CHANCES = mapOf(
            0 to 100,   // +0 → +1: 100%
            1 to 90,    // +1 → +2: 90%
            2 to 80,    // +2 → +3: 80%
            3 to 70,    // +3 → +4: 70%
            4 to 60,    // +4 → +5: 60%
            5 to 50,    // +5 → +6: 50%
            6 to 40,    // +6 → +7: 40%
            7 to 30,    // +7 → +8: 30%
            8 to 20,    // +8 → +9: 20%
            9 to 10     // +9 → +10: 10%
        )

        // ⭐ КОЛИЧЕСТВО МАТЕРИАЛОВ ДЛЯ КАЖДОГО УРОВНЯ
        private val MATERIALS_NEEDED = mapOf(
            0 to 1,     // +0 → +1: 1 материал
            1 to 2,     // +1 → +2: 2 материала
            2 to 3,     // +2 → +3: 3 материала
            3 to 4,     // +3 → +4: 4 материала
            4 to 5,     // +4 → +5: 5 материалов
            5 to 6,     // +5 → +6: 6 материалов
            6 to 7,     // +6 → +7: 7 материалов
            7 to 8,     // +7 → +8: 8 материалов
            8 to 9,     // +8 → +9: 9 материалов
            9 to 10     // +9 → +10: 10 материалов
        )

        // ⭐ СТОИМОСТЬ В ЗОЛОТЕ ДЛЯ КАЖДОГО УРОВНЯ
        private val GOLD_COST = mapOf(
            0 to 100,
            1 to 200,
            2 to 300,
            3 to 400,
            4 to 500,
            5 to 700,
            6 to 900,
            7 to 1200,
            8 to 1500,
            9 to 2000
        )

        // ⭐ ПУБЛИЧНЫЕ МЕТОДЫ ДЛЯ ДОСТУПА ИЗВНЕ
        fun getSuccessChance(currentLevel: Int): Int {
            return SUCCESS_CHANCES[currentLevel] ?: 0
        }

        fun getMaterialsNeeded(currentLevel: Int): Int {
            return MATERIALS_NEEDED[currentLevel] ?: 1
        }

        fun getGoldCost(currentLevel: Int): Int {
            return GOLD_COST[currentLevel] ?: 100
        }
    }

    data class RefineResult(
        val success: Boolean,
        val broken: Boolean,
        val newLevel: Int,
        val message: String
    )

    fun canRefine(item: Item, player: Player, inventory: Inventory): Boolean {
        println("🔍 canRefine(): уровень=${item.refineLevel}, тип=${item.type}")

        if (item.refineLevel >= 10) {
            println("🔍 Достигнут максимум!")
            return false
        }

        if (item.type == Item.ItemType.CONSUMABLE) {
            println("🔍 Это расходник!")
            return false
        }

        val materialId = if (item.type == Item.ItemType.WEAPON || item.type == Item.ItemType.SHIELD) {
            "oridecon"
        } else {
            "elunium"
        }

        val needed = getMaterialsNeeded(item.refineLevel)
        val hasMaterials = countMaterials(inventory, materialId) >= needed
        println("🔍 Материалы: $materialId, нужно: $needed, есть: ${countMaterials(inventory, materialId)}")

        val cost = getGoldCost(item.refineLevel)
        val hasGold = player.gold >= cost
        println("🔍 Золото: нужно: $cost, есть: ${player.gold}")

        val result = hasMaterials && hasGold
        println("🔍 canRefine = $result")
        return result
    }

    fun refine(item: Item, player: Player, inventory: Inventory): RefineResult {
        val currentLevel = item.refineLevel

        // ⭐ ЛОГИ ДЛЯ ОТЛАДКИ
        println("🔨 Заточка: ${item.name}, текущий уровень: $currentLevel")
        println("🔨 Нужно материалов: ${getMaterialsNeeded(currentLevel)}, золота: ${getGoldCost(currentLevel)}")

        // ⭐ ПРОВЕРКА
        if (currentLevel >= 10) {
            return RefineResult(false, false, currentLevel, "❌ Предмет уже заточен до максимума!")
        }

        if (item.type == Item.ItemType.CONSUMABLE) {
            return RefineResult(false, false, currentLevel, "❌ Нельзя заточить расходник!")
        }

        val materialId = if (item.type == Item.ItemType.WEAPON || item.type == Item.ItemType.SHIELD) {
            "oridecon"
        } else {
            "elunium"
        }

        val needed = getMaterialsNeeded(currentLevel)
        val hasMaterials = countMaterials(inventory, materialId) >= needed

        if (!hasMaterials) {
            return RefineResult(false, false, currentLevel, "❌ Недостаточно материалов! Нужно: $needed")
        }

        val cost = getGoldCost(currentLevel)
        if (player.gold < cost) {
            return RefineResult(false, false, currentLevel, "❌ Недостаточно золота! Нужно: $cost 💰")
        }

        // Списываем ресурсы
        player.gold -= cost
        removeMaterials(inventory, materialId, needed)

        // ⭐ РАСЧЁТ ШАНСА
        val chance = getSuccessChance(currentLevel)
        val random = Random.nextInt(100)
        val success = random < chance

        if (success) {
            // ⭐ УВЕЛИЧИВАЕМ УРОВЕНЬ
            println("🔨 УСПЕХ! Новый уровень: ${item.refineLevel}")
            item.refineLevel++
            return RefineResult(
                true,
                false,
                item.refineLevel,
                "✅ Заточка успешна! ${item.getDisplayName()} теперь +${item.refineLevel} ${item.getRefineStars()}"
            )
        } else {
            val broken = Random.nextBoolean()
            if (broken) {
                return RefineResult(
                    false,
                    true,
                    currentLevel,
                    "💔 Предмет сломался при заточке! ${item.getDisplayName()} уничтожен."
                )
            } else {
                return RefineResult(
                    false,
                    false,
                    currentLevel,
                    "❌ Заточка не удалась! ${item.getDisplayName()} остался без изменений."
                )
            }
        }
    }

    private fun countMaterials(inventory: Inventory, materialId: String): Int {
        var totalCount = 0
        for (item in inventory.getItems()) {
            if (item != null && item.id == materialId) {
                // ⭐ СУММИРУЕМ КОЛИЧЕСТВО В СТАКЕ
                totalCount += item.quantity
                println("🔍 Найден материал: ${item.name}, quantity=${item.quantity}, всего: $totalCount")
            }
        }
        return totalCount
    }

    private fun removeMaterials(inventory: Inventory, materialId: String, count: Int) {
        var removed = 0
        val items = inventory.getItems()
        println("🔍 Начинаем удаление $count материалов $materialId")

        for (i in items.indices) {
            if (removed >= count) break
            val item = items[i]
            if (item != null && item.id == materialId) {
                val toRemove = minOf(item.quantity, count - removed)
                println("🔍 В стаке ${item.quantity}, нужно удалить $toRemove")

                if (toRemove >= item.quantity) {
                    // Удаляем весь стак
                    inventory.removeItem(i)
                    removed += toRemove
                } else {
                    // Уменьшаем количество
                    item.quantity -= toRemove
                    removed += toRemove
                }
                println("🔍 Удалено $toRemove, осталось удалить: ${count - removed}")
            }
        }
        println("🔍 Итого удалено: $removed из $count")
    }
}
