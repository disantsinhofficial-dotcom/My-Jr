package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Deck
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Living
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.domain.model.Channel
import com.example.domain.model.Device
import com.example.domain.model.Room
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

@Composable
fun RoomsScreen(
    viewModel: MainViewModel,
    rooms: List<Room>,
    devices: List<Device>,
    channels: List<Channel>,
    modifier: Modifier = Modifier
) {
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var newRoomName by remember { mutableStateOf("") }

    if (showAddRoomDialog) {
        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("Add New Room", color = JrTextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter room label (e.g. Dining Hall, Balcony, Kids Room):", color = JrTextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newRoomName,
                        onValueChange = { newRoomName = it },
                        label = { Text("Room Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newRoomName.isNotBlank()) {
                            viewModel.addRoom(newRoomName.trim())
                            newRoomName = ""
                            showAddRoomDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JrPrimary)
                ) {
                    Text("Add Room")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddRoomDialog = false }) {
                    Text("Cancel", color = JrTextSecondary)
                }
            },
            containerColor = JrCardDark
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JrSurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rooms & Zones",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextPrimary
                    )
                    Text(
                        text = "Control and organize switchboards room-wise",
                        fontSize = 12.sp,
                        color = JrTextMuted
                    )
                }

                Button(
                    onClick = { showAddRoomDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JrPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Room",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Room", fontSize = 12.sp)
                }
            }
        }

        // List each room and its devices
        items(rooms) { room ->
            val roomDevices = devices.filter { it.roomId == room.id }
            val roomChannels = channels.filter { ch -> roomDevices.any { it.id == ch.deviceId } }

            val roomIcon = when (room.name.lowercase()) {
                "living room" -> Icons.Filled.Living
                "master bedroom", "bedroom" -> Icons.Filled.Bed
                "kitchen" -> Icons.Filled.Kitchen
                "home office", "office" -> Icons.Filled.Work
                "bathroom" -> Icons.Filled.Bathtub
                "balcony & patio", "balcony" -> Icons.Filled.Deck
                else -> Icons.Filled.MeetingRoom
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp)),
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
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(JrSecondary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = roomIcon,
                                    contentDescription = room.name,
                                    tint = JrSecondary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = room.name,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JrTextPrimary
                                )
                                Text(
                                    text = "${roomDevices.size} Boards • ${roomChannels.size} Channels",
                                    fontSize = 11.sp,
                                    color = JrTextMuted
                                )
                            }
                        }

                        // Room delete button if custom
                        if (room.id.startsWith("room_") && !room.id.contains("living") && !room.id.contains("bedroom")) {
                            IconButton(
                                onClick = { viewModel.deleteRoom(room.id) },
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete Room",
                                    tint = JrError,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (roomDevices.isEmpty()) {
                        Text(
                            text = "No switchboards assigned to ${room.name}. Edit a board to assign it here.",
                            fontSize = 12.sp,
                            color = JrTextMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (device in roomDevices) {
                                val boardChannels = roomChannels.filter { it.deviceId == device.id }
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
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
