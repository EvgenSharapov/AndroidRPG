package com.example.myapplication.manager

import com.example.myapplication.model.*
import kotlin.random.Random

/**
 * Менеджер дропа — управляет всеми дроп-таблицами для мобов
 *
 * Баланс предметов:
 * - Оружие (WEAPON): только attack
 * - Броня (HELMET, CHEST, PANTS, BOOTS, GLOVES, BRACERS): только defense
 * - Щиты (SHIELD): defense
 * - Аксессуары (RING, NECKLACE, WINGS): health + специальные статы
 *
 * Редкость → Статы:
 * COMMON:    attack 3-5,   defense 2-3
 * UNCOMMON:  attack 8-12,  defense 4-6
 * RARE:      attack 16-22, defense 8-12
 * EPIC:      attack 26-34, defense 14-20
 * LEGENDARY: attack 38-48, defense 24-32
 * MYTHIC:    attack 52-65, defense 36-45
 */
class DropManager {

    private val dropConfigs = mutableListOf<MobDrop>()
    private val random = Random

    init {
        initDrops()
    }

    private fun initDrops() {
        // ============================================
        // 1. ФЛАФФИ (тип 0) — уровни 1-9
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
                        stats = ItemStats(attack = 5),
                        description = "Старый ржавый меч"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_small",
                        itemName = "Маленький торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.COMMON,
                        chance = 25.0,
                        description = "Восстанавливает 30 HP 🎂"
                    ),
                    // ⭐ ОРИДИКОН (редкий дроп)
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 3.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
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
                        stats = ItemStats(attack = 12),
                        description = "Прочный железный меч"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_medium",
                        itemName = "Средний торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 20.0,
                        description = "Восстанавливает 60 HP 🍰"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 5.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 5.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 3,
                goldMax = 7,
                goldChance = 85
            )
        )

        // Уровень 5-6
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
                        stats = ItemStats(attack = 20),
                        description = "Качественный стальной меч"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 20.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 8.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 8.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 6,
                goldMax = 12,
                goldChance = 90
            )
        )

        // Уровень 7-9
        dropConfigs.add(
            MobDrop(
                mobType = 0,
                minLevel = 7,
                maxLevel = 9,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_flame",
                        itemName = "Пламенный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 5.0,
                        stats = ItemStats(attack = 30),
                        description = "Меч, пылающий огнём"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 12.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 12.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 10,
                goldMax = 20,
                goldChance = 95
            )
        )

        // ============================================
        // 2. ПАУК (тип 1) — уровни 1-9
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
                        stats = ItemStats(attack = 6),
                        description = "Кинжал из кости паука"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_small",
                        itemName = "Маленький торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.COMMON,
                        chance = 25.0,
                        description = "Восстанавливает 30 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 3.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
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
                        stats = ItemStats(attack = 14),
                        description = "Клинок, пропитанный ядом"
                    ),
                    DropTable.DropEntry(
                        itemId = "crusader_shield",
                        itemName = "Щит крестоносца",
                        itemType = Item.ItemType.SHIELD,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 15.0,
                        stats = ItemStats(defense = 8),
                        description = "Прочный щит с крестом"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_medium",
                        itemName = "Средний торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 20.0,
                        description = "Восстанавливает 60 HP 🍰"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 5.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 5.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 5,
                goldMax = 10,
                goldChance = 80
            )
        )

        // Уровень 5-6
        dropConfigs.add(
            MobDrop(
                mobType = 1,
                minLevel = 5,
                maxLevel = 6,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_venomous",
                        itemName = "Меч смертельного яда",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 0.5,
                        stats = ItemStats(attack = 28),
                        description = "Меч с сильнейшим ядом"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 20.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 8.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 8.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 8,
                goldMax = 15,
                goldChance = 85
            )
        )

        // Уровень 7-9
        dropConfigs.add(
            MobDrop(
                mobType = 1,
                minLevel = 7,
                maxLevel = 9,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "sword_venomous",
                        itemName = "Меч смертельного яда",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 3.0,
                        stats = ItemStats(attack = 32),
                        description = "Меч с сильнейшим ядом"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 12.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 12.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 12,
                goldMax = 25,
                goldChance = 95
            )
        )

        // ============================================
        // 3. МНОГОГЛАЗ (тип 2) — уровни 1-9
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
                        stats = ItemStats(attack = 8),
                        description = "Тяжёлый деревянный топор"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_small",
                        itemName = "Маленький торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.COMMON,
                        chance = 25.0,
                        description = "Восстанавливает 30 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 3.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
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
                        stats = ItemStats(attack = 16),
                        description = "Тяжёлый железный топор"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_medium",
                        itemName = "Средний торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 20.0,
                        description = "Восстанавливает 60 HP 🍰"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 5.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 5.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 6,
                goldMax = 12,
                goldChance = 80
            )
        )

        // Уровень 5-6
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
                        stats = ItemStats(attack = 22),
                        description = "Мощный боевой топор"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_legendary",
                        itemName = "Легендарный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 0.3,
                        stats = ItemStats(attack = 40),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 20.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 8.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 8.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 10,
                goldMax = 20,
                goldChance = 90
            )
        )

        // Уровень 7-9
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
                        stats = ItemStats(attack = 24),
                        description = "Мощный боевой топор"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_legendary",
                        itemName = "Легендарный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 1.5,
                        stats = ItemStats(attack = 42),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 12.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 12.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 15,
                goldMax = 30,
                goldChance = 95
            )
        )

        // ============================================
        // 4. КРАСНЫЙ РЫЦАРЬ (тип 3) — УРОВЕНЬ 8-10
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
                        stats = ItemStats(attack = 32),
                        description = "Меч, пылающий огнём"
                    ),
                    DropTable.DropEntry(
                        itemId = "helmet_1",
                        itemName = "Шлем рыцаря",
                        itemType = Item.ItemType.HELMET,
                        rarity = ItemRarity.RARE,
                        chance = 5.0,
                        stats = ItemStats(defense = 12),
                        description = "Прочный шлем красного рыцаря 🪖"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 30.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 15.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 15.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 20,
                goldMax = 40,
                goldChance = 95
            )
        )

        // ============================================
        // 5. ЗЕЛЁНЫЙ СЛИЗЕНЬ (тип 4) — УРОВЕНЬ 8-10
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
                        stats = ItemStats(attack = 34),
                        description = "Меч с сильнейшим ядом"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 35.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 15.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 15.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 15,
                goldMax = 30,
                goldChance = 90
            )
        )

        // ============================================
        // 6. СТАЛЬНОЙ РЫЦАРЬ (тип 5) — УРОВЕНЬ 9-11
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
                        stats = ItemStats(attack = 45),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "chest_1",
                        itemName = "Стальная броня",
                        itemType = Item.ItemType.CHEST,
                        rarity = ItemRarity.EPIC,
                        chance = 3.0,
                        stats = ItemStats(defense = 22),
                        description = "Прочная стальная броня 🛡️"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_mythic",
                        itemName = "Мифический клинок",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.MYTHIC,
                        chance = 0.5,
                        stats = ItemStats(attack = 58),
                        description = "Оружие богов"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 30.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 20.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 20.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 30,
                goldMax = 50,
                goldChance = 95
            )
        )

        // ============================================
        // 7. ГОБЛИН (тип 6) — УРОВЕНЬ 12-13
        // ============================================

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
                        chance = 0.2,
                        stats = ItemStats(attack = 48),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_mythic",
                        itemName = "Мифический клинок",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.MYTHIC,
                        chance = 0.4,
                        stats = ItemStats(attack = 60),
                        description = "Оружие богов"
                    ),
                    DropTable.DropEntry(
                        itemId = "helmet_2",
                        itemName = "Шлем гоблина",
                        itemType = Item.ItemType.HELMET,
                        rarity = ItemRarity.EPIC,
                        chance = 1.0,
                        stats = ItemStats(defense = 16),
                        description = "Шлем, снятый с могучего гоблина"
                    ),
                    DropTable.DropEntry(
                        itemId = "chest_1",
                        itemName = "Броня гоблина",
                        itemType = Item.ItemType.CHEST,
                        rarity = ItemRarity.EPIC,
                        chance = 1.0,
                        stats = ItemStats(defense = 18, health = 10),
                        description = "Прочная броня, снятая с гоблина-воина 🛡️"
                    ),
                    DropTable.DropEntry(
                        itemId = "ring_1",
                        itemName = "Кольцо гоблина",
                        itemType = Item.ItemType.RING,
                        rarity = ItemRarity.RARE,
                        chance = 0.2,
                        stats = ItemStats(health = 20, dodge = 5),
                        description = "Кольцо, найденное у гоблина 💍"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 20.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 25.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 25.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 40,
                goldMax = 70,
                goldChance = 95
            )
        )

        // ============================================
        // 8. МОНАХ (тип 7) — УРОВЕНЬ 12-13
        // ============================================

        dropConfigs.add(
            MobDrop(
                mobType = 7,
                minLevel = 12,
                maxLevel = 13,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "gloves_1",
                        itemName = "Перчатки монаха",
                        itemType = Item.ItemType.GLOVES,
                        rarity = ItemRarity.EPIC,
                        chance = 30.0,
                        stats = ItemStats(defense = 8, health = 6),
                        description = "Перчатки, укрепляющие силу духа 🥊"
                    ),
                    DropTable.DropEntry(
                        itemId = "pants_1",
                        itemName = "Штаны монаха",
                        itemType = Item.ItemType.PANTS,
                        rarity = ItemRarity.EPIC,
                        chance = 30.0,
                        stats = ItemStats(defense = 8, health = 8),
                        description = "Удобные штаны для долгих странствий 👖"
                    ),
                    DropTable.DropEntry(
                        itemId = "shield_1",
                        itemName = "Деревянный щит",
                        itemType = Item.ItemType.SHIELD,
                        rarity = ItemRarity.EPIC,
                        chance = 30.0,
                        stats = ItemStats(defense = 22),
                        description = "Крепкий деревянный щит 🛡️"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_arachnid",
                        itemName = "Мощный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 0.5,
                        stats = ItemStats(attack = 38),
                        description = "Мощный меч"
                    ),
                    DropTable.DropEntry(
                        itemId = "ring_1",
                        itemName = "Кольцо силы",
                        itemType = Item.ItemType.RING,
                        rarity = ItemRarity.RARE,
                        chance = 0.2,
                        stats = ItemStats(health = 25, crit = 5, dodge = 3),
                        description = "Древнее мощное кольцо силы 💍"
                    ),
                    DropTable.DropEntry(
                        itemId = "necklace_1",
                        itemName = "Ожерелье выносливости",
                        itemType = Item.ItemType.NECKLACE,
                        rarity = ItemRarity.EPIC,
                        chance = 0.2,
                        stats = ItemStats(health = 30, hpRegen = 3),
                        description = "Древнее ожерелье, повышающее живучесть 📿"
                    ),
                        DropTable.DropEntry(
                            itemId = "rune_strength",
                            itemName = "Руна силы",
                            itemType = Item.ItemType.CONSUMABLE,
                            rarity = ItemRarity.RARE,
                            chance = 55.0,
                            description = "Увеличивает атаку на 3 💎",
                            runeStats = ItemStats(attack = 3)
                        ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 25.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 25.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 50,
                goldMax = 80,
                goldChance = 95
            )
        )

        // ============================================
        // 9. ОРК (тип 8) — УРОВЕНЬ 13-14
        // ============================================

        dropConfigs.add(
            MobDrop(
                mobType = 8,
                minLevel = 13,
                maxLevel = 14,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "chest_2",
                        itemName = "Броня орка",
                        itemType = Item.ItemType.CHEST,
                        rarity = ItemRarity.RARE,
                        chance = 30.0,
                        stats = ItemStats(defense = 14),
                        description = "Тяжёлая броня, снятая с поверженного орка 🛡️"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_moonlight",
                        itemName = "Лунный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 10.0,
                        stats = ItemStats(attack = 42),
                        description = "Меч, светящийся в лунном свете"
                    ),
                    DropTable.DropEntry(
                        itemId = "shield_1",
                        itemName = "Деревянный щит",
                        itemType = Item.ItemType.SHIELD,
                        rarity = ItemRarity.EPIC,
                        chance = 0.4,
                        stats = ItemStats(defense = 22),
                        description = "Крепкий деревянный щит 🛡️"
                    ),
                    DropTable.DropEntry(
                        itemId = "ring_1",
                        itemName = "Кольцо силы",
                        itemType = Item.ItemType.RING,
                        rarity = ItemRarity.RARE,
                        chance = 0.2,
                        stats = ItemStats(health = 30, crit = 3, critDamage = 5),
                        description = "Древнее мощное кольцо силы 💍"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 25.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 25.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 40,
                goldMax = 70,
                goldChance = 90
            )
        )

        // ============================================
        // 10. ТРОЛЛЬ (тип 9) — УРОВЕНЬ 14-15
        // ============================================

        dropConfigs.add(
            MobDrop(
                mobType = 9,
                minLevel = 14,
                maxLevel = 15,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "boots_1",
                        itemName = "Сапоги тролля",
                        itemType = Item.ItemType.BOOTS,
                        rarity = ItemRarity.RARE,
                        chance = 40.0,
                        stats = ItemStats(defense = 12),
                        description = "Массивные сапоги, снятые с тролля 👢"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_flame",
                        itemName = "Пламенный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 30.0,
                        stats = ItemStats(attack = 48),
                        description = "Меч, пылающий огнём"
                    ),
                    DropTable.DropEntry(
                        itemId = "shield_1",
                        itemName = "Деревянный щит",
                        itemType = Item.ItemType.SHIELD,
                        rarity = ItemRarity.EPIC,
                        chance = 0.4,
                        stats = ItemStats(defense = 24),
                        description = "Крепкий деревянный щит 🛡️"
                    ),
                    DropTable.DropEntry(
                        itemId = "ring_1",
                        itemName = "Кольцо силы",
                        itemType = Item.ItemType.RING,
                        rarity = ItemRarity.RARE,
                        chance = 0.2,
                        stats = ItemStats(health = 35, crit = 5, critDamage = 10, dodge = 5),
                        description = "Древнее мощное кольцо силы 💍"
                    ),
                    DropTable.DropEntry(
                        itemId = "cake_large",
                        itemName = "Большой торт",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 25.0,
                        description = "Восстанавливает 120 HP 🎂"
                    ),
                    DropTable.DropEntry(
                        itemId = "oridecon",
                        itemName = "Оридикон",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 25.0,
                        description = "Редкий минерал для заточки оружия ⛏️",
                        quantity = 1
                    ),
                    DropTable.DropEntry(
                        itemId = "elunium",
                        itemName = "Элуниум",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.UNCOMMON,
                        chance = 25.0,
                        description = "Редкий минерал для заточки брони ⛏️",
                        quantity = 1
                    )
                ),
                goldMin = 50,
                goldMax = 90,
                goldChance = 95
            )
        )
    }

    fun getDropForMob(mob: Mob): Pair<List<Item>, Int> {
        for (config in dropConfigs) {
            if (config.matches(mob)) {
                val (items, gold) = config.getDrop(random)

                if (mob.isBoss) {
                    val boostedItems = mutableListOf<Item>()
                    val multiplier = 3
                    for (item in items) {
                        for (i in 0 until multiplier) {
                            val boostedItem = item.copy(
                                id = "${item.id}_${i}",
                                name = if (i > 0) "${item.name} (${i+1})" else item.name
                            )
                            boostedItems.add(boostedItem)
                        }
                    }
                    val boostedGold = gold * (3 + random.nextInt(3))
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
