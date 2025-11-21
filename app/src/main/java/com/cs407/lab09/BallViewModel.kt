package com.cs407.lab09

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.sqrt

class BallViewModel : ViewModel() {

    private var ball: Ball? = null
    private var lastTimestamp: Long = 0L


    private val _ballPosition = MutableStateFlow(Offset.Zero)
    val ballPosition: StateFlow<Offset> = _ballPosition.asStateFlow()

    fun initBall(fieldWidth: Float, fieldHeight: Float, ballSizePx: Float) {
        if (ball == null) {
            Log.d("BallVM", "initBall called: width=$fieldWidth, height=$fieldHeight, size=$ballSizePx")
            ball = Ball(fieldWidth, fieldHeight, ballSizePx)
            _ballPosition.value = Offset(ball!!.posX, ball!!.posY)
            Log.d("BallVM", "Initial position: (${ball!!.posX}, ${ball!!.posY})")
        }
    }
    fun onSensorDataChanged(event: SensorEvent) {
        val currentBall = ball ?: return

        if (event.sensor.type == Sensor.TYPE_GRAVITY) {
            if (lastTimestamp == 0L) {
                lastTimestamp = event.timestamp
                return
            }

            val NS2S = 1.0f / 1_000_000_000.0f
            var dT = (event.timestamp - lastTimestamp) * NS2S



            val scale = 50f          // 自己试
            val xAcc = -event.values[0] * scale
            val yAcc =  event.values[1] * scale

            currentBall.updatePositionAndVelocity(xAcc, yAcc, dT)
            _ballPosition.update { Offset(currentBall.posX, currentBall.posY) }

            lastTimestamp = event.timestamp
        }
    }


    fun reset() {
        Log.d("BallVM", "Reset called")
        ball?.reset()

        ball?.let {
            Log.d("BallVM", "Reset position: (${it.posX}, ${it.posY})")
            _ballPosition.value = Offset(it.posX, it.posY)
        }

        lastTimestamp = 0L
    }
}