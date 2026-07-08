package com.example.myapplication.renderer.mobs

import android.graphics.Color
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class GoblinRenderer : BaseMobRenderer(
    spriteName = "goblin",
    fallbackColor = Color.rgb(50, 180, 50),
    size = 25f
) {
    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.WALK_LEFT -> "walk_left"
        MobAction.WALK_RIGHT -> "walk_right"
        else -> "idle"
    }

    override fun getBattleScale(): Float = 3.0f
    override fun getMapSize(): Float = 110f
}
