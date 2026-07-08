package com.example.myapplication.renderer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import com.example.myapplication.GameView
import com.example.myapplication.model.IMobRenderer
import com.example.myapplication.model.Mob
import com.example.myapplication.model.MobAction

abstract class BaseMobRenderer(
    override val spriteName: String,
    override val fallbackColor: Int,
    override val size: Float = 25f
) : IMobRenderer {

    override var isAttacking = false
    override var attackFrameIndex = 0

    override fun drawOnMap(
        canvas: Canvas,
        x: Float,
        y: Float,
        mob: Mob,
        sizeMultiplier: Float,
        gameView: GameView?
    ) {
        if (gameView == null) {
            drawFallbackOnMap(canvas, x, y, sizeMultiplier)
            return
        }

        val animName = if (mob.isBoss && isAttacking) "attack" else getAnimationName(MobAction.IDLE)
        val animationFrames = gameView.getMobAnimationFrames(spriteName, animName)

        if (animationFrames.isNotEmpty()) {
            val frameIndex = if (isAttacking) {
                attackFrameIndex % animationFrames.size
            } else {
                (System.currentTimeMillis() / 300 % animationFrames.size).toInt()
            }
            val currentFrame = animationFrames[frameIndex % animationFrames.size]

            val displaySize = getMapSize() * sizeMultiplier
            val dstRect = RectF(
                x - displaySize / 2,
                y - displaySize / 2,
                x + displaySize / 2,
                y + displaySize / 2
            )

            val spriteSheet = gameView.getMobSpriteSheet(spriteName)
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackOnMap(canvas, x, y, sizeMultiplier)
            }
        } else {
            drawFallbackOnMap(canvas, x, y, sizeMultiplier)
        }
    }

    override fun drawInBattle(
        canvas: Canvas,
        x: Float,
        y: Float,
        mob: Mob,
        scale: Float,
        gameView: GameView
    ) {
        val animName = if (isAttacking) getAnimationName(MobAction.ATTACK) else getAnimationName(MobAction.IDLE)
        val animationFrames = gameView.getMobAnimationFrames(spriteName, animName)

        if (animationFrames.isNotEmpty()) {
            val frameIndex = if (isAttacking) {
                attackFrameIndex % animationFrames.size
            } else {
                (System.currentTimeMillis() / 200 % animationFrames.size).toInt()
            }
            val currentFrame = animationFrames[frameIndex % animationFrames.size]

            val battleScale = getBattleScale()
            val displayWidth = size * battleScale * scale * 1.5f
            val displayHeight = size * battleScale * scale * 1.5f

            val dstRect = RectF(
                x - displayWidth / 2,
                y - displayHeight / 2,
                x + displayWidth / 2,
                y + displayHeight / 2
            )

            val spriteSheet = gameView.getMobSpriteSheet(spriteName)
            if (spriteSheet != null) {
                canvas.drawBitmap(spriteSheet, currentFrame, dstRect, null)
            } else {
                drawFallbackInBattle(canvas, x, y, scale)
            }
        } else {
            drawFallbackInBattle(canvas, x, y, scale)
        }
    }

    protected open fun drawFallbackOnMap(canvas: Canvas, x: Float, y: Float, sizeMultiplier: Float) {
        drawFallbackShape(canvas, x, y, size * sizeMultiplier, fallbackColor)
    }

    protected open fun drawFallbackInBattle(canvas: Canvas, x: Float, y: Float, scale: Float) {
        drawFallbackShape(canvas, x, y, size * scale * 1.2f, fallbackColor)
    }

    protected open fun drawFallbackShape(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x, y, radius, paint)

        // Глаза
        val eyePaint = Paint().apply {
            this.color = Color.WHITE
        }
        val pupilPaint = Paint().apply {
            this.color = Color.BLACK
        }

        val eyeOffset = radius * 0.35f
        val eyeRadius = radius * 0.3f
        val pupilRadius = radius * 0.15f

        canvas.drawCircle(x - eyeOffset, y - radius * 0.2f, eyeRadius, eyePaint)
        canvas.drawCircle(x + eyeOffset, y - radius * 0.2f, eyeRadius, eyePaint)
        canvas.drawCircle(x - eyeOffset + pupilRadius * 0.5f, y - radius * 0.2f, pupilRadius, pupilPaint)
        canvas.drawCircle(x + eyeOffset + pupilRadius * 0.5f, y - radius * 0.2f, pupilRadius, pupilPaint)
    }
}
