package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.domain.model.ChannelType
import com.example.domain.model.Device
import com.example.ui.theme.JrError
import com.example.ui.theme.JrPlateBezel
import com.example.ui.theme.JrPrimary
import com.example.ui.theme.JrSecondary
import com.example.ui.theme.JrSuccess
import com.example.ui.theme.JrTextMuted
import com.example.ui.theme.JrTextPrimary
import com.example.ui.theme.JrTextSecondary

@Composable
fun SwitchboardPlate(
    device: Device,
    channels: List<Channel>,
    onToggleSwitch: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onFanSpeedChange: (Channel, Int) -> Unit,
    onFanTogglePower: (Channel) -> Unit,
    onOpenFanDial: (Channel) -> Unit,
    onEditBoard: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Premium Faceplate Frame
    Box(
        modifier = modifier
            .testTag("switchboard_plate_${device.id}")
            .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = Color.Black)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E2736),
                        Color(0xFF141A24),
                        Color(0xFF0F141C)
                    )
                )
            )
            .border(
                2.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFF3B485D),
                        Color(0xFF1B222E),
                        Color(0xFF2E3849)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Corner screw accents & Brand Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScrewHeadAccent()

                // Centered Engraved Brand Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0A0D12))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (device.isOnline) JrSuccess else JrError)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "JR AUTOMATION • ${device.boardModel}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JrSecondary,
                            letterSpacing = 1.sp
                        )
                    }
                }

                ScrewHeadAccent()
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Board Name & Connectivity Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = device.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (device.isOnline) Icons.Filled.Wifi else Icons.Filled.WifiOff,
                            contentDescription = "Wi-Fi Status",
                            tint = if (device.isOnline) JrSecondary else JrError,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${device.ipAddress} • ${device.firmwareVersion}",
                            fontSize = 11.sp,
                            color = JrTextMuted
                        )
                    }
                }

                IconButton(
                    onClick = onEditBoard,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Board",
                        tint = JrTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic Channels Grid
            // Separate switches from fan regulators
            val switches = channels.filter { it.type != ChannelType.FAN }
            val fans = channels.filter { it.type == ChannelType.FAN }

            // Render switches in 2-column grid pairs
            val switchPairs = switches.chunked(2)
            for (pair in switchPairs) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (sw in pair) {
                        TactileRockerSwitch(
                            channel = sw,
                            onToggle = { onToggleSwitch(sw) },
                            onFavoriteToggle = { onToggleFavorite(sw) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Render Fan regulators
            for (fan in fans) {
                InteractiveFanRegulatorCard(
                    channel = fan,
                    onSpeedChange = { newSpeed -> onFanSpeedChange(fan, newSpeed) },
                    onTogglePower = { onFanTogglePower(fan) },
                    onOpenFullRegulator = { onOpenFanDial(fan) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bottom screw accents
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScrewHeadAccent()
                Text(
                    text = "${channels.size} Modular Channels",
                    fontSize = 10.sp,
                    color = JrTextMuted
                )
                ScrewHeadAccent()
            }
        }
    }
}

/**
 * Recessed philips-head wall screw accent
 */
@Composable
fun ScrewHeadAccent() {
    Box(
        modifier = Modifier
            .size(14.dp)
            .clip(CircleShape)
            .background(Color(0xFF26303D))
            .border(1.dp, Color(0xFF131A24), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Cross indentation
        Box(
            modifier = Modifier
                .width(8.dp)
                .height(1.5.dp)
                .background(Color(0xFF0F141A))
        )
        Box(
            modifier = Modifier
                .width(1.5.dp)
                .height(8.dp)
                .background(Color(0xFF0F141A))
        )
    }
}
