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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.domain.model.ChannelType
import com.example.domain.model.Device
import com.example.domain.model.Room
import com.example.hardware.DiscoveredNetworkBoard
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

@Composable
fun DevicesScreen(
    viewModel: MainViewModel,
    devices: List<Device>,
    channels: List<Channel>,
    rooms: List<Room>,
    discoveredNetworkBoards: List<DiscoveredNetworkBoard>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JrSurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Device Management",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextPrimary
                    )
                    Text(
                        text = "ESP32 Boards • BLE Provisioning • Wi-Fi mDNS",
                        fontSize = 12.sp,
                        color = JrTextMuted
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.startNetworkDiscovery()
                        viewModel.startBleScan()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "Refresh",
                        tint = JrPrimary
                    )
                }
            }
        }

        // Action Buttons Row: BLE Provisioning + Manual Custom Board Wizard
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { viewModel.setShowAddDeviceSheet(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = JrPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.BluetoothSearching,
                        contentDescription = "Scan BLE",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Scan & Provision", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.setShowManualWizard(true) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = "Wizard",
                        tint = JrSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Board Wizard", color = JrSecondary, fontSize = 13.sp)
                }
            }
        }

        // Discovered Local Network Boards (mDNS)
        if (discoveredNetworkBoards.isNotEmpty()) {
            item {
                Text(
                    text = "Discovered on Local Wi-Fi (mDNS)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = JrSecondary
                )
            }

            items(discoveredNetworkBoards) { board ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = JrCardDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = board.serviceName,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = JrTextPrimary
                            )
                            Text(
                                text = "IP: ${board.hostAddress}:${board.port} • ${board.model}",
                                fontSize = 12.sp,
                                color = JrTextMuted
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.enrollDiscoveredNetworkBoard(
                                    ip = board.hostAddress,
                                    roomId = rooms.firstOrNull()?.id ?: "room_living"
                                ) { success, msg -> }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JrSecondary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Link Board", color = Color.Black, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Registered Switchboards Header
        item {
            Text(
                text = "Registered Switchboards (${devices.size})",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = JrTextPrimary
            )
        }

        items(devices) { device ->
            val devChannels = channels.filter { it.deviceId == device.id }
            val switchesCount = devChannels.count { it.type != ChannelType.FAN }
            val fansCount = devChannels.count { it.type == ChannelType.FAN }
            val roomName = rooms.firstOrNull { it.id == device.roomId }?.name ?: "Unassigned"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("device_card_${device.id}")
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(18.dp))
                    .clickable { viewModel.openDeviceDetail(device) },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(JrPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeveloperBoard,
                                    contentDescription = "Board",
                                    tint = JrPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = device.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JrTextPrimary
                                )
                                Text(
                                    text = "${device.boardModel} • $roomName",
                                    fontSize = 12.sp,
                                    color = JrTextSecondary
                                )
                            }
                        }

                        // Online Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (device.isOnline) JrSuccess.copy(alpha = 0.15f) else JrError.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (device.isOnline) JrSuccess else JrError)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (device.isOnline) "ONLINE" else "OFFLINE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (device.isOnline) JrSuccess else JrError
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hardware Specs Details
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "IP: ${device.ipAddress} • MAC: ${device.macAddress.take(8)}...",
                            fontSize = 11.sp,
                            color = JrTextMuted
                        )
                        Text(
                            text = "$switchesCount Switches | $fansCount Fans",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JrSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.setEditingDevice(device) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = JrTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = { viewModel.deleteDevice(device.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = JrError,
                                modifier = Modifier.size(16.dp)
                            )
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
