package com.example.myapplication.model

import android.graphics.Bitmap
import kotlin.math.min

enum class ItemRarity {
    COMMON,      // Обычный — белый
    UNCOMMON,    // Необычный — зелёный
    RARE,        // Редкий — синий
    EPIC,        // Эпический — фиолетовый
    LEGENDARY,   // Легендарный — оранжевый
    MYTHIC       // Мифический — золотой
}

data class Item(
    val id: String,
    val name: String,
    val type: ItemType,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val icon: Bitmap? = null,
    val description: String = "",
    val stats: ItemStats = ItemStats(),
    var refineLevel: Int = 0,
    var isRefined: Boolean = false,
    var quantity: Int = 1,
    var runes: MutableList<Rune> = mutableListOf()  // ⭐ СПИСОК РУН
) {
    enum class ItemType {
        WEAPON,      // Оружие
        HELMET,      // Шлем
        CHEST,       // Броня/Нагрудник
        PANTS,       // Поножи/Штаны
        BOOTS,       // Сапоги
        GLOVES,      // Перчатки
        BRACERS,     // Наручи
        SHIELD,      // Щит
        RING,        // Кольцо
        NECKLACE,    // Амулет
        CONSUMABLE   // Расходник
    }

    // ⭐ МАКСИМАЛЬНОЕ КОЛИЧЕСТВО СЛОТОВ ДЛЯ РУН В ЗАВИСИМОСТИ ОТ РЕДКОСТИ
    fun getMaxRuneSlots(): Int {
        return when (rarity) {
            ItemRarity.COMMON -> 0
            ItemRarity.UNCOMMON -> 0
            ItemRarity.RARE -> 1
            ItemRarity.EPIC -> 2
            ItemRarity.LEGENDARY -> 3
            ItemRarity.MYTHIC -> 4
        }
    }

    // ⭐ ПРОВЕРКА, МОЖНО ЛИ ВСТАВИТЬ РУНУ
    fun canAddRune(): Boolean {
        return runes.size < getMaxRuneSlots()
    }

    // ⭐ ДОБАВЛЕНИЕ РУНЫ
    fun addRune(rune: Rune): Boolean {
        if (!canAddRune()) return false
        runes.add(rune)
        return true
    }

    // ⭐ УДАЛЕНИЕ РУНЫ ПО ИНДЕКСУ
    fun removeRune(index: Int): Rune? {
        if (index < 0 || index >= runes.size) return null
        return runes.removeAt(index)
    }

    // ⭐ ПОЛУЧЕНИЕ ВСЕХ СТАТОВ ОТ РУН
    fun getRuneStats(): ItemStats {
        var attack = 0
        var defense = 0
        var health = 0
        var dodge = 0
        var crit = 0
        var critDamage = 0
        var hpRegen = 0
        var moveSpeed = 0
        var goldBonus = 0
        var expBonus = 0

        for (rune in runes) {
            attack += rune.stats.attack
            defense += rune.stats.defense
            health += rune.stats.health
            dodge += rune.stats.dodge
            crit += rune.stats.crit
            critDamage += rune.stats.critDamage
            hpRegen += rune.stats.hpRegen
            moveSpeed += rune.stats.moveSpeed
            goldBonus += rune.stats.goldBonus
            expBonus += rune.stats.expBonus
        }

        return ItemStats(
            attack = attack,
            defense = defense,
            health = health,
            dodge = dodge,
            crit = crit,
            critDamage = critDamage,
            hpRegen = hpRegen,
            moveSpeed = moveSpeed,
            goldBonus = goldBonus,
            expBonus = expBonus
        )
    }

    // ⭐ ВСЕГДА ЛИ ПРЕДМЕТ МОЖНО ЗАТОЧИТЬ?
    fun isRefinable(): Boolean {
        return when (type) {
            ItemType.WEAPON, ItemType.HELMET, ItemType.CHEST, ItemType.PANTS,
            ItemType.BOOTS, ItemType.GLOVES, ItemType.BRACERS, ItemType.SHIELD -> true
            else -> false
        }
    }

    // ⭐ ПОЛУЧАЕМ ФИНАЛЬНЫЙ УРОН (для оружия) С УЧЁТОМ РУН
    fun getFinalAttack(): Int {
        if (type != ItemType.WEAPON) return 0

        val baseAttack = stats.attack
        val bonusMultiplier = ItemStats.getRefineMultiplier(refineLevel)
        val runeBonus = getRuneStats().attack
        return (baseAttack * (1f + bonusMultiplier)).toInt() + runeBonus
    }

    // ⭐ ПОЛУЧАЕМ ФИНАЛЬНУЮ ЗАЩИТУ (для брони и щитов) С УЧЁТОМ РУН
    fun getFinalDefense(): Int {
        if (type == ItemType.WEAPON || type == ItemType.CONSUMABLE) return 0

        val baseDefense = stats.defense
        val bonusMultiplier = ItemStats.getRefineMultiplier(refineLevel)
        val runeBonus = getRuneStats().defense
        return (baseDefense * (1f + bonusMultiplier)).toInt() + runeBonus
    }

    // ⭐ ПОЛУЧАЕМ ФИНАЛЬНЫЕ СТАТЫ С УЧЁТОМ РУН
    fun getFinalStats(): ItemStats {
        val baseStats = when (type) {
            ItemType.WEAPON -> ItemStats(attack = stats.attack)
            ItemType.SHIELD, ItemType.HELMET, ItemType.CHEST, ItemType.PANTS,
            ItemType.BOOTS, ItemType.GLOVES, ItemType.BRACERS -> ItemStats(defense = stats.defense)
            else -> stats
        }

        val refineBonus = if (type == ItemType.WEAPON) {
            ItemStats(attack = (stats.attack * ItemStats.getRefineMultiplier(refineLevel)).toInt())
        } else if (type != ItemType.CONSUMABLE) {
            ItemStats(defense = (stats.defense * ItemStats.getRefineMultiplier(refineLevel)).toInt())
        } else {
            ItemStats()
        }

        val runeStats = getRuneStats()

        return ItemStats(
            attack = baseStats.attack + refineBonus.attack + runeStats.attack,
            defense = baseStats.defense + refineBonus.defense + runeStats.defense,
            health = baseStats.health + runeStats.health,
            dodge = baseStats.dodge + runeStats.dodge,
            crit = baseStats.crit + runeStats.crit,
            critDamage = baseStats.critDamage + runeStats.critDamage,
            hpRegen = baseStats.hpRegen + runeStats.hpRegen,
            moveSpeed = baseStats.moveSpeed + runeStats.moveSpeed,
            goldBonus = runeStats.goldBonus,
            expBonus = runeStats.expBonus
        )
    }

    fun getDisplayName(): String {
        return if (refineLevel > 0) {
            "$name +$refineLevel"
        } else {
            name
        }
    }

    fun getRarityColor(): Int {
        return when (rarity) {
            ItemRarity.COMMON -> android.graphics.Color.rgb(200, 200, 200)
            ItemRarity.UNCOMMON -> android.graphics.Color.rgb(50, 200, 50)
            ItemRarity.RARE -> android.graphics.Color.rgb(50, 150, 255)
            ItemRarity.EPIC -> android.graphics.Color.rgb(200, 100, 255)
            ItemRarity.LEGENDARY -> android.graphics.Color.rgb(255, 150, 50)
            ItemRarity.MYTHIC -> android.graphics.Color.rgb(255, 215, 0)
        }
    }

    fun getRarityName(): String {
        return when (rarity) {
            ItemRarity.COMMON -> "Обычный"
            ItemRarity.UNCOMMON -> "Необычный"
            ItemRarity.RARE -> "Редкий"
            ItemRarity.EPIC -> "Эпический"
            ItemRarity.LEGENDARY -> "Легендарный"
            ItemRarity.MYTHIC -> "Мифический"
        }
    }

    fun getRefineStars(): String {
        return when (refineLevel) {
            0 -> ""
            1 -> "⭐"
            2 -> "⭐⭐"
            3 -> "⭐⭐⭐"
            4 -> "⭐⭐⭐⭐"
            5 -> "⭐⭐⭐⭐⭐"
            6 -> "⭐⭐⭐⭐⭐⭐"
            7 -> "⭐⭐⭐⭐⭐⭐⭐"
            8 -> "⭐⭐⭐⭐⭐⭐⭐⭐"
            9 -> "⭐⭐⭐⭐⭐⭐⭐⭐⭐"
            10 -> "⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐"
            else -> ""
        }
    }

    fun isStackable(): Boolean {
        return type == ItemType.CONSUMABLE && refineLevel == 0 && runes.isEmpty()
    }

    fun addQuantity(amount: Int = 1): Item {
        this.quantity += amount
        return this
    }

    fun use(player: Player): Boolean {
        return when (id) {
            "cake_small" -> {
                player.hp = min(player.hp + 30f, player.calculateMaxHp())
                true
            }
            "cake_medium" -> {
                player.hp = min(player.hp + 60f, player.calculateMaxHp())
                true
            }
            "cake_large" -> {
                player.hp = min(player.hp + 120f, player.calculateMaxHp())
                true
            }
            else -> false
        }
    }
}

data class ItemStats(
    val attack: Int = 0,
    val defense: Int = 0,
    val health: Int = 0,
    val dodge: Int = 0,
    val crit: Int = 0,
    val critDamage: Int = 0,
    val hpRegen: Int = 0,
    val moveSpeed: Int = 0,
    val goldBonus: Int = 0,
    val expBonus: Int = 0
) {
    companion object {
        private val REFINE_BONUSES = mapOf(
            0 to 0f,
            1 to 0.05f,
            2 to 0.07f,
            3 to 0.10f,
            4 to 0.14f,
            5 to 0.20f,
            6 to 0.28f,
            7 to 0.40f,
            8 to 0.56f,
            9 to 0.80f,
            10 to 1.12f
        )

        fun getRefineMultiplier(refineLevel: Int): Float {
            return REFINE_BONUSES[refineLevel] ?: 0f
        }
    }
}
