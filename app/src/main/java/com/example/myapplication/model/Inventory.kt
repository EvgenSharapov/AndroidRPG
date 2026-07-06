package com.example.myapplication.model

class Inventory {
    // Инвентарь — список предметов (максимум 20 слотов)
    private val items: MutableList<Item?> = MutableList(20) { null }

    // Экипировка — карта слот → предмет
    private val equipment: MutableMap<EquipmentSlot, Item> = mutableMapOf()

    // Текущая выбранная ячейка
    var selectedSlot: Int = -1

    // Добавить предмет в инвентарь
    fun addItem(item: Item): Boolean {
        val emptyIndex = items.indexOf(null)
        if (emptyIndex == -1) return false // Инвентарь полон
        items[emptyIndex] = item
        return true
    }

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
        var attack = 0
        var defense = 0
        var health = 0
        var agility = 0
        var strength = 0
        var luck = 0

        for ((_, item) in equipment) {
            attack += item.stats.attack
            defense += item.stats.defense
            health += item.stats.health
            agility += item.stats.agility
            strength += item.stats.strength
            luck += item.stats.luck
        }

        return ItemStats(attack, defense, health, agility, strength, luck)
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
        if (index < 0 || index >= items.size) return null
        val item = items[index]
        items[index] = null
        return item
    }
}