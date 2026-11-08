package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Device
import com.example.domain.model.Room
import com.example.hardware.BleConnectionState
import com.example.hardware.DiscoveredBleDevice
import com.example.ui.theme.JrCardDark
import com.example.ui.theme.JrError
import com.example.ui.theme.JrPrimary
import com.example.ui.theme.JrSecondary
import com.example.ui.theme.JrSuccess
import com.example.ui.theme.JrTextMuted
import com.example.ui.theme.JrTextPrimary
import com.example.ui.theme.JrTextSecondary
import com.example.viewmodel.MainViewModel

@Composable
fun BleProvisioningDialog(
    viewModel: MainViewModel,
    discoveredDevices: List<DiscoveredBleDevice>,
    connectionState: BleConnectionState,
    rooms: List<Room>,
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Scan, 2: WiFi input, 3: Connecting/Provisioning
    var selectedDevice by remember { mutableStateOf<DiscoveredBleDevice?>(null) }
    var ssid by remember { mutableStateOf("Home_WiFi_2.4G") }
    var password by remember { mutableStateOf("") }
    var selectedRoomId by remember { mutableStateOf(rooms.firstOrNull()?.id ?: "room_living") }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.startBleScan()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (step == 1) "Scan ESP32 Bluetooth" else "Wi-Fi Provisioning",
                color = JrTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (step == 1) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Nearby BLE Switchboards:",
                            fontSize = 13.sp,
                            color = JrTextSecondary
                        )
                        IconButton(onClick = { viewModel.startBleScan() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Rescan", tint = JrPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (discoveredDevices.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = JrPrimary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Searching for ESP32 boards...", fontSize = 12.sp, color = JrTextMuted)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    // Simulated board discovered for quick testing
                                    selectedDevice = DiscoveredBleDevice(
                                        name = "JR-Switchboard-ESP32",
                                        address = "34:85:18:9B:4C:A2",
                                        rssi = -52,
                                        isJrSwitchboard = true
                                    )
                                    step = 2
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = JrCardDark)
                            ) {
                                Text("Select Detected ESP32 Board", fontSize = 11.sp, color = JrSecondary)
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(discoveredDevices) { dev ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selectedDevice?.address == dev.address) JrPrimary.copy(alpha = 0.2f) else JrCardDark)
                                        .clickable {
                                            selectedDevice = dev
                                            step = 2
                                        }
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Bluetooth, contentDescription = null, tint = JrPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(dev.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = JrTextPrimary)
                                            Text(dev.address, fontSize = 10.sp, color = JrTextMuted)
                                        }
                                    }
                                    Text("${dev.rssi} dBm", fontSize = 11.sp, color = JrSecondary)
                                }
                            }
                        }
                    }
                } else if (step == 2) {
                    Text(
                        text = "Transfer network credentials to ${selectedDevice?.name ?: "Board"}:",
                        fontSize = 13.sp,
                        color = JrTextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ssid,
                        onValueChange = { ssid = it },
                        label = { Text("Wi-Fi SSID (2.4GHz)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Wi-Fi Password") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (statusMessage != null) {
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 12.sp,
                            color = JrSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (step == 2) {
                Button(
                    onClick = {
                        val addr = selectedDevice?.address ?: "34:85:18:9B:4C:A2"
                        statusMessage = "Provisioning via BLE..."
                        viewModel.connectAndProvision(
                            deviceAddress = addr,
                            ssid = ssid,
                            pass = password,
                            roomId = selectedRoomId
                        ) { success, msg ->
                            statusMessage = msg
                            if (success) {
                                onDismiss()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JrPrimary)
                ) {
                    Text("Provision Board")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JrTextSecondary)
            }
        },
        containerColor = Color(0xFF131924)
    )
}

@Composable
fun ManualBoardWizardDialog(
    rooms: List<Room>,
    onDismiss: () -> Unit,
    onCreateBoard: (name: String, model: String, ip: String, roomId: String, switches: Int, fans: Int) -> Unit
) {
    var boardName by remember { mutableStateOf("Living Room 6-CH Board") }
    var selectedPreset by remember { mutableStateOf("6S_2F") }
    var ipAddress by remember { mutableStateOf("192.168.1.120") }
    var selectedRoomId by remember { mutableStateOf(rooms.firstOrNull()?.id ?: "room_living") }

    var switchesCount by remember { mutableIntStateOf(6) }
    var fansCount by remember { mutableIntStateOf(2) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Manual Switchboard Wizard",
                color = JrTextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Configure custom modular switchboard hardware:",
                    fontSize = 12.sp,
                    color = JrTextSecondary
                )

                OutlinedTextField(
                    value = boardName,
                    onValueChange = { boardName = it },
                    label = { Text("Board Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Board Channel Presets", fontSize = 12.sp, color = JrTextMuted)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        Triple("2S", 2, 0),
                        Triple("4S", 4, 0),
                        Triple("4S+1F", 4, 1),
                        Triple("6S+2F", 6, 2),
                        Triple("8S", 8, 0),
                        Triple("12S", 12, 0)
                    ).forEach { (label, s, f) ->
                        val isSel = switchesCount == s && fansCount == f
                        Button(
                            onClick = {
                                switchesCount = s
                                fansCount = f
                                boardName = "JR-Board ($label)"
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) JrPrimary else JrCardDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = { ipAddress = it },
                    label = { Text("Hardware IP Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Configuring $switchesCount Relays + $fansCount Precision Fan Regulators",
                    fontSize = 12.sp,
                    color = JrSecondary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val model = "JR-SB-${switchesCount}S-${fansCount}F"
                    onCreateBoard(boardName, model, ipAddress, selectedRoomId, switchesCount, fansCount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = JrPrimary)
            ) {
                Text("Generate Board Layout")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JrTextSecondary)
            }
        },
        containerColor = Color(0xFF131924)
    )
}

@Composable
fun EditDeviceDialog(
    device: Device,
    rooms: List<Room>,
    onDismiss: () -> Unit,
    onSave: (newName: String, newRoomId: String) -> Unit
) {
    var name by remember { mutableStateOf(device.name) }
    var selectedRoomId by remember { mutableStateOf(device.roomId) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Customize Switchboard", color = JrTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Board Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Assign to Room:", fontSize = 12.sp, color = JrTextMuted)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    rooms.forEach { room ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedRoomId == room.id) JrPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable { selectedRoomId = room.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = room.name,
                                fontSize = 13.sp,
                                fontWeight = if (selectedRoomId == room.id) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedRoomId == room.id) JrSecondary else JrTextPrimary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, selectedRoomId) },
                colors = ButtonDefaults.buttonColors(containerColor = JrPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JrTextSecondary)
            }
        },
        containerColor = Color(0xFF131924)
    )
}
