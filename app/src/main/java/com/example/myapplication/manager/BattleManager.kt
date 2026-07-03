package com.example.myapplication.manager

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import com.example.myapplication.model.*
import com.example.myapplication.renderer.BattleRenderer

class BattleManager {
    enum class BattleState {
        NONE, STARTED, PLAYER_TURN, ENEMY_TURN, VICTORY, DEFEAT
    }

    var state = BattleState.NONE
    var currentMob: Mob? = null
    var player: Player? = null
    var onBattleEnd: (() -> Unit)? = null

    var onItemDrop: ((Item) -> Unit)? = null

    private var turnTimer = 0
    private var isProcessing = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val random = java.util.Random()

    fun startBattle(player: Player, mob: Mob) {
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

                    // Запускаем анимацию атаки моба
                    if (currentMob?.type == 0) {
                        // Флаффи атакует с анимацией подбегания
                        BattleRenderer.triggerFluffyAttack()
                    } else {
                        BattleRenderer.triggerMobAttack()
                    }

                    mainHandler.postDelayed({
                        val mob = currentMob!!
                        val damage = 5f + random.nextInt(10).toFloat()
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
                                onBattleEnd?.invoke()
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
                        onBattleEnd?.invoke()
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

    fun playerAttack() {
        if (state != BattleState.PLAYER_TURN || currentMob == null || isProcessing) return

        isProcessing = true
        val mob = currentMob!!

        BattleRenderer.triggerPlayerAttack()

        val baseDamage = 10f + (player?.level?.times(2) ?: 2).toFloat()
        val bonusDamage = when (mob.type) {
            0 -> 5f
            1 -> 0f
            2 -> -5f
            else -> 0f
        }
        val damage = baseDamage + bonusDamage + random.nextInt(5).toFloat()
        mob.hp -= damage
        if (mob.hp < 0) mob.hp = 0f

        BattleRenderer.triggerHitEffect(mob.x, mob.y)
        BattleRenderer.showDamageNumber(mob.x, mob.y - 50f, "-${damage.toInt()}", Color.YELLOW)
        BattleRenderer.knockbackMob(40f, -15f)

        mainHandler.postDelayed({
            if (mob.hp <= 0) {
                mob.hp = 0f
                mob.isDead = true
                state = BattleState.VICTORY

                // ⭐ ДРОП ПРЕДМЕТОВ
                // Шанс дропа меча с Флаффи (20%)
                if (mob.type == 0) {  // Флаффи
                    val dropChance = random.nextInt(100)
                    if (dropChance < 20) {
                        val sword = Item(
                            id = "sword_01",
                            name = "Старый меч",
                            type = Item.ItemType.WEAPON,
                            icon = null,
                            description = "Простой меч, найденный у Флаффи",
                            stats = ItemStats(attack = 5)
                        )
                        // ⭐ ВЫЗЫВАЕМ CALLBACK!
                        onItemDrop?.invoke(sword) ?: println("❌ onItemDrop is NULL!")

                        BattleRenderer.showDamageNumber(
                            mob.x, mob.y - 130f,
                            "🗡️ Дроп: Старый меч!", Color.rgb(255, 200, 100)
                        )
                        println("🗡️ Дроп: Старый меч! (20%)")
                    }
                }

                // ⭐ РАСЧЁТ ОПЫТА
                val playerLevel = player?.level ?: 1
                val mobLevel = mob.level
                val levelDiff = mobLevel - playerLevel
                var expReward = mobLevel * 10

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
                    "+${expReward} EXP 💫", Color.rgb(100, 200, 255)
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
