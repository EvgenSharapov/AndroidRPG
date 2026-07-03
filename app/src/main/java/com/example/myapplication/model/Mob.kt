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
    var startY: Float = y       // ← НАЧАЛЬНАЯ ПОЗИЦИЯ (для респауна)
) {
    fun getTypeName(): String = when (type) {
        0 -> "Флаффи"
        1 -> "Паук"
        2 -> "Многоглаз"
        else -> "Моб"
    }

    fun getColor(): Int = when (type) {
        0 -> android.graphics.Color.rgb(255, 200, 200)
        1 -> android.graphics.Color.rgb(100, 150, 50)
        2 -> android.graphics.Color.rgb(150, 50, 200)
        else -> android.graphics.Color.GRAY
    }

    fun getExpReward(): Int = level * 10

    fun respawn() {
        isDead = false
        hp = maxHp
        x = startX
        y = startY
        deathTimer = 0
        respawnTimer = 0
    }
}