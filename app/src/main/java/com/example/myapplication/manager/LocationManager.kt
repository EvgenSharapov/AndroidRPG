package com.example.myapplication.manager

import android.graphics.RectF
import com.example.myapplication.model.*

class LocationManager {
    enum class Location {
        CITY, FOREST, WASTELAND
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
    }

    fun getCurrentData(): LocationData = locations[currentLocation] ?: locations[Location.CITY]!!

    fun getLocationName(location: Location): String = when (location) {
        Location.CITY -> "Город 🏙️"
        Location.FOREST -> "Лес 🌲"
        Location.WASTELAND -> "Пустошь 🏜️"
    }

    fun getLocationColor(location: Location): Int = when (location) {
        Location.CITY -> android.graphics.Color.rgb(180, 160, 120)
        Location.FOREST -> android.graphics.Color.rgb(40, 100, 40)
        Location.WASTELAND -> android.graphics.Color.rgb(140, 120, 80)
    }

    fun moveTo(location: Location) {
        currentLocation = location
    }
}
