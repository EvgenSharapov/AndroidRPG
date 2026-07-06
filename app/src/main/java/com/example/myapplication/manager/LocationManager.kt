package com.example.myapplication.manager

import android.graphics.RectF
import com.example.myapplication.model.*

class LocationManager {
    enum class Location {
        CITY, FOREST, WASTELAND, ROCKS, CASTLE, DESERT
    }

    private val locations = mutableMapOf<Location, LocationData>()
    var currentLocation = Location.CITY
        private set

    init {
        initLocations()
    }

    private fun initLocations() {
        // ВСЕ МОБЫ РАВНОМЕРНО РАСПРЕДЕЛЕНЫ В КВАДРАТЕ X: 100-1000, Y: 450-1800

        // ===== ГОРОД =====
        locations[Location.CITY] = LocationData(
            name = "Город",
            mobs = mutableListOf(
                // Флаффи (тип 0) - уровень 1 (6 штук)
                Mob(200f, 600f, 0, 20f, 20f, level = 1),
                Mob(450f, 1000f, 0, 20f, 20f, level = 1),
                Mob(700f, 700f, 0, 20f, 20f, level = 1),
                Mob(350f, 1500f, 0, 20f, 20f, level = 1),
                Mob(600f, 1700f, 0, 20f, 20f, level = 1),
                Mob(900f, 1200f, 0, 20f, 20f, level = 1),

                // Пауки (тип 1) - уровень 2 (4 штуки)
                Mob(180f, 900f, 1, 35f, 35f, level = 2),
                Mob(550f, 1200f, 1, 35f, 35f, level = 2),
                Mob(800f, 600f, 1, 35f, 35f, level = 2),
                Mob(400f, 1700f, 1, 35f, 35f, level = 2),

                // Многоглазы (тип 2) - уровень 3 (4 штуки)
                Mob(300f, 750f, 2, 50f, 50f, level = 3),
                Mob(650f, 1400f, 2, 50f, 50f, level = 3),
                Mob(850f, 900f, 2, 50f, 50f, level = 3),
                Mob(500f, 500f, 2, 50f, 50f, level = 3)
            ),
            npcs = mutableListOf(
                NPC(150f, 200f, "Горожанин", "Добро пожаловать в город!")
            ),
            buildings = mutableListOf()
        )

        // ===== ЛЕС =====
        locations[Location.FOREST] = LocationData(
            name = "Лес",
            mobs = mutableListOf(
                // Флаффи (тип 0) - уровень 2-3 (5 штук)
                Mob(200f, 700f, 0, 25f, 25f, level = 2),
                Mob(500f, 1100f, 0, 25f, 25f, level = 2),
                Mob(800f, 600f, 0, 30f, 30f, level = 3),
                Mob(350f, 1600f, 0, 30f, 30f, level = 3),
                Mob(700f, 1700f, 0, 30f, 30f, level = 3),

                // Пауки (тип 1) - уровень 3-4 (4 штуки)
                Mob(180f, 1100f, 1, 40f, 40f, level = 3),
                Mob(600f, 800f, 1, 45f, 45f, level = 4),
                Mob(850f, 1500f, 1, 45f, 45f, level = 4),
                Mob(400f, 1400f, 1, 45f, 45f, level = 4),

                // Многоглазы (тип 2) - уровень 4-5 (4 штуки)
                Mob(250f, 800f, 2, 60f, 60f, level = 4),
                Mob(650f, 1300f, 2, 70f, 70f, level = 5),
                Mob(900f, 700f, 2, 70f, 70f, level = 5),
                Mob(500f, 1600f, 2, 70f, 70f, level = 5)
            ),
            npcs = mutableListOf(
                NPC(150f, 150f, "Лесник", "Осторожно, в лесу водятся пауки!")
            ),
            buildings = mutableListOf()
        )

        // ===== ПУСТОШЬ =====
        locations[Location.WASTELAND] = LocationData(
            name = "Пустошь",
            mobs = mutableListOf(
                // Флаффи (тип 0) - уровень 3-4 (4 штуки)
                Mob(200f, 800f, 0, 30f, 30f, level = 3),
                Mob(600f, 1100f, 0, 35f, 35f, level = 4),
                Mob(900f, 700f, 0, 35f, 35f, level = 4),
                Mob(400f, 1600f, 0, 35f, 35f, level = 4),

                // Пауки (тип 1) - уровень 4-5 (4 штуки)
                Mob(180f, 1200f, 1, 45f, 45f, level = 4),
                Mob(700f, 800f, 1, 55f, 55f, level = 5),
                Mob(450f, 600f, 1, 55f, 55f, level = 5),
                Mob(850f, 1700f, 1, 55f, 55f, level = 5),

                // Многоглазы (тип 2) - уровень 5-6 (4 штуки)
                Mob(300f, 900f, 2, 70f, 70f, level = 5),
                Mob(650f, 1500f, 2, 80f, 80f, level = 6),
                Mob(500f, 1300f, 2, 80f, 80f, level = 6),
                Mob(900f, 1000f, 2, 80f, 80f, level = 6)
            ),
            npcs = mutableListOf(
                NPC(150f, 150f, "Выживший", "Здесь опасно! Держись подальше.")
            ),
            buildings = mutableListOf()
        )

        // ===== СКАЛЫ =====
        locations[Location.ROCKS] = LocationData(
            name = "Скалы",
            mobs = mutableListOf(
                // Флаффи (уровень 5-6) (4 штуки)
                Mob(200f, 700f, 0, 40f, 40f, level = 5),
                Mob(600f, 1200f, 0, 45f, 45f, level = 6),
                Mob(400f, 1600f, 0, 45f, 45f, level = 6),
                Mob(900f, 800f, 0, 45f, 45f, level = 6),

                // Пауки (уровень 5-7) (4 штуки)
                Mob(180f, 1100f, 1, 55f, 55f, level = 5),
                Mob(700f, 700f, 1, 60f, 60f, level = 6),
                Mob(450f, 1500f, 1, 70f, 70f, level = 7),
                Mob(850f, 1700f, 1, 70f, 70f, level = 7),

                // Многоглазы (уровень 6-7) (4 штуки)
                Mob(300f, 800f, 2, 80f, 80f, level = 6),
                Mob(650f, 1400f, 2, 90f, 90f, level = 7),
                Mob(550f, 600f, 2, 90f, 90f, level = 7),
                Mob(900f, 1500f, 2, 90f, 90f, level = 7),

                // Красный рыцарь (тип 3) - уровень 8 (2 штуки)
                Mob(250f, 1300f, 3, 120f, 120f, level = 8),
                Mob(750f, 1000f, 3, 120f, 120f, level = 8),

                // Зелёный слизень (тип 4) - уровень 8 (2 штуки)
                Mob(400f, 700f, 4, 90f, 90f, level = 8),
                Mob(850f, 1600f, 4, 90f, 90f, level = 8),

                // Стальной рыцарь (тип 5) - уровень 9 (2 штуки)
                Mob(550f, 1700f, 5, 150f, 150f, level = 9),
                Mob(200f, 500f, 5, 150f, 150f, level = 9)
            ),
            npcs = mutableListOf(
                NPC(100f, 100f, "Странник", "Осторожно, здесь водятся сильные монстры!")
            ),
            buildings = mutableListOf()
        )

        // ===== ЗАМОК =====
        locations[Location.CASTLE] = LocationData(
            name = "Замок",
            mobs = mutableListOf(
                // Красный рыцарь (тип 3) (4 штуки)
                Mob(180f, 700f, 3, 120f, 120f, level = 8),
                Mob(500f, 1200f, 3, 130f, 130f, level = 9),
                Mob(800f, 600f, 3, 130f, 130f, level = 9),
                Mob(350f, 1700f, 3, 130f, 130f, level = 9),

                // Зелёный слизень (тип 4) (4 штуки)
                Mob(250f, 1000f, 4, 90f, 90f, level = 8),
                Mob(650f, 800f, 4, 100f, 100f, level = 9),
                Mob(900f, 1300f, 4, 100f, 100f, level = 9),
                Mob(450f, 1500f, 4, 100f, 100f, level = 9),

                // Стальной рыцарь (тип 5) (4 штуки)
                Mob(200f, 1400f, 5, 150f, 150f, level = 9),
                Mob(600f, 600f, 5, 165f, 165f, level = 10),
                Mob(850f, 1700f, 5, 165f, 165f, level = 10),
                Mob(400f, 900f, 5, 165f, 165f, level = 10),

                // Гоблины (тип 6) (4 штуки)
                Mob(300f, 1100f, 6, 180f, 180f, level = 12),
                Mob(700f, 1500f, 6, 190f, 190f, level = 13),
                Mob(550f, 700f, 6, 190f, 190f, level = 13),
                Mob(900f, 900f, 6, 190f, 190f, level = 13)
            ),
            npcs = mutableListOf(
                NPC(100f, 100f, "Стражник", "Добро пожаловать в замок! Будь осторожен.")
            ),
            buildings = mutableListOf()
        )

        // ============================================
        // ⭐ НОВАЯ ЛОКАЦИЯ: ПУСТЫНЯ (12-15 уровень)
        // ============================================
        locations[Location.DESERT] = LocationData(
            name = "Пустыня 🏜️",
            mobs = mutableListOf(
                // ===== Монах (тип 7) - уровень 12-13 =====
                Mob(200f, 600f, 7, 200f, 200f, level = 12),
                Mob(500f, 1100f, 7, 200f, 200f, level = 12),
                Mob(800f, 700f, 7, 220f, 220f, level = 13),
                Mob(350f, 1500f, 7, 220f, 220f, level = 13),
                Mob(700f, 1700f, 7, 220f, 220f, level = 13),

                // ===== ОРК (тип 8) - уровень 13-14 =====
                Mob(150f, 800f, 8, 280f, 280f, level = 13),
                Mob(450f, 1300f, 8, 280f, 280f, level = 13),
                Mob(850f, 900f, 8, 300f, 300f, level = 14),
                Mob(250f, 1600f, 8, 300f, 300f, level = 14),

                // ===== ТРОЛЛЬ (тип 9) - уровень 14-15 =====
                Mob(300f, 700f, 9, 350f, 350f, level = 14),
                Mob(650f, 1400f, 9, 350f, 350f, level = 14),
                Mob(900f, 1600f, 9, 380f, 380f, level = 15),
                Mob(550f, 500f, 9, 380f, 380f, level = 15)

            ),
            npcs = mutableListOf(
            ),
            buildings = mutableListOf()
        )
    }

    fun getCurrentData(): LocationData = locations[currentLocation] ?: locations[Location.CITY]!!

    fun getLocationName(location: Location): String = when (location) {
        Location.CITY -> "Город 🏙️"
        Location.FOREST -> "Лес 🌲"
        Location.WASTELAND -> "Пустошь 🏜️"
        Location.ROCKS -> "Скалы ⛰️"
        Location.CASTLE -> "Замок 🏰"
        Location.DESERT -> "Пустыня 🏜️"
    }

    fun moveTo(location: Location) {
        currentLocation = location
    }
}
