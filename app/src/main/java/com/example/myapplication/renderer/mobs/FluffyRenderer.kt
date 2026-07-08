package com.example.myapplication.renderer.mobs

import android.graphics.Canvas
import android.graphics.Color
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class FluffyRenderer : BaseMobRenderer(
    spriteName = "fluffy",
    fallbackColor = Color.rgb(255, 200, 200),
    size = 25f
) {
    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    override fun getBattleScale(): Float = 3.0f
    override fun getMapSize(): Float = 100f
}
