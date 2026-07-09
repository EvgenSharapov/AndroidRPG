package com.example.myapplication

import com.example.myapplication.model.Player
import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.MotionEvent
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import com.example.myapplication.manager.*
import com.example.myapplication.model.*
import com.example.myapplication.renderer.*
import com.example.myapplication.ui.InventoryScreen
import com.example.myapplication.ui.StatsScreen
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import android.graphics.Color
import com.example.myapplication.ui.MapScreen
import com.example.myapplication.ui.ShopScreen
import com.google.gson.reflect.TypeToken
import kotlin.math.sin

class GameView(context: Context) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private var isRunning = false

    private enum class GameState {
        GAME, MAP, BATTLE, STATS, INVENTORY, SHOP
    }
    private var gameState = GameState.GAME

    private val locationManager = LocationManager()
    private val cameraManager = CameraManager()
    private val battleManager = BattleManager()

    private val player = Player(speed = 6f)
    private var selectedMob: Mob? = null

    private var screenWidth = 0f
    private var screenHeight = 0f
    private val worldWidth = 1200f
    private val worldHeight = 2000f

    private var messageText = ""
    private var messageTimer = 0
    private var lastTouchTime = 0L
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var bossOnMap = false

    // МЕНЮ
    private var isMenuOpen = false
    private val menuButtonSize = 105f

    private val POPUP_MENU_WIDTH = 700f
    private val POPUP_MENU_HEIGHT = 700f
    private val POPUP_BTN_WIDTH = 620f
    private val POPUP_BTN_HEIGHT = 70f
    private val POPUP_BTN_SPACING = 15f
    private val POPUP_CLOSE_SIZE = 45f

    // UI Экраны
    private lateinit var mapScreen: MapScreen
    private lateinit var statsScreen: StatsScreen
    private lateinit var shopScreen: ShopScreen
    private lateinit var inventoryScreen: InventoryScreen
    private lateinit var spriteManager: SpriteManager
    private lateinit var saveManager: SaveManager

    private val inventory = Inventory()

    // Анимация персонажа
    private var currentSprite = "character_zombie"
    private var currentAnimation = "idle"
    private var currentFrameIndex = 0
    private var frameTimer = 0

    private var targetMob: Mob? = null  // ← моб, к которому идем
    private val BATTLE_DISTANCE = 60f   // ← дистанция для начала боя
    private var isMovingToMob = false   // ← флаг движения к мобу
    private var battleStartCooldown = 0 // ← задержка перед боем

    // Анимация боя
    private var battleFrameIndex = 0
    private var battleFrameTimer = 0
    private var isAttackingInBattle = false
    private var isLevelChecked = false

    // Эффект респауна
    private var respawnEffectTimer = 0
    private var respawnEffectX = 0f
    private var respawnEffectY = 0f

    // Регенерация HP
    private var regenTimer = 0
    private val REGEN_INTERVAL = 60

    // сброс игры
    private var showResetFullConfirm = false

    init {
        spriteManager = SpriteManager(context)
        GameRenderer.loadBackgrounds(context)
        statsScreen = StatsScreen()
        inventoryScreen = InventoryScreen(context, spriteManager)
        saveManager = SaveManager(context)
        shopScreen = ShopScreen()
        mapScreen = MapScreen()

        loadGame()
    }

    // ========== ОСНОВНОЙ ЦИКЛ ==========
    override fun run() {
        while (isRunning) {
            update()
            draw()
            sleep()
        }
    }

    private fun update() {
        when (gameState) {
            GameState.GAME -> updateGame()
            GameState.BATTLE -> {
                battleManager.update()
                if ((battleManager.state == BattleManager.BattleState.VICTORY ||
                            battleManager.state == BattleManager.BattleState.DEFEAT) &&
                    !isLevelChecked) {
                    isAttackingInBattle = false
                    isLevelChecked = true
                    checkLevelUp()
                }
            }
            GameState.MAP, GameState.STATS, GameState.INVENTORY, GameState.SHOP -> { /* Ничего не обновляем */ }
        }
        if (messageTimer > 0) messageTimer--
    }

    private fun updateGame() {
        // Движение игрока
        if (player.isMoving) {
            val dx = player.targetX - player.x
            val dy = player.targetY - player.y
            val dist = sqrt(dx * dx + dy * dy)

            // ⭐ ИСПОЛЬЗУЕМ СКОРОСТЬ С РУНАМИ
            val currentSpeed = player.getSpeed(inventory)

            if (dist < currentSpeed) {
                player.x = player.targetX
                player.y = player.targetY
                player.isMoving = false

                if (isMovingToMob && targetMob != null && !targetMob!!.isDead) {
                    startBattle(targetMob!!)
                    isMovingToMob = false
                    targetMob = null
                }
            } else {
                player.x += (dx / dist) * currentSpeed
                player.y += (dy / dist) * currentSpeed
                updateFacing(dx, dy)

                if (isMovingToMob && targetMob != null && !targetMob!!.isDead) {
                    val currentDist = sqrt(
                        (player.x - targetMob!!.x) * (player.x - targetMob!!.x) +
                                (player.y - targetMob!!.y) * (player.y - targetMob!!.y)
                    )

                    if (currentDist < BATTLE_DISTANCE) {
                        player.isMoving = false
                        player.x = player.targetX
                        player.y = player.targetY
                        startBattle(targetMob!!)
                        isMovingToMob = false
                        targetMob = null
                    }
                }
            }
        }

        // ⭐ ПРОВЕРЯЕМ, НЕ УМЕР ЛИ МОБ, К КОТОРОМУ МЫ ИДЕМ
        if (isMovingToMob && targetMob != null && targetMob!!.isDead) {
            println("💀 Моб умер, пока мы шли к нему!")
            isMovingToMob = false
            targetMob = null
            selectedMob = null
            player.isMoving = false
        }

        checkLocationTransition()

        // Обновление мобов через LocationManager
        locationManager.respawnMobs()

        // ⭐ РЕГЕНЕРАЦИЯ HP С УЧЕТОМ РУН
        if (gameState == GameState.GAME) {
            regenTimer++
            val regenAmount = player.getHpRegen(inventory)  // ← с учетом рун

            if (regenTimer >= REGEN_INTERVAL) {
                regenTimer = 0
                val maxHp = player.getMaxHp(inventory)  // ← с учетом рун
                if (player.hp < maxHp) {
                    player.hp = min(player.hp + regenAmount, maxHp)
                }
            }
        }
    }

    private fun updateFacing(dx: Float, dy: Float) {
        player.facing = when {
            abs(dx) > abs(dy) -> if (dx > 0) 2 else 1
            dy > 0 -> 0
            else -> 3
        }
    }

    private fun checkLocationTransition() {
        when (locationManager.currentLocation) {
            LocationManager.Location.CITY -> {
                if (player.x > worldWidth - 60f) {
                    locationManager.moveTo(LocationManager.Location.FOREST)
                    player.x = 60f
                    player.targetX = player.x
                    bossOnMap = false
                    // ⭐ СБРАСЫВАЕМ СОСТОЯНИЕ ДВИЖЕНИЯ К МОБУ
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("🌲 Вы вошли в Лес!")
                } else if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.WASTELAND)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("🏜️ Вы вошли в Пустошь!")
                }
            }
            LocationManager.Location.FOREST -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.CITY)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("🏙️ Вы вернулись в Город!")
                } else if (player.x > worldWidth - 60f) {
                    locationManager.moveTo(LocationManager.Location.ROCKS)
                    player.x = 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("⛰️ Вы вошли в Скалы!")
                }
            }
            LocationManager.Location.WASTELAND -> {
                if (player.x > worldWidth - 60f) {
                    locationManager.moveTo(LocationManager.Location.CITY)
                    player.x = 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("🏙️ Вы вернулись в Город!")
                } else if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.DESERT)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("🏜️ Вы вошли в Пустыню!")
                }
            }
            LocationManager.Location.ROCKS -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.FOREST)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("🌲 Вы вернулись в Лес!")
                } else if (player.x > worldWidth - 60f) {
                    locationManager.moveTo(LocationManager.Location.CASTLE)
                    player.x = 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("🏰 Вы вошли в Замок!")
                }
            }
            LocationManager.Location.CASTLE -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.ROCKS)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("⛰️ Вы вернулись в Скалы!")
                }
            }
            LocationManager.Location.DESERT -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.WASTELAND)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    isMovingToMob = false
                    targetMob = null
                    selectedMob = null
                    showMessage("🏜️ Вы вернулись в Пустошь!")
                }
            }
        }
    }

    private fun showMessage(text: String) {
        messageText = text
        messageTimer = 120
    }

    // ========== СИСТЕМА ПРОКАЧКИ ==========

    private fun checkLevelUp() {
        var leveledUp = false
        while (player.exp >= player.maxExp) {
            player.exp -= player.maxExp
            player.level++
            player.maxExp = (player.maxExp * 1.5f).toInt()
            player.skillPoints += 5
            leveledUp = true
            showMessage("🎉 УРОВЕНЬ ${player.level}! +5 очков прокачки!")
            saveGame()
        }

        if (leveledUp) {
            // ⭐ ИСПРАВЛЕНО: используем getMaxHp(inventory)
            player.hp = player.getMaxHp(inventory)
            println("✅ HP восстановлен: ${player.hp}")
        }
    }

    private fun openStats() {
        gameState = GameState.STATS
    }

    private fun closeStats() {
        gameState = GameState.GAME
        saveGame()
    }

    private fun upgradeStat(statType: Player.StatType) {
        if (player.upgradeStat(statType)) {
            val statName = when (statType) {
                Player.StatType.STRENGTH -> "Сила"
                Player.StatType.ENDURANCE -> "Выносливость"
                Player.StatType.AGILITY -> "Ловкость"
                Player.StatType.DEXTERITY -> "Сноровка"
                Player.StatType.LUCK -> "Удача"
            }
            showMessage("⬆ $statName +1!")

            if (statType == Player.StatType.ENDURANCE) {
                player.hp = player.calculateMaxHp()
            }
            saveGame()
        } else {
            showMessage("❌ Нет очков для прокачки!")
        }
    }

    // ========== БОЙ ==========

    private fun startBattle(mob: Mob) {
        // ⭐ ПРОВЕРЯЕМ, ЧТО МОБ НЕ МЕРТВ И НЕ БОСС (если бой уже идет)
        if (mob.isDead) {
            println("❌ Моб уже мертв!")
            return
        }

        if (gameState == GameState.BATTLE) {
            println("❌ Бой уже идет!")
            return
        }

        println("⚔️ startBattle() вызван! Моб: ${mob.getTypeName()}, isBoss=${mob.isBoss}")
        isLevelChecked = false

        // ⭐ СОХРАНЯЕМ ПОЗИЦИЮ ИГРОКА (она останется той же)
        // player.x и player.y уже установлены

        battleManager.onItemDrop = { item ->
            if (inventory.addItem(item)) {
                showMessage("🗡️ ${item.name} добавлен в инвентарь!")
                println("✅ ${item.name} добавлен в инвентарь")
                saveGame()
            } else {
                showMessage("⚠️ Инвентарь полон! ${item.name} потерян.")
                println("❌ Инвентарь полон! ${item.name} потерян.")
            }
        }

        battleManager.onBattleEnd = { victory ->
            println("🏆 Бой окончен! Победа: $victory, Босс: ${mob.isBoss}")

            if (mob.isBoss) {
                bossOnMap = false
                println("👑 Босс убит! Флаг bossOnMap = false")
            } else if (victory) {
                checkBossSpawn(mob)
            }

            gameState = GameState.GAME
            battleManager.endBattle()
            isAttackingInBattle = false

            // ⭐ НЕ МЕНЯЕМ ПОЗИЦИЮ ИГРОКА! ОСТАЕТСЯ НА МЕСТЕ
            // player.x и player.y остаются теми же, где был бой

            showMessage("Бой окончен!")
            saveGame()
        }

        battleManager.startBattle(player, mob, inventory)
        gameState = GameState.BATTLE
        isAttackingInBattle = false
        battleFrameIndex = 0
        battleFrameTimer = 0
        showMessage("⚔️ Бой начался!")
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun attackInBattle() {
        isAttackingInBattle = true
        battleFrameIndex = 0
        battleFrameTimer = 0

        battleManager.playerAttack()

        if (battleManager.state == BattleManager.BattleState.VICTORY) {
            android.os.Handler().postDelayed({
                battleManager.endBattle()
                gameState = GameState.GAME
                isAttackingInBattle = false
                checkLevelUp()
                saveGame()
            }, 1500)
        } else if (battleManager.state == BattleManager.BattleState.DEFEAT) {
            showMessage("💀 Вы погибли...")

            android.os.Handler().postDelayed({
                battleManager.endBattle()
                gameState = GameState.GAME

                val maxHp = player.calculateMaxHp()
                player.hp = 1f

                player.x = 400f
                player.y = 400f
                isAttackingInBattle = false
                saveGame()
                showMessage("💀 Вы возродились! HP: ${player.hp.toInt()}/${maxHp.toInt()}")
            }, 1500)
        }
    }

    private fun teleportToLocation(location: LocationManager.Location) {
        if (location == locationManager.currentLocation) {
            showMessage("Вы уже здесь!")
            return
        }
        locationManager.moveTo(location)
        player.x = worldWidth / 2
        player.y = worldHeight / 2
        player.targetX = player.x
        player.targetY = player.y
        player.isMoving = false
        selectedMob = null
        gameState = GameState.GAME
        showMessage("Телепортация в ${locationManager.getLocationName(location)}")
    }

    // ========== ОТРИСОВКА ==========

    private fun draw() {
        val canvas = holder.lockCanvas() ?: return
        screenWidth = canvas.width.toFloat()
        screenHeight = canvas.height.toFloat()

        try {
            when (gameState) {
                GameState.GAME -> drawGame(canvas)
                GameState.MAP -> {
                    drawGame(canvas)
                    mapScreen.draw(
                        canvas,
                        screenWidth,
                        screenHeight,
                        locationManager
                    ) { location ->
                        teleportToLocation(location)
                    }
                }
                GameState.BATTLE -> drawBattle(canvas)
                GameState.STATS -> {
                    drawGame(canvas)
                    val damage = player.getDamage(inventory)
                    val defense = player.getDefense(inventory)
                    statsScreen.draw(
                        canvas,
                        screenWidth,
                        screenHeight,
                        player,
                        inventory,
                        damage,
                        defense,
                        { statType: Player.StatType -> upgradeStat(statType) },
                        { closeStats() },
                        { resetStats() }
                    )
                }
                GameState.INVENTORY -> {
                    drawGame(canvas)
                    inventoryScreen.draw(
                        canvas,
                        screenWidth,
                        screenHeight,
                        inventory,
                        player,
                        this,
                        { closeInventory() },
                        { newName -> renamePlayer(newName) },
                        { message -> showMessage(message) }
                    )
                }
                GameState.SHOP -> {
                    drawGame(canvas)
                    shopScreen.draw(
                        canvas,
                        screenWidth,
                        screenHeight,
                        player,
                        { closeShop() },
                        { item -> buyShopItem(item) },
                        { openRenameFromShop() },
                        { resetStatsFromShop() }
                    )
                }
            }
        } finally {
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawGame(canvas: Canvas) {
        cameraManager.follow(player.x, player.y, screenWidth, screenHeight, worldWidth, worldHeight)

        val locationData = locationManager.getCurrentData()

        GameRenderer.drawBackground(canvas, locationManager.currentLocation, screenWidth, screenHeight)

        // Сетка
        val gridPaint = Paint().apply {
            color = Color.argb(50, 0, 0, 0)
            strokeWidth = 1f
        }
        val startX = -(cameraManager.x % 60f)
        val startY = -(cameraManager.y % 60f)
        for (i in -1..25) {
            val x = startX + i * 60f
            canvas.drawLine(x, 0f, x, screenHeight, gridPaint)
        }
        for (i in -1..18) {
            val y = startY + i * 60f
            canvas.drawLine(0f, y, screenWidth, y, gridPaint)
        }

        // Здания
        for (building in locationData.buildings) {
            val drawX = building.left - cameraManager.x
            val drawY = building.top - cameraManager.y
            val drawRect = RectF(drawX, drawY, drawX + building.width(), drawY + building.height())

            val buildPaint = Paint().apply {
                color = Color.rgb(150, 120, 80)
                style = Paint.Style.FILL
            }
            canvas.drawRect(drawRect, buildPaint)

            val roofPaint = Paint().apply { color = Color.rgb(180, 80, 40) }
            canvas.drawRect(drawX - 5f, drawY - 20f, drawX + building.width() + 5f, drawY + 5f, roofPaint)

            val doorPaint = Paint().apply { color = Color.rgb(80, 50, 30) }
            canvas.drawRect(
                drawX + building.width() / 2 - 10f,
                drawY + building.height() - 25f,
                drawX + building.width() / 2 + 10f,
                drawY + building.height() - 5f,
                doorPaint
            )
        }

        drawTransitionIndicators(canvas)

        // NPC
        for (npc in locationData.npcs) {
            val drawX = npc.x - cameraManager.x
            val drawY = npc.y - cameraManager.y

            val npcPaint = Paint().apply { color = Color.rgb(200, 180, 150) }
            canvas.drawCircle(drawX, drawY - 10f, 20f, npcPaint)

            val headPaint = Paint().apply { color = Color.rgb(255, 220, 180) }
            canvas.drawCircle(drawX, drawY - 30f, 15f, headPaint)

            val textPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 16f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(npc.name, drawX, drawY - 60f, textPaint)
        }

        // Мобы
        for (mob in locationData.mobs) {
            if (mob.isDead) continue
            val drawX = mob.x - cameraManager.x
            val drawY = mob.y - cameraManager.y
            MobRenderer.drawMob(canvas, mob, drawX, drawY, selectedMob == mob, this)
        }

        drawPlayer(canvas)

        // ⭐ ОТРИСОВКА ЦЕЛИ (моб, к которому идем)
        if (isMovingToMob && targetMob != null && !targetMob!!.isDead) {
            val drawX = targetMob!!.x - cameraManager.x
            val drawY = targetMob!!.y - cameraManager.y

            // Круг вокруг цели
            val targetPaint = Paint().apply {
                color = Color.argb(100, 255, 255, 100)
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
            canvas.drawCircle(drawX, drawY, 50f, targetPaint)

            // Пульсирующий круг
            val pulse = (150 + sin(System.currentTimeMillis() / 300.0) * 50).toInt()
            val pulsePaint = Paint().apply {
                color = Color.argb(pulse, 255, 255, 100)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawCircle(drawX, drawY,
                (60f + sin(System.currentTimeMillis() / 500.0) * 10f).toFloat(), pulsePaint)

            // Линия от игрока к цели
            val linePaint = Paint().apply {
                color = Color.argb(80, 255, 255, 100)
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            val playerDrawX = player.x - cameraManager.x
            val playerDrawY = player.y - cameraManager.y
            canvas.drawLine(playerDrawX, playerDrawY, drawX, drawY, linePaint)

            // ⭐ РАСЧЕТ ДИСТАНЦИИ В МЕТРАХ
            val distPx = sqrt(
                (player.x - targetMob!!.x) * (player.x - targetMob!!.x) +
                        (player.y - targetMob!!.y) * (player.y - targetMob!!.y)
            )
            val distMeters = (distPx / 10f).toInt()  // ← переводим в метры (1 метр = 10 пикселей)
            val distText = "${distMeters} м"

            // ⭐ ФОН ДЛЯ ТЕКСТА ДИСТАНЦИИ (чтобы было видно)
            val midX = (playerDrawX + drawX) / 2
            val midY = (playerDrawY + drawY) / 2 - 40f

            // Тень/фон для текста
            val bgTextPaint = Paint().apply {
                color = Color.argb(180, 0, 0, 0)
                style = Paint.Style.FILL
            }
            val textWidth = 160f
            val textHeight = 60f
            canvas.drawRoundRect(
                RectF(midX - textWidth / 2, midY - textHeight / 2, midX + textWidth / 2, midY + textHeight / 2),
                15f, 15f, bgTextPaint
            )

            // Рамка вокруг текста
            val borderTextPaint = Paint().apply {
                color = Color.argb(200, 255, 215, 0)
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(
                RectF(midX - textWidth / 2, midY - textHeight / 2, midX + textWidth / 2, midY + textHeight / 2),
                15f, 15f, borderTextPaint
            )

            // ⭐ БОЛЬШОЙ ТЕКСТ ДИСТАНЦИИ
            val distPaint = Paint().apply {
                color = Color.rgb(255, 255, 100)
                textSize = 52f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(distText, midX, midY + 18f, distPaint)

            // ⭐ СТРЕЛКА К ЦЕЛИ (более заметная)
            val arrowPaint = Paint().apply {
                color = Color.argb(200, 255, 255, 100)
                textSize = 45f  // ← УВЕЛИЧИЛИ!
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("▼", midX, midY + 60f, arrowPaint)
        }

        drawUI(canvas)

        // ⭐ ОКНО ПОДТВЕРЖДЕНИЯ ПОЛНОГО СБРОСА
        if (showResetFullConfirm) {
            drawResetFullConfirmDialog(canvas)
        }
    }

    private fun drawResetFullConfirmDialog(canvas: Canvas) {
        // Затемнение фона
        val dimPaint = Paint().apply {
            color = Color.argb(200, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, dimPaint)

        // Окно подтверждения
        val dialogPaint = Paint().apply {
            color = Color.argb(240, 30, 20, 40)
        }
        val dialogWidth = 600f
        val dialogHeight = 400f
        val dialogX = (screenWidth - dialogWidth) / 2
        val dialogY = (screenHeight - dialogHeight) / 2

        canvas.drawRoundRect(
            RectF(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight),
            25f, 25f, dialogPaint
        )

        // Рамка
        val borderPaint = Paint().apply {
            color = Color.argb(150, 255, 100, 100)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(
            RectF(dialogX, dialogY, dialogX + dialogWidth, dialogY + dialogHeight),
            25f, 25f, borderPaint
        )

        // Свечение
        val glowPaint = Paint().apply {
            shader = RadialGradient(
                screenWidth / 2, dialogY + 60f, 300f,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(screenWidth / 2, dialogY + 60f, 300f, glowPaint)

        // Иконка
        val iconPaint = Paint().apply {
            color = Color.rgb(255, 200, 100)
            textSize = 70f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("⚠️", screenWidth / 2, dialogY + 80f, iconPaint)

        // Заголовок
        val titlePaint = Paint().apply {
            color = Color.rgb(255, 100, 100)
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("ПОЛНЫЙ СБРОС!", screenWidth / 2, dialogY + 140f, titlePaint)

        // Текст предупреждения
        val textPaint = Paint().apply {
            color = Color.argb(200, 255, 255, 200)
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Весь прогресс будет потерян!",
            screenWidth / 2,
            dialogY + 185f,
            textPaint
        )
        canvas.drawText(
            "Игра начнется заново с 1 уровня.",
            screenWidth / 2,
            dialogY + 220f,
            textPaint
        )

        val warningPaint = Paint().apply {
            color = Color.rgb(255, 200, 100)
            textSize = 22f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(
            "Это действие нельзя отменить!",
            screenWidth / 2,
            dialogY + 260f,
            warningPaint
        )

        // Кнопка "ДА" (красная)
        val btnWidth = 180f
        val btnHeight = 60f
        val btnY = dialogY + dialogHeight - 80f
        val btnSpacing = 30f

        val yesPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + 50f, btnY, dialogX + 50f + btnWidth, btnY + btnHeight),
            15f, 15f, yesPaint
        )

        val yesGlow = Paint().apply {
            shader = RadialGradient(
                dialogX + 50f + btnWidth / 2, btnY + btnHeight / 2, 100f,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(dialogX + 50f + btnWidth / 2, btnY + btnHeight / 2, 100f, yesGlow)

        val yesText = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("✅ ДА", dialogX + 50f + btnWidth / 2, btnY + btnHeight / 2 + 10f, yesText)

        // Кнопка "НЕТ" (серая)
        val noPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(dialogX + dialogWidth - btnWidth - 50f, btnY, dialogX + dialogWidth - 50f, btnY + btnHeight),
            15f, 15f, noPaint
        )

        val noText = Paint().apply {
            color = Color.WHITE
            textSize = 30f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("❌ НЕТ", dialogX + dialogWidth - btnWidth / 2 - 50f, btnY + btnHeight / 2 + 10f, noText)
    }

    private fun drawGrid(canvas: Canvas) {
        val gridPaint = Paint().apply {
            color = Color.argb(50, 0, 0, 0)
            strokeWidth = 1f
        }
        val startX = -(cameraManager.x % 60f)
        val startY = -(cameraManager.y % 60f)
        for (i in -1..25) {
            val x = startX + i * 60f
            canvas.drawLine(x, 0f, x, screenHeight, gridPaint)
        }
        for (i in -1..18) {
            val y = startY + i * 60f
            canvas.drawLine(0f, y, screenWidth, y, gridPaint)
        }
    }

    private fun drawBuilding(canvas: Canvas, building: RectF) {
        val drawX = building.left - cameraManager.x
        val drawY = building.top - cameraManager.y
        val drawRect = RectF(drawX, drawY, drawX + building.width(), drawY + building.height())

        val buildPaint = Paint().apply {
            color = Color.rgb(150, 120, 80)
            style = Paint.Style.FILL
        }
        canvas.drawRect(drawRect, buildPaint)

        val roofPaint = Paint().apply { color = Color.rgb(180, 80, 40) }
        canvas.drawRect(drawX - 5f, drawY - 20f, drawX + building.width() + 5f, drawY + 5f, roofPaint)

        val doorPaint = Paint().apply { color = Color.rgb(80, 50, 30) }
        canvas.drawRect(
            drawX + building.width() / 2 - 10f,
            drawY + building.height() - 25f,
            drawX + building.width() / 2 + 10f,
            drawY + building.height() - 5f,
            doorPaint
        )
    }

    private fun drawNPC(canvas: Canvas, npc: NPC) {
        val drawX = npc.x - cameraManager.x
        val drawY = npc.y - cameraManager.y

        val npcPaint = Paint().apply { color = Color.rgb(200, 180, 150) }
        canvas.drawCircle(drawX, drawY - 10f, 20f, npcPaint)

        val headPaint = Paint().apply { color = Color.rgb(255, 220, 180) }
        canvas.drawCircle(drawX, drawY - 30f, 15f, headPaint)

        val textPaint = Paint().apply {
            color = Color.YELLOW
            textSize = 16f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(npc.name, drawX, drawY - 60f, textPaint)
    }

    private fun drawTransitionIndicators(canvas: Canvas) {
        val arrowPaint = Paint().apply {
            color = Color.argb(200, 255, 255, 100)
            textSize = 40f
            textAlign = Paint.Align.CENTER
        }

        when (locationManager.currentLocation) {
            LocationManager.Location.CITY -> {
                canvas.drawText("🌲 →", worldWidth - cameraManager.x - 30f, screenHeight / 2, arrowPaint)
                canvas.drawText("← 🏜️", 30f - cameraManager.x, screenHeight / 2, arrowPaint)
            }
            LocationManager.Location.FOREST -> {
                canvas.drawText("← 🏙️", 30f - cameraManager.x, screenHeight / 2, arrowPaint)
                canvas.drawText("⛰️ →", worldWidth - cameraManager.x - 30f, screenHeight / 2, arrowPaint)
            }
            LocationManager.Location.WASTELAND -> {
                canvas.drawText("🏙️ →", worldWidth - cameraManager.x - 30f, screenHeight / 2, arrowPaint)
                canvas.drawText("🏜️ →", worldWidth - cameraManager.x - 30f, screenHeight / 2, arrowPaint)
            }
            LocationManager.Location.ROCKS -> {
                canvas.drawText("← 🌲", 30f - cameraManager.x, screenHeight / 2, arrowPaint)
                canvas.drawText("🏰 →", worldWidth - cameraManager.x - 30f, screenHeight / 2, arrowPaint)
            }
            LocationManager.Location.CASTLE -> {
                canvas.drawText("← ⛰️", 30f - cameraManager.x, screenHeight / 2, arrowPaint)
            }
            LocationManager.Location.DESERT -> {
                canvas.drawText("← 🏜️", 30f - cameraManager.x, screenHeight / 2, arrowPaint)
            }
        }
    }

    private fun drawPlayer(canvas: Canvas) {
        val x = player.x - cameraManager.x
        val y = player.y - cameraManager.y

        currentAnimation = when {
            player.isMoving && player.facing == 1 -> "walk_left"
            player.isMoving && player.facing == 2 -> "walk_right"
            player.isMoving -> "walk_right"
            player.attackCooldown > 0 -> "attack"
            else -> "idle"
        }

        val animationFrames = spriteManager.getAnimationFrames(currentSprite, currentAnimation)

        if (animationFrames.isNotEmpty()) {
            frameTimer++
            val animation = spriteManager.getAnimation(currentSprite, currentAnimation)
            val speed = animation?.speed ?: 8
            if (frameTimer > speed) {
                frameTimer = 0
                currentFrameIndex = (currentFrameIndex + 1) % animationFrames.size
            }

            val currentFrame = animationFrames[currentFrameIndex % animationFrames.size]

            val displayWidth = 120f
            val displayHeight = 120f
            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = spriteManager.getSpriteSheet(currentSprite)
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            }
        }
    }

    fun drawBattlePlayer(canvas: Canvas, x: Float, y: Float, scale: Float) {
        val spriteName = "character_zombie"
        val animName = if (isAttackingInBattle) "attack" else "idle"

        val animationFrames = spriteManager.getAnimationFrames(spriteName, animName)

        if (animationFrames.isNotEmpty()) {
            battleFrameTimer++
            val animation = spriteManager.getAnimation(spriteName, animName)
            val speed = animation?.speed ?: 8

            if (battleFrameTimer > speed) {
                battleFrameTimer = 0
                battleFrameIndex = (battleFrameIndex + 1) % animationFrames.size
                if (isAttackingInBattle && battleFrameIndex == 0) {
                    isAttackingInBattle = false
                }
            }

            val currentFrame = animationFrames[battleFrameIndex % animationFrames.size]

            val displayWidth = 200f * scale
            val displayHeight = 200f * scale

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = spriteManager.getSpriteSheet(spriteName)
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            }
        }
    }

    private fun drawBattle(canvas: Canvas) {
        val locationName = locationManager.getLocationName(locationManager.currentLocation)
        BattleRenderer.drawBattle(
            canvas,
            screenWidth,
            screenHeight,
            battleManager,
            player,
            locationName,
            this
        )
    }

    private fun drawUI(canvas: Canvas) {
        val locationData = locationManager.getCurrentData()

        val locPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📍 ${locationData.name}", screenWidth / 2, 50f, locPaint)

        // Кнопка меню
        drawMenuButton(canvas)

        if (isMenuOpen) {
            drawPopupMenu(canvas)
        }

        // Статистика
        val bgStatPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(10f, 20f, 450f, 160f),
            15f, 15f, bgStatPaint
        )

        val borderStatPaint = Paint().apply {
            color = Color.argb(100, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(
            RectF(10f, 20f, 450f, 160f),
            15f, 15f, borderStatPaint
        )

        val statPaint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val mobCount = locationData.mobs.count { !it.isDead }

        // ⭐ ИСПРАВЛЕНО: используем getMaxHp(inventory)
        canvas.drawText(
            "❤️ HP: ${player.hp.toInt()}/${player.getMaxHp(inventory).toInt()}",
            25f, 65f, statPaint
        )

        statPaint.textSize = 32f
        canvas.drawText(
            "⭐ Ур.${player.level} | Опыт: ${player.exp}/${player.maxExp}",
            25f, 105f, statPaint
        )

        statPaint.textSize = 28f
        canvas.drawText(
            "👾 Мобы: $mobCount | 🎯 Очки: ${player.skillPoints}",
            25f, 140f, statPaint
        )

        if (messageTimer > 0) {
            val msgPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 24f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(messageText, screenWidth / 2, screenHeight - 80f, msgPaint)
        }

        val hintPaint = Paint().apply {
            color = Color.argb(180, 255, 255, 255)
            textSize = 18f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🖱 Тап - движение | Двойной тап по мобу - бой", 20f, screenHeight - 30f, hintPaint)
    }

    private fun drawMenuButton(canvas: Canvas) {
        val menuX = screenWidth - menuButtonSize - 20f
        val menuY = 20f

        val shadowPaint = Paint().apply {
            color = Color.argb(60, 0, 0, 0)
        }
        canvas.drawCircle(menuX + 3f, menuY + 3f, menuButtonSize / 2, shadowPaint)

        val menuBtnPaint = Paint().apply {
            shader = LinearGradient(
                menuX, menuY,
                menuX + menuButtonSize, menuY + menuButtonSize,
                Color.rgb(255, 180, 50),
                Color.rgb(200, 100, 20),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(menuX + menuButtonSize / 2, menuY + menuButtonSize / 2, menuButtonSize / 2, menuBtnPaint)

        val borderPaint = Paint().apply {
            color = Color.argb(150, 255, 255, 200)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawCircle(menuX + menuButtonSize / 2, menuY + menuButtonSize / 2, menuButtonSize / 2, borderPaint)

        val iconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("☰", menuX + menuButtonSize / 2, menuY + menuButtonSize / 2 + 14f, iconPaint)
    }

    private fun drawStatsPanel(canvas: Canvas, locationData: LocationData) {
        val bgStatPaint = Paint().apply {
            color = Color.argb(180, 0, 0, 0)
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(
            RectF(10f, 20f, 450f, 160f),
            15f, 15f, bgStatPaint
        )

        val borderStatPaint = Paint().apply {
            color = Color.argb(100, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(
            RectF(10f, 20f, 450f, 160f),
            15f, 15f, borderStatPaint
        )

        val statPaint = Paint().apply {
            color = Color.WHITE
            textSize = 36f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val mobCount = locationData.mobs.count { !it.isDead }

        canvas.drawText(
            "❤️ HP: ${player.hp.toInt()}/${player.getMaxHp(inventory).toInt()}",
            25f, 65f, statPaint
        )

        statPaint.textSize = 32f
        canvas.drawText(
            "⭐ Ур.${player.level} | Опыт: ${player.exp}/${player.maxExp}",
            25f, 105f, statPaint
        )

        statPaint.textSize = 28f
        canvas.drawText(
            "👾 Мобы: $mobCount | 🎯 Очки: ${player.skillPoints}",
            25f, 140f, statPaint
        )
    }

    private fun drawPopupMenu(canvas: Canvas) {
        val dimPaint = Paint().apply {
            color = Color.argb(150, 0, 0, 0)
        }
        canvas.drawRect(0f, 0f, screenWidth, screenHeight, dimPaint)

        val menuX = (screenWidth - POPUP_MENU_WIDTH) / 2
        val menuY = (screenHeight - POPUP_MENU_HEIGHT) / 2

        val menuBgPaint = Paint().apply {
            shader = LinearGradient(
                menuX, menuY,
                menuX, menuY + POPUP_MENU_HEIGHT,
                Color.rgb(40, 30, 50),
                Color.rgb(20, 15, 30),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(
            RectF(menuX, menuY, menuX + POPUP_MENU_WIDTH, menuY + POPUP_MENU_HEIGHT),
            30f, 30f, menuBgPaint
        )

        val borderPaint = Paint().apply {
            color = Color.argb(150, 255, 215, 0)
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(
            RectF(menuX, menuY, menuX + POPUP_MENU_WIDTH, menuY + POPUP_MENU_HEIGHT),
            30f, 30f, borderPaint
        )

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 48f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📋 МЕНЮ", screenWidth / 2, menuY + 80f, titlePaint)

        val linePaint = Paint().apply {
            color = Color.argb(80, 255, 255, 255)
            strokeWidth = 3f
        }
        canvas.drawLine(menuX + 60f, menuY + 110f, menuX + POPUP_MENU_WIDTH - 60f, menuY + 110f, linePaint)

        val btnWidth = POPUP_BTN_WIDTH
        val btnHeight = POPUP_BTN_HEIGHT
        val btnStartX = menuX + 40f
        val btnStartY = menuY + 140f
        val btnSpacing = POPUP_BTN_SPACING

        drawMenuItem(canvas, btnStartX, btnStartY, btnWidth, btnHeight, "🗺️ Карта", Color.rgb(50, 150, 200))
        drawMenuItem(canvas, btnStartX, btnStartY + (btnHeight + btnSpacing), btnWidth, btnHeight, "📊 Характеристики", Color.rgb(200, 150, 50))
        drawMenuItem(canvas, btnStartX, btnStartY + (btnHeight + btnSpacing) * 2, btnWidth, btnHeight, "🎒 Инвентарь", Color.rgb(100, 150, 200))
        drawMenuItem(canvas, btnStartX, btnStartY + (btnHeight + btnSpacing) * 3, btnWidth, btnHeight, "🏪 Магазин", Color.rgb(50, 200, 150))

        // ⭐ НОВАЯ КНОПКА - ПОЛНЫЙ СБРОС (красная, внизу)
        drawMenuItem(
            canvas,
            btnStartX,
            btnStartY + (btnHeight + btnSpacing) * 4,
            btnWidth,
            btnHeight,
            "🗑️ Полный сброс",
            Color.rgb(200, 50, 50)
        )

        // Кнопка закрытия
        val closeX = menuX + POPUP_MENU_WIDTH - 45f
        val closeY = menuY + 45f

        val closeBgPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
        }
        canvas.drawCircle(closeX, closeY, POPUP_CLOSE_SIZE, closeBgPaint)

        val closeIconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("✕", closeX, closeY + 14f, closeIconPaint)
    }

    private fun drawMenuItem(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        text: String,
        btnColor: Int
    ) {
        val red = Color.red(btnColor)
        val green = Color.green(btnColor)
        val blue = Color.blue(btnColor)

        val shadowPaint = Paint().apply {
            color = Color.argb(60, 0, 0, 0)
        }
        canvas.drawRoundRect(
            RectF(x + 4f, y + 4f, x + width + 4f, y + height + 4f),
            15f, 15f, shadowPaint
        )

        val btnPaint = Paint().apply {
            shader = LinearGradient(
                x, y,
                x, y + height,
                btnColor,
                Color.rgb(
                    maxOf(red - 50, 0),
                    maxOf(green - 50, 0),
                    maxOf(blue - 50, 0)
                ),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(
            RectF(x, y, x + width, y + height),
            15f, 15f, btnPaint
        )

        val borderPaint = Paint().apply {
            color = Color.argb(100, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(
            RectF(x, y, x + width, y + height),
            15f, 15f, borderPaint
        )

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(text, x + width / 2, y + height / 2 + 9f, textPaint)
    }

    // ========== ОБРАБОТКА КАСАНИЙ ==========

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // ⭐ ОКНО ПОДТВЕРЖДЕНИЯ ПОЛНОГО СБРОСА
            if (showResetFullConfirm) {
                val dialogWidth = 600f
                val dialogHeight = 400f
                val dialogX = (screenWidth - dialogWidth) / 2
                val dialogY = (screenHeight - dialogHeight) / 2
                val btnWidth = 180f
                val btnHeight = 60f
                val btnY = dialogY + dialogHeight - 80f

                // Кнопка "ДА"
                if (x > dialogX + 50f && x < dialogX + 50f + btnWidth &&
                    y > btnY && y < btnY + btnHeight) {
                    confirmFullReset()
                    showResetFullConfirm = false
                    return true
                }

                // Кнопка "НЕТ"
                if (x > dialogX + dialogWidth - btnWidth - 50f && x < dialogX + dialogWidth - 50f &&
                    y > btnY && y < btnY + btnHeight) {
                    showResetFullConfirm = false
                    return true
                }

                // Клик вне диалога - закрываем
                if (x < dialogX || x > dialogX + dialogWidth || y < dialogY || y > dialogY + dialogHeight) {
                    showResetFullConfirm = false
                    return true
                }
                return true
            }

            // ⭐ МЕНЮ
            if (isMenuOpen) {
                val menuX = (screenWidth - POPUP_MENU_WIDTH) / 2
                val menuY = (screenHeight - POPUP_MENU_HEIGHT) / 2

                val closeX = menuX + POPUP_MENU_WIDTH - 45f
                val closeY = menuY + 45f
                if (x > closeX - POPUP_CLOSE_SIZE && x < closeX + POPUP_CLOSE_SIZE &&
                    y > closeY - POPUP_CLOSE_SIZE && y < closeY + POPUP_CLOSE_SIZE) {
                    isMenuOpen = false
                    return true
                }

                val btnStartX = menuX + 40f
                val btnStartY = menuY + 140f
                val btnWidth = POPUP_BTN_WIDTH
                val btnHeight = POPUP_BTN_HEIGHT
                val btnSpacing = POPUP_BTN_SPACING

                // Карта
                if (x > btnStartX && x < btnStartX + btnWidth &&
                    y > btnStartY && y < btnStartY + btnHeight) {
                    isMenuOpen = false
                    gameState = if (gameState == GameState.MAP) GameState.GAME else GameState.MAP
                    return true
                }

                // Характеристики
                if (x > btnStartX && x < btnStartX + btnWidth &&
                    y > btnStartY + (btnHeight + btnSpacing) &&
                    y < btnStartY + (btnHeight + btnSpacing) + btnHeight) {
                    isMenuOpen = false
                    openStats()
                    return true
                }

                // Инвентарь
                if (x > btnStartX && x < btnStartX + btnWidth &&
                    y > btnStartY + (btnHeight + btnSpacing) * 2 &&
                    y < btnStartY + (btnHeight + btnSpacing) * 2 + btnHeight) {
                    isMenuOpen = false
                    openInventory()
                    return true
                }

                // Магазин
                if (x > btnStartX && x < btnStartX + btnWidth &&
                    y > btnStartY + (btnHeight + btnSpacing) * 3 &&
                    y < btnStartY + (btnHeight + btnSpacing) * 3 + btnHeight) {
                    isMenuOpen = false
                    openShop()
                    return true
                }

                // ⭐ НОВАЯ КНОПКА - ПОЛНЫЙ СБРОС
                if (x > btnStartX && x < btnStartX + btnWidth &&
                    y > btnStartY + (btnHeight + btnSpacing) * 4 &&
                    y < btnStartY + (btnHeight + btnSpacing) * 4 + btnHeight) {
                    isMenuOpen = false
                    showResetFullConfirm = true
                    return true
                }

                if (x < menuX || x > menuX + POPUP_MENU_WIDTH || y < menuY || y > menuY + POPUP_MENU_HEIGHT) {
                    isMenuOpen = false
                    return true
                }
                return true
            }

            // ⭐ КНОПКА МЕНЮ
            if (gameState != GameState.INVENTORY && gameState != GameState.STATS && gameState != GameState.SHOP) {
                val menuX = screenWidth - menuButtonSize - 20f
                val menuY = 20f
                if (x > menuX && x < menuX + menuButtonSize &&
                    y > menuY && y < menuY + menuButtonSize) {
                    isMenuOpen = !isMenuOpen
                    return true
                }
            }

            // ⭐ ЭКРАН ХАРАКТЕРИСТИК
            if (gameState == GameState.STATS) {
                statsScreen.handleTouch(
                    x, y, screenWidth, screenHeight, player,
                    { statType -> upgradeStat(statType) },
                    { closeStats() },
                    { resetStats() },
                    { showResetFullConfirm = true }  // ← передаем callback для полного сброса
                )
                return true
            }

            // ⭐ ЭКРАН ИНВЕНТАРЯ
            if (gameState == GameState.INVENTORY) {
                inventoryScreen.handleTouch(
                    x, y, screenWidth, screenHeight, inventory, player,
                    { index -> equipItem(index) },
                    { slot -> unequipItem(slot) },
                    { closeInventory() },
                    { index -> deleteItem(index) },
                    { index -> useItem(index) },
                    { newName -> renamePlayer(newName) },
                    { index -> refineItem(index) },
                    { message -> showMessage(message) }
                )
                return true
            }

            // ⭐ МАГАЗИН
            if (gameState == GameState.SHOP) {
                shopScreen.handleTouch(
                    x, y, screenWidth, screenHeight, player,
                    { closeShop() },
                    { item -> buyShopItem(item) },
                    { openRenameFromShop() },
                    { resetStatsFromShop() }
                )
                return true
            }

            // ⭐ КАРТА
            if (gameState == GameState.MAP) {
                val handled = mapScreen.handleTouch(
                    x, y, screenWidth, screenHeight, locationManager
                ) { location ->
                    teleportToLocation(location)
                }
                if (!handled) {
                    gameState = GameState.GAME
                }
                return true
            }

            // ⭐ БОЙ
            if (gameState == GameState.BATTLE) {
                val btnWidth = screenWidth * 0.4f
                val btnHeight = 140f
                val btnX = (screenWidth - btnWidth) / 2
                val btnY = screenHeight - btnHeight - 30f

                if (x > btnX && x < btnX + btnWidth &&
                    y > btnY && y < btnY + btnHeight) {
                    attackInBattle()
                    return true
                }
                return true
            }

            // ===== ИГРОВОЙ РЕЖИМ =====
            val currentTime = System.currentTimeMillis()
            val touchWorldX = x + cameraManager.x
            val touchWorldY = y + cameraManager.y

            val location = locationManager.getCurrentData()

            // ⭐ ПРОВЕРЯЕМ, КЛИКНУЛИ ЛИ ПО МОБУ
            var mobClicked: Mob? = null
            for (mob in location.mobs) {
                if (mob.isDead) continue
                if (abs(mob.x - touchWorldX) < 50 && abs(mob.y - touchWorldY) < 50) {
                    mobClicked = mob
                    break
                }
            }

            if (mobClicked != null) {
                // ⭐ КЛИК ПО МОБУ - НАЧИНАЕМ ДВИЖЕНИЕ К НЕМУ
                selectedMob = if (selectedMob == mobClicked) null else mobClicked
                targetMob = mobClicked
                isMovingToMob = true
                battleStartCooldown = 0

                player.targetX = mobClicked.x
                player.targetY = mobClicked.y
                player.isMoving = true

                println("🚶 Идем к мобу: ${mobClicked.getTypeName()} на позицию (${mobClicked.x}, ${mobClicked.y})")
                return true
            } else {
                // ⭐ КЛИК ПО ЗЕМЛЕ - ОБЫЧНОЕ ДВИЖЕНИЕ
                selectedMob = null
                targetMob = null
                isMovingToMob = false

                val newX = max(20f, min(touchWorldX, worldWidth - 20f))
                val newY = max(20f, min(touchWorldY, worldHeight - 20f))
                player.targetX = newX
                player.targetY = newY
                player.isMoving = true
            }

            lastTouchTime = currentTime
            lastTouchX = x
            lastTouchY = y
            return true
        }
        return true
    }

    private fun handleMenuTouch(x: Float, y: Float): Boolean {
        val menuX = (screenWidth - POPUP_MENU_WIDTH) / 2
        val menuY = (screenHeight - POPUP_MENU_HEIGHT) / 2

        val closeX = menuX + POPUP_MENU_WIDTH - 45f
        val closeY = menuY + 45f
        if (x > closeX - POPUP_CLOSE_SIZE && x < closeX + POPUP_CLOSE_SIZE &&
            y > closeY - POPUP_CLOSE_SIZE && y < closeY + POPUP_CLOSE_SIZE) {
            isMenuOpen = false
            return true
        }

        val btnStartX = menuX + 40f
        val btnStartY = menuY + 140f
        val btnWidth = POPUP_BTN_WIDTH
        val btnHeight = POPUP_BTN_HEIGHT
        val btnSpacing = POPUP_BTN_SPACING

        // КАРТА
        if (x > btnStartX && x < btnStartX + btnWidth &&
            y > btnStartY && y < btnStartY + btnHeight) {
            isMenuOpen = false
            gameState = if (gameState == GameState.MAP) GameState.GAME else GameState.MAP
            return true
        }

        // Характеристики
        if (x > btnStartX && x < btnStartX + btnWidth &&
            y > btnStartY + (btnHeight + btnSpacing) &&
            y < btnStartY + (btnHeight + btnSpacing) + btnHeight) {
            isMenuOpen = false
            openStats()
            return true
        }

        // Инвентарь
        if (x > btnStartX && x < btnStartX + btnWidth &&
            y > btnStartY + (btnHeight + btnSpacing) * 2 &&
            y < btnStartY + (btnHeight + btnSpacing) * 2 + btnHeight) {
            isMenuOpen = false
            openInventory()
            return true
        }

        // Магазин
        if (x > btnStartX && x < btnStartX + btnWidth &&
            y > btnStartY + (btnHeight + btnSpacing) * 3 &&
            y < btnStartY + (btnHeight + btnSpacing) * 3 + btnHeight) {
            isMenuOpen = false
            openShop()
            return true
        }

        if (x < menuX || x > menuX + POPUP_MENU_WIDTH || y < menuY || y > menuY + POPUP_MENU_HEIGHT) {
            isMenuOpen = false
            return true
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    private fun handleBattleTouch(x: Float, y: Float): Boolean {
        val btnWidth = screenWidth * 0.4f
        val btnHeight = 140f
        val btnX = (screenWidth - btnWidth) / 2
        val btnY = screenHeight - btnHeight - 30f

        if (x > btnX && x < btnX + btnWidth &&
            y > btnY && y < btnY + btnHeight) {
            attackInBattle()
            return true
        }
        return true
    }

    private fun handleGameTouch(x: Float, y: Float): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastTouchTime

        // Двойной тап по мобу
        if (timeDiff < 500 && abs(x - lastTouchX) < 50 && abs(y - lastTouchY) < 50) {
            val location = locationManager.getCurrentData()
            val touchWorldX = x + cameraManager.x
            val touchWorldY = y + cameraManager.y

            for (mob in location.mobs) {
                if (mob.isDead) continue
                if (abs(mob.x - touchWorldX) < 40 && abs(mob.y - touchWorldY) < 40) {
                    startBattle(mob)
                    lastTouchTime = 0
                    return true
                }
            }
        }

        val touchWorldX = x + cameraManager.x
        val touchWorldY = y + cameraManager.y

        val location = locationManager.getCurrentData()
        var mobClicked: Mob? = null
        for (mob in location.mobs) {
            if (mob.isDead) continue
            if (abs(mob.x - touchWorldX) < 40 && abs(mob.y - touchWorldY) < 40) {
                mobClicked = mob
                break
            }
        }

        if (mobClicked != null) {
            selectedMob = if (selectedMob == mobClicked) null else mobClicked
        } else {
            selectedMob = null
            val newX = max(20f, min(touchWorldX, worldWidth - 20f))
            val newY = max(20f, min(touchWorldY, worldHeight - 20f))
            player.targetX = newX
            player.targetY = newY
            player.isMoving = true
        }

        lastTouchTime = currentTime
        lastTouchX = x
        lastTouchY = y
        return true
    }

    private fun sleep() {
        try {
            Thread.sleep(16)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun resume() {
        isRunning = true
        thread = Thread(this)
        thread?.start()
    }

    fun pause() {
        isRunning = false
        thread?.join()
        thread = null
    }

    // ===== ДЛЯ BattleRenderer и MobRenderer =====

    fun getMobAnimationFrames(spriteName: String, animName: String): List<Rect> {
        return spriteManager.getAnimationFrames(spriteName, animName)
    }

    fun getMobSpriteSheet(spriteName: String): Bitmap? {
        return spriteManager.getSpriteSheet(spriteName)
    }

    fun getPlayerLevel(): Int = player.level

    // ===== ДЛЯ InventoryScreen =====
    fun getCharacterIdleFrames(): List<Rect> {
        return spriteManager.getAnimationFrames("character_zombie", "idle")
    }

    fun getCharacterIdleSpriteSheet(): Bitmap? {
        return spriteManager.getSpriteSheet("character_zombie")
    }

    // ===== ИНВЕНТАРЬ =====
    private fun openInventory() {
        gameState = GameState.INVENTORY
    }

    private fun closeInventory() {
        gameState = GameState.GAME
        saveGame()
    }

    private fun equipItem(index: Int) {
        if (inventory.equip(index)) {
            val items = inventory.getItems()
            val item = items.getOrNull(index)
            if (item != null) {
                showMessage("✅ ${item.name} экипирован!")
                saveGame()
            }
        } else {
            showMessage("❌ Нельзя экипировать!")
        }
    }

    private fun unequipItem(slot: EquipmentSlot) {
        val item = inventory.unequip(slot)
        if (item != null) {
            showMessage("🔽 ${item.name} снят!")
            saveGame()
        } else {
            showMessage("❌ Нет места в инвентаре!")
        }
    }

    // ===== СОХРАНЕНИЕ =====
    private fun loadGame() {
        val hasSave = saveManager.loadGame(player, inventory)
        if (hasSave) {
            // Применяем бонусы от рун после загрузки
            val maxHp = player.getMaxHp(inventory)
            if (player.hp > maxHp) {
                player.hp = maxHp
            }
            player.speed = player.getSpeed(inventory)

            // Проверяем уровень (на случай, если опыт накопился)
            while (player.exp >= player.maxExp) {
                player.exp -= player.maxExp
                player.level++
                player.maxExp = (player.maxExp * 1.5f).toInt()
                player.skillPoints += 5
                println("🎉 УРОВЕНЬ ${player.level} восстановлен при загрузке!")
            }

            showMessage("💾 Прогресс загружен!")
            println("✅ Загружен игрок: ${player.name}, ур.${player.level}, HP: ${player.hp}/${player.getMaxHp(inventory)}")
            println("   Сила: ${player.strength}, Выносливость: ${player.endurance}")
            println("   Ловкость: ${player.agility}, Сноровка: ${player.dexterity}, Удача: ${player.luck}")
        } else {
            showMessage("🆕 Новая игра!")
            println("🆕 Создан новый игрок: ${player.name}")
            println("   Начальные характеристики: Сила=5, Выносливость=5, Ловкость=5, Сноровка=5, Удача=5")
        }
    }

    private fun saveGame() {
        saveManager.saveGame(player, inventory)
    }

    private fun deleteItem(index: Int) {
        val item = inventory.removeItem(index)
        if (item != null) {
            showMessage("🗑️ ${item.name} удалён!")
            inventory.selectedSlot = -1
            saveGame()
        } else {
            showMessage("❌ Не удалось удалить предмет!")
        }
    }

    private fun useItem(index: Int) {
        val items = inventory.getItems()
        if (index >= items.size) return

        val item = items[index] ?: return
        if (item.type != Item.ItemType.CONSUMABLE) {
            showMessage("❌ Нельзя использовать!")
            return
        }

        if (item.id.startsWith("cake_")) {
            val maxHp = player.getMaxHp(inventory)
            if (player.hp >= maxHp) {
                showMessage("❌ HP уже максимальный!")
                return
            }

            val healAmount = when (item.id) {
                "cake_small" -> 30f
                "cake_medium" -> 60f
                "cake_large" -> 120f
                else -> 0f
            }

            player.hp = min(player.hp + healAmount, maxHp)
            inventory.removeOneItem(index)

            showMessage("🍰 ${item.name} использован! HP: ${player.hp.toInt()}/${maxHp.toInt()}")
            saveGame()
            return
        }

        if (item.use(player)) {
            inventory.removeOneItem(index)
            showMessage("✅ ${item.name} использован!")
            saveGame()
        } else {
            showMessage("❌ Нельзя использовать!")
        }
    }

    private fun checkBossSpawn(mob: Mob) {
        if (bossOnMap) return

        val bossChance = 5  // 5% шанс
        val random = (0..99).random()

        if (random < bossChance) {
            val margin = 200f
            val bossX = margin + (worldWidth - margin * 2) * (0.2f + 0.6f * (0..100).random() / 100f)
            val bossY = margin + (worldHeight - margin * 2) * (0.2f + 0.6f * (0..100).random() / 100f)

            val mobLevel = mob.level

            // Уровень босса = уровень моба + 2 (не +5!)
            val bossLevel = mobLevel + 2

            // HP: базовое * 2 (не * 3!)
            val bossHp = mob.maxHp * 2f

            val bossAttack = (mob.attack * 1.2f).toInt()

            val bossDefense = (mob.defense * 1.2f).toInt()

            val boss = Mob(
                x = bossX,
                y = bossY,
                type = mob.type,
                hp = bossHp,
                maxHp = bossHp,
                level = bossLevel,
                isBoss = true,
                attack = bossAttack,
                defense = bossDefense
            ).apply {
                startX = bossX
                startY = bossY
            }

            val location = locationManager.getCurrentData()
            location.mobs.add(boss)
            bossOnMap = true

            showMessage("👑 БОСС ПОЯВИЛСЯ! ${boss.getTypeName()} (ур.${boss.level})")

            respawnEffectTimer = 40
            respawnEffectX = bossX - cameraManager.x
            respawnEffectY = bossY - cameraManager.y
        }
    }

    private fun resetStats() {
        if (player.gold < 1000) {
            showMessage("❌ Недостаточно золота! Нужно 1000 💰")
            return
        }

        player.gold -= 1000

        val totalPoints = player.strength + player.endurance +
                player.agility + player.dexterity + player.luck

        player.strength = 5
        player.endurance = 5
        player.agility = 5
        player.dexterity = 5
        player.luck = 5

        player.skillPoints += (totalPoints - 25)
        player.hp = player.calculateMaxHp()

        showMessage("🔄 Характеристики сброшены! +${player.skillPoints} очков прокачки!")
        saveGame()
    }

    private fun renamePlayer(newName: String) {
        if (player.gold < 500) {
            showMessage("❌ Недостаточно золота! Нужно 500 💰")
            return
        }

        player.gold -= 500
        val oldName = player.name
        player.name = newName

        showMessage("✅ Имя изменено с \"$oldName\" на \"${player.name}\"!")
        saveGame()
    }

    // ===== МАГАЗИН =====
    private fun openShop() {
        gameState = GameState.SHOP
    }

    private fun closeShop() {
        gameState = GameState.GAME
        saveGame()
    }

    private fun openRenameFromShop() {
        closeShop()
        openInventory()
    }

    private fun resetStatsFromShop() {
        closeShop()
        resetStats()
    }

    private fun buyShopItem(item: ShopScreen.ShopItem) {
        if (player.gold < item.price) {
            showMessage("❌ Недостаточно золота!")
            return
        }

        player.gold -= item.price

        val newItem = Item(
            id = item.id,
            name = item.name,
            type = Item.ItemType.CONSUMABLE,
            rarity = item.rarity,
            stats = ItemStats(),
            description = when (item.id) {
                "cake_small" -> "Восстанавливает 30 HP 🎂"
                "cake_medium" -> "Восстанавливает 60 HP 🍰"
                "cake_large" -> "Восстанавливает 120 HP 🎂"
                else -> "Восстанавливает HP"
            }
        )

        if (inventory.addItem(newItem)) {
            showMessage("✅ ${item.name} куплен и добавлен в инвентарь!")
            saveGame()
        } else {
            player.gold += item.price
            showMessage("⚠️ Инвентарь полон!")
        }
    }

    private fun refineItem(index: Int) {
        val items = inventory.getItems()
        if (index >= items.size) return

        val item = items[index] ?: return

        if (item.type == Item.ItemType.CONSUMABLE) {
            showMessage("❌ Нельзя заточить расходник!")
            return
        }

        if (item.refineLevel >= 10) {
            showMessage("❌ Предмет уже заточен до максимума!")
            return
        }

        val refineManager = RefineManager()
        val canRefine = refineManager.canRefine(item, player, inventory)

        if (!canRefine) {
            val needed = RefineManager.getMaterialsNeeded(item.refineLevel)
            val cost = RefineManager.getGoldCost(item.refineLevel)
            val materialName = if (item.type == Item.ItemType.WEAPON || item.type == Item.ItemType.SHIELD) {
                "Оридикона"
            } else {
                "Элуниума"
            }
            showMessage("❌ Не хватает ресурсов! Нужно: $needed $materialName и $cost 💰")
            return
        }

        val result = refineManager.refine(item, player, inventory)
        showMessage(result.message)

        if (result.broken) {
            inventory.removeItem(index)
            inventory.selectedSlot = -1
            showMessage("💔 ${item.name} уничтожен при заточке!")
        }

        saveGame()
    }

    private fun resetFullGame() {
        // Показываем окно подтверждения
        showResetFullConfirm = true
    }

    // Вызываем этот метод из меню и StatsScreen
    private fun confirmFullReset() {
        // Сбрасываем все данные игрока
        player.name = "Герой"
        player.level = 1
        player.exp = 0
        player.maxExp = 50
        player.skillPoints = 0
        player.gold = 0
        player.hp = 100f
        player.x = 400f
        player.y = 400f
        player.targetX = 400f
        player.targetY = 400f
        player.isMoving = false

        // Сбрасываем характеристики
        player.strength = 5
        player.endurance = 5
        player.agility = 5
        player.dexterity = 5
        player.luck = 5

        // ⭐ ОЧИЩАЕМ ИНВЕНТАРЬ - УДАЛЯЕМ ВСЕ ПРЕДМЕТЫ
        val items = inventory.getItems()
        for (i in items.indices) {
            if (items[i] != null) {
                inventory.removeItem(i)
            }
        }

        // ⭐ ОЧИЩАЕМ ЭКИПИРОВКУ - СНИМАЕМ ВСЕ ВЕЩИ
        val equipment = inventory.getAllEquipment()
        for ((slot, _) in equipment) {
            inventory.unequip(slot)
        }

        // ⭐ ОЧИЩАЕМ ВЫБРАННЫЙ СЛОТ
        inventory.selectedSlot = -1

        // Сбрасываем флаги
        bossOnMap = false
        selectedMob = null
        targetMob = null
        isMovingToMob = false

        // Сохраняем
        saveGame()

        showMessage("🔄 Игра полностью сброшена! Начинайте заново.")
        println("🔄 Полный сброс игры выполнен!")
        println("   - Инвентарь очищен")
        println("   - Экипировка снята")
        println("   - Характеристики сброшены")

        // Закрываем окно подтверждения
        showResetFullConfirm = false
    }
}