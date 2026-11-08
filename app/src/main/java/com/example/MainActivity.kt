package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FullFanRegulatorDialog
import com.example.ui.screens.AutomationsScreen
import com.example.ui.screens.BleProvisioningDialog
import com.example.ui.screens.DevicesScreen
import com.example.ui.screens.EditDeviceDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ManualBoardWizardDialog
import com.example.ui.screens.RoomsScreen
import com.example.ui.screens.SettingsAndDocsScreen
import com.example.ui.screens.SwitchboardDetailScreen
import com.example.ui.theme.JRAutomationTheme
import com.example.ui.theme.JrPrimary
import com.example.ui.theme.JrSecondary
import com.example.ui.theme.JrSurfaceDark
import com.example.ui.theme.JrTextMuted
import com.example.ui.theme.JrTextPrimary
import com.example.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JRAutomationTheme(darkTheme = true) {
                JRAutomationApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JRAutomationApp(viewModel: MainViewModel = viewModel()) {
    // Runtime permissions for Bluetooth & Wi-Fi scanning
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val selectedDevice by viewModel.selectedDevice.collectAsStateWithLifecycle()
    val activeFanDial by viewModel.activeFanDial.collectAsStateWithLifecycle()
    val showAddDeviceSheet by viewModel.showAddDeviceSheet.collectAsStateWithLifecycle()
    val showManualWizard by viewModel.showManualWizard.collectAsStateWithLifecycle()
    val editingDevice by viewModel.editingDevice.collectAsStateWithLifecycle()

    val devices by viewModel.devices.collectAsStateWithLifecycle()
    val channels by viewModel.channels.collectAsStateWithLifecycle()
    val favoriteChannels by viewModel.favoriteChannels.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val automations by viewModel.automations.collectAsStateWithLifecycle()
    val discoveredBleDevices by viewModel.discoveredBleDevices.collectAsStateWithLifecycle()
    val bleConnectionState by viewModel.bleConnectionState.collectAsStateWithLifecycle()
    val discoveredNetworkBoards by viewModel.discoveredNetworkBoards.collectAsStateWithLifecycle()

    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()

    // If a dedicated switchboard detail screen is open, display it full screen
    if (selectedDevice != null) {
        SwitchboardDetailScreen(
            device = selectedDevice!!,
            channels = channels,
            viewModel = viewModel,
            onBack = { viewModel.closeDeviceDetail() }
        )
    } else {
        Scaffold(
            bottomBar = {
                JRBottomNavigationBar(
                    currentTab = currentTab,
                    onTabSelected = { viewModel.setTab(it) }
                )
            },
            containerColor = JrSurfaceDark,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> HomeScreen(
                        viewModel = viewModel,
                        devices = devices,
                        channels = channels,
                        favoriteChannels = favoriteChannels,
                        rooms = rooms,
                        recentLogs = recentLogs
                    )
                    1 -> DevicesScreen(
                        viewModel = viewModel,
                        devices = devices,
                        channels = channels,
                        rooms = rooms,
                        discoveredNetworkBoards = discoveredNetworkBoards
                    )
                    2 -> RoomsScreen(
                        viewModel = viewModel,
                        rooms = rooms,
                        devices = devices,
                        channels = channels
                    )
                    3 -> AutomationsScreen(
                        viewModel = viewModel,
                        automations = automations,
                        devices = devices
                    )
                    4 -> SettingsAndDocsScreen()
                }
            }
        }
    }

    // Modal Sheet for Full Circular Fan Dial Regulator
    if (activeFanDial != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeFanDial() },
            sheetState = sheetState,
            containerColor = Color(0xFF131924)
        ) {
            FullFanRegulatorDialog(
                channel = activeFanDial!!,
                onDismiss = { viewModel.closeFanDial() },
                onSpeedChange = { newSpeed -> viewModel.setFanSpeed(activeFanDial!!, newSpeed) },
                onTogglePower = { viewModel.setFanSpeed(activeFanDial!!, if (activeFanDial!!.state) 0 else 2) }
            )
        }
    }

    // Provisioning Dialog (Bluetooth LE + Wi-Fi credentials)
    if (showAddDeviceSheet) {
        BleProvisioningDialog(
            viewModel = viewModel,
            discoveredDevices = discoveredBleDevices,
            connectionState = bleConnectionState,
            rooms = rooms,
            onDismiss = { viewModel.setShowAddDeviceSheet(false) }
        )
    }

    // Manual Custom Board Wizard
    if (showManualWizard) {
        ManualBoardWizardDialog(
            rooms = rooms,
            onDismiss = { viewModel.setShowManualWizard(false) },
            onCreateBoard = { name, model, ip, roomId, switches, fans ->
                viewModel.createCustomBoard(name, model, ip, roomId, switches, fans)
            }
        )
    }

    // Edit Board Dialog
    if (editingDevice != null) {
        EditDeviceDialog(
            device = editingDevice!!,
            rooms = rooms,
            onDismiss = { viewModel.setEditingDevice(null) },
            onSave = { newName, newRoomId ->
                viewModel.updateDevice(editingDevice!!, newName, newRoomId)
            }
        )
    }
}

@Composable
fun JRBottomNavigationBar(
    currentTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF101620),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val tabs = listOf(
            Triple(0, "Home", Icons.Filled.Home to Icons.Outlined.Home),
            Triple(1, "Devices", Icons.Filled.Devices to Icons.Outlined.Devices),
            Triple(2, "Rooms", Icons.Filled.MeetingRoom to Icons.Outlined.MeetingRoom),
            Triple(3, "Automate", Icons.Filled.Bolt to Icons.Outlined.Bolt),
            Triple(4, "System", Icons.Filled.Settings to Icons.Outlined.Settings)
        )

        tabs.forEach { (index, label, iconPair) ->
            val isSelected = currentTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) iconPair.first else iconPair.second,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = JrSecondary,
                    selectedTextColor = JrSecondary,
                    unselectedIconColor = JrTextMuted,
                    unselectedTextColor = JrTextMuted,
                    indicatorColor = JrPrimary.copy(alpha = 0.2f)
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}
