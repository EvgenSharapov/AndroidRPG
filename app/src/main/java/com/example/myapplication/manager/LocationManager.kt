package com.example.myapplication.manager

import com.example.myapplication.model.*


class LocationManager {
    enum class Location {
        CITY, FOREST, WASTELAND, ROCKS, CASTLE, DESERT
    }

    private val locationData = mutableMapOf<Location, LocationData>()
    private val locationConfigs = mutableMapOf<Location, LocationConfig>()
    var currentLocation = Location.CITY
        private set

    init {
        initLocationConfigs()
        initLocations()
    }

    private fun initLocationConfigs() {
        // ===== ГОРОД (Уровни 1-3) =====
        locationConfigs[Location.CITY] = LocationConfig(
            name = "Город 🏙️",
            mobTemplates = listOf(
                // Андре (тип 0) - уровни 1-2
                MobTemplate(
                    type = 0,
                    count = 6,
                    minLevel = 1,
                    maxLevel = 2,
                    baseHp = 45f,
                    baseAttack = 5,
                    baseDefense = 11,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Пауки (тип 1) - уровни 2-3
                MobTemplate(
                    type = 1,
                    count = 4,
                    minLevel = 2,
                    maxLevel = 3,
                    baseHp = 40f,
                    baseAttack = 8,
                    baseDefense = 14,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Многоглазы (тип 2) - уровни 3-4
                MobTemplate(
                    type = 2,
                    count = 3,
                    minLevel = 3,
                    maxLevel = 4,
                    baseHp = 60f,
                    baseAttack = 10,
                    baseDefense = 16,
                    positionRange = 150f..950f to 500f..1900f
                )
            ),
            npcs = listOf(
                NPCData("Горожанин", 150f, 200f, "Добро пожаловать в город!")
            )
        )

        // ===== ЛЕС (Уровни 2-5) =====
        locationConfigs[Location.FOREST] = LocationConfig(
            name = "Лес 🌲",
            mobTemplates = listOf(
                // Андре (тип 0) - уровни 2-3
                MobTemplate(
                    type = 0,
                    count = 5,
                    minLevel = 2,
                    maxLevel = 3,
                    baseHp = 30f,
                    baseAttack = 12,
                    baseDefense = 2,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Пауки (тип 1) - уровни 3-4
                MobTemplate(
                    type = 1,
                    count = 4,
                    minLevel = 3,
                    maxLevel = 4,
                    baseHp = 50f,
                    baseAttack = 18,
                    baseDefense = 6,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Многоглазы (тип 2) - уровни 4-5
                MobTemplate(
                    type = 2,
                    count = 4,
                    minLevel = 4,
                    maxLevel = 5,
                    baseHp = 75f,
                    baseAttack = 26,
                    baseDefense = 10,
                    positionRange = 150f..950f to 500f..1900f
                )
            ),
            npcs = listOf(
                NPCData("Лесник", 150f, 150f, "Осторожно, в лесу водятся пауки!")
            )
        )

        // ===== ПУСТОШЬ (Уровни 3-6) =====
        locationConfigs[Location.WASTELAND] = LocationConfig(
            name = "Пустошь 🏜️",
            mobTemplates = listOf(
                // Андре (тип 0) - уровни 3-4
                MobTemplate(
                    type = 0,
                    count = 4,
                    minLevel = 3,
                    maxLevel = 4,
                    baseHp = 40f,
                    baseAttack = 18,
                    baseDefense = 6,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Пауки (тип 1) - уровни 4-5
                MobTemplate(
                    type = 1,
                    count = 4,
                    minLevel = 4,
                    maxLevel = 5,
                    baseHp = 60f,
                    baseAttack = 26,
                    baseDefense = 10,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Многоглазы (тип 2) - уровни 5-6
                MobTemplate(
                    type = 2,
                    count = 4,
                    minLevel = 5,
                    maxLevel = 6,
                    baseHp = 90f,
                    baseAttack = 34,
                    baseDefense = 16,
                    positionRange = 150f..950f to 500f..1900f
                )
            ),
            npcs = listOf(
                NPCData("Выживший", 150f, 150f, "Здесь опасно! Держись подальше.")
            )
        )

        // ===== СКАЛЫ (Уровни 5-9) =====
        locationConfigs[Location.ROCKS] = LocationConfig(
            name = "Скалы ⛰️",
            mobTemplates = listOf(
                // Андре (тип 0) - уровни 5-6
                MobTemplate(
                    type = 0,
                    count = 3,
                    minLevel = 5,
                    maxLevel = 6,
                    baseHp = 50f,
                    baseAttack = 30,
                    baseDefense = 14,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Пауки (тип 1) - уровни 5-7
                MobTemplate(
                    type = 1,
                    count = 3,
                    minLevel = 5,
                    maxLevel = 7,
                    baseHp = 70f,
                    baseAttack = 36,
                    baseDefense = 18,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Многоглазы (тип 2) - уровни 6-7
                MobTemplate(
                    type = 2,
                    count = 3,
                    minLevel = 6,
                    maxLevel = 7,
                    baseHp = 100f,
                    baseAttack = 44,
                    baseDefense = 24,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Красный рыцарь (тип 3) - уровни 8-9
                MobTemplate(
                    type = 3,
                    count = 3,
                    minLevel = 8,
                    maxLevel = 9,
                    baseHp = 150f,
                    baseAttack = 50,
                    baseDefense = 30,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Зелёный слизень (тип 4) - уровни 8-9
                MobTemplate(
                    type = 4,
                    count = 3,
                    minLevel = 8,
                    maxLevel = 9,
                    baseHp = 120f,
                    baseAttack = 48,
                    baseDefense = 28,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Стальной рыцарь (тип 5) - уровни 9-10
                MobTemplate(
                    type = 5,
                    count = 2,
                    minLevel = 9,
                    maxLevel = 10,
                    baseHp = 180f,
                    baseAttack = 58,
                    baseDefense = 36,
                    positionRange = 150f..950f to 500f..1900f
                )
            ),
            npcs = listOf(
                NPCData("Странник", 100f, 100f, "Осторожно, здесь водятся сильные монстры!")
            )
        )

        // ===== ЗАМОК (Уровни 8-13) =====
        locationConfigs[Location.CASTLE] = LocationConfig(
            name = "Замок 🏰",
            mobTemplates = listOf(
                // Красный рыцарь (тип 3) - уровни 8-9
                MobTemplate(
                    type = 3,
                    count = 4,
                    minLevel = 8,
                    maxLevel = 9,
                    baseHp = 160f,
                    baseAttack = 52,
                    baseDefense = 32,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Зелёный слизень (тип 4) - уровни 8-9
                MobTemplate(
                    type = 4,
                    count = 4,
                    minLevel = 8,
                    maxLevel = 9,
                    baseHp = 130f,
                    baseAttack = 50,
                    baseDefense = 30,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Стальной рыцарь (тип 5) - уровни 9-10
                MobTemplate(
                    type = 5,
                    count = 4,
                    minLevel = 9,
                    maxLevel = 10,
                    baseHp = 200f,
                    baseAttack = 60,
                    baseDefense = 38,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Гоблин (тип 6) - уровни 12-13
                MobTemplate(
                    type = 6,
                    count = 4,
                    minLevel = 12,
                    maxLevel = 13,
                    baseHp = 220f,
                    baseAttack = 65,
                    baseDefense = 42,
                    positionRange = 150f..950f to 500f..1900f
                )
            ),
            npcs = listOf(
                NPCData("Стражник", 100f, 100f, "Добро пожаловать в замок! Будь осторожен.")
            )
        )

        // ===== ПУСТЫНЯ (Уровни 12-15) =====
        locationConfigs[Location.DESERT] = LocationConfig(
            name = "Пустыня 🏜️",
            mobTemplates = listOf(
                // Монах (тип 7) - уровни 12-13
                MobTemplate(
                    type = 7,
                    count = 5,
                    minLevel = 12,
                    maxLevel = 13,
                    baseHp = 240f,
                    baseAttack = 62,
                    baseDefense = 45,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Орк/Ящер (тип 8) - уровни 13-14
                MobTemplate(
                    type = 8,
                    count = 4,
                    minLevel = 13,
                    maxLevel = 14,
                    baseHp = 320f,
                    baseAttack = 72,
                    baseDefense = 55,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Тролль (тип 9) - уровни 14-15
                MobTemplate(
                    type = 9,
                    count = 4,
                    minLevel = 14,
                    maxLevel = 15,
                    baseHp = 400f,
                    baseAttack = 80,
                    baseDefense = 70,
                    positionRange = 150f..950f to 500f..1900f
                )
            )
        )
    }

    private fun initLocations() {
        for ((location, config) in locationConfigs) {
            val mobs = config.generateMobs().toMutableList()
            val npcs = config.npcs.map {
                NPC(it.x, it.y, it.name, it.dialog)
            }.toMutableList()

            locationData[location] = LocationData(
                name = config.name,
                mobs = mobs,
                npcs = npcs,
                buildings = mutableListOf()
            )
        }
    }

    fun getCurrentData(): LocationData {
        return locationData[currentLocation] ?: locationData[Location.CITY]!!
    }

    fun getLocationName(location: Location): String {
        return locationConfigs[location]?.name ?: "Неизвестно"
    }

    fun moveTo(location: Location) {
        if (location == currentLocation) return
        currentLocation = location

        // Если в локации нет мобов - генерируем
        val data = locationData[location]
        if (data != null && data.mobs.isEmpty()) {
            val config = locationConfigs[location]
            if (config != null) {
                data.mobs.clear()
                data.mobs.addAll(config.generateMobs())
                println("🔄 Локация ${data.name} загружена: ${data.mobs.size} мобов")
            }
        }
    }

    fun respawnMobs() {
        for ((location, data) in locationData) {
            // Воскрешаем мобов с истекшим таймером
            for (mob in data.mobs) {
                if (mob.isDead && mob.respawnTimer >= 900) {
                    mob.respawn()
                    println("🔄 РЕСПАУН: ${mob.getTypeName()} воскрес!")
                }
            }

            // Если все мобы мертвы и нет боссов - перегенерируем
            val aliveMobs = data.mobs.filter { !it.isDead }
            val hasBoss = data.mobs.any { it.isBoss }

            if (aliveMobs.isEmpty() && data.mobs.isNotEmpty() && !hasBoss) {
                val config = locationConfigs[location]
                if (config != null) {
                    data.mobs.clear()
                    data.mobs.addAll(config.generateMobs())
                    println("🔄 Локация ${data.name} перегенерирована: ${data.mobs.size} мобов")
                }
            }
        }
    }

    fun spawnBoss(mobType: Int, level: Int, positionX: Float, positionY: Float): Mob {
        // Создаём босса на основе типа моба
        val baseHp = when (mobType) {
            0 -> 80f   // Андре
            1 -> 120f  // Паук
            2 -> 180f  // Многоглаз
            3 -> 300f  // Красный рыцарь
            4 -> 250f  // Зелёный слизень
            5 -> 400f  // Стальной рыцарь
            6 -> 500f  // Гоблин
            7 -> 600f  // Монах
            8 -> 700f  // Орк
            9 -> 900f  // Тролль
            else -> 200f
        }

        val baseAttack = when (mobType) {
            0 -> 20
            1 -> 28
            2 -> 36
            3 -> 55
            4 -> 50
            5 -> 65
            6 -> 75
            7 -> 85
            8 -> 95
            9 -> 110
            else -> 30
        }

        val baseDefense = when (mobType) {
            0 -> 5
            1 -> 10
            2 -> 15
            3 -> 35
            4 -> 30
            5 -> 45
            6 -> 55
            7 -> 65
            8 -> 75
            9 -> 90
            else -> 10
        }

        val boss = Mob(
            x = positionX.coerceIn(50f, 1150f),
            y = positionY.coerceIn(50f, 750f),
            type = mobType,
            hp = baseHp * 3f,
            maxHp = baseHp * 3f,
            level = level,
            isBoss = true,
            attack = (baseAttack * 1.5f).toInt(),
            defense = (baseDefense * 1.5f).toInt()
        ).apply {
            startX = this.x
            startY = this.y
        }

        return boss
    }

    fun getLocationConfig(location: Location): LocationConfig? {
        return locationConfigs[location]
    }
}
