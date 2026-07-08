package com.example.myapplication.model

data class Player(
    var name: String = "Герой",
    var x: Float = 400f,
    var y: Float = 400f,
    var targetX: Float = 400f,
    var targetY: Float = 400f,
    var speed: Float = 5f,
    var isMoving: Boolean = false,
    var attackCooldown: Int = 0,
    var facing: Int = 0,
    var hp: Float = 100f,
    var level: Int = 1,
    var exp: Int = 0,
    var maxExp: Int = 50,
    var skillPoints: Int = 0,
    var gold: Int = 0,

    // ===== ХАРАКТЕРИСТИКИ =====
    var strength: Int = 5,
    var endurance: Int = 5,
    var agility: Int = 5,
    var dexterity: Int = 5,
    var luck: Int = 5
) {
    // ===== ПРОИЗВОДНЫЕ ХАРАКТЕРИСТИКИ =====

    // Максимальное HP (зависит от выносливости)
    // ⭐ Переименовал, чтобы не конфликтовать с полем maxHp
    fun calculateMaxHp(): Float = 100f + endurance * 5

    // ⭐ РАСЧЁТ УРОНА (с учётом оружия и характеристик)
    fun getDamage(inventory: Inventory): Int {
        var baseDamage = 10f + strength * 2f

        // Добавляем урон от оружия
        val weapon = inventory.getEquipment(EquipmentSlot.WEAPON)
        if (weapon != null) {
            baseDamage += weapon.getFinalAttack()
        }

        return baseDamage.toInt()
    }

    // ⭐ РАСЧЁТ ЗАЩИТЫ (с учётом брони и щитов)
    fun getDefense(inventory: Inventory): Int {
        var totalDefense = 0

        // Суммируем защиту со всех предметов брони
        val armorSlots = listOf(
            EquipmentSlot.HELMET,
            EquipmentSlot.CHEST,
            EquipmentSlot.PANTS,
            EquipmentSlot.BOOTS,
            EquipmentSlot.GLOVES,
            EquipmentSlot.BRACERS,
            EquipmentSlot.SHIELD
        )

        for (slot in armorSlots) {
            val item = inventory.getEquipment(slot)
            if (item != null) {
                totalDefense += item.getFinalDefense()
            }
        }

        return totalDefense
    }

    // ⭐ РАСЧЁТ ДОПОЛНИТЕЛЬНЫХ СТАТОВ (аксессуары)
    fun getBonusStats(inventory: Inventory): ItemStats {
        var bonusHealth = 0
        var bonusDodge = 0
        var bonusCrit = 0
        var bonusCritDamage = 0
        var bonusHpRegen = 0
        var bonusMoveSpeed = 0

        val accessorySlots = listOf(
            EquipmentSlot.RING1,
            EquipmentSlot.RING2,
            EquipmentSlot.NECKLACE
        )

        for (slot in accessorySlots) {
            val item = inventory.getEquipment(slot)
            if (item != null) {
                bonusHealth += item.stats.health
                bonusDodge += item.stats.dodge
                bonusCrit += item.stats.crit
                bonusCritDamage += item.stats.critDamage
                bonusHpRegen += item.stats.hpRegen
                bonusMoveSpeed += item.stats.moveSpeed
            }
        }

        return ItemStats(
            health = bonusHealth,
            dodge = bonusDodge,
            crit = bonusCrit,
            critDamage = bonusCritDamage,
            hpRegen = bonusHpRegen,
            moveSpeed = bonusMoveSpeed
        )
    }

    fun getDodgeChance(): Float = 5f + agility * 2f

    fun getHitChance(): Float = 80f + dexterity * 2f

    fun getCritChance(): Float = 5f + luck * 2f

    fun getCritDamage(): Float = 1.5f + luck * 0.1f

    fun checkLevelUp(): Boolean {
        if (exp >= maxExp) {
            exp -= maxExp
            level++
            maxExp = (maxExp * 1.5f).toInt()
            skillPoints += 5
            return true
        }
        return false
    }

    fun upgradeStat(statType: StatType): Boolean {
        if (skillPoints <= 0) return false

        when (statType) {
            StatType.STRENGTH -> strength++
            StatType.ENDURANCE -> {
                endurance++
                hp = calculateMaxHp()
            }
            StatType.AGILITY -> agility++
            StatType.DEXTERITY -> dexterity++
            StatType.LUCK -> luck++
        }
        skillPoints--
        return true
    }

    enum class StatType {
        STRENGTH,
        ENDURANCE,
        AGILITY,
        DEXTERITY,
        LUCK
    }
}