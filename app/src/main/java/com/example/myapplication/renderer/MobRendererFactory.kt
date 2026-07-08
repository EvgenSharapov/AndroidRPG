package com.example.myapplication.renderer

import com.example.myapplication.model.IMobRenderer

object MobRendererFactory {
    private val renderers = mutableMapOf<Int, IMobRenderer>()

    init {
        register(0, FluffyRenderer())
        register(1, SpiderRenderer())
        register(2, ManyEyesRenderer())
        register(3, RedKnightRenderer())
        register(4, SlimeGreenRenderer())
        register(5, SteelKnightRenderer())
        register(6, GoblinRenderer())
        register(7, MonkRenderer())
        register(8, OrkRenderer())
        register(9, TrollRenderer())
    }

    fun getRenderer(type: Int): IMobRenderer = renderers[type] ?: DefaultRenderer()
    private fun register(type: Int, renderer: IMobRenderer) { renderers[type] = renderer }
}
