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

    var strength: Int = 5,
    var endurance: Int = 5,
    var agility: Int = 5,
    var dexterity: Int = 5,
    var luck: Int = 5
) {

    companion object {
        const val BASE_SPEED = 5f
        const val BASE_DODGE = 5f
        const val BASE_CRIT = 5f
        const val BASE_CRIT_DAMAGE = 1.5f
        const val BASE_HIT_CHANCE = 80f
    }

    // ⭐ БАЗОВОЕ МАКСИМАЛЬНОЕ HP (без учета рун)
    fun calculateBaseMaxHp(): Float = 100f + endurance * 5f

    // ⭐ ДЛЯ СОВМЕСТИМОСТИ со старым кодом (используется в BattleManager)
    fun calculateMaxHp(): Float = calculateBaseMaxHp()

    // ⭐ РАСЧЕТ МАКСИМАЛЬНОГО HP (с учетом рун)
    fun getMaxHp(inventory: Inventory): Float {
        val baseMaxHp = calculateBaseMaxHp()
        val totalStats = inventory.getTotalStats()
        return baseMaxHp + totalStats.health
    }

    // ⭐ РАСЧЕТ УРОНА (с учетом рун)
    fun getDamage(inventory: Inventory): Int {
        var baseDamage = 10f + strength * 2f

        val weapon = inventory.getEquipment(EquipmentSlot.WEAPON)
        if (weapon != null) {
            baseDamage += weapon.getFinalAttack()
        }

        val totalStats = inventory.getTotalStats()
        baseDamage += totalStats.attack

        return baseDamage.toInt()
    }

    // ⭐ РАСЧЕТ ЗАЩИТЫ (с учетом рун)
    fun getDefense(inventory: Inventory): Int {
        var totalDefense = 0

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

        val totalStats = inventory.getTotalStats()
        totalDefense += totalStats.defense

        return totalDefense
    }

    // ⭐ РАСЧЕТ РЕГЕНЕРАЦИИ HP (с учетом рун)
    fun getHpRegen(inventory: Inventory): Float {
        val baseRegen = 1f + endurance * 0.05f
        val totalStats = inventory.getTotalStats()
        return baseRegen + totalStats.hpRegen
    }

    // ⭐ РАСЧЕТ УВОРОТА (с учетом рун)
    fun getDodgeChance(inventory: Inventory): Float {
        val baseDodge = 5f + agility * 2f
        val totalStats = inventory.getTotalStats()
        return baseDodge + totalStats.dodge
    }

    // ⭐ РАСЧЕТ ТОЧНОСТИ (с учетом рун)
    fun getHitChance(inventory: Inventory): Float {
        val baseHit = BASE_HIT_CHANCE + dexterity * 2f
        // Точность не имеет прямых рун, но может быть добавлена позже
        return baseHit
    }

    // ⭐ РАСЧЕТ ШАНСА КРИТА (с учетом рун)
    fun getCritChance(inventory: Inventory): Float {
        val baseCrit = 5f + luck * 2f
        val totalStats = inventory.getTotalStats()
        return baseCrit + totalStats.crit
    }

    // ⭐ РАСЧЕТ УРОНА КРИТА (с учетом рун)
    fun getCritDamage(inventory: Inventory): Float {
        val baseCritDamage = 1.5f + luck * 0.1f
        val totalStats = inventory.getTotalStats()
        return baseCritDamage + totalStats.critDamage / 100f
    }

    // ⭐ РАСЧЕТ СКОРОСТИ (с учетом рун)
    fun getSpeed(inventory: Inventory): Float {
        val baseSpeed = BASE_SPEED
        val totalStats = inventory.getTotalStats()
        return baseSpeed + totalStats.moveSpeed / 10f
    }

    // ⭐ РАСЧЕТ БОНУСА К ЗОЛОТУ (с учетом рун)
    fun getGoldBonus(inventory: Inventory): Int {
        val totalStats = inventory.getTotalStats()
        return totalStats.goldBonus
    }

    // ⭐ РАСЧЕТ БОНУСА К ОПЫТУ (с учетом рун)
    fun getExpBonus(inventory: Inventory): Int {
        val totalStats = inventory.getTotalStats()
        return totalStats.expBonus
    }

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
                hp = calculateBaseMaxHp()
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
