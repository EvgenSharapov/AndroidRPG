package com.example.myapplication.manager

import com.example.myapplication.model.*
import kotlin.random.Random

/**
 * Менеджер дропа — управляет всеми дроп-таблицами для мобов
 */
class DropManager {

    private val dropConfigs = mutableListOf<MobDrop>()
    private val random = Random

    init {
        initDrops()
    }

    private fun initDrops() {
        // ============================================
        // 1. ФЛАФФИ (тип 0)
        // ============================================

        // Уровень 1-2
        dropConfigs.add(
            MobDrop(
                mobType = 0,
                minLevel = 1,
                maxLevel = 2,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_rusty",
                        itemName = "Ржавый меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.COMMON,
                        chance = 15.0,
                        stats = ItemStats(attack = 3),
                        description = "Старый ржавый меч"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_small",
                        itemName = "Маленький торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.COMMON,
                        chance = 25.0,
                        description = "Восстанавливает 30 HP 🎂"
                    )
                ),
                goldMin = 1,
                goldMax = 4,
                goldChance = 80
            )
        )

        // Уровень 3-4
        dropConfigs.add(
            MobDrop(
                mobType = 0,
                minLevel = 3,
                maxLevel = 4,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_iron",
                        itemName = "Железный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 10.0,
                        stats = ItemStats(attack = 7, strength = 1),
                        description = "Прочный железный меч"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_medium",
                        itemName = "Средний торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 20.0,
                        description = "Восстанавливает 60 HP 🍰"
                    )
                ),
                goldMin = 3,
                goldMax = 7,
                goldChance = 85
            )
        )

        // Уровень 5-6 (редкий дроп)
        dropConfigs.add(
            MobDrop(
                mobType = 0,
                minLevel = 5,
                maxLevel = 6,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_steel",
                        itemName = "Стальной меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.RARE,
                        chance = 5.0,
                        stats = ItemStats(attack = 12, strength = 2),
                        description = "Качественный стальной меч"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_flame",
                        itemName = "Пламенный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 1.0,
                        stats = ItemStats(attack = 18, strength = 3, agility = 1),
                        description = "Меч, пылающий огнём"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 20.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 6,
                goldMax = 12,
                goldChance = 90
            )
        )

        // ============================================
        // 2. ПАУК (тип 1)
        // ============================================

        // Уровень 1-2
        dropConfigs.add(
            MobDrop(
                mobType = 1,
                minLevel = 1,
                maxLevel = 2,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "dagger_bone",
                        itemName = "Костяной кинжал",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.COMMON,
                        chance = 12.0,
                        stats = ItemStats(attack = 4, agility = 1),
                        description = "Кинжал из кости паука"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_small",
                        itemName = "Маленький торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.COMMON,
                        chance = 25.0,
                        description = "Восстанавливает 30 HP 🎂"
                    )
                ),
                goldMin = 2,
                goldMax = 5,
                goldChance = 75
            )
        )

        // Уровень 3-4
        dropConfigs.add(
            MobDrop(
                mobType = 1,
                minLevel = 3,
                maxLevel = 4,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_venom",
                        itemName = "Ядовитый клинок",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 8.0,
                        stats = ItemStats(attack = 9, agility = 2),
                        description = "Клинок, пропитанный ядом"
                    ),
                    DropTable.DropEntry(
                        itemId = "crusader_shield",
                        itemName = "Щит крестоносца",
                        itemType = Item.ItemType.SHIELD,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 15.0,
                        stats = ItemStats(defense = 10, strength = 1),
                        description = "Прочный щит с крестом"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_medium",
                        itemName = "Средний торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 20.0,
                        description = "Восстанавливает 60 HP 🍰"
                    )
                ),
                goldMin = 5,
                goldMax = 10,
                goldChance = 80
            )
        )

        // Уровень 5-6 (редкий дроп)
        dropConfigs.add(
            MobDrop(
                mobType = 1,
                minLevel = 5,
                maxLevel = 6,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_arachnid",
                        itemName = "Паучий меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.RARE,
                        chance = 3.0,
                        stats = ItemStats(attack = 14, agility = 3, luck = 1),
                        description = "Меч, сплетённый из паутины"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_venomous",
                        itemName = "Меч смертельного яда",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 0.5,
                        stats = ItemStats(attack = 22, agility = 4, strength = 1),
                        description = "Меч с сильнейшим ядом"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 20.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 8,
                goldMax = 15,
                goldChance = 85
            )
        )

        // ============================================
        // 3. МНОГОГЛАЗ (тип 2)
        // ============================================

        // Уровень 1-2
        dropConfigs.add(
            MobDrop(
                mobType = 2,
                minLevel = 1,
                maxLevel = 2,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "axe_wooden",
                        itemName = "Деревянный топор",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.COMMON,
                        chance = 10.0,
                        stats = ItemStats(attack = 6, strength = 2),
                        description = "Тяжёлый деревянный топор"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_small",
                        itemName = "Маленький торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.COMMON,
                        chance = 25.0,
                        description = "Восстанавливает 30 HP 🎂"
                    )
                ),
                goldMin = 3,
                goldMax = 6,
                goldChance = 70
            )
        )

        // Уровень 3-4
        dropConfigs.add(
            MobDrop(
                mobType = 2,
                minLevel = 3,
                maxLevel = 4,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "axe_iron",
                        itemName = "Железный топор",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 8.0,
                        stats = ItemStats(attack = 11, strength = 3),
                        description = "Тяжёлый железный топор"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_moonlight",
                        itemName = "Лунный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.RARE,
                        chance = 2.0,
                        stats = ItemStats(attack = 16, luck = 3, agility = 1),
                        description = "Меч, светящийся в лунном свете"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_medium",
                        itemName = "Средний торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 20.0,
                        description = "Восстанавливает 60 HP 🍰"
                    )
                ),
                goldMin = 6,
                goldMax = 12,
                goldChance = 80
            )
        )

        // Уровень 5-6 (редкий дроп)
        dropConfigs.add(
            MobDrop(
                mobType = 2,
                minLevel = 5,
                maxLevel = 6,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "axe_battle",
                        itemName = "Боевой топор",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.RARE,
                        chance = 4.0,
                        stats = ItemStats(attack = 17, strength = 4, agility = 2),
                        description = "Мощный боевой топор"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_legendary",
                        itemName = "Легендарный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 0.3,
                        stats = ItemStats(attack = 28, strength = 5, agility = 3, luck = 2),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 20.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 10,
                goldMax = 20,
                goldChance = 90
            )
        )

        // ============================================
// ФЛАФФИ (тип 0) — УРОВЕНЬ 7+
// ============================================
        dropConfigs.add(
            MobDrop(
                mobType = 0,
                minLevel = 7,
                maxLevel = 9,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_steel",
                        itemName = "Стальной меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.RARE,
                        chance = 15.0,
                        stats = ItemStats(attack = 12, strength = 2),
                        description = "Качественный стальной меч"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_flame",
                        itemName = "Пламенный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 5.0,
                        stats = ItemStats(attack = 18, strength = 3, agility = 1),
                        description = "Меч, пылающий огнём"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 10,
                goldMax = 20,
                goldChance = 95
            )
        )

// ============================================
// ПАУК (тип 1) — УРОВЕНЬ 7+
// ============================================
        dropConfigs.add(
            MobDrop(
                mobType = 1,
                minLevel = 7,
                maxLevel = 9,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_arachnid",
                        itemName = "Паучий меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.RARE,
                        chance = 10.0,
                        stats = ItemStats(attack = 14, agility = 3, luck = 1),
                        description = "Меч, сплетённый из паутины"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_venomous",
                        itemName = "Меч смертельного яда",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 3.0,
                        stats = ItemStats(attack = 22, agility = 4, strength = 1),
                        description = "Меч с сильнейшим ядом"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 12,
                goldMax = 25,
                goldChance = 95
            )
        )

// ============================================
// МНОГОГЛАЗ (тип 2) — УРОВЕНЬ 7+
// ============================================
        dropConfigs.add(
            MobDrop(
                mobType = 2,
                minLevel = 7,
                maxLevel = 9,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "axe_battle",
                        itemName = "Боевой топор",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.RARE,
                        chance = 12.0,
                        stats = ItemStats(attack = 17, strength = 4, agility = 2),
                        description = "Мощный боевой топор"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_legendary",
                        itemName = "Легендарный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 1.5,  // 1.5% (было 0.3, но для 7 уровня повышаем)
                        stats = ItemStats(attack = 28, strength = 5, agility = 3, luck = 2),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 15,
                goldMax = 30,
                goldChance = 95
            )
        )

        // ============================================
// КРАСНЫЙ РЫЦАРЬ (тип 3) — УРОВЕНЬ 8-10
// ============================================
        dropConfigs.add(
            MobDrop(
                mobType = 3,
                minLevel = 8,
                maxLevel = 10,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_flame",
                        itemName = "Пламенный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 8.0,
                        stats = ItemStats(attack = 18, strength = 3, agility = 1),
                        description = "Меч, пылающий огнём"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_legendary",
                        itemName = "Легендарный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 2.0,
                        stats = ItemStats(attack = 28, strength = 5, agility = 3, luck = 2),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 30.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 20,
                goldMax = 40,
                goldChance = 95
            )
        )

// ============================================
// ЗЕЛЁНЫЙ СЛИЗЕНЬ (тип 4) — УРОВЕНЬ 8-10
// ============================================
        dropConfigs.add(
            MobDrop(
                mobType = 4,
                minLevel = 8,
                maxLevel = 10,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_venomous",
                        itemName = "Меч смертельного яда",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 5.0,
                        stats = ItemStats(attack = 22, agility = 4, strength = 1),
                        description = "Меч с сильнейшим ядом"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 35.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 15,
                goldMax = 30,
                goldChance = 90
            )
        )

// ============================================
// СТАЛЬНОЙ РЫЦАРЬ (тип 5) — УРОВЕНЬ 9-11
// ============================================
        dropConfigs.add(
            MobDrop(
                mobType = 5,
                minLevel = 9,
                maxLevel = 11,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_legendary",
                        itemName = "Легендарный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 4.0,
                        stats = ItemStats(attack = 28, strength = 5, agility = 3, luck = 2),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_mythic",
                        itemName = "Мифический клинок",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.MYTHIC,
                        chance = 0.5,  // 0.5% — очень редкий
                        stats = ItemStats(attack = 35, strength = 7, agility = 5, luck = 3),
                        description = "Оружие богов"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 30.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 30,
                goldMax = 50,
                goldChance = 95
            )
        )

// ГОБЛИН (тип 6) — УРОВЕНЬ 12-13

        dropConfigs.add(
            MobDrop(
                mobType = 6,
                minLevel = 12,
                maxLevel = 13,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_legendary",
                        itemName = "Легендарный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 5.0,
                        stats = ItemStats(attack = 28, strength = 5, agility = 3, luck = 2),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_mythic",
                        itemName = "Мифический клинок",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.MYTHIC,
                        chance = 1.0,
                        stats = ItemStats(attack = 35, strength = 7, agility = 5, luck = 3),
                        description = "Оружие богов"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 30.0,
                        description = "Восстанавливает 120 HP 🎂"
                    )
                ),
                goldMin = 40,
                goldMax = 70,
                goldChance = 95
            )
        )
    }



    fun getDropForMob(mob: Mob): Pair<List<Item>, Int> {
        // Ищем конфигурацию дропа
        for (config in dropConfigs) {
            if (config.matches(mob)) {
                val (items, gold) = config.getDrop(random)

                // ⭐ ЕСЛИ ЭТО БОСС — УВЕЛИЧИВАЕМ ДРОП
                if (mob.isBoss) {
                    val boostedItems = mutableListOf<Item>()
                    val multiplier = 3  // Увеличиваем количество предметов в 3 раза

                    // Удваиваем предметы
                    for (item in items) {
                        // Повторяем предмет несколько раз
                        for (i in 0 until multiplier) {
                            val boostedItem = item.copy(
                                id = "${item.id}_${i}",
                                name = if (i > 0) "${item.name} (${i+1})" else item.name
                            )
                            boostedItems.add(boostedItem)
                        }

                        // Добавляем редкие предметы с повышенным шансом
                        // (они уже есть в дроп-таблице, просто увеличиваем количество)
                    }

                    // Увеличиваем золото в 3-5 раз
                    val boostedGold = gold * (3 + random.nextInt(3))  // 3-5 раз

                    println("👑 Босс дроп: ${boostedItems.size} предметов, ${boostedGold} золота")
                    return Pair(boostedItems, boostedGold)
                }

                return Pair(items, gold)
            }
        }

        val defaultGold = random.nextInt(1, 4)
        return Pair(emptyList(), defaultGold)
    }

    fun addDropConfig(config: MobDrop) {
        dropConfigs.add(config)
    }
}
