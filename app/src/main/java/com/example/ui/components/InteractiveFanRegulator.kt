package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.ui.theme.JrCardDark
import com.example.ui.theme.JrError
import com.example.ui.theme.JrPrimary
import com.example.ui.theme.JrSecondary
import com.example.ui.theme.JrSurfaceVariantDark
import com.example.ui.theme.JrTextMuted
import com.example.ui.theme.JrTextPrimary
import com.example.ui.theme.JrTextSecondary
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun InteractiveFanRegulatorCard(
    channel: Channel,
    onSpeedChange: (newSpeed: Int) -> Unit,
    onTogglePower: () -> Unit,
    onOpenFullRegulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentSpeed = channel.fanSpeed
    val maxSpeed = channel.maxFanSpeed.coerceAtLeast(3)
    val isRunning = channel.state && currentSpeed > 0

    // Fan Blade Spin Animation: speed 1 is slow, speed 5 is fast
    val infiniteTransition = rememberInfiniteTransition(label = "fanSpin")
    val durationMs = when {
        !isRunning -> 0
        currentSpeed == 1 -> 1400
        currentSpeed == 2 -> 900
        currentSpeed == 3 -> 600
        currentSpeed == 4 -> 400
        else -> 260
    }

    val rotationAngle by if (isRunning) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = durationMs, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spinAngle"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier
            .testTag("fan_regulator_${channel.channelIndex}")
            .shadow(
                elevation = if (isRunning) 8.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = if (isRunning) JrPrimary else Color.Black
            )
            .clip(RoundedCornerShape(20.dp))
            .background(JrCardDark)
            .border(
                1.5.dp,
                if (isRunning) JrPrimary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.06f),
                RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Channel info, Full screen button, Power toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FAN ${channel.channelIndex}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = JrTextMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = channel.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = JrTextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onOpenFullRegulator,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "Full Dial",
                            tint = JrPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Power Button
                    IconButton(
                        onClick = {
                            performHaptic(context)
                            onTogglePower()
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) JrPrimary else JrSurfaceVariantDark)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PowerSettingsNew,
                            contentDescription = "Power",
                            tint = if (isRunning) Color.White else JrTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center Circular Dial Graphic with spinning fan blades
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { onOpenFullRegulator() },
                contentAlignment = Alignment.Center
            ) {
                // Circular arc scale
                Canvas(modifier = Modifier.size(120.dp)) {
                    val stroke = 8.dp.toPx()
                    val radius = (size.minDimension - stroke) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    // Background track
                    drawCircle(
                        color = Color(0xFF263345),
                        radius = radius,
                        center = center,
                        style = Stroke(width = stroke)
                    )

                    // Active Arc based on speed ratio
                    val sweepAngle = if (isRunning) (currentSpeed.toFloat() / maxSpeed.toFloat()) * 360f else 0f
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(JrSecondary, JrPrimary, JrSecondary)
                        ),
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )
                }

                // Inner rotating Fan Blades Icon
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(if (isRunning) Color(0xFF1B2433) else Color(0xFF131A24))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Air,
                        contentDescription = "Fan Blades",
                        tint = if (isRunning) JrSecondary else JrTextMuted,
                        modifier = Modifier
                            .size(42.dp)
                            .rotate(rotationAngle)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Speed display
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (isRunning) "Speed $currentSpeed" else "Fan OFF",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isRunning) JrSecondary else JrTextMuted
                )
                if (isRunning) {
                    Text(
                        text = " / $maxSpeed",
                        fontSize = 12.sp,
                        color = JrTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stepped Speed Pills (0, 1, 2, 3, 4, 5)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrement Button
                OutlinedIconButton(
                    onClick = {
                        if (currentSpeed > 0) {
                            performHaptic(context)
                            onSpeedChange(currentSpeed - 1)
                        }
                    },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Remove,
                        contentDescription = "Decrease",
                        tint = JrTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Quick Pills
                for (step in 0..maxSpeed) {
                    val isSelected = (step == currentSpeed && isRunning) || (step == 0 && !isRunning)
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) JrPrimary else JrSurfaceVariantDark
                            )
                            .clickable {
                                performHaptic(context)
                                onSpeedChange(step)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$step",
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else JrTextSecondary
                        )
                    }
                }

                // Increment Button
                OutlinedIconButton(
                    onClick = {
                        if (currentSpeed < maxSpeed) {
                            performHaptic(context)
                            onSpeedChange(currentSpeed + 1)
                        }
                    },
                    modifier = Modifier.size(32.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Increase",
                        tint = JrTextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Full interactive circular regulator dial dialog / bottom sheet
 */
@Composable
fun FullFanRegulatorDialog(
    channel: Channel,
    onDismiss: () -> Unit,
    onSpeedChange: (Int) -> Unit,
    onTogglePower: () -> Unit
) {
    val context = LocalContext.current
    val maxSpeed = channel.maxFanSpeed.coerceAtLeast(3)
    var currentSpeed = channel.fanSpeed
    val isRunning = channel.state && currentSpeed > 0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = Color(0xFF131924)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.2f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = channel.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = JrTextPrimary
            )
            Text(
                text = "Precision Hardware Speed Regulator",
                fontSize = 12.sp,
                color = JrTextMuted
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Rotary Drag Canvas Dial
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touchPos = change.position
                            val angleRad = atan2(touchPos.y - center.y, touchPos.x - center.x)
                            var angleDeg = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                            if (angleDeg < 0) angleDeg += 360f

                            // Map 0..360 deg to 0..maxSpeed
                            val newStep = ((angleDeg / 360f) * maxSpeed).roundToInt().coerceIn(0, maxSpeed)
                            if (newStep != currentSpeed) {
                                performHaptic(context)
                                onSpeedChange(newStep)
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(240.dp)) {
                    val stroke = 14.dp.toPx()
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = (size.minDimension - stroke) / 2

                    // Outer bezel
                    drawCircle(
                        color = Color(0xFF1F2937),
                        radius = radius,
                        center = center,
                        style = Stroke(width = stroke)
                    )

                    // Active sweep
                    val sweep = if (isRunning) (currentSpeed.toFloat() / maxSpeed.toFloat()) * 360f else 0f
                    drawArc(
                        brush = Brush.sweepGradient(listOf(JrSecondary, JrPrimary, JrSecondary)),
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = stroke, cap = StrokeCap.Round)
                    )

                    // Draw tick marks for each speed step
                    for (step in 0..maxSpeed) {
                        val angle = (-90f + (step.toFloat() / maxSpeed) * 360f) * (PI / 180f).toFloat()
                        val tickInnerRadius = radius - 18.dp.toPx()
                        val tickOuterRadius = radius - 8.dp.toPx()
                        val p1 = Offset(center.x + tickInnerRadius * cos(angle), center.y + tickInnerRadius * sin(angle))
                        val p2 = Offset(center.x + tickOuterRadius * cos(angle), center.y + tickOuterRadius * sin(angle))
                        drawLine(
                            color = if (step <= currentSpeed && isRunning) JrSecondary else Color.White.copy(alpha = 0.3f),
                            start = p1,
                            end = p2,
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Center Dial Knob
                Column(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(0xFF283548), Color(0xFF161E2B))
                            )
                        )
                        .border(2.dp, JrPrimary.copy(alpha = 0.4f), CircleShape),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (isRunning) "$currentSpeed" else "OFF",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isRunning) JrSecondary else JrTextMuted
                    )
                    Text(
                        text = if (isRunning) "LEVEL" else "STANDBY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Power button + Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        performHaptic(context)
                        onTogglePower()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) JrError else JrPrimary
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PowerSettingsNew,
                        contentDescription = "Toggle Power",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isRunning) "Turn OFF" else "Turn ON")
                }

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = JrSurfaceVariantDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(text = "Done")
                }
            }
        }
    }
}

private fun performHaptic(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(20)
            }
        }
    } catch (e: Exception) {
        // Non-fatal
    }
}
