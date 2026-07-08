package com.example.myapplication.renderer.mobs

import android.graphics.Color
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.BaseMobRenderer

class SpiderRenderer : BaseMobRenderer(
    spriteName = "spider",
    fallbackColor = Color.rgb(100, 150, 50),
    size = 25f
) {
    private val battleSpriteName = "spider_battle"

    override fun getAnimationName(action: MobAction): String = when (action) {
        MobAction.IDLE -> "idle"
        MobAction.ATTACK -> "attack"
        MobAction.RUN -> "run"
        else -> "idle"
    }

    override val spriteName: String
        get() = if (isAttacking) battleSpriteName else super.spriteName

    override fun getBattleScale(): Float = 2.8f
}
