package com.example.myapplication.renderer.mobs

import android.graphics.Color
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class SteelKnightRenderer : BaseMobRenderer(
    spriteName = "steel_knight",
    fallbackColor = Color.rgb(150, 150, 200),
    size = 25f
) {
    private val battleSpriteName = "steel_knight_battle"

    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    // ИСПРАВЛЕНО: override val вместо fun
    override val spriteName: String
        get() = if (isAttacking) battleSpriteName else super.spriteName

    override fun getBattleScale(): Float = 3.5f
    override fun getMapSize(): Float = 130f
}