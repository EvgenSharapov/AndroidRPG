package com.example.myapplication.manager

import android.graphics.RectF
import com.example.myapplication.model.*

class LocationManager {
    enum class Location {
        CITY, FOREST, WASTELAND, ROCKS, CASTLE
    }

    private val locations = mutableMapOf<Location, LocationData>()
    var currentLocation = Location.CITY
        private set

    init {
        initLocations()
    }

    private fun initLocations() {
        // ===== ГОРОД =====
        locations[Location.CITY] = LocationData(
            name = "Город",
            mobs = mutableListOf(
                // По 3 моба каждого вида
                Mob(150f, 300f, 0, 20f, 20f, level = 1),
                Mob(400f, 500f, 0, 20f, 20f, level = 1),
                Mob(700f, 250f, 0, 20f, 20f, level = 1),

                Mob(250f, 600f, 1, 35f, 35f, level = 2),
                Mob(550f, 350f, 1, 35f, 35f, level = 2),
                Mob(850f, 600f, 1, 35f, 35f, level = 2),

                Mob(100f, 150f, 2, 50f, 50f, level = 3),
                Mob(500f, 700f, 2, 50f, 50f, level = 3),
                Mob(900f, 400f, 2, 50f, 50f, level = 3)
            ),
            npcs = mutableListOf(),
            buildings = mutableListOf()
        )

        // ===== ЛЕС =====
        locations[Location.FOREST] = LocationData(
            name = "Лес",
            mobs = mutableListOf(
                Mob(200f, 300f, 0, 25f, 25f, level = 2),
                Mob(500f, 200f, 0, 25f, 25f, level = 2),
                Mob(800f, 400f, 0, 25f, 25f, level = 2),

                Mob(150f, 600f, 1, 40f, 40f, level = 3),
                Mob(450f, 700f, 1, 40f, 40f, level = 3),
                Mob(750f, 500f, 1, 40f, 40f, level = 3),

                Mob(300f, 100f, 2, 60f, 60f, level = 4),
                Mob(600f, 350f, 2, 60f, 60f, level = 4),
                Mob(900f, 650f, 2, 60f, 60f, level = 4)
            ),
            npcs = mutableListOf(),
            buildings = mutableListOf()
        )

        // ===== ПУСТОШЬ =====
        locations[Location.WASTELAND] = LocationData(
            name = "Пустошь",
            mobs = mutableListOf(
                Mob(200f, 400f, 0, 30f, 30f, level = 3),
                Mob(500f, 250f, 0, 30f, 30f, level = 3),
                Mob(800f, 500f, 0, 30f, 30f, level = 3),

                Mob(150f, 200f, 1, 45f, 45f, level = 4),
                Mob(400f, 650f, 1, 45f, 45f, level = 4),
                Mob(700f, 300f, 1, 45f, 45f, level = 4),

                Mob(300f, 600f, 2, 70f, 70f, level = 5),
                Mob(600f, 100f, 2, 70f, 70f, level = 5),
                Mob(900f, 700f, 2, 70f, 70f, level = 5)
            ),
            npcs = mutableListOf(),
            buildings = mutableListOf()
        )

        // ===== ⭐ СКАЛЫ (НОВАЯ ЛОКАЦИЯ) =====
        locations[Location.ROCKS] = LocationData(
            name = "Скалы",
            mobs = mutableListOf(
                // Флаффи (уровень 5-6)
                Mob(150f, 300f, 0, 40f, 40f, level = 5).apply { startX = x; startY = y },
                Mob(500f, 200f, 0, 40f, 40f, level = 5).apply { startX = x; startY = y },
                Mob(850f, 400f, 0, 40f, 40f, level = 5).apply { startX = x; startY = y },
                Mob(250f, 600f, 0, 45f, 45f, level = 6).apply { startX = x; startY = y },
                Mob(600f, 700f, 0, 45f, 45f, level = 6).apply { startX = x; startY = y },
                // Пауки (уровень 5-7)
                Mob(100f, 150f, 1, 55f, 55f, level = 5).apply { startX = x; startY = y },
                Mob(700f, 250f, 1, 55f, 55f, level = 5).apply { startX = x; startY = y },
                Mob(350f, 450f, 1, 60f, 60f, level = 6).apply { startX = x; startY = y },
                Mob(750f, 600f, 1, 60f, 60f, level = 6).apply { startX = x; startY = y },
                Mob(200f, 700f, 1, 70f, 70f, level = 7).apply { startX = x; startY = y },
                // Многоглазы (уровень 6-7)
                Mob(400f, 100f, 2, 80f, 80f, level = 6).apply { startX = x; startY = y },
                Mob(800f, 500f, 2, 80f, 80f, level = 6).apply { startX = x; startY = y },
                Mob(500f, 800f, 2, 90f, 90f, level = 7).apply { startX = x; startY = y },
                Mob(900f, 200f, 2, 90f, 90f, level = 7).apply { startX = x; startY = y }
            ),
            npcs = mutableListOf(
                NPC(100f, 100f, "Странник", "Осторожно, здесь водятся сильные монстры!")
            ),
            buildings = mutableListOf()
        )
        locations[Location.CASTLE] = LocationData(
            name = "Замок",
            mobs = mutableListOf(
                // Красный рыцарь (тип 3) — HP × 1.5
                Mob(150f, 300f, 3, 120f, 120f, level = 8).apply { startX = x; startY = y },      // было 80
                Mob(500f, 200f, 3, 120f, 120f, level = 8).apply { startX = x; startY = y },      // было 80
                Mob(800f, 500f, 3, 130f, 130f, level = 9).apply { startX = x; startY = y },      // было 85
                Mob(300f, 700f, 3, 130f, 130f, level = 9).apply { startX = x; startY = y },      // было 85
                // Зелёный слизень (тип 4) — HP × 1.5
                Mob(200f, 400f, 4, 90f, 90f, level = 8).apply { startX = x; startY = y },        // было 60
                Mob(600f, 600f, 4, 90f, 90f, level = 8).apply { startX = x; startY = y },        // было 60
                Mob(900f, 300f, 4, 100f, 100f, level = 9).apply { startX = x; startY = y },      // было 65
                Mob(400f, 100f, 4, 100f, 100f, level = 9).apply { startX = x; startY = y },      // было 65
                // Стальной рыцарь (тип 5) — HP × 1.5
                Mob(100f, 150f, 5, 150f, 150f, level = 9).apply { startX = x; startY = y },      // было 100
                Mob(700f, 700f, 5, 150f, 150f, level = 9).apply { startX = x; startY = y },      // было 100
                Mob(350f, 450f, 5, 165f, 165f, level = 10).apply { startX = x; startY = y },     // было 110
                Mob(850f, 200f, 5, 165f, 165f, level = 10).apply { startX = x; startY = y },     // было 110

                Mob(150f, 500f, 6, 180f, 180f, level = 12).apply { startX = x; startY = y },
                Mob(450f, 200f, 6, 180f, 180f, level = 12).apply { startX = x; startY = y },
                Mob(750f, 600f, 6, 190f, 190f, level = 13).apply { startX = x; startY = y },
                Mob(300f, 700f, 6, 190f, 190f, level = 13).apply { startX = x; startY = y }

            ),
            npcs = mutableListOf(
                NPC(100f, 100f, "Стражник", "Добро пожаловать в замок! Будь осторожен.")
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
    }

    fun moveTo(location: Location) {
        currentLocation = location
    }
}
