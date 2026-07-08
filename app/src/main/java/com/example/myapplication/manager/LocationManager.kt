package com.example.myapplication.manager

import android.graphics.RectF
import com.example.myapplication.model.*

//class LocationManager {
//    enum class Location {
//        CITY, FOREST, WASTELAND, ROCKS, CASTLE, DESERT
//    }
//
//    private val locations = mutableMapOf<Location, LocationData>()
//    var currentLocation = Location.CITY
//        private set
//
//    init {
//        initLocations()
//    }
//
//    private fun initLocations() {
//        // ВСЕ МОБЫ РАВНОМЕРНО РАСПРЕДЕЛЕНЫ В КВАДРАТЕ X: 100-1000, Y: 450-1800
//
//        // ===== ГОРОД =====
//        locations[Location.CITY] = LocationData(
//            name = "Город",
//            mobs = mutableListOf(
//                // Флаффи (тип 0) - уровень 1 (6 штук)
//                Mob(200f, 600f, 0, 20f, 20f, level = 1, attack = 8, defense = 0),
//                Mob(450f, 1000f, 0, 20f, 20f, level = 1, attack = 8, defense = 0),
//                Mob(700f, 700f, 0, 20f, 20f, level = 1, attack = 8, defense = 0),
//                Mob(350f, 1500f, 0, 20f, 20f, level = 1, attack = 8, defense = 0),
//                Mob(600f, 1700f, 0, 20f, 20f, level = 1, attack = 8, defense = 0),
//                Mob(900f, 1200f, 0, 20f, 20f, level = 1, attack = 8, defense = 0),
//
//                // Пауки (тип 1) - уровень 2 (4 штуки)
//                Mob(180f, 900f, 1, 35f, 35f, level = 2, attack = 12, defense = 2),
//                Mob(550f, 1200f, 1, 35f, 35f, level = 2, attack = 12, defense = 2),
//                Mob(800f, 600f, 1, 35f, 35f, level = 2, attack = 12, defense = 2),
//                Mob(400f, 1700f, 1, 35f, 35f, level = 2, attack = 12, defense = 2),
//
//                // Многоглазы (тип 2) - уровень 3 (4 штуки)
//                Mob(300f, 750f, 2, 50f, 50f, level = 3, attack = 16, defense = 4),
//                Mob(650f, 1400f, 2, 50f, 50f, level = 3, attack = 16, defense = 4),
//                Mob(850f, 900f, 2, 50f, 50f, level = 3, attack = 16, defense = 4),
//                Mob(500f, 500f, 2, 50f, 50f, level = 3, attack = 16, defense = 4)
//            ),
//            npcs = mutableListOf(
//                NPC(150f, 200f, "Горожанин", "Добро пожаловать в город!")
//            ),
//            buildings = mutableListOf()
//        )
//
//        // ===== ЛЕС =====
//        locations[Location.FOREST] = LocationData(
//            name = "Лес",
//            mobs = mutableListOf(
//                // Флаффи (тип 0) - уровень 2-3 (5 штук)
//                Mob(200f, 700f, 0, 25f, 25f, level = 2, attack = 12, defense = 2),
//                Mob(500f, 1100f, 0, 25f, 25f, level = 2, attack = 12, defense = 2),
//                Mob(800f, 600f, 0, 30f, 30f, level = 3, attack = 16, defense = 4),
//                Mob(350f, 1600f, 0, 30f, 30f, level = 3, attack = 16, defense = 4),
//                Mob(700f, 1700f, 0, 30f, 30f, level = 3, attack = 16, defense = 4),
//
//                // Пауки (тип 1) - уровень 3-4 (4 штуки)
//                Mob(180f, 1100f, 1, 40f, 40f, level = 3, attack = 18, defense = 6),
//                Mob(600f, 800f, 1, 45f, 45f, level = 4, attack = 22, defense = 8),
//                Mob(850f, 1500f, 1, 45f, 45f, level = 4, attack = 22, defense = 8),
//                Mob(400f, 1400f, 1, 45f, 45f, level = 4, attack = 22, defense = 8),
//
//                // Многоглазы (тип 2) - уровень 4-5 (4 штуки)
//                Mob(250f, 800f, 2, 60f, 60f, level = 4, attack = 24, defense = 10),
//                Mob(650f, 1300f, 2, 70f, 70f, level = 5, attack = 28, defense = 12),
//                Mob(900f, 700f, 2, 70f, 70f, level = 5, attack = 28, defense = 12),
//                Mob(500f, 1600f, 2, 70f, 70f, level = 5, attack = 28, defense = 12)
//            ),
//            npcs = mutableListOf(
//                NPC(150f, 150f, "Лесник", "Осторожно, в лесу водятся пауки!")
//            ),
//            buildings = mutableListOf()
//        )
//
//        // ===== ПУСТОШЬ =====
//        locations[Location.WASTELAND] = LocationData(
//            name = "Пустошь",
//            mobs = mutableListOf(
//                // Флаффи (тип 0) - уровень 3-4 (4 штуки)
//                Mob(200f, 800f, 0, 30f, 30f, level = 3, attack = 18, defense = 6),
//                Mob(600f, 1100f, 0, 35f, 35f, level = 4, attack = 22, defense = 8),
//                Mob(900f, 700f, 0, 35f, 35f, level = 4, attack = 22, defense = 8),
//                Mob(400f, 1600f, 0, 35f, 35f, level = 4, attack = 22, defense = 8),
//
//                // Пауки (тип 1) - уровень 4-5 (4 штуки)
//                Mob(180f, 1200f, 1, 45f, 45f, level = 4, attack = 24, defense = 10),
//                Mob(700f, 800f, 1, 55f, 55f, level = 5, attack = 30, defense = 14),
//                Mob(450f, 600f, 1, 55f, 55f, level = 5, attack = 30, defense = 14),
//                Mob(850f, 1700f, 1, 55f, 55f, level = 5, attack = 30, defense = 14),
//
//                // Многоглазы (тип 2) - уровень 5-6 (4 штуки)
//                Mob(300f, 900f, 2, 70f, 70f, level = 5, attack = 32, defense = 16),
//                Mob(650f, 1500f, 2, 80f, 80f, level = 6, attack = 36, defense = 18),
//                Mob(500f, 1300f, 2, 80f, 80f, level = 6, attack = 36, defense = 18),
//                Mob(900f, 1000f, 2, 80f, 80f, level = 6, attack = 36, defense = 18)
//            ),
//            npcs = mutableListOf(
//                NPC(150f, 150f, "Выживший", "Здесь опасно! Держись подальше.")
//            ),
//            buildings = mutableListOf()
//        )
//
//        // ===== СКАЛЫ =====
//        locations[Location.ROCKS] = LocationData(
//            name = "Скалы",
//            mobs = mutableListOf(
//                // Флаффи (уровень 5-6) (4 штуки)
//                Mob(200f, 700f, 0, 40f, 40f, level = 5, attack = 30, defense = 14),
//                Mob(600f, 1200f, 0, 45f, 45f, level = 6, attack = 36, defense = 18),
//                Mob(400f, 1600f, 0, 45f, 45f, level = 6, attack = 36, defense = 18),
//                Mob(900f, 800f, 0, 45f, 45f, level = 6, attack = 36, defense = 18),
//
//                // Пауки (уровень 5-7) (4 штуки)
//                Mob(180f, 1100f, 1, 55f, 55f, level = 5, attack = 32, defense = 16),
//                Mob(700f, 700f, 1, 60f, 60f, level = 6, attack = 38, defense = 20),
//                Mob(450f, 1500f, 1, 70f, 70f, level = 7, attack = 44, defense = 24),
//                Mob(850f, 1700f, 1, 70f, 70f, level = 7, attack = 44, defense = 24),
//
//                // Многоглазы (уровень 6-7) (4 штуки)
//                Mob(300f, 800f, 2, 80f, 80f, level = 6, attack = 40, defense = 22),
//                Mob(650f, 1400f, 2, 90f, 90f, level = 7, attack = 46, defense = 26),
//                Mob(550f, 600f, 2, 90f, 90f, level = 7, attack = 46, defense = 26),
//                Mob(900f, 1500f, 2, 90f, 90f, level = 7, attack = 46, defense = 26),
//
//                // Красный рыцарь (тип 3) - уровень 8 (2 штуки)
//                Mob(250f, 1300f, 3, 120f, 120f, level = 8, attack = 50, defense = 30),
//                Mob(750f, 1000f, 3, 120f, 120f, level = 8, attack = 50, defense = 30),
//
//                // Зелёный слизень (тип 4) - уровень 8 (2 штуки)
//                Mob(400f, 700f, 4, 90f, 90f, level = 8, attack = 48, defense = 28),
//                Mob(850f, 1600f, 4, 90f, 90f, level = 8, attack = 48, defense = 28),
//
//                // Стальной рыцарь (тип 5) - уровень 9 (2 штуки)
//                Mob(550f, 1700f, 5, 150f, 150f, level = 9, attack = 55, defense = 35),
//                Mob(200f, 500f, 5, 150f, 150f, level = 9, attack = 55, defense = 35)
//            ),
//            npcs = mutableListOf(
//                NPC(100f, 100f, "Странник", "Осторожно, здесь водятся сильные монстры!")
//            ),
//            buildings = mutableListOf()
//        )
//
//        // ===== ЗАМОК =====
//        LocationData(
//            name = "Замок",
//            mobs = mutableListOf(
//                // Красный рыцарь (тип 3) (4 штуки)
//                Mob(180f, 700f, 3, 120f, 120f, level = 8, attack = 42, defense = 25),
//                Mob(500f, 1200f, 3, 130f, 130f, level = 9, attack = 48, defense = 33),
//                Mob(800f, 600f, 3, 130f, 130f, level = 9, attack = 48, defense = 33),
//                Mob(350f, 1700f, 3, 130f, 130f, level = 9, attack = 48, defense = 33),
//
//                // Зелёный слизень (тип 4) (4 штуки)
//                Mob(250f, 1000f, 4, 90f, 90f, level = 8, attack = 40, defense = 25),
//                Mob(650f, 800f, 4, 100f, 100f, level = 9, attack = 46, defense = 33),
//                Mob(900f, 1300f, 4, 100f, 100f, level = 9, attack = 46, defense = 33),
//                Mob(450f, 1500f, 4, 100f, 100f, level = 9, attack = 46, defense = 33),
//
//                // Стальной рыцарь (тип 5) (4 штуки)
//                Mob(200f, 1400f, 5, 150f, 150f, level = 9, attack = 50, defense = 30),
//                Mob(600f, 600f, 5, 165f, 165f, level = 10, attack = 55, defense = 35),
//                Mob(850f, 1700f, 5, 165f, 165f, level = 10, attack = 55, defense = 35),
//                Mob(400f, 900f, 5, 165f, 165f, level = 10, attack = 55, defense = 35),
//
//                // Гоблины (тип 6) (4 штуки)
//                Mob(300f, 1100f, 6, 180f, 180f, level = 12, attack = 57, defense = 40),
//                Mob(700f, 1500f, 6, 190f, 190f, level = 13, attack = 63, defense = 45),
//                Mob(550f, 700f, 6, 190f, 190f, level = 13, attack = 63, defense = 45),
//                Mob(900f, 900f, 6, 190f, 190f, level = 13, attack = 63, defense = 45)
//            ),
//            npcs = mutableListOf(
//                NPC(100f, 100f, "Стражник", "Добро пожаловать в замок! Будь осторожен.")
//            ),
//            buildings = mutableListOf()
//        ).also { locations[Location.CASTLE] = it }
//
//        // ============================================
//        // ⭐ НОВАЯ ЛОКАЦИЯ: ПУСТЫНЯ (12-15 уровень)
//        // ============================================
//        locations[Location.DESERT] = LocationData(
//            name = "Пустыня 🏜️",
//            mobs = mutableListOf(
//                // ===== Монах (тип 7) - уровень 12-13 =====
//                Mob(200f, 600f, 7, 200f, 200f, level = 12, attack = 60, defense = 45),
//                Mob(500f, 1100f, 7, 200f, 200f, level = 12, attack = 60, defense = 45),
//                Mob(800f, 700f, 7, 220f, 220f, level = 13, attack = 67, defense = 50),
//                Mob(350f, 1500f, 7, 220f, 220f, level = 13, attack = 67, defense = 50),
//                Mob(700f, 1700f, 7, 220f, 220f, level = 13, attack = 67, defense = 50),
//
//                // ===== ОРК (тип 8) - уровень 13-14 =====
//                Mob(150f, 800f, 8, 280f, 280f, level = 13, attack = 70, defense = 55),
//                Mob(450f, 1300f, 8, 280f, 280f, level = 13, attack = 70, defense = 55),
//                Mob(850f, 900f, 8, 300f, 300f, level = 14, attack = 75, defense = 62),
//                Mob(250f, 1600f, 8, 300f, 300f, level = 14, attack = 75, defense = 62),
//
//                // ===== ТРОЛЛЬ (тип 9) - уровень 14-15 =====
//                Mob(300f, 700f, 9, 350f, 350f, level = 14, attack = 75, defense = 70),
//                Mob(650f, 1400f, 9, 350f, 350f, level = 14, attack = 75, defense = 70),
//                Mob(900f, 1600f, 9, 380f, 380f, level = 15, attack = 80, defense = 75),
//                Mob(550f, 500f, 9, 380f, 380f, level = 15, attack = 80, defense = 75)
//            ),
//            npcs = mutableListOf(),
//            buildings = mutableListOf()
//        )
//    }
//
//    fun getCurrentData(): LocationData = locations[currentLocation] ?: locations[Location.CITY]!!
//
//    fun getLocationName(location: Location): String = when (location) {
//        Location.CITY -> "Город 🏙️"
//        Location.FOREST -> "Лес 🌲"
//        Location.WASTELAND -> "Пустошь 🏜️"
//        Location.ROCKS -> "Скалы ⛰️"
//        Location.CASTLE -> "Замок 🏰"
//        Location.DESERT -> "Пустыня 🏜️"
//    }
//
//    fun moveTo(location: Location) {
//        currentLocation = location
//    }
//}



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
                // Флаффи (тип 0) - уровни 1-2
                MobTemplate(
                    type = 0,
                    count = 6,
                    minLevel = 1,
                    maxLevel = 2,
                    baseHp = 25f,
                    baseAttack = 8,
                    baseDefense = 1,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Пауки (тип 1) - уровни 2-3
                MobTemplate(
                    type = 1,
                    count = 4,
                    minLevel = 2,
                    maxLevel = 3,
                    baseHp = 40f,
                    baseAttack = 14,
                    baseDefense = 3,
                    positionRange = 150f..950f to 500f..1900f
                ),
                // Многоглазы (тип 2) - уровни 3-4
                MobTemplate(
                    type = 2,
                    count = 3,
                    minLevel = 3,
                    maxLevel = 4,
                    baseHp = 60f,
                    baseAttack = 20,
                    baseDefense = 5,
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
                // Флаффи (тип 0) - уровни 2-3
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
                // Флаффи (тип 0) - уровни 3-4
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
                // Флаффи (тип 0) - уровни 5-6
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
            0 -> 80f   // Флаффи
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
