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

    // 暴露球的位置作为 StateFlow
    private val _ballPosition = MutableStateFlow(Offset.Zero)
    val ballPosition: StateFlow<Offset> = _ballPosition.asStateFlow()

    /**
     * 当 UI 知道游戏场地的大小时调用
     */
    fun initBall(fieldWidth: Float, fieldHeight: Float, ballSizePx: Float) {
        if (ball == null) {
            Log.d("BallVM", "initBall called: width=$fieldWidth, height=$fieldHeight, size=$ballSizePx")
            ball = Ball(fieldWidth, fieldHeight, ballSizePx)
            _ballPosition.value = Offset(ball!!.posX, ball!!.posY)
            Log.d("BallVM", "Initial position: (${ball!!.posX}, ${ball!!.posY})")
        }
    }

    /**
     * 当来自 UI 的 SensorEventListener 调用时
     *
     * ⚠️ 关键点：重力传感器的正确理解
     *
     * 重力传感器返回的是设备相对于重力的加速度向量：
     * - x 轴：左(-) 到 右(+)
     * - y 轴：下(-) 到 上(+)（设备坐标系）
     * - z 轴：屏幕内(-) 到 屏幕外(+)
     *
     * 但屏幕坐标系是：
     * - x 轴：左(0) 到 右(增加)  ✓ 一致
     * - y 轴：上(0) 到 下(增加)  ✗ 相反！
     *
     * 所以需要反转 y 轴的加速度：yAcc = -event.values[1]
     *
     * 当设备平放，屏幕朝上时：
     * - x ≈ 0, y ≈ 0, z ≈ -9.8 (重力指向屏幕内，即向地心)
     *
     * 当设备向右倾斜时：
     * - x 增加（正值）→ 球向右移动 ✓
     * - y 保持接近 0
     * - z 减少（朝 0 靠近）
     *
     * 当设备向前倾斜时（屏幕顶部下降）：
     * - x 保持接近 0
     * - y 减少（变成负值，因为重力往前倾）
     * - 反转后：yAcc = -y ，变成正值 → 球向下移动 ✓
     * - z 减少（朝 0 靠近）
     */
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
        // 重置球的状态
        ball?.reset()

        // 更新 StateFlow 为重置位置
        ball?.let {
            Log.d("BallVM", "Reset position: (${it.posX}, ${it.posY})")
            _ballPosition.value = Offset(it.posX, it.posY)
        }

        // 重置 lastTimestamp
        lastTimestamp = 0L
    }
}