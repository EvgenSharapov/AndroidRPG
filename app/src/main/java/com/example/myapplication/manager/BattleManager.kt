package com.example.myapplication.manager

import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.example.myapplication.model.*
import com.example.myapplication.renderer.BattleRenderer

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
    private val random = java.util.Random()
    private val dropManager = DropManager()

    fun startBattle(player: Player, mob: Mob, inventory: Inventory) {
        this.player = player
        this.currentMob = mob
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
            }
            BattleState.ENEMY_TURN -> {
                if (!isProcessing) {
                    isProcessing = true
                    turnTimer = 0

                    if (currentMob?.type == 0) {
                        BattleRenderer.triggerFluffyAttack()
                    } else {
                        // ⭐ ПЕРЕДАЁМ ТИП МОБА ДЛЯ АНИМАЦИИ АТАКИ
                        BattleRenderer.triggerMobAttack(currentMob?.type ?: -1)
                    }

                    mainHandler.postDelayed({
                        val mob = currentMob!!
                        var damage = 5f + random.nextInt(10).toFloat()

                        when (mob.type) {
                            3, 4, 5 -> damage *= 2f
                        }

                        if (mob.isBoss) {
                            damage *= 1.5f
                        }
                        player!!.hp -= damage

                        if (player!!.hp < 0) player!!.hp = 0f

                        BattleRenderer.triggerHitEffect(player!!.x, player!!.y)
                        BattleRenderer.showDamageNumber(
                            player!!.x, player!!.y - 50f,
                            "-${damage.toInt()}", Color.RED
                        )

                        if (player!!.hp <= 0) {
                            player!!.hp = 0f
                            state = BattleState.DEFEAT
                            mainHandler.postDelayed({
                                onBattleEnd?.invoke(false)
                            }, 2000)
                        } else {
                            state = BattleState.PLAYER_TURN
                        }
                        isProcessing = false
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
                // Уже обработано выше
            }
            else -> {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun playerAttack() {
        if (state != BattleState.PLAYER_TURN || currentMob == null || isProcessing) return

        isProcessing = true
        val mob = currentMob!!

        BattleRenderer.triggerPlayerAttack()

        val baseDamage = 10f + (player?.level?.times(2) ?: 2).toFloat()
        val bossMultiplier = if (mob.isBoss) 0.5f else 1f
        val bonusDamage = when (mob.type) {
            0 -> 5f
            1 -> 0f
            2 -> -5f
            else -> 0f
        }
        val damage = (baseDamage + bonusDamage + random.nextInt(5).toFloat()) * bossMultiplier
        mob.hp -= damage
        if (mob.hp < 0) mob.hp = 0f

        if (mob.isBoss) {
            BattleRenderer.triggerHitEffect(mob.x, mob.y)
            BattleRenderer.showDamageNumber(mob.x, mob.y - 70f, "-${damage.toInt()} 💥", Color.rgb(255, 200, 100))
            BattleRenderer.knockbackMob(20f, -10f)
        } else {
            BattleRenderer.triggerHitEffect(mob.x, mob.y)
            BattleRenderer.showDamageNumber(mob.x, mob.y - 50f, "-${damage.toInt()}", Color.YELLOW)
            BattleRenderer.knockbackMob(40f, -15f)
        }

        mainHandler.postDelayed({
            if (mob.hp <= 0) {
                mob.hp = 0f
                mob.isDead = true
                state = BattleState.VICTORY

                val (droppedItems, gold) = dropManager.getDropForMob(mob)

                if (gold > 0) {
                    player?.gold = (player?.gold ?: 0) + gold
                    val goldColor = if (mob.isBoss) Color.rgb(255, 215, 0) else Color.rgb(255, 215, 0)
                    BattleRenderer.showDamageNumber(
                        mob.x, mob.y - 130f,
                        if (mob.isBoss) "💰 +${gold} золота! (БОСС)" else "💰 +${gold} золота!",
                        goldColor
                    )
                    println("💰 Добавлено $gold золота! Всего: ${player?.gold}")
                }

                for (item in droppedItems) {
                    onItemDrop?.invoke(item)
                    println("📦 Дроп: ${item.name}")
                }

                val playerLevel = player?.level ?: 1
                val mobLevel = mob.level
                val levelDiff = mobLevel - playerLevel

                var expReward = if (mob.isBoss) mobLevel * 50 else mobLevel * 10

                if (levelDiff <= -2) {
                    expReward = (expReward * 0.3f).toInt()
                    println("⚠️ Моб слабее на ${-levelDiff} уровня. Опыт уменьшен до 30% ($expReward)")
                } else if (levelDiff >= 3) {
                    expReward = (expReward * 1.5f).toInt()
                    println("🔥 Моб сильнее на $levelDiff уровня. Опыт увеличен до 150% ($expReward)")
                }

                player?.exp = (player?.exp ?: 0) + expReward
                println("✅ Добавлено $expReward опыта! Моб уровня ${mob.level}, игрок уровня ${playerLevel}")

                BattleRenderer.showDamageNumber(
                    mob.x, mob.y - 100f,
                    if (mob.isBoss) "👑 +${expReward} EXP! (БОСС)" else "+${expReward} EXP 💫",
                    if (mob.isBoss) Color.rgb(255, 215, 0) else Color.rgb(100, 200, 255)
                )
            } else {
                state = BattleState.ENEMY_TURN
            }
            isProcessing = false
        }, 500)
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