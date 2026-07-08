package com.example.myapplication.model

import kotlin.random.Random

data class LocationConfig(
    val name: String,
    val mobTemplates: List<MobTemplate>,
    val npcs: List<NPCData> = emptyList(),
    val transitionText: Map<String, String> = emptyMap()
) {
    fun generateMobs(): List<Mob> {
        return mobTemplates.flatMap { template ->
            val xRange = template.positionRange.first
            val yRange = template.positionRange.second

            // Разбиваем область на ячейки для равномерного распределения
            val cols = kotlin.math.ceil(kotlin.math.sqrt(template.count.toFloat())).toInt()
            val rows = kotlin.math.ceil(template.count.toFloat() / cols).toInt()

            val cellWidth = (xRange.endInclusive - xRange.start) / cols
            val cellHeight = (yRange.endInclusive - yRange.start) / rows

            (0 until template.count).map { index ->
                val col = index % cols
                val row = index / rows

                // Случайное смещение внутри ячейки
                val offsetX = kotlin.random.Random.nextFloat() * cellWidth * 0.6f
                val offsetY = kotlin.random.Random.nextFloat() * cellHeight * 0.6f

                val x = xRange.start + col * cellWidth + cellWidth * 0.2f + offsetX
                val y = yRange.start + row * cellHeight + cellHeight * 0.2f + offsetY

                val level = if (template.maxLevel > template.minLevel) {
                    kotlin.random.Random.nextInt(template.minLevel, template.maxLevel + 1)
                } else {
                    template.minLevel
                }

                val hpMultiplier = 1f + (level - template.minLevel) * 0.15f
                val hp = template.baseHp * hpMultiplier
                val attackBonus = (level - template.minLevel) * 2
                val attack = template.baseAttack + attackBonus
                val defenseBonus = (level - template.minLevel) * 1
                val defense = template.baseDefense + defenseBonus

                Mob(
                    // ⭐ УБИРАЕМ ОГРАНИЧЕНИЕ ДО 750f, ИСПОЛЬЗУЕМ ВЕСЬ МИР
                    x = x.coerceIn(50f, 1150f),
                    y = y.coerceIn(50f, 1950f),  // ← 2000 - 50 = 1950
                    type = template.type,
                    hp = hp,
                    maxHp = hp,
                    level = level,
                    attack = attack,
                    defense = defense,
                    isBoss = false,
                    isDead = false
                ).apply {
                    startX = this.x
                    startY = this.y
                }
            }
        }
    }
}

// ⭐ MobTemplate и NPCData вынесены за пределы LocationConfig
data class MobTemplate(
    val type: Int,
    val count: Int,
    val minLevel: Int,
    val maxLevel: Int,
    val baseHp: Float,
    val baseAttack: Int,
    val baseDefense: Int,
    val positionRange: Pair<ClosedFloatingPointRange<Float>, ClosedFloatingPointRange<Float>>
)

data class NPCData(
    val name: String,
    val x: Float,
    val y: Float,
    val dialog: String
)