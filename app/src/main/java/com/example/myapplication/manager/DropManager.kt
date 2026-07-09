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
    private val bossDropConfigs = mutableListOf<MobDrop>()
    private val random = Random

    init {
        initDrops()
    }

    private fun initDrops() {

        initBossRunes()

        initNormalDrops()
    }

    private fun initBossRunes() {

// ⭐ БОСС-РУНЫ (уникальные для каждого босса)

// 1. Андре-босс (тип 0) - Руна ярости
        bossDropConfigs.add(
            MobDrop(
                mobType = 0,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_fury",
                        itemName = "Руна ярости",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.EPIC,
                        chance = 10.0, // 100% шанс для босса
                        description = "Увеличивает атаку на 10 💢",
                        runeStats = ItemStats(attack = 10),
                        quantity = 1
                    )
                ),
                goldMin = 50,
                goldMax = 100,
                goldChance = 100
            )
        )

// 2. Паук-босс (тип 1) - Руна паука
        bossDropConfigs.add(
            MobDrop(
                mobType = 1,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_spider",
                        itemName = "Руна паука",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.EPIC,
                        chance = 10.0,
                        description = "Увеличивает защиту на 10 🕷️",
                        runeStats = ItemStats(defense = 10),
                        quantity = 1
                    )
                ),
                goldMin = 50,
                goldMax = 100,
                goldChance = 100
            )
        )

// 3. Многоглаз-босс (тип 2) - Руна мудрости
        bossDropConfigs.add(
            MobDrop(
                mobType = 2,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_wisdom",
                        itemName = "Руна мудрости",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.EPIC,
                        chance = 10.0,
                        description = "Увеличивает крит на 5% и крит. урон на 10% 🧠",
                        runeStats = ItemStats(crit = 5, critDamage = 10),
                        quantity = 1
                    )
                ),
                goldMin = 60,
                goldMax = 120,
                goldChance = 100
            )
        )

// 4. Красный рыцарь-босс (тип 3) - Руна рыцаря
        bossDropConfigs.add(
            MobDrop(
                mobType = 3,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_knight",
                        itemName = "Руна рыцаря",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.EPIC,
                        chance = 10.0,
                        description = "Увеличивает защиту на 8 и HP на 20 🗡️",
                        runeStats = ItemStats(defense = 8, health = 20),
                        quantity = 1
                    )
                ),
                goldMin = 60,
                goldMax = 120,
                goldChance = 100
            )
        )

// 5. Зелёный слизень-босс (тип 4) - Руна выносливости
        bossDropConfigs.add(
            MobDrop(
                mobType = 4,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_endurance",
                        itemName = "Руна выносливости",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.EPIC,
                        chance = 10.0,
                        description = "Увеличивает HP на 30 и регенерацию на 3 HP/сек 💚",
                        runeStats = ItemStats(health = 30, hpRegen = 3),
                        quantity = 1
                    )
                ),
                goldMin = 70,
                goldMax = 140,
                goldChance = 100
            )
        )

// 6. Стальной рыцарь-босс (тип 5) - Руна стали
        bossDropConfigs.add(
            MobDrop(
                mobType = 5,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_steel",
                        itemName = "Руна стали",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 10.0,
                        description = "Увеличивает защиту на 12 ⛓️",
                        runeStats = ItemStats(defense = 12),
                        quantity = 1
                    )
                ),
                goldMin = 80,
                goldMax = 160,
                goldChance = 100
            )
        )

// 7. Гоблин-босс (тип 6) - Руна алчности
        bossDropConfigs.add(
            MobDrop(
                mobType = 6,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_greed",
                        itemName = "Руна алчности",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 10.0,
                        description = "Увеличивает получаемое золото на 15% 💰",
                        runeStats = ItemStats(goldBonus = 15),
                        quantity = 1
                    )
                ),
                goldMin = 100,
                goldMax = 200,
                goldChance = 100
            )
        )

// 8. Монах-босс (тип 7) - Руна просветления
        bossDropConfigs.add(
            MobDrop(
                mobType = 7,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_enlightenment",
                        itemName = "Руна просветления",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 10.0,
                        description = "Увеличивает получаемый опыт на 15% ✨",
                        runeStats = ItemStats(expBonus = 15),
                        quantity = 1
                    )
                ),
                goldMin = 100,
                goldMax = 200,
                goldChance = 100
            )
        )

// 9. Орк-босс (тип 8) - Руна ярости орка
        bossDropConfigs.add(
            MobDrop(
                mobType = 8,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_ork_fury",
                        itemName = "Руна ярости орка",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.LEGENDARY,
                        chance = 10.0,
                        description = "Увеличивает атаку на 15 и крит на 5% 🗡️💥",
                        runeStats = ItemStats(attack = 15, crit = 5),
                        quantity = 1
                    )
                ),
                goldMin = 120,
                goldMax = 240,
                goldChance = 100
            )
        )

// 10. Тролль-босс (тип 9) - Руна тролля
        bossDropConfigs.add(
            MobDrop(
                mobType = 9,
                minLevel = 1,
                maxLevel = 99,
                dropTable = DropTable.createMixedDrop(
                    DropTable.DropEntry(
                        itemId = "rune_troll",
                        itemName = "Руна тролля",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.MYTHIC,
                        chance = 10.0,
                        description = "Увеличивает HP на 40, регенерацию на 5 HP/сек и крит. урон на 5% 🧌",
                        runeStats = ItemStats(health = 40, hpRegen = 5, critDamage = 5),
                        quantity = 1
                    )
                ),
                goldMin = 150,
                goldMax = 300,
                goldChance = 100
            )
        )
    }

    private fun initNormalDrops() {
        // ============================================
        // 1. Андре (тип 0) — уровни 1-9
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
                        chance = 1.0,
                        stats = ItemStats(attack = 45),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "chest_1",
                        itemName = "Стальная броня",
                        itemType = Item.ItemType.CHEST,
                        rarity = ItemRarity.EPIC,
                        chance = 2.0,
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
                        itemId = "rune_defense",
                        itemName = "Руна защиты",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 2.0,
                        description = "Увеличивает защиту на 3 🛡️",
                        runeStats = ItemStats(defense = 3),
                        quantity = 1
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
                        chance = 0.3,
                        stats = ItemStats(attack = 48),
                        description = "Меч древних героев"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_mythic",
                        itemName = "Мифический клинок",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.MYTHIC,
                        chance = 0.1,
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
                        itemId = "rune_health",
                        itemName = "Руна жизни",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.RARE,
                        chance = 2.0,
                        description = "Увеличивает HP на 15 ❤️",
                        runeStats = ItemStats(health = 15),
                        quantity = 1
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
                        chance = 1.0,
                        stats = ItemStats(defense = 8, health = 6),
                        description = "Перчатки, укрепляющие силу духа 🥊"
                    ),
                    DropTable.DropEntry(
                        itemId = "pants_1",
                        itemName = "Штаны монаха",
                        itemType = Item.ItemType.PANTS,
                        rarity = ItemRarity.EPIC,
                        chance = 1.0,
                        stats = ItemStats(defense = 8, health = 8),
                        description = "Удобные штаны для долгих странствий 👖"
                    ),
                    DropTable.DropEntry(
                        itemId = "shield_1",
                        itemName = "Деревянный щит",
                        itemType = Item.ItemType.SHIELD,
                        rarity = ItemRarity.EPIC,
                        chance = 1.0,
                        stats = ItemStats(defense = 22),
                        description = "Крепкий деревянный щит 🛡️"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_arachnid",
                        itemName = "Мощный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 0.3,
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
                        chance = 2.0,
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
                        chance = 1.0,
                        stats = ItemStats(defense = 14),
                        description = "Тяжёлая броня, снятая с поверженного орка 🛡️"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_moonlight",
                        itemName = "Лунный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 0.3,
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
                        itemId = "rune_crit",
                        itemName = "Руна крита",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.EPIC,
                        chance = 2.0,
                        description = "Увеличивает шанс крита на 5% 💥",
                        runeStats = ItemStats(crit = 5),
                        quantity = 1
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
                        chance = 1.0,
                        stats = ItemStats(defense = 12),
                        description = "Массивные сапоги, снятые с тролля 👢"
                    ),
                    DropTable.DropEntry(
                        itemId = "sword_flame",
                        itemName = "Пламенный меч",
                        itemType = Item.ItemType.WEAPON,
                        rarity = ItemRarity.EPIC,
                        chance = 0.3,
                        stats = ItemStats(attack = 48),
                        description = "Меч, пылающий огнём"
                    ),
                    DropTable.DropEntry(
                        itemId = "shield_1",
                        itemName = "Деревянный щит",
                        itemType = Item.ItemType.SHIELD,
                        rarity = ItemRarity.EPIC,
                        chance = 0.3,
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
                        itemId = "rune_regen",
                        itemName = "Руна регенерации",
                        itemType = Item.ItemType.CONSUMABLE,
                        rarity = ItemRarity.EPIC,
                        chance = 2.0,
                        description = "Восстанавливает 2 HP/сек 🔄",
                        runeStats = ItemStats(hpRegen = 2),
                        quantity = 1
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

    // ============================================
    // ⭐ ПОЛУЧЕНИЕ ДРОПА
    // ============================================
    fun getDropForMob(mob: Mob): Pair<List<Item>, Int> {
        println("🔍 getDropForMob: тип=${mob.type}, уровень=${mob.level}, isBoss=${mob.isBoss}")

        // ⭐ ЕСЛИ ЭТО БОСС - ИЩЕМ В БОСС-КОНФИГАХ
        if (mob.isBoss) {
            for (config in bossDropConfigs) {
                if (config.matches(mob)) {
                    println("👑 Найден БОСС-конфиг для типа ${mob.type}")
                    val (items, gold) = config.getDrop(random)
                    println("👑 Босс дроп: ${items.size} предметов, ${gold} золота")
                    for (item in items) {
                        println("   - ${item.name} (${item.id})")
                    }
                    return Pair(items, gold)
                }
            }
            println("⚠️ Босс-конфиг НЕ НАЙДЕН для типа ${mob.type}, используем обычный")
        }

        // ⭐ ОБЫЧНЫЙ ДРОП (для обычных мобов или если босс-конфиг не найден)
        for (config in dropConfigs) {
            if (config.matches(mob)) {
                val (items, gold) = config.getDrop(random)
                println("📦 Обычный дроп: ${items.size} предметов, ${gold} золота")
                return Pair(items, gold)
            }
        }

        println("❌ Конфиг не найден для типа ${mob.type}, уровень ${mob.level}")
        val defaultGold = random.nextInt(1, 4)
        return Pair(emptyList(), defaultGold)
    }

    fun addDropConfig(config: MobDrop) {
        dropConfigs.add(config)
    }
}
