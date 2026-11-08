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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.AutomationEntity
import com.example.domain.model.ActionType
import com.example.domain.model.Device
import com.example.domain.model.TriggerType
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
fun AutomationsScreen(
    viewModel: MainViewModel,
    automations: List<AutomationEntity>,
    devices: List<Device>,
    modifier: Modifier = Modifier
) {
    var showCreateDialog by remember { mutableStateOf(false) }

    if (showCreateDialog) {
        CreateAutomationDialog(
            devices = devices,
            onDismiss = { showCreateDialog = false },
            onCreate = { name, triggerType, triggerDetail, actionType, deviceId, chIndex, value ->
                viewModel.createAutomation(name, triggerType, triggerDetail, actionType, deviceId, chIndex, value)
                showCreateDialog = false
            }
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
                        text = "Local Automations",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextPrimary
                    )
                    Text(
                        text = "Device-side rules • Zero cloud server dependency",
                        fontSize = 12.sp,
                        color = JrTextMuted
                    )
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JrPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "New Rule",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Rule", fontSize = 12.sp)
                }
            }
        }

        // Quick Scene Triggers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JrCardDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Quick Scene Actions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.masterAllOff() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E1C25)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.PowerOff, contentDescription = null, tint = JrError, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("All Off", fontSize = 11.sp, color = JrError)
                        }

                        Button(
                            onClick = {
                                // Trigger evening scene
                                viewModel.createAutomation(
                                    name = "Sunset Ambiance",
                                    triggerType = TriggerType.TIME_SCHEDULE,
                                    triggerDetail = "18:00",
                                    actionType = ActionType.CHANNEL_ON,
                                    deviceId = devices.firstOrNull()?.id ?: "",
                                    channelIndex = 1,
                                    actionValue = 1
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2837)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.FlashOn, contentDescription = null, tint = JrPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sunset Mode", fontSize = 11.sp, color = JrPrimary)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Active Automation Rules (${automations.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = JrTextPrimary
            )
        }

        if (automations.isEmpty()) {
            item {
                Text(
                    text = "No automations yet. Tap 'New Rule' to create scheduled lighting, fan temp triggers, or presence rules.",
                    fontSize = 13.sp,
                    color = JrTextMuted
                )
            }
        } else {
            items(automations) { rule ->
                val triggerIcon = when (rule.triggerType) {
                    "TIME_SCHEDULE" -> Icons.Filled.AccessTime
                    "TEMPERATURE" -> Icons.Filled.DeviceThermostat
                    "MOTION" -> Icons.Filled.DirectionsRun
                    else -> Icons.Filled.Bolt
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = JrCardDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(JrPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = triggerIcon,
                                    contentDescription = "Trigger",
                                    tint = JrPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = rule.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JrTextPrimary
                                )
                                Text(
                                    text = "When: ${rule.triggerDetail} -> Action: ${rule.actionType}",
                                    fontSize = 12.sp,
                                    color = JrTextSecondary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = rule.isEnabled,
                                onCheckedChange = { viewModel.toggleAutomation(rule.id, it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = JrSecondary
                                )
                            )

                            IconButton(
                                onClick = { viewModel.deleteAutomation(rule.id) },
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
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CreateAutomationDialog(
    devices: List<Device>,
    onDismiss: () -> Unit,
    onCreate: (name: String, triggerType: TriggerType, triggerDetail: String, actionType: ActionType, deviceId: String, chIndex: Int, value: Int) -> Unit
) {
    var name by remember { mutableStateOf("Evening Lights ON") }
    var triggerType by remember { mutableStateOf(TriggerType.TIME_SCHEDULE) }
    var triggerDetail by remember { mutableStateOf("19:00") }
    var actionType by remember { mutableStateOf(ActionType.CHANNEL_ON) }
    var selectedDeviceId by remember { mutableStateOf(devices.firstOrNull()?.id ?: "") }
    var channelIndex by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Local Automation", color = JrTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Trigger Condition", fontSize = 12.sp, color = JrTextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            triggerType = TriggerType.TIME_SCHEDULE
                            triggerDetail = "20:00"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (triggerType == TriggerType.TIME_SCHEDULE) JrPrimary else JrSurfaceDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Time", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            triggerType = TriggerType.TEMPERATURE
                            triggerDetail = "> 30°C"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (triggerType == TriggerType.TEMPERATURE) JrPrimary else JrSurfaceDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Temp", fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            triggerType = TriggerType.MOTION
                            triggerDetail = "Motion Detected"
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (triggerType == TriggerType.MOTION) JrPrimary else JrSurfaceDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Motion", fontSize = 11.sp)
                    }
                }

                OutlinedTextField(
                    value = triggerDetail,
                    onValueChange = { triggerDetail = it },
                    label = { Text("Trigger Value / Threshold") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Action", fontSize = 12.sp, color = JrTextMuted)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = { actionType = ActionType.CHANNEL_ON },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (actionType == ActionType.CHANNEL_ON) JrSecondary else JrSurfaceDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Turn ON", fontSize = 11.sp, color = if (actionType == ActionType.CHANNEL_ON) Color.Black else Color.White)
                    }
                    Button(
                        onClick = { actionType = ActionType.CHANNEL_OFF },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (actionType == ActionType.CHANNEL_OFF) JrError else JrSurfaceDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Turn OFF", fontSize = 11.sp)
                    }
                    Button(
                        onClick = { actionType = ActionType.ALL_OFF },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (actionType == ActionType.ALL_OFF) Color(0xFFFF9800) else JrSurfaceDark
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("All Off", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, triggerType, triggerDetail, actionType, selectedDeviceId, channelIndex, 1)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JrPrimary)
            ) {
                Text("Save Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JrTextSecondary)
            }
        },
        containerColor = JrCardDark
    )
}
