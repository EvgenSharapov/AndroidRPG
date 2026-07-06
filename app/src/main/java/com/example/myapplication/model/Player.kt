package com.example.myapplication.model

data class Player(
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

    // Урон (зависит от силы)
    fun getDamage(): Float = 10f + strength * 2f

    // Шанс уворота (ловкость)
    fun getDodgeChance(): Float = 5f + agility * 2f

    // Шанс попадания (сноровка)
    fun getHitChance(): Float = 80f + dexterity * 2f

    // Шанс крита (удача)
    fun getCritChance(): Float = 5f + luck * 2f

    // Критический урон (удача)
    fun getCritDamage(): Float = 1.5f + luck * 0.1f

    // Проверка на получение нового уровня
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

    // Прокачка характеристики
    fun upgradeStat(stat: StatType): Boolean {
        if (skillPoints <= 0) return false

        when (stat) {
            StatType.STRENGTH -> strength++
            StatType.ENDURANCE -> {
                endurance++
                // Обновляем HP при прокачке выносливости
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