package com.example.myapplication.manager

import com.example.myapplication.model.Inventory
import com.example.myapplication.model.Mob
import com.example.myapplication.model.Player
import kotlin.random.Random

object BattleCalculator {

    data class PlayerDamageResult(
        val damage: Int,
        val isCritical: Boolean,
        val isDodged: Boolean
    )

    data class MobDamageResult(
        val damage: Float,
        val isDodged: Boolean
    )

    fun calculatePlayerDamage(
        player: Player,
        inventory: Inventory,
        mob: Mob,
        random: Random = Random
    ): PlayerDamageResult {
        val mobDodgeChance = 5f + mob.level * 2f
        if (random.nextInt(100) < mobDodgeChance.toInt()) {
            return PlayerDamageResult(0, false, true)
        }

        val baseDamage = player.getDamage(inventory)
        val randomBonus = random.nextInt(5)
        var damage = (baseDamage + randomBonus).toFloat()

        val mobDefense = mob.defense.toFloat()
        damage = maxOf(1f, damage - mobDefense)

        val critChance = player.getCritChance(inventory)
        var isCritical = false
        if (random.nextFloat() * 100 < critChance) {
            isCritical = true
            damage *= player.getCritDamage(inventory)
        }

        if (mob.isBoss) {
            damage *= 0.5f
        }

        return PlayerDamageResult(
            damage = damage.toInt(),
            isCritical = isCritical,
            isDodged = false
        )
    }

    fun calculateMobDamage(
        mob: Mob,
        player: Player,
        inventory: Inventory,
        random: Random = Random
    ): MobDamageResult {
        val dodgeChance = player.getDodgeChance(inventory)
        if (random.nextFloat() * 100 < dodgeChance) {
            return MobDamageResult(0f, true)
        }

        var damage = mob.attack.toFloat()
        damage += (random.nextInt(7) - 3).toFloat()

        if (mob.isBoss) {
            damage *= 1.5f
        }

        val playerDefense = player.getDefense(inventory)
        damage = maxOf(1f, damage - playerDefense)

        return MobDamageResult(damage, false)
    }

    fun calculateExperienceReward(
        mob: Mob,
        playerLevel: Int,
        player: Player,
        inventory: Inventory
    ): Int {
        val levelDiff = mob.level - playerLevel
        var expReward = if (mob.isBoss) {
            mob.level * 50
        } else {
            mob.level * 10
        }

        when {
            levelDiff <= -2 -> expReward = (expReward * 0.3f).toInt()
            levelDiff >= 3 -> expReward = (expReward * 1.5f).toInt()
        }

        val expBonus = player.getExpBonus(inventory)
        expReward = (expReward * (1f + expBonus / 100f)).toInt()

        return maxOf(1, expReward)
    }

    fun calculateGoldReward(
        mob: Mob,
        player: Player,
        inventory: Inventory,
        random: Random = Random
    ): Int {
        var gold = if (mob.isBoss) {
            random.nextInt(50, 150)
        } else {
            random.nextInt(1, 20)
        }

        val goldBonus = player.getGoldBonus(inventory)
        gold = (gold * (1f + goldBonus / 100f)).toInt()

        return maxOf(1, gold)
    }
}
