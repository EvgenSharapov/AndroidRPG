package com.example.myapplication.model

class Inventory {
    // Инвентарь — список предметов (максимум 20 слотов)
    private val items: MutableList<Item?> = MutableList(20) { null }

    // Экипировка — карта слот → предмет
    private val equipment: MutableMap<EquipmentSlot, Item> = mutableMapOf()

    // Текущая выбранная ячейка
    var selectedSlot: Int = -1

    // Экипировать предмет
    fun equip(index: Int): Boolean {
        val item = items[index] ?: return false
        val slot = getSlotForItem(item)
        if (slot == null) return false

        // Если в слоте уже есть предмет — снимаем его в инвентарь
        val oldItem = equipment[slot]
        if (oldItem != null) {
            if (!addItem(oldItem)) return false // нет места
        }

        equipment[slot] = item
        items[index] = null
        return true
    }

    // Снять предмет с экипировки
    fun unequip(slot: EquipmentSlot): Item? {
        val item = equipment.remove(slot) ?: return null
        if (!addItem(item)) {
            // Если нет места — возвращаем обратно
            equipment[slot] = item
            return null
        }
        return item
    }

    // ⭐ ДОБАВЛЯЕМ ПРЕДМЕТ С УЧЁТОМ СТАКИВАНИЯ
    fun addItem(item: Item): Boolean {
        // ⭐ УБЕЖДАЕМСЯ, ЧТО RUNES НЕ NULL
        if (item.runes == null) {
            item.runes = mutableListOf()
        }

        if (item.isStackable()) {
            for (i in items.indices) {
                val existingItem = items[i]
                if (existingItem != null &&
                    existingItem.id == item.id &&
                    existingItem.isStackable() &&
                    existingItem.refineLevel == item.refineLevel) {
                    existingItem.addQuantity(item.quantity)
                    return true
                }
            }
        }

        val emptyIndex = items.indexOf(null)
        if (emptyIndex == -1) return false
        items[emptyIndex] = item
        return true
    }

    // ⭐ УДАЛЕНИЕ ОДНОЙ ЕДИНИЦЫ ИЗ СТАКА
    fun removeOneItem(index: Int): Item? {
        val item = items[index] ?: return null
        if (item.isStackable() && item.quantity > 1) {
            item.quantity--
            return item
        } else {
            items[index] = null
            return item
        }
    }


    // Получить слот для типа предмета
    private fun getSlotForItem(item: Item): EquipmentSlot? {
        return when (item.type) {
            Item.ItemType.WEAPON -> EquipmentSlot.WEAPON
            Item.ItemType.SHIELD -> EquipmentSlot.SHIELD
            Item.ItemType.HELMET -> EquipmentSlot.HELMET
            Item.ItemType.CHEST -> EquipmentSlot.CHEST
            Item.ItemType.PANTS -> EquipmentSlot.PANTS
            Item.ItemType.BOOTS -> EquipmentSlot.BOOTS
            Item.ItemType.GLOVES -> EquipmentSlot.GLOVES
            Item.ItemType.BRACERS -> EquipmentSlot.BRACERS
            Item.ItemType.RING -> {
                // Ищем свободное кольцо
                if (!equipment.containsKey(EquipmentSlot.RING1)) {
                    EquipmentSlot.RING1
                } else if (!equipment.containsKey(EquipmentSlot.RING2)) {
                    EquipmentSlot.RING2
                } else {
                    null
                }
            }
            Item.ItemType.NECKLACE -> EquipmentSlot.NECKLACE
            Item.ItemType.CONSUMABLE -> null // расходники не экипируются
        }
    }

    // Получить все предметы инвентаря
    fun getItems(): List<Item?> = items.toList()

    // Получить экипированный предмет в слоте
    fun getEquipment(slot: EquipmentSlot): Item? = equipment[slot]

    // Получить все экипированные предметы
    fun getAllEquipment(): Map<EquipmentSlot, Item> = equipment.toMap()

    // Получить суммарные статы экипировки
    fun getTotalStats(): ItemStats {
        var totalAttack = 0
        var totalDefense = 0
        var totalHealth = 0
        var totalDodge = 0
        var totalCrit = 0
        var totalCritDamage = 0
        var totalHpRegen = 0
        var totalMoveSpeed = 0
        var totalGoldBonus = 0
        var totalExpBonus = 0

        for ((_, item) in equipment) {
            val stats = item.getFinalStats()
            totalAttack += stats.attack
            totalDefense += stats.defense
            totalHealth += stats.health
            totalDodge += stats.dodge
            totalCrit += stats.crit
            totalCritDamage += stats.critDamage
            totalHpRegen += stats.hpRegen
            totalMoveSpeed += stats.moveSpeed
            totalGoldBonus += stats.goldBonus
            totalExpBonus += stats.expBonus
        }

        return ItemStats(
            attack = totalAttack,
            defense = totalDefense,
            health = totalHealth,
            dodge = totalDodge,
            crit = totalCrit,
            critDamage = totalCritDamage,
            hpRegen = totalHpRegen,
            moveSpeed = totalMoveSpeed,
            goldBonus = totalGoldBonus,
            expBonus = totalExpBonus
        )
    }

    // Проверить, занят ли слот
    fun isSlotEquipped(slot: EquipmentSlot): Boolean = equipment.containsKey(slot)

    // В классе Inventory добавь:
    fun setItems(newItems: List<Item?>) {
        for (i in 0 until minOf(items.size, newItems.size)) {
            items[i] = newItems[i]
        }
    }

    fun setEquipment(newEquipment: Map<EquipmentSlot, Item>) {
        equipment.clear()
        equipment.putAll(newEquipment)
    }

    fun removeItem(index: Int): Item? {
        val item = items[index] ?: return null
        items[index] = null
        return item
    }

    // ⭐ ВСТАВКА РУНЫ В ПРЕДМЕТ
    fun insertRune(itemIndex: Int, runeIndex: Int): Boolean {
        val item = items[itemIndex] ?: return false
        val runeItem = items[runeIndex] ?: return false

        // Проверяем, что руна действительно руна
        if (!runeItem.id.startsWith("rune_")) return false

        // Проверяем, что предмет может принять руну
        if (!item.canAddRune()) return false

        // Создаём руну из предмета
        val rune = Rune(
            id = runeItem.id,
            name = runeItem.name,
            description = runeItem.description,
            stats = runeItem.stats,
            rarity = runeItem.rarity
        )

        // Добавляем руну в предмет
        if (item.addRune(rune)) {
            // Удаляем руну из инвентаря
            items[runeIndex] = null
            return true
        }
        return false
    }

    // ⭐ ИЗВЛЕЧЕНИЕ РУНЫ ИЗ ПРЕДМЕТА
    fun extractRune(itemIndex: Int, runeIndex: Int): Rune? {
        val item = items[itemIndex] ?: return null
        return item.removeRune(runeIndex)
    }
}