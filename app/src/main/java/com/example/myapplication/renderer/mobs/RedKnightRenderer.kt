package com.example.myapplication.renderer.mobs

import android.graphics.Color
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class RedKnightRenderer : BaseMobRenderer(
    spriteName = "red_knight",
    fallbackColor = Color.rgb(200, 50, 50),
    size = 25f
) {
    private val battleSpriteName = "red_knight_battle"

    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    // ИСПРАВЛЕНО: override val вместо fun
    override val spriteName: String
        get() = if (isAttacking) battleSpriteName else super.spriteName

    override fun getBattleScale(): Float = 3.2f
    override fun getMapSize(): Float = 120f
}