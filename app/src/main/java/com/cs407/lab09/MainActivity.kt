package com.cs407.lab09

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs407.lab09.R
import com.cs407.lab09.ui.theme.Lab09Theme
import kotlin.math.roundToInt

// Main Activity
class MainActivity : ComponentActivity() {

    private val viewModel: BallViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ 禁用屏幕旋转 - 固定为竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContent {
            Lab09Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GameScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun GameScreen(viewModel: BallViewModel) {
    val context = LocalContext.current

    // 初始化传感器管理器
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // 获取重力传感器
    val gravitySensor = remember {
        sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    // 注册监听器
    DisposableEffect(sensorManager, gravitySensor) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    viewModel.onSensorDataChanged(it)
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // Do nothing
            }
        }

        if (gravitySensor != null) {
            sensorManager.registerListener(
                listener,
                gravitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        onDispose {
            if (gravitySensor != null) {
                sensorManager.unregisterListener(listener, gravitySensor)
            }
        }
    }

    // UI 布局
    Column(modifier = Modifier.fillMaxSize()) {
        // 1. Reset 按钮
        Button(
            onClick = {
                viewModel.reset()
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        ) {
            Text(text = "Reset")
        }

        // 2. 游戏场地
        val ballSize = 50.dp
        val ballSizePx = with(LocalDensity.current) { ballSize.toPx() }

        // 从 ViewModel 的 StateFlow 收集球的位置
        val ballPosition by viewModel.ballPosition.collectAsStateWithLifecycle()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .paint(
                    painter = painterResource(id = R.drawable.field),
                    contentScale = ContentScale.FillBounds
                )
                .onSizeChanged { size ->
                    // 告诉 ViewModel 场地的大小
                    viewModel.initBall(
                        fieldWidth = size.width.toFloat(),
                        fieldHeight = size.height.toFloat(),
                        ballSizePx = ballSizePx
                    )
                }
        ) {
            // 3. 球
            Image(
                painter = painterResource(id = R.drawable.soccer),
                contentDescription = "Soccer Ball",
                modifier = Modifier
                    .size(ballSize)
                    .offset {
                        IntOffset(
                            x = ballPosition.x.roundToInt(),
                            y = ballPosition.y.roundToInt()
                        )
                    }
            )
        }
    }
}