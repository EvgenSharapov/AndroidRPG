package com.example.myapplication.renderer

import com.example.myapplication.model.IMobRenderer
import com.example.myapplication.model.MobAction
import com.example.myapplication.renderer.mobs.*

object MobRendererFactory {
    private val renderers = mutableMapOf<Int, IMobRenderer>()
    private val defaultRenderer = DefaultMobRenderer()

    init {
        register(0, AndreRenderer())
        register(1, HornRenderer())
        register(2, WolfRenderer())
        register(3, RedKnightRenderer())
        register(4, SlimeGreenRenderer())
        register(5, SteelKnightRenderer())
        register(6, GoblinRenderer())
        register(7, MonkRenderer())
        register(8, OrkRenderer())
        register(9, TrollRenderer())
    }

    fun getRenderer(type: Int): IMobRenderer {
        return renderers[type] ?: defaultRenderer
    }

    private fun register(type: Int, renderer: IMobRenderer) {
        renderers[type] = renderer
    }
}

class DefaultMobRenderer : BaseMobRenderer(
    spriteName = "default",
    fallbackColor = android.graphics.Color.GRAY,
    size = 25f
) {
    override fun getAnimationName(action: MobAction): String = "idle"
}
