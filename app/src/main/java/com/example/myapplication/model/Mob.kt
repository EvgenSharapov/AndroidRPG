package com.example.myapplication.model

data class Mob(
    var x: Float,
    var y: Float,
    var type: Int,
    var hp: Float,
    var maxHp: Float,
    var isDead: Boolean = false,
    var attackTimer: Int = 0,
    var deathTimer: Int = 0,
    var level: Int = 1,
    var respawnTimer: Int = 0,  // ← ТАЙМЕР РЕСПАУНА
    var startX: Float = x,      // ← НАЧАЛЬНАЯ ПОЗИЦИЯ (для респауна)
    var startY: Float = y,     // ← НАЧАЛЬНАЯ ПОЗИЦИЯ (для респауна)
    var isBoss: Boolean = false,
    var attack: Int = 5,      // ← ДОБАВЛЯЕМ БАЗОВУЮ АТАКУ
    var defense: Int = 0      // ← ДОБАВЛЯЕМ БАЗОВУЮ ЗАЩИТУ
) {
    fun getTypeName(): String = when (type) {
        0 -> if (isBoss) "👑 Флаффи-босс" else "Флаффи"
        1 -> if (isBoss) "👑 Паук-босс" else "Паук"
        2 -> if (isBoss) "👑 Многоглаз-босс" else "Многоглаз"
        3 -> if (isBoss) "👑 Красный рыцарь" else "Красный рыцарь"
        4 -> if (isBoss) "👑 Зелёный слизень" else "Зелёный слизень"
        5 -> if (isBoss) "👑 Стальной рыцарь" else "Стальной рыцарь"
        6 -> if (isBoss) "👑 Гоблин-босс" else "Гоблин"
        7 -> if (isBoss) "👑 Монах-босс" else "Монах"
        8 -> if (isBoss) "👑 Ящер-босс" else "Ящер"
        9 -> if (isBoss) "👑 Тролль-босс" else "Тролль"
        else -> "Моб"
    }

    fun getColor(): Int = when (type) {
        0 -> android.graphics.Color.rgb(255, 200, 200)
        1 -> android.graphics.Color.rgb(100, 150, 50)
        2 -> android.graphics.Color.rgb(150, 50, 200)
        else -> android.graphics.Color.GRAY
    }

    fun getExpReward(): Int {
        val base = level * 10
        return if (isBoss) (base * 5).toInt() else base
    }

    fun getSizeMultiplier(): Float = if (isBoss) 3.5f else 1f  // Босс в 3.5 раза больше

    fun respawn() {
        isDead = false
        hp = maxHp
        x = startX
        y = startY
        deathTimer = 0
        respawnTimer = 0
        isBoss = false
    }
}
