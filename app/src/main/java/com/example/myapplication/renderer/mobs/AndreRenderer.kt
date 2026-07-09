package com.example.myapplication.renderer.mobs

import android.graphics.Color
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class AndreRenderer : BaseMobRenderer(
    spriteName = "mob_andre",
    fallbackColor = Color.rgb(100, 150, 200),
    size = 25f
) {
    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    override fun getBattleScale(): Float = 2.8f
    override fun getMapSize(): Float = 100f

    fun getBattleAnimationName(): String = "attack"
}
