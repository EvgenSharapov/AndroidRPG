package com.example.myapplication.manager

import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.example.myapplication.model.*
import com.example.myapplication.renderer.BattleRenderer
import kotlin.random.Random

class BattleManager {
    enum class BattleState {
        NONE, STARTED, PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT
    }

    var state = BattleState.NONE
    var currentMob: Mob? = null
    var player: Player? = null
    var onBattleEnd: ((Boolean) -> Unit)? = null
    var onItemDrop: ((Item) -> Unit)? = null

    private var turnTimer = 0
    private var isProcessing = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val random = Random
    private val dropManager = DropManager()
    var inventory: Inventory? = null

    fun startBattle(player: Player, mob: Mob, inventory: Inventory) {
        this.player = player
        this.currentMob = mob
        this.inventory = inventory

        if (mob.isBoss) {
            mob.hp = mob.maxHp
            println("👑 Босс готов к бою: HP ${mob.hp}/${mob.maxHp}")
        } else {
            if (mob.hp < mob.maxHp) {
                mob.hp = mob.maxHp
                println("🔄 Моб восстановлен: ${mob.getTypeName()} HP ${mob.hp}/${mob.maxHp}")
            }
        }

        state = BattleState.STARTED
        turnTimer = 0
        isProcessing = false
        BattleRenderer.reset()
    }

    fun update() {
        if (state == BattleState.NONE || player == null || currentMob == null) return

        when (state) {
            BattleState.STARTED -> {
                state = BattleState.PLAYER_TURN
                println("⚔️ Ход игрока! Атакуйте!")
            }

            BattleState.ENEMY_TURN -> {
                if (!isProcessing) {
                    isProcessing = true
                    turnTimer = 0

                    if (currentMob?.type == 0) {
                        BattleRenderer.triggerFluffyAttack()
                    } else {
                        BattleRenderer.triggerMobAttack(currentMob?.type ?: -1)
                    }

                    mainHandler.postDelayed({
                        executeMobAttack()
                    }, 800)
                }
            }

            BattleState.VICTORY -> {
                if (!isProcessing) {
                    isProcessing = true
                    mainHandler.postDelayed({
                        onBattleEnd?.invoke(true)
                        isProcessing = false
                    }, 2000)
                }
            }

            BattleState.DEFEAT -> {
                // Обрабатывается в executeMobAttack
            }

            else -> {}
        }
    }

    private fun executeMobAttack() {
        val mob = currentMob ?: run {
            isProcessing = false
            return
        }
        val player = player ?: run {
            isProcessing = false
            return
        }
        val inventory = inventory ?: run {
            isProcessing = false
            return
        }

        val result = BattleCalculator.calculateMobDamage(mob, player, inventory, random)

        if (result.isDodged) {
            BattleRenderer.showBattleDamageNumber(
                "player",
                "УВОРОТ!",
                Color.rgb(100, 200, 255),
                -100f
            )
            println("💨 Игрок уклонился от атаки ${mob.getTypeName()}!")
            state = BattleState.PLAYER_TURN
            isProcessing = false
            return
        }

        val damage = result.damage
        player.hp -= damage
        if (player.hp < 0) player.hp = 0f

        BattleRenderer.triggerHitEffect(player.x, player.y)
        BattleRenderer.showBattleDamageNumber(
            "player",
            "-${damage.toInt()}",
            Color.RED,
            -100f
        )

        // ⭐ ИСПРАВЛЕНО: используем getMaxHp(inventory) для отображения
        val maxHp = player.getMaxHp(inventory)
        println("💥 ${mob.getTypeName()} нанёс ${damage.toInt()} урона игроку! HP: ${player.hp.toInt()}/$maxHp")

        if (player.hp <= 0) {
            handlePlayerDeath(mob)
        } else {
            state = BattleState.PLAYER_TURN
            isProcessing = false
            println("👉 Ход игрока! Атакуйте!")
        }
    }

    private fun handlePlayerDeath(mob: Mob) {
        player?.hp = 0f
        state = BattleState.DEFEAT

        mob.hp = mob.maxHp
        if (mob.isBoss) {
            println("👑 Босс восстановил HP (смерть игрока): ${mob.hp}/${mob.maxHp}")
        } else {
            println("🔄 Моб восстановил HP (смерть игрока): ${mob.hp}/${mob.maxHp}")
        }

        BattleRenderer.showBattleDamageNumber(
            "player",
            "💀 ПОРАЖЕНИЕ!",
            Color.RED,
            -150f
        )

        mainHandler.postDelayed({
            onBattleEnd?.invoke(false)
            isProcessing = false
        }, 2000)
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun playerAttack() {
        if (state != BattleState.PLAYER_TURN || currentMob == null || isProcessing) return

        if (inventory == null) {
            println("❌ inventory == null в playerAttack!")
            return
        }

        isProcessing = true
        val mob = currentMob!!
        val inv = inventory!!

        BattleRenderer.triggerPlayerAttack()

        val result = BattleCalculator.calculatePlayerDamage(player!!, inv, mob, random)

        if (result.isDodged) {
            BattleRenderer.showBattleDamageNumber(
                "mob",
                "УВОРОТ!",
                Color.rgb(100, 200, 255),
                -100f
            )
            println("💨 ${mob.getTypeName()} уклонился от атаки!")
            state = BattleState.ENEMY_TURN
            isProcessing = false
            return
        }

        val damage = result.damage.toFloat()
        mob.hp -= damage
        if (mob.hp < 0) mob.hp = 0f

        val damageText = if (result.isCritical) {
            "-${damage.toInt()} 💥 КРИТ!"
        } else {
            "-${damage.toInt()}"
        }

        val damageColor = if (result.isCritical) {
            Color.rgb(255, 200, 50)
        } else {
            Color.YELLOW
        }

        BattleRenderer.triggerHitEffect(mob.x, mob.y)
        BattleRenderer.showBattleDamageNumber(
            "mob",
            damageText,
            damageColor,
            -100f
        )

        val knockbackX = if (mob.isBoss) 20f else 40f
        val knockbackY = if (mob.isBoss) -10f else -15f
        BattleRenderer.knockbackMob(knockbackX, knockbackY)

        println("⚔️ Игрок нанёс ${damage.toInt()} урона ${mob.getTypeName()}! HP: ${mob.hp.toInt()}/${mob.maxHp.toInt()}")

        mainHandler.postDelayed({
            if (mob.hp <= 0) {
                handleMobDeath(mob)
            } else {
                state = BattleState.ENEMY_TURN
                println("👹 Ход ${mob.getTypeName()}!")
            }
            isProcessing = false
        }, 500)
    }

    private fun handleMobDeath(mob: Mob) {
        mob.hp = 0f
        mob.isDead = true
        state = BattleState.VICTORY

        val player = player ?: return
        val inventory = inventory ?: return

        val (droppedItems, gold) = dropManager.getDropForMob(mob)

        if (gold > 0) {
            player.gold += gold
            BattleRenderer.showBattleDamageNumber(
                "mob",
                if (mob.isBoss) "💰 +${gold} золота! (БОСС)" else "💰 +${gold} золота!",
                Color.rgb(255, 215, 0),
                -180f
            )
            println("💰 Добавлено $gold золота! Всего: ${player.gold}")
        }

        for (item in droppedItems) {
            onItemDrop?.invoke(item)
            println("📦 Дроп: ${item.name}")
        }

        // ⭐ ИСПРАВЛЕНО: используем обновленный метод с бонусами
        val expReward = BattleCalculator.calculateExperienceReward(
            mob = mob,
            playerLevel = player.level,
            player = player,
            inventory = inventory
        )
        player.exp += expReward

        val expText = if (mob.isBoss) "👑 +${expReward} EXP! (БОСС)" else "+${expReward} EXP 💫"
        val expColor = if (mob.isBoss) Color.rgb(255, 215, 0) else Color.rgb(100, 200, 255)

        BattleRenderer.showBattleDamageNumber(
            "mob",
            expText,
            expColor,
            -150f
        )

        println("✅ Добавлено $expReward опыта! Моб уровня ${mob.level}, игрок уровня ${player.level}")

        checkPlayerLevelUp()

        mainHandler.postDelayed({
            onBattleEnd?.invoke(true)
        }, 1500)
    }

    private fun checkPlayerLevelUp() {
        val player = player ?: return
        val inventory = inventory ?: return
        var leveledUp = false

        while (player.exp >= player.maxExp) {
            player.exp -= player.maxExp
            player.level++
            player.maxExp = (player.maxExp * 1.5f).toInt()
            player.skillPoints += 5
            leveledUp = true
            println("🎉 УРОВЕНЬ ${player.level}! +5 очков прокачки!")
        }

        if (leveledUp) {
            // ⭐ ИСПРАВЛЕНО: используем getMaxHp(inventory)
            player.hp = player.getMaxHp(inventory)
            BattleRenderer.showBattleDamageNumber(
                "player",
                "🎉 УРОВЕНЬ ${player.level}!",
                Color.rgb(255, 215, 0),
                -200f
            )
            println("✅ HP восстановлен: ${player.hp}")
        }
    }

    fun endBattle() {
        state = BattleState.NONE
        currentMob = null
        player = null
        isProcessing = false
        BattleRenderer.reset()
    }

    fun isBattleActive(): Boolean = state != BattleState.NONE
}
