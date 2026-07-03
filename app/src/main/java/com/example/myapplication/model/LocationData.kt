package com.example.myapplication.model

import android.graphics.RectF

data class LocationData(
    var name: String,
    var mobs: MutableList<Mob>,
    var npcs: MutableList<NPC>,
    var buildings: MutableList<RectF>
)
