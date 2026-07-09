package com.example.myapplication.renderer.mobs

import android.graphics.Color
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class HornRenderer : BaseMobRenderer(
    spriteName = "mob_horn",
    fallbackColor = Color.rgb(150, 100, 50),
    size = 30f
) {
    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    override fun getBattleScale(): Float = 2.8f
    override fun getMapSize(): Float = 150f
}
