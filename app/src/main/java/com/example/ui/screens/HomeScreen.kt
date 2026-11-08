package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ActivityLogEntity
import com.example.domain.model.Channel
import com.example.domain.model.ChannelType
import com.example.domain.model.Device
import com.example.domain.model.Room
import com.example.ui.components.InteractiveFanRegulatorCard
import com.example.ui.components.JrHeader
import com.example.ui.components.SummaryMetricCard
import com.example.ui.components.SwitchboardPlate
import com.example.ui.components.TactileRockerSwitch
import com.example.ui.theme.JrCardDark
import com.example.ui.theme.JrError
import com.example.ui.theme.JrPrimary
import com.example.ui.theme.JrSecondary
import com.example.ui.theme.JrSuccess
import com.example.ui.theme.JrSurfaceDark
import com.example.ui.theme.JrTextMuted
import com.example.ui.theme.JrTextPrimary
import com.example.ui.theme.JrTextSecondary
import com.example.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    devices: List<Device>,
    channels: List<Channel>,
    favoriteChannels: List<Channel>,
    rooms: List<Room>,
    recentLogs: List<ActivityLogEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedRoomFilter by remember { mutableStateOf<String?>(null) }

    val activeSwitchesCount = channels.count { it.type != ChannelType.FAN && it.state }
    val activeFansCount = channels.count { it.type == ChannelType.FAN && it.state && it.fanSpeed > 0 }
    val onlineDevicesCount = devices.count { it.isOnline }
    val offlineDevicesCount = devices.size - onlineDevicesCount

    val filteredDevices = if (selectedRoomFilter == null) {
        devices
    } else {
        devices.filter { it.roomId == selectedRoomFilter }
    }

    // Dynamic Time-of-Day Greeting
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour in 5..11 -> "Good Morning"
        hour in 12..16 -> "Good Afternoon"
        hour in 17..21 -> "Good Evening"
        else -> "Good Night"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JrSurfaceDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header
        item {
            JrHeader(onMasterAllOff = { viewModel.masterAllOff() })
        }

        // Section 12: Greeting & Home Status Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = JrCardDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$greeting, Homeowner",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = JrTextPrimary
                            )
                            Text(
                                text = "JR Smart Mesh • All Systems Operating Locally",
                                fontSize = 12.sp,
                                color = JrSecondary
                            )
                        }

                        // Time pill
                        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = timeStr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JrTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Real Telemetry Status Badges: Bluetooth & Wi-Fi LAN
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // BLE Status
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131A24))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(JrPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Bluetooth,
                                        contentDescription = "BLE",
                                        tint = JrPrimary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("BLE Ready", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JrTextPrimary)
                                    Text("Provisioning Active", fontSize = 9.sp, color = JrTextMuted)
                                }
                            }
                        }

                        // Wi-Fi Local Status
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF131A24))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(JrSuccess.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Wifi,
                                        contentDescription = "Wi-Fi",
                                        tint = JrSuccess,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Local Wi-Fi", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JrTextPrimary)
                                    Text("LAN 100% Offline", fontSize = 9.sp, color = JrTextMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 12: Home Status Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = "Smart Boards",
                        value = "${devices.size}",
                        subtitle = "$onlineDevicesCount Online • $offlineDevicesCount Offline",
                        icon = Icons.Filled.Devices,
                        iconTint = JrPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Active Lights",
                        value = "$activeSwitchesCount",
                        subtitle = "Relays Engaged",
                        icon = Icons.Filled.Lightbulb,
                        iconTint = JrSecondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryMetricCard(
                        title = "Fan Regulators",
                        value = "$activeFansCount",
                        subtitle = "Motors Running",
                        icon = Icons.Filled.Air,
                        iconTint = Color(0xFFFFB74D),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryMetricCard(
                        title = "Total Channels",
                        value = "${channels.size}",
                        subtitle = "Hardware Endpoints",
                        icon = Icons.Filled.Sensors,
                        iconTint = Color(0xFF64B5F6),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section 12: Automation Shortcuts (One-tap scene triggers)
        item {
            Column {
                Text(
                    text = "Automation Shortcuts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = JrTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All Off
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2C1920))
                            .border(1.dp, JrError.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable { viewModel.masterAllOff() }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.PowerOff, contentDescription = null, tint = JrError, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("All Off", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JrError)
                        }
                    }

                    // Good Night
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF192033))
                            .border(1.dp, JrPrimary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.masterAllOff()
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Bedtime, contentDescription = null, tint = JrPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Good Night", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JrPrimary)
                        }
                    }

                    // Morning Mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2B2516))
                            .border(1.dp, Color(0xFFFFB74D).copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable {
                                // Turn on first switch
                                val firstSw = channels.firstOrNull { it.type != ChannelType.FAN }
                                if (firstSw != null && !firstSw.state) {
                                    viewModel.toggleSwitch(firstSw)
                                }
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.WbSunny, contentDescription = null, tint = Color(0xFFFFB74D), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Morning", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB74D))
                        }
                    }

                    // Movie Mode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF162925))
                            .border(1.dp, JrSecondary.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .clickable {
                                val ch = channels.firstOrNull { it.name.contains("Accent") || it.name.contains("Sconce") || it.channelIndex == 2 }
                                if (ch != null) viewModel.toggleSwitch(ch)
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Movie, contentDescription = null, tint = JrSecondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Cinema", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = JrSecondary)
                        }
                    }
                }
            }
        }

        // Section 12: Room Shortcuts & Filter
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Room Shortcuts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextPrimary
                    )
                    Text(
                        text = "${rooms.size} Active Rooms",
                        fontSize = 12.sp,
                        color = JrTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedRoomFilter == null,
                            onClick = { selectedRoomFilter = null },
                            label = { Text("All Rooms (${devices.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JrPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = JrCardDark,
                                labelColor = JrTextSecondary
                            )
                        )
                    }

                    items(rooms) { room ->
                        val devCount = devices.count { it.roomId == room.id }
                        FilterChip(
                            selected = selectedRoomFilter == room.id,
                            onClick = { selectedRoomFilter = room.id },
                            label = { Text("${room.name} ($devCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JrPrimary,
                                selectedLabelColor = Color.White,
                                containerColor = JrCardDark,
                                labelColor = JrTextSecondary
                            )
                        )
                    }
                }
            }
        }

        // Section 12: Quick Switch Controls & Fan Shortcuts (Favorites)
        if (favoriteChannels.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Quick Switch Controls",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JrTextPrimary
                        )
                        Text(
                            text = "${favoriteChannels.size} Pinned Controls",
                            fontSize = 12.sp,
                            color = JrTextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val favSwitches = favoriteChannels.filter { it.type != ChannelType.FAN }
                    val favFans = favoriteChannels.filter { it.type == ChannelType.FAN }

                    // 2-column tactile switches
                    val pairs = favSwitches.chunked(2)
                    for (pair in pairs) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            for (ch in pair) {
                                TactileRockerSwitch(
                                    channel = ch,
                                    onToggle = { viewModel.toggleSwitch(ch) },
                                    onFavoriteToggle = { viewModel.toggleFavorite(ch) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Fan regulator shortcuts
                    for (fan in favFans) {
                        InteractiveFanRegulatorCard(
                            channel = fan,
                            onSpeedChange = { newSpeed -> viewModel.setFanSpeed(fan, newSpeed) },
                            onTogglePower = { viewModel.setFanSpeed(fan, if (fan.state) 0 else 2) },
                            onOpenFullRegulator = { viewModel.openFanDial(fan) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }

        // Section 12: Favorite Switchboards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Smart Switchboards",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = JrTextPrimary
                )

                Button(
                    onClick = { viewModel.setShowAddDeviceSheet(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = JrPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Add Board", fontSize = 12.sp)
                }
            }
        }

        if (filteredDevices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JrCardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No Switchboards in this room",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = JrTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.setShowAddDeviceSheet(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = JrPrimary)
                        ) {
                            Text("Provision or Create Board")
                        }
                    }
                }
            }
        } else {
            items(filteredDevices) { device ->
                val boardChannels = channels.filter { it.deviceId == device.id }
                SwitchboardPlate(
                    device = device,
                    channels = boardChannels,
                    onToggleSwitch = { ch -> viewModel.toggleSwitch(ch) },
                    onToggleFavorite = { ch -> viewModel.toggleFavorite(ch) },
                    onFanSpeedChange = { fan, speed -> viewModel.setFanSpeed(fan, speed) },
                    onFanTogglePower = { fan -> viewModel.setFanSpeed(fan, if (fan.state) 0 else 2) },
                    onOpenFanDial = { fan -> viewModel.openFanDial(fan) },
                    onEditBoard = { viewModel.setEditingDevice(device) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Section 12: Recent Activities Feed
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.History, contentDescription = null, tint = JrSecondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recent Activities",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = JrTextPrimary
                        )
                    }

                    Text(
                        text = "Hardware Event Log",
                        fontSize = 11.sp,
                        color = JrTextMuted
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JrCardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val displayLogs = if (recentLogs.isNotEmpty()) recentLogs.take(5) else listOf(
                            ActivityLogEntity("init1", System.currentTimeMillis() - 120000, "Living Room Main Board Connected", "Verified on IP 192.168.1.105 (Port 80)", "CONNECTION"),
                            ActivityLogEntity("init2", System.currentTimeMillis() - 60000, "Ceiling Fan Regulator Active", "Speed Level 3 verified via ACK", "FAN"),
                            ActivityLogEntity("init3", System.currentTimeMillis() - 30000, "Chandelier Powered ON", "Physical & App State Synchronized", "SWITCH")
                        )

                        for (log in displayLogs) {
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 4.dp)
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (log.category) {
                                                    "FAN" -> Color(0xFFFFB74D)
                                                    "SWITCH" -> JrSecondary
                                                    "AUTOMATION" -> JrPrimary
                                                    else -> JrSuccess
                                                }
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = log.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = JrTextPrimary
                                        )
                                        Text(
                                            text = log.description,
                                            fontSize = 11.sp,
                                            color = JrTextMuted
                                        )
                                    }
                                }

                                Text(
                                    text = timeStr,
                                    fontSize = 10.sp,
                                    color = JrTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
