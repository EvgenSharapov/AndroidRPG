package com.example.myapplication

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
    private val worldHeight = 800f

    private var messageText = ""
    private var messageTimer = 0
    private var lastTouchTime = 0L
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var bossOnMap = false

    // ⭐ МЕНЮ
    private var isMenuOpen = false
    private val menuButtonSize = 105f

    private val POPUP_MENU_WIDTH = 700f
    private val POPUP_MENU_HEIGHT = 600f
    private val POPUP_BTN_WIDTH = 620f
    private val POPUP_BTN_HEIGHT = 70f
    private val POPUP_BTN_SPACING = 15f
    private val POPUP_CLOSE_SIZE = 45f

    // ⭐ КАРТА
    private lateinit var mapScreen: MapScreen

    private lateinit var spriteManager: SpriteManager

    // Для карты
    private var currentSprite = "character_zombie"
    private var currentAnimation = "idle"
    private var currentFrameIndex = 0
    private var frameTimer = 0

    // Для боя
    private var battleSprite = "battle_idle"
    private var battleAnimation = "idle"
    private var battleFrameIndex = 0
    private var battleFrameTimer = 0
    private var isAttackingInBattle = false
    private var isLevelChecked = false

    // ⭐ ЭФФЕКТ РЕСПАУНА
    private var respawnEffectTimer = 0
    private var respawnEffectX = 0f
    private var respawnEffectY = 0f

    // Экран характеристик
    private lateinit var statsScreen: StatsScreen

    // Регенерация HP
    private var regenTimer = 0
    private val REGEN_INTERVAL = 60

    // ⭐ МАГАЗИН
    private lateinit var shopScreen: ShopScreen
    private var isShopOpen = false

    private lateinit var inventoryScreen: InventoryScreen
    private val inventory = Inventory()

    private lateinit var saveManager: SaveManager

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
        if (player.isMoving) {
            val dx = player.targetX - player.x
            val dy = player.targetY - player.y
            val dist = sqrt(dx * dx + dy * dy)

            if (dist < player.speed) {
                player.x = player.targetX
                player.y = player.targetY
                player.isMoving = false
            } else {
                player.x += (dx / dist) * player.speed
                player.y += (dy / dist) * player.speed
                updateFacing(dx, dy)
            }
        }

        checkLocationTransition()

        val location = locationManager.getCurrentData()

        var deadCount = 0
        for (mob in location.mobs) {
            if (mob.isDead) {
                deadCount++
                mob.deathTimer++
                mob.respawnTimer++

                if (mob.respawnTimer % 60 == 0) {
                    println("💀 ${mob.getTypeName()} мёртв ${mob.respawnTimer}/900")
                }

                if (mob.respawnTimer >= 900) {
                    mob.respawn()
                    println("🔄 РЕСПАУН: ${mob.getTypeName()} воскрес!")
                    showMessage("🔄 ${mob.getTypeName()} воскрес!")
                }
            }
        }

        if (deadCount > 0) {
            println("💀 Всего мертвых мобов: $deadCount")
        }

        location.mobs.removeAll { it.isDead && it.deathTimer > 1800 }

        if (gameState == GameState.GAME) {
            regenTimer++

            val baseRegen = 1f
            val enduranceBonus = player.endurance * 0.05f
            val regenAmount = baseRegen + enduranceBonus

            if (regenTimer >= REGEN_INTERVAL) {
                regenTimer = 0
                if (player.hp < player.calculateMaxHp()) {
                    player.hp = min(player.hp + regenAmount, player.calculateMaxHp())
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
                    showMessage("🌲 Вы вошли в Лес!")
                } else if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.WASTELAND)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    showMessage("🏜️ Вы вошли в Пустошь!")
                }
            }
            LocationManager.Location.FOREST -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.CITY)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    showMessage("🏙️ Вы вернулись в Город!")
                } else if (player.x > worldWidth - 60f) {
                    locationManager.moveTo(LocationManager.Location.ROCKS)
                    player.x = 60f
                    player.targetX = player.x
                    bossOnMap = false
                    showMessage("⛰️ Вы вошли в Скалы!")
                }
            }
            LocationManager.Location.WASTELAND -> {
                if (player.x > worldWidth - 60f) {
                    locationManager.moveTo(LocationManager.Location.CITY)
                    player.x = 60f
                    player.targetX = player.x
                    bossOnMap = false
                    showMessage("🏙️ Вы вернулись в Город!")
                }
            }
            LocationManager.Location.ROCKS -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.FOREST)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    showMessage("🌲 Вы вернулись в Лес!")
                } else if (player.x > worldWidth - 60f) {
                    locationManager.moveTo(LocationManager.Location.CASTLE)
                    player.x = 60f
                    player.targetX = player.x
                    bossOnMap = false
                    showMessage("🏰 Вы вошли в Замок!")
                }
            }
            LocationManager.Location.CASTLE -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.ROCKS)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
                    showMessage("⛰️ Вы вернулись в Скалы!")
                }
            }
            LocationManager.Location.DESERT -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.WASTELAND)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    bossOnMap = false
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
        println("🔍 checkLevelUp() ВЫЗВАН! Опыт: ${player.exp}/${player.maxExp}")
        var leveledUp = false
        while (player.exp >= player.maxExp) {
            player.exp -= player.maxExp
            player.level++
            player.maxExp = (player.maxExp * 1.5f).toInt()
            player.skillPoints += 5
            leveledUp = true
            println("🎉 УРОВЕНЬ ${player.level}! skillPoints: ${player.skillPoints}")
            showMessage("🎉 УРОВЕНЬ ${player.level}! +5 очков прокачки!")
            saveGame()
        }

        if (leveledUp) {
            player.hp = player.calculateMaxHp()
            println("✅ HP восстановлен: ${player.hp}")
        } else {
            println("❌ Уровень НЕ повысился. Нужно: ${player.maxExp}, есть: ${player.exp}")
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
        println("⚔️ startBattle() вызван! Моб: ${mob.getTypeName()}")
        isLevelChecked = false

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
            if (victory && !mob.isBoss) {
                checkBossSpawn(mob)
            }
            gameState = GameState.GAME
            battleManager.endBattle()
            isAttackingInBattle = false
            player.x = 400f
            player.y = 400f
            player.targetX = player.x
            player.targetY = player.y
            player.isMoving = false
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
        println("⚔️ attackInBattle() вызван!")
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

            val boss = battleManager.currentMob

            android.os.Handler().postDelayed({
                if (boss != null && boss.isBoss) {
                    boss.hp = boss.maxHp
                    showMessage("👑 Босс восстановил все HP!")
                }

                battleManager.endBattle()
                gameState = GameState.GAME
                player.hp = player.calculateMaxHp()
                player.x = 400f
                player.y = 400f
                isAttackingInBattle = false
                saveGame()
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
                    statsScreen.draw(
                        canvas,
                        screenWidth,
                        screenHeight,
                        player,
                        { statType -> upgradeStat(statType) },
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
                        { newName -> renamePlayer(newName) }
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

        for (mob in locationData.mobs) {
            if (mob.isDead) continue
            val drawX = mob.x - cameraManager.x
            val drawY = mob.y - cameraManager.y
            MobRenderer.drawMob(canvas, mob, drawX, drawY, selectedMob == mob, this)
        }

        drawPlayer(canvas)
        drawUI(canvas)
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

        val glowPaint = Paint().apply {
            shader = RadialGradient(
                menuX + menuButtonSize / 2, menuY + menuButtonSize / 2, menuButtonSize,
                Color.argb(80, 255, 200, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(menuX + menuButtonSize / 2, menuY + menuButtonSize / 2, menuButtonSize, glowPaint)

        val iconPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("☰", menuX + menuButtonSize / 2, menuY + menuButtonSize / 2 + 14f, iconPaint)

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

        canvas.drawText(
            "❤️ HP: ${player.hp.toInt()}/${player.calculateMaxHp().toInt()}",
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

    // ========== ОБРАБОТКА КАСАНИЙ ==========

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

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

                // ⭐ КАРТА
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

                // ⭐ МАГАЗИН
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
                    { resetStats() }
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
                    { newName -> renamePlayer(newName) }
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

            // ⭐ КАРТА — ИСПОЛЬЗУЕМ mapScreen
            if (gameState == GameState.MAP) {
                val handled = mapScreen.handleTouch(
                    x, y, screenWidth, screenHeight, locationManager
                ) { location ->
                    teleportToLocation(location)
                }
                // Если клик вне карты — закрываем
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
            val timeDiff = currentTime - lastTouchTime

            if (timeDiff < 500 && abs(x - lastTouchX) < 50 && abs(y - lastTouchY) < 50) {
                val location = locationManager.getCurrentData()
                val touchWorldX = x + cameraManager.x
                val touchWorldY = y + cameraManager.y

                var foundMob: Mob? = null
                for (mob in location.mobs) {
                    if (mob.isDead) continue
                    if (abs(mob.x - touchWorldX) < 40 && abs(mob.y - touchWorldY) < 40) {
                        foundMob = mob
                        break
                    }
                }

                if (foundMob != null) {
                    startBattle(foundMob)
                    lastTouchTime = 0
                    return true
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
            println("🔽 ${item.name} снят с ${slot.name}")
            saveGame()
        } else {
            showMessage("❌ Нет места в инвентаре!")
        }
    }

    // ===== СОХРАНЕНИЕ =====
    private fun loadGame() {
        val hasSave = saveManager.loadGame(player, inventory)
        if (hasSave) {
            if (player.hp > player.calculateMaxHp()) {
                player.hp = player.calculateMaxHp()
            }
            showMessage("💾 Прогресс загружен!")
        } else {
            showMessage("🆕 Новая игра!")
        }
    }

    private fun saveGame() {
        saveManager.saveGame(player, inventory)
        println("💾 Прогресс сохранён")
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
        if (index < items.size) {
            val item = items[index]
            if (item != null && item.type == Item.ItemType.CONSUMABLE) {
                if (item.use(player)) {
                    inventory.removeItem(index)
                    showMessage("🍰 ${item.name} использован! HP: ${player.hp.toInt()}/${player.calculateMaxHp().toInt()}")
                    saveGame()
                } else {
                    showMessage("❌ HP уже максимальный!")
                }
            }
        }
    }

    private fun checkBossSpawn(mob: Mob) {
        if (bossOnMap) return

        val bossChance = 5
        val random = (0..99).random()

        if (random < bossChance) {
            val margin = 200f
            val bossX = margin + (worldWidth - margin * 2) * (0.2f + 0.6f * (0..100).random() / 100f)
            val bossY = margin + (worldHeight - margin * 2) * (0.2f + 0.6f * (0..100).random() / 100f)

            val boss = Mob(
                x = bossX,
                y = bossY,
                type = mob.type,
                hp = mob.maxHp * 3,
                maxHp = mob.maxHp * 3,
                level = mob.level + 5,
                isBoss = true
            ).apply {
                startX = bossX
                startY = bossY
            }

            val location = locationManager.getCurrentData()
            location.mobs.add(boss)
            bossOnMap = true

            showMessage("👑 БОСС ПОЯВИЛСЯ! ${boss.getTypeName()} (ур.${boss.level})")
            println("👑 БОСС: ${boss.getTypeName()} появился на карте! (ур.${boss.level})")

            respawnEffectTimer = 40
            respawnEffectX = bossX - cameraManager.x
            respawnEffectY = bossY - cameraManager.y
        }
    }

    // ⭐ МЕТОД СБРОСА ХАРАКТЕРИСТИК
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
        println("🔄 Сброс характеристик: возвращено ${player.skillPoints} очков")
    }

    // ⭐ МЕТОД ПЕРЕИМЕНОВАНИЯ
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
        println("👤 Имя изменено: $oldName → ${player.name}")
    }

    // ⭐ МАГАЗИН
    private fun openShop() {
        gameState = GameState.SHOP
        isShopOpen = true
    }

    private fun closeShop() {
        gameState = GameState.GAME
        isShopOpen = false
        saveGame()
    }

    private fun openRenameFromShop() {
        closeShop()
        openInventory()
        // TODO: автоматически открыть диалог переименования
    }

    private fun resetStatsFromShop() {
        closeShop()
        resetStats()
    }

    // ⭐ МЕТОД ПОКУПКИ ПРЕДМЕТА
    private fun buyShopItem(item: ShopScreen.ShopItem) {
        println("🛒 buyShopItem() ВЫЗВАН! Товар: ${item.name}, Цена: ${item.price}, Золото: ${player.gold}")

        if (player.gold < item.price) {
            showMessage("❌ Недостаточно золота!")
            return
        }

        // Списываем золото
        player.gold -= item.price
        println("💰 Списано ${item.price} золота. Осталось: ${player.gold}")

        // ⭐ ДЛЯ ТОРТОВ — СОЗДАЁМ ПРЕДМЕТ И ДОБАВЛЯЕМ В ИНВЕНТАРЬ
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

        // Добавляем в инвентарь
        if (inventory.addItem(newItem)) {
            showMessage("✅ ${item.name} куплен и добавлен в инвентарь!")
            saveGame()
            println("📦 ${item.name} добавлен в инвентарь")
        } else {
            // Если инвентарь полон — возвращаем золото
            player.gold += item.price
            showMessage("⚠️ Инвентарь полон!")
            println("❌ Инвентарь полон! Возвращено ${item.price} золота")
        }
    }

    // ⭐ МЕТОД ДЛЯ ОТРИСОВКИ МЕНЮ
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

        val closeX = menuX + POPUP_MENU_WIDTH - 45f
        val closeY = menuY + 45f

        val closeBgPaint = Paint().apply {
            color = Color.rgb(200, 50, 50)
        }
        canvas.drawCircle(closeX, closeY, POPUP_CLOSE_SIZE, closeBgPaint)

        val closeGlowPaint = Paint().apply {
            shader = RadialGradient(
                closeX, closeY, POPUP_CLOSE_SIZE + 20f,
                Color.argb(60, 255, 100, 100),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(closeX, closeY, POPUP_CLOSE_SIZE + 20f, closeGlowPaint)

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

        val glowPaint = Paint().apply {
            shader = RadialGradient(
                x + width / 2, y + height / 2, width * 0.6f,
                Color.argb(60, 255, 255, 255),
                Color.TRANSPARENT,
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(x + width / 2, y + height / 2, width * 0.6f, glowPaint)

        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 26f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText(text, x + width / 2, y + height / 2 + 9f, textPaint)
    }
}