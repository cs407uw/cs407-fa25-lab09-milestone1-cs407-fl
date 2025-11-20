package com.cs407.lab09

import android.util.Log

/**
 * Represents a ball that can move. (No Android UI imports!)
 *
 * Constructor parameters:
 * - backgroundWidth: the width of the background, of type Float
 * - backgroundHeight: the height of the background, of type Float
 * - ballSize: the width/height of the ball, of type Float
 */
class Ball(
    private val backgroundWidth: Float,
    private val backgroundHeight: Float,
    private val ballSize: Float
) {
    var posX = 0f
    var posY = 0f
    var velocityX = 0f
    var velocityY = 0f
    private var accX = 0f
    private var accY = 0f

    private var isFirstUpdate = true

    init {
        reset()
    }

    /**
     * Updates the ball's position and velocity based on the given acceleration and time step.
     *
     * 根据公式：
     * 速度: v1 = v0 + 0.5 * (a1 + a0) * dT     ... (1)
     * 位置: l = v0 * dT + (1/6) * dT² * (3*a0 + a1)  ... (2)
     */
    fun updatePositionAndVelocity(xAcc: Float, yAcc: Float, dT: Float) {
        if (isFirstUpdate) {
            isFirstUpdate = false
            accX = xAcc
            accY = yAcc
            return
        }

        // 旧加速度 a0
        val a0x = accX
        val a0y = accY
        // 新加速度 a1
        val a1x = xAcc
        val a1y = yAcc

        val dt = dT

        // --- 1. 先用公式 (1) 算新速度 v1 ---
        val v1x = velocityX + 0.5f * (a0x + a1x) * dt
        val v1y = velocityY + 0.5f * (a0y + a1y) * dt

        // --- 2. 再用公式 (2) 算位移 ---
        val dx = velocityX * dt + (1f / 6f) * dt * dt * (3f * a0x + a1x)
        val dy = velocityY * dt + (1f / 6f) * dt * dt * (3f * a0y + a1y)

        posX += dx
        posY += dy

        velocityX = v1x
        velocityY = v1y
        accX = a1x
        accY = a1y

        checkBoundaries()
    }


    /**
     * Ensures the ball does not move outside the boundaries.
     * When it collides, velocity and acceleration perpendicular to the
     * boundary should be set to 0.
     */
    fun checkBoundaries() {
        // 左边界
        if (posX < 0) {
            Log.d("Ball", "  Collision: left boundary (posX=$posX)")
            posX = 0f
            velocityX = 0f
            accX = 0f
        }
        // 右边界
        if (posX + ballSize > backgroundWidth) {
            Log.d("Ball", "  Collision: right boundary (posX=$posX)")
            posX = backgroundWidth - ballSize
            velocityX = 0f
            accX = 0f
        }
        // 上边界
        if (posY < 0) {
            Log.d("Ball", "  Collision: top boundary (posY=$posY)")
            posY = 0f
            velocityY = 0f
            accY = 0f
        }
        // 下边界
        if (posY + ballSize > backgroundHeight) {
            Log.d("Ball", "  Collision: bottom boundary (posY=$posY)")
            posY = backgroundHeight - ballSize
            velocityY = 0f
            accY = 0f
        }
    }

    /**
     * Resets the ball to the center of the screen with zero
     * velocity and acceleration.
     */
    fun reset() {
        posX = (backgroundWidth - ballSize) / 2f
        posY = (backgroundHeight - ballSize) / 2f
        velocityX = 0f
        velocityY = 0f
        accX = 0f
        accY = 0f
        isFirstUpdate = true
        Log.d("Ball", "reset() called: position=($posX, $posY)")
    }
}