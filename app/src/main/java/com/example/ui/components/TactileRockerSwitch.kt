package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.ui.theme.JrCardDark
import com.example.ui.theme.JrPrimary
import com.example.ui.theme.JrSecondary
import com.example.ui.theme.JrSwitchLedOff
import com.example.ui.theme.JrSwitchLedOn
import com.example.ui.theme.JrTextMuted
import com.example.ui.theme.JrTextPrimary
import com.example.ui.theme.JrTextSecondary

@Composable
fun TactileRockerSwitch(
    channel: Channel,
    onToggle: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOn = channel.state

    val rockerElevation by animateFloatAsState(
        targetValue = if (isOn) 8f else 2f,
        animationSpec = tween(durationMillis = 180),
        label = "rockerElevation"
    )

    val ledColor by animateColorAsState(
        targetValue = if (isOn) JrSwitchLedOn else JrSwitchLedOff,
        animationSpec = tween(durationMillis = 200),
        label = "ledColor"
    )

    val borderGlow by animateColorAsState(
        targetValue = if (isOn) JrSecondary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.06f),
        animationSpec = tween(durationMillis = 200),
        label = "borderGlow"
    )

    val rockerBrush = if (isOn) {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF26354A),
                Color(0xFF1B2433)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1E2837),
                Color(0xFF131A24)
            )
        )
    }

    Box(
        modifier = modifier
            .testTag("switch_channel_${channel.channelIndex}")
            .shadow(
                elevation = rockerElevation.dp,
                shape = RoundedCornerShape(18.dp),
                spotColor = if (isOn) JrSecondary else Color.Black
            )
            .clip(RoundedCornerShape(18.dp))
            .background(JrCardDark)
            .border(1.5.dp, borderGlow, RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                performHapticFeedback(context)
                onToggle()
            }
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Channel number, favorite button, LED indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Channel Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "CH ${channel.channelIndex}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (channel.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (channel.isFavorite) JrPrimary else JrTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Tactile micro-LED Indicator dot with glow
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .shadow(if (isOn) 6.dp else 0.dp, CircleShape, spotColor = JrSecondary)
                            .clip(CircleShape)
                            .background(ledColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Rocker Switch Plate Graphic (Tactile Visual)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(rockerBrush)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Channel Category Icon
                    val iconVector = when (channel.iconName) {
                        "tv" -> Icons.Filled.Tv
                        "bed" -> Icons.Filled.Bed
                        "socket" -> Icons.Filled.Power
                        else -> Icons.Filled.Lightbulb
                    }

                    Icon(
                        imageVector = iconVector,
                        contentDescription = channel.name,
                        tint = if (isOn) JrSecondary else JrTextMuted,
                        modifier = Modifier.size(26.dp)
                    )

                    // Rocker indentation tactile line
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isOn) JrSecondary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.1f))
                    )

                    // ON/OFF State Text
                    Text(
                        text = if (isOn) "ON" else "OFF",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isOn) JrSecondary else JrTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Channel Label Name
            Text(
                text = channel.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = JrTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (isOn) "Active • Drawing Power" else "Standby",
                fontSize = 11.sp,
                color = if (isOn) JrSecondary else JrTextSecondary,
                maxLines = 1
            )
        }
    }
}

private fun performHapticFeedback(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(25)
            }
        }
    } catch (e: Exception) {
        // Haptic feedback failure is non-fatal
    }
}
