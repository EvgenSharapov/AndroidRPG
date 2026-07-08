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
    var inventory: Inventory? = null

    fun startBattle(player: Player, mob: Mob, inventory: Inventory) {
        this.player = player
        this.currentMob = mob
        this.inventory = inventory

        // ⭐ ЕСЛИ ЭТО БОСС — УБЕДИМСЯ, ЧТО У НЕГО ПОЛНОЕ HP
        if (mob.isBoss) {
            mob.hp = mob.maxHp
            println("👑 Босс готов к бою: HP ${mob.hp}/${mob.maxHp}")
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
                        val mob = currentMob!!

                        // ⭐ РАСЧЁТ УРОНА МОБА
                        var damage = mob.attack.toFloat()  // Используем атаку моба
                        damage += (random.nextInt(7) - 3).toFloat()  // Случайность ±3

                        if (mob.isBoss) {
                            damage *= 1.5f  // Боссы бьют сильнее
                        }

                        // ⭐ ЗАЩИТА ИГРОКА
                        val playerDefense = if (inventory != null) {
                            player!!.getDefense(inventory!!)
                        } else {
                            0
                        }

                        // Урон не может быть меньше 1
                        damage = maxOf(1f, damage - playerDefense)

                        // Наносим урон игроку
                        player!!.hp -= damage
                        if (player!!.hp < 0) player!!.hp = 0f

                        // Эффекты и отображение урона
                        BattleRenderer.triggerHitEffect(player!!.x, player!!.y)
                        BattleRenderer.showBattleDamageNumber(
                            "player",
                            "-${damage.toInt()}",
                            Color.RED,
                            -100f
                        )

                        // Проверка смерти игрока
                        if (player!!.hp <= 0) {
                            player!!.hp = 1f
                            state = BattleState.DEFEAT

                            // Восстанавливаем HP моба при смерти игрока
                            mob.hp = mob.maxHp
                            if (mob.isBoss) {
                                println("👑 Босс восстановил HP (смерть игрока): ${mob.hp}/${mob.maxHp}")
                            } else {
                                println("🔄 Моб восстановил HP (смерть игрока): ${mob.hp}/${mob.maxHp}")
                            }

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

        // ⭐ ПРОВЕРЯЕМ НАЛИЧИЕ INVENTORY
        if (inventory == null) {
            println("❌ inventory == null в playerAttack!")
            return
        }

        isProcessing = true
        val mob = currentMob!!
        val inv = inventory!!

        BattleRenderer.triggerPlayerAttack()

        // ⭐ РАСЧЁТ УРОНА ИГРОКА
        val playerDamage = player!!.getDamage(inv)
        val randomBonus = random.nextInt(5)
        var damage = playerDamage + randomBonus.toFloat()

        // ⭐ ЗАЩИТА МОБА
        val mobDefense = mob.defense.toFloat()
        damage = maxOf(1f, damage - mobDefense)  // Минимум 1 урона

        // ⭐ МНОЖИТЕЛЬ ДЛЯ БОССА (боссы получают меньше урона)
        val bossMultiplier = if (mob.isBoss) 0.5f else 1f
        damage *= bossMultiplier

        // Наносим урон мобу
        mob.hp -= damage
        if (mob.hp < 0) mob.hp = 0f

        // ⭐ ЭФФЕКТЫ И ОТОБРАЖЕНИЕ УРОНА
        if (mob.isBoss) {
            BattleRenderer.triggerHitEffect(mob.x, mob.y)
            BattleRenderer.showBattleDamageNumber(
                "mob",
                "-${damage.toInt()} 💥",
                Color.rgb(255, 200, 100),
                -120f
            )
            BattleRenderer.knockbackMob(20f, -10f)
        } else {
            BattleRenderer.triggerHitEffect(mob.x, mob.y)
            BattleRenderer.showBattleDamageNumber(
                "mob",
                "-${damage.toInt()}",
                Color.YELLOW,
                -100f
            )
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
                    BattleRenderer.showBattleDamageNumber(
                        "mob",
                        if (mob.isBoss) "💰 +${gold} золота! (БОСС)" else "💰 +${gold} золота!",
                        Color.rgb(255, 215, 0),
                        -180f
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

                BattleRenderer.showBattleDamageNumber(
                    "mob",
                    if (mob.isBoss) "👑 +${expReward} EXP! (БОСС)" else "+${expReward} EXP 💫",
                    if (mob.isBoss) Color.rgb(255, 215, 0) else Color.rgb(100, 200, 255),
                    -150f
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
