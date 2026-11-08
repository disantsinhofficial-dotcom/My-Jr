package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Channel
import com.example.domain.model.Device
import com.example.ui.components.SwitchboardPlate
import com.example.ui.theme.JrCardDark
import com.example.ui.theme.JrError
import com.example.ui.theme.JrPrimary
import com.example.ui.theme.JrSecondary
import com.example.ui.theme.JrSurfaceDark
import com.example.ui.theme.JrTextMuted
import com.example.ui.theme.JrTextPrimary
import com.example.ui.theme.JrTextSecondary
import com.example.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchboardDetailScreen(
    device: Device,
    channels: List<Channel>,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
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
                                contentDescription = null,
                                tint = if (device.isOnline) JrSecondary else JrError,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${device.boardModel} • IP: ${device.ipAddress}",
                                fontSize = 11.sp,
                                color = JrTextMuted
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = JrTextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setEditingDevice(device) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit Board",
                            tint = JrSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = JrSurfaceDark
                )
            )
        },
        containerColor = JrSurfaceDark,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Live Graphical Faceplate",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = JrSecondary
                )
            }

            item {
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

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "Hardware Telemetry & Specs",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Model: ${device.boardModel}\n" +
                                "• Firmware: ${device.firmwareVersion}\n" +
                                "• MAC Address: ${device.macAddress}\n" +
                                "• Communication: Local HTTP REST / WebSocket Port 80\n" +
                                "• Status: ${if (device.isOnline) "ONLINE (Verified 0ms)" else "OFFLINE"}",
                        fontSize = 12.sp,
                        color = JrTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}
