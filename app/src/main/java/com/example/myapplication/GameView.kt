package com.example.myapplication

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.example.myapplication.manager.*
import com.example.myapplication.model.*
import com.example.myapplication.renderer.*
import com.example.myapplication.ui.InventoryScreen
import com.example.myapplication.ui.StatsScreen
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class GameView(context: Context) : SurfaceView(context), Runnable {

    private var thread: Thread? = null
    private var isRunning = false

    private enum class GameState {
        GAME, MAP, BATTLE, STATS, INVENTORY
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

    private lateinit var spriteManager: SpriteManager

    // Для карты
    private var currentSprite = "character"
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

    // Экран характеристик
    private lateinit var statsScreen: StatsScreen

    // Регенерация HP
    private var regenTimer = 0
    private val REGEN_INTERVAL = 60  // 60 кадров ≈ 1 секунда (при 60 FPS)
    private val REGEN_AMOUNT = 1f    // 1 HP за тик

    private lateinit var inventoryScreen: InventoryScreen
    private val inventory = Inventory()

    init {
        spriteManager = SpriteManager(context)
        GameRenderer.loadBackgrounds(context)
        statsScreen = StatsScreen()
        inventoryScreen = InventoryScreen()
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
            GameState.MAP -> { /* Ничего не обновляем */ }
            GameState.STATS -> { /* Ничего не обновляем */ }
            GameState.INVENTORY -> { /* Ничего не обновляем */ }
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
        location.mobs.removeAll { it.isDead && it.deathTimer > 30 }

        for (mob in location.mobs) {
            if (mob.isDead) mob.deathTimer++
        }

        // ⭐ РЕГЕНЕРАЦИЯ HP ТОЛЬКО НА КАРТЕ (НЕ В БОЮ!)
        // gameState == GameState.GAME — это проверка, что мы на карте
        if (gameState == GameState.GAME) {
            regenTimer++
            if (regenTimer >= REGEN_INTERVAL) {
                regenTimer = 0
                if (player.hp < player.calculateMaxHp()) {
                    player.hp = min(player.hp + REGEN_AMOUNT, player.calculateMaxHp())
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
                    showMessage("🌲 Вы вошли в Лес!")
                } else if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.WASTELAND)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    showMessage("🏜️ Вы вошли в Пустошь!")
                }
            }
            LocationManager.Location.FOREST -> {
                if (player.x < 60f) {
                    locationManager.moveTo(LocationManager.Location.CITY)
                    player.x = worldWidth - 60f
                    player.targetX = player.x
                    showMessage("🏙️ Вы вернулись в Город!")
                }
            }
            LocationManager.Location.WASTELAND -> {
                if (player.x > worldWidth - 60f) {
                    locationManager.moveTo(LocationManager.Location.CITY)
                    player.x = 60f
                    player.targetX = player.x
                    showMessage("🏙️ Вы вернулись в Город!")
                }
            }
        }
    }

    private fun showMessage(text: String) {
        messageText = text
        messageTimer = 120
    }

    // ========== СИСТЕМА ПРОКАЧКИ ==========

    // ⭐ ОСНОВНАЯ ПРОВЕРКА ПОВЫШЕНИЯ УРОВНЯ
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
        } else {
            showMessage("❌ Нет очков для прокачки!")
        }
    }

    // ========== БОЙ ==========

    private fun startBattle(mob: Mob) {
        println("⚔️ startBattle() вызван! Моб: ${mob.getTypeName()}")
        isLevelChecked = false

        // ⭐ ОБРАБОТЧИК ДРОПА
        battleManager.onItemDrop = { item ->
            if (inventory.addItem(item)) {
                showMessage("🗡️ ${item.name} добавлен в инвентарь!")
                println("✅ ${item.name} добавлен в инвентарь")
            } else {
                showMessage("⚠️ Инвентарь полон! ${item.name} потерян.")
                println("❌ Инвентарь полон! ${item.name} потерян.")
            }
        }

        battleManager.startBattle(player, mob)
        battleManager.onBattleEnd = {
            println("🏁 onBattleEnd() вызван!")
            gameState = GameState.GAME
            battleManager.endBattle()
            isAttackingInBattle = false
            player.x = 400f
            player.y = 400f
            player.targetX = player.x
            player.targetY = player.y
            player.isMoving = false
            showMessage("Бой окончен!")
        }
        gameState = GameState.BATTLE
        isAttackingInBattle = false
        battleFrameIndex = 0
        battleFrameTimer = 0
        showMessage("⚔️ Бой начался!")
    }

    private fun attackInBattle() {
        println("⚔️ attackInBattle() вызван!")
        isAttackingInBattle = true
        battleFrameIndex = 0
        battleFrameTimer = 0

        battleManager.playerAttack()

        if (battleManager.state == BattleManager.BattleState.VICTORY) {
            println("🏆 Победа! Опыт до проверки: ${player.exp}/${player.maxExp}")
            showMessage("💥 Победа! +20 опыта")
            android.os.Handler().postDelayed({
                battleManager.endBattle()
                gameState = GameState.GAME
                isAttackingInBattle = false
                println("🔍 Проверяем уровень после победы...")
                checkLevelUp()
            }, 1500)
        } else if (battleManager.state == BattleManager.BattleState.DEFEAT) {
            println("💀 Поражение!")
            showMessage("💀 Вы погибли...")
            android.os.Handler().postDelayed({
                battleManager.endBattle()
                gameState = GameState.GAME
                player.hp = player.calculateMaxHp()
                player.x = 400f
                player.y = 400f
                isAttackingInBattle = false
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
                GameState.MAP -> MapRenderer.drawMap(canvas, screenWidth, screenHeight, locationManager)
                GameState.BATTLE -> drawBattle(canvas)
                GameState.STATS -> {
                    drawGame(canvas)
                    statsScreen.draw(
                        canvas,
                        screenWidth,
                        screenHeight,
                        player,
                        { statType -> upgradeStat(statType) },
                        { closeStats() }
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
                        { closeInventory() }
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
            }
            LocationManager.Location.WASTELAND -> {
                canvas.drawText("🏙️ →", worldWidth - cameraManager.x - 30f, screenHeight / 2, arrowPaint)
            }
        }
    }

    private fun drawPlayer(canvas: Canvas) {
        val x = player.x - cameraManager.x
        val y = player.y - cameraManager.y

        currentAnimation = when {
            player.isMoving -> "run"
            player.attackCooldown > 0 -> "attack"
            else -> "idle"
        }

        val animationFrames = spriteManager.getAnimationFrames(currentSprite, currentAnimation)

        if (animationFrames.isNotEmpty()) {
            frameTimer++
            val animation = spriteManager.getAnimation(currentSprite, currentAnimation)
            if (animation != null && frameTimer > animation.speed) {
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
                if (player.facing == 1) {
                    canvas.save()
                    canvas.scale(-1f, 1f, x, y)
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                    canvas.restore()
                } else {
                    canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
                }
            }
        }
    }

    fun drawBattlePlayer(canvas: Canvas, x: Float, y: Float, scale: Float) {
        battleSprite = if (isAttackingInBattle) "battle_attack" else "battle_idle"
        battleAnimation = if (isAttackingInBattle) "attack" else "idle"

        val animationFrames = spriteManager.getAnimationFrames(battleSprite, battleAnimation)

        if (animationFrames.isNotEmpty()) {
            battleFrameTimer++
            val animation = spriteManager.getAnimation(battleSprite, battleAnimation)
            val speed = animation?.speed ?: 8

            if (battleFrameTimer > speed) {
                battleFrameTimer = 0
                battleFrameIndex = (battleFrameIndex + 1) % animationFrames.size
                if (isAttackingInBattle && battleFrameIndex == 0) {
                    isAttackingInBattle = false
                }
            }

            val currentFrame = animationFrames[battleFrameIndex % animationFrames.size]

            val displayWidth = 150f * scale
            val displayHeight = 150f * scale

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = spriteManager.getSpriteSheet(battleSprite)
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

        // Название локации
        val locPaint = Paint().apply {
            color = Color.WHITE
            textSize = 28f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("📍 ${locationData.name}", screenWidth / 2, 50f, locPaint)

        val btnWidth = 180f
        val btnHeight = 60f
        val btnRight = screenWidth - 20f
        val btnLeft = btnRight - btnWidth

        // ----- КНОПКА "КАРТА" -----
        val mapBtnPaint = Paint().apply {
            color = Color.rgb(50, 100, 200)
            style = Paint.Style.FILL
        }
        canvas.drawRect(btnLeft, 20f, btnRight, 20f + btnHeight, mapBtnPaint)
        val mapTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 22f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🗺️ Карта", btnLeft + btnWidth / 2, 20f + btnHeight / 2 + 8f, mapTextPaint)

        // ----- КНОПКА "ХАРАКТЕРИСТИКИ" -----
        val statsBtnPaint = Paint().apply {
            color = Color.rgb(200, 150, 50)
            style = Paint.Style.FILL
        }
        canvas.drawRect(btnLeft, 20f + btnHeight + 10f, btnRight, 20f + btnHeight * 2 + 10f, statsBtnPaint)
        val statsTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("📊 Хар-ки", btnLeft + btnWidth / 2, 20f + btnHeight * 1.5f + 10f + 8f, statsTextPaint)

        // Индикатор очков прокачки (над кнопкой характеристик)
        if (player.skillPoints > 0) {
            val pointsPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 16f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText("+${player.skillPoints}", btnLeft + btnWidth / 2, 20f + btnHeight + 10f - 8f, pointsPaint)
        }

        // ----- КНОПКА "ИНВЕНТАРЬ" -----
        val invBtnPaint = Paint().apply {
            color = Color.rgb(100, 150, 200)
            style = Paint.Style.FILL
        }
        canvas.drawRect(btnLeft, 20f + btnHeight * 2 + 20f, btnRight, 20f + btnHeight * 3 + 20f, invBtnPaint)
        val invTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
        }
        canvas.drawText("🎒 Инвентарь", btnLeft + btnWidth / 2, 20f + btnHeight * 2.5f + 20f + 8f, invTextPaint)

        // ----- СТАТИСТИКА -----
        val statPaint = Paint().apply {
            color = Color.WHITE
            textSize = 20f
            textAlign = Paint.Align.LEFT
            typeface = Typeface.DEFAULT_BOLD
        }
        val mobCount = locationData.mobs.count { !it.isDead }
        canvas.drawText("❤️ HP: ${player.hp.toInt()}/${player.calculateMaxHp().toInt()}", 20f, 40f, statPaint)
        canvas.drawText("⭐ Ур.${player.level} | Опыт: ${player.exp}/${player.maxExp} | 🎯 Очки: ${player.skillPoints}", 20f, 70f, statPaint)
        canvas.drawText("👾 Мобы: $mobCount", 20f, 100f, statPaint)

        // Сообщение
        if (messageTimer > 0) {
            val msgPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 24f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.DEFAULT_BOLD
            }
            canvas.drawText(messageText, screenWidth / 2, screenHeight - 80f, msgPaint)
        }

        // Подсказка
        val hintPaint = Paint().apply {
            color = Color.argb(150, 255, 255, 255)
            textSize = 16f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText("🖱 Тап - движение | Двойной тап по мобу - бой", 20f, screenHeight - 30f, hintPaint)
    }

    // ========== ОБРАБОТКА КАСАНИЙ ==========

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val x = event.x
            val y = event.y

            // ЭКРАН ХАРАКТЕРИСТИК
            if (gameState == GameState.STATS) {
                statsScreen.handleTouch(
                    x, y, screenWidth, screenHeight, player,
                    { statType -> upgradeStat(statType) },
                    { closeStats() }
                )
                return true
            }

// В onTouchEvent, для GameState.INVENTORY:
            if (gameState == GameState.INVENTORY) {
                inventoryScreen.handleTouch(
                    x, y, screenWidth, screenHeight, inventory,
                    { index -> equipItem(index) },      // ← экипировка
                    { slot -> unequipItem(slot) },      // ← снятие
                    { closeInventory() }
                )
                return true
            }

            // Кнопка "Карта"
            if (x > screenWidth - 180f && x < screenWidth - 20f && y > 20f && y < 80f) {
                gameState = if (gameState == GameState.MAP) GameState.GAME else GameState.MAP
                return true
            }

            // Кнопка "Характеристики"
            if (x > screenWidth - 180f && x < screenWidth - 20f && y > 90f && y < 150f) {
                openStats()
                return true
            }

            // Кнопка "Инвентарь"
            if (x > screenWidth - 180f && x < screenWidth - 20f && y > 160f && y < 220f) {
                openInventory()
                return true
            }

            // Обработка кликов на карте
            if (gameState == GameState.MAP) {
                val handled = MapRenderer.handleMapClick(
                    x, y, screenWidth, screenHeight, locationManager
                ) { location ->
                    teleportToLocation(location)
                }
                if (handled) return true
                gameState = GameState.GAME
                return true
            }

            // БОЕВОЙ РЕЖИМ
            if (gameState == GameState.BATTLE) {
                if (x > screenWidth - 180f && x < screenWidth - 30f &&
                    y > screenHeight - 90f && y < screenHeight - 30f) {
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

    private fun openInventory() {
        gameState = GameState.INVENTORY
    }

    private fun closeInventory() {
        gameState = GameState.GAME
    }

    private fun equipItem(index: Int) {
        inventory.equip(index)
    }

    // ===== ДЛЯ InventoryScreen =====
    fun getCharacterIdleFrames(): List<Rect> {
        return spriteManager.getAnimationFrames("battle_idle", "idle")
    }

    fun getCharacterIdleSpriteSheet(): Bitmap? {
        return spriteManager.getSpriteSheet("battle_idle")
    }

    // Добавь метод для снятия предмета:
    private fun unequipItem(slot: EquipmentSlot) {
        val item = inventory.unequip(slot)
        if (item != null) {
            showMessage("🔽 ${item.name} снят!")
            println("🔽 ${item.name} снят с ${slot.name}")
        } else {
            showMessage("❌ Нет места в инвентаре!")
        }
    }

}
