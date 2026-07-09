package com.example.myapplication.renderer.mobs

import android.graphics.Color
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class WolfRenderer : BaseMobRenderer(
    spriteName = "mob_wolf",
    fallbackColor = Color.rgb(180, 180, 200),
    size = 35f
) {
    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    override fun getBattleScale(): Float = 3.0f
    override fun getMapSize(): Float = 160f
}
