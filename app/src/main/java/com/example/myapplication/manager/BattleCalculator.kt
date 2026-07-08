package com.example.myapplication.manager

import com.example.myapplication.model.Inventory
import com.example.myapplication.model.Mob
import com.example.myapplication.model.Player
import kotlin.random.Random

object BattleCalculator {
    fun calculatePlayerDamage(
        player: Player,
        inventory: Inventory,
        mob: Mob,
        random: Random
    ): Int {
        val baseDamage = player.getDamage(inventory)
        val bonus = random.nextInt(5)
        val defense = mob.defense.toFloat()
        var damage = (baseDamage + bonus).toFloat() - defense
        damage = maxOf(1f, damage)

        if (mob.isBoss) damage *= 0.5f
        return damage.toInt()
    }

    fun calculateMobDamage(
        mob: Mob,
        player: Player,
        inventory: Inventory,
        random: Random
    ): Float {
        var damage = mob.attack.toFloat() + (random.nextInt(7) - 3)
        if (mob.isBoss) damage *= 1.5f
        val defense = player.getDefense(inventory)
        return maxOf(1f, damage - defense)
    }
}
