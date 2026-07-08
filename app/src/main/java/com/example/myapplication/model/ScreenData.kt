package com.example.myapplication.model

import com.example.myapplication.GameView

data class ScreenData(
    val player: Player,
    val inventory: Inventory,
    val gameView: GameView,
    val locationManager: Any? = null
)
