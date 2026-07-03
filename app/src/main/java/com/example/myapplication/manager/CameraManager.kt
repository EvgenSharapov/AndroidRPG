package com.example.myapplication.manager

import kotlin.math.max
import kotlin.math.min

class CameraManager {
    var x = 0f
    var y = 0f

    fun follow(targetX: Float, targetY: Float, screenWidth: Float, screenHeight: Float, worldWidth: Float, worldHeight: Float) {
        x = targetX - screenWidth / 2
        y = targetY - screenHeight / 2
        x = max(0f, min(x, worldWidth - screenWidth))
        y = max(0f, min(y, worldHeight - screenHeight))
    }
}
