package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.repository.SmartHomeRepository
import com.example.domain.model.ActionType
import com.example.domain.model.Channel
import com.example.domain.model.ChannelType
import com.example.domain.model.Device
import com.example.domain.model.Room
import com.example.domain.model.TriggerType
import com.example.hardware.BleConnectionState
import com.example.hardware.DiscoveredBleDevice
import com.example.hardware.DiscoveredNetworkBoard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SmartHomeRepository(application.applicationContext)

    // Current Navigation Tab (0: Home, 1: Devices, 2: Rooms, 3: Automations, 4: Docs & Settings)
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Selected Device for Switchboard Detail screen (null = none open)
    private val _selectedDevice = MutableStateFlow<Device?>(null)
    val selectedDevice: StateFlow<Device?> = _selectedDevice.asStateFlow()

    // Active Fan for full dial modal
    private val _activeFanDial = MutableStateFlow<Channel?>(null)
    val activeFanDial: StateFlow<Channel?> = _activeFanDial.asStateFlow()

    // Dialog state for adding device
    private val _showAddDeviceSheet = MutableStateFlow(false)
    val showAddDeviceSheet: StateFlow<Boolean> = _showAddDeviceSheet.asStateFlow()

    private val _showManualWizard = MutableStateFlow(false)
    val showManualWizard: StateFlow<Boolean> = _showManualWizard.asStateFlow()

    private val _editingDevice = MutableStateFlow<Device?>(null)
    val editingDevice: StateFlow<Device?> = _editingDevice.asStateFlow()

    // BLE Discovery State
    private val _discoveredBleDevices = MutableStateFlow<List<DiscoveredBleDevice>>(emptyList())
    val discoveredBleDevices: StateFlow<List<DiscoveredBleDevice>> = _discoveredBleDevices.asStateFlow()

    val bleConnectionState: StateFlow<BleConnectionState> = repository.bleManager.connectionState

    // Network Discovery State
    private val _discoveredNetworkBoards = MutableStateFlow<List<DiscoveredNetworkBoard>>(emptyList())
    val discoveredNetworkBoards: StateFlow<List<DiscoveredNetworkBoard>> = _discoveredNetworkBoards.asStateFlow()

    // Data streams from Room Database
    val devices: StateFlow<List<Device>> = repository.allDevices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channels: StateFlow<List<Channel>> = repository.allChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<Channel>> = repository.favoriteChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rooms: StateFlow<List<Room>> = repository.allRooms
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val automations: StateFlow<List<AutomationEntity>> = repository.allAutomations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<ActivityLogEntity>> = repository.recentLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: Int) {
        _currentTab.value = tab
    }

    fun openDeviceDetail(device: Device) {
        _selectedDevice.value = device
    }

    fun closeDeviceDetail() {
        _selectedDevice.value = null
    }

    fun openFanDial(channel: Channel) {
        _activeFanDial.value = channel
    }

    fun closeFanDial() {
        _activeFanDial.value = null
    }

    fun setShowAddDeviceSheet(show: Boolean) {
        _showAddDeviceSheet.value = show
    }

    fun setShowManualWizard(show: Boolean) {
        _showManualWizard.value = show
    }

    fun setEditingDevice(device: Device?) {
        _editingDevice.value = device
    }

    fun toggleSwitch(channel: Channel) {
        viewModelScope.launch {
            repository.toggleSwitch(channel)
        }
    }

    fun setFanSpeed(channel: Channel, speed: Int) {
        viewModelScope.launch {
            repository.setFanSpeed(channel, speed)
            // If active in dial, keep updated
            if (_activeFanDial.value?.id == channel.id) {
                _activeFanDial.value = channel.copy(fanSpeed = speed, state = speed > 0)
            }
        }
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            repository.toggleChannelFavorite(channel.id, channel.isFavorite)
        }
    }

    fun masterAllOff() {
        viewModelScope.launch {
            repository.executeMasterAllOff()
        }
    }

    fun startBleScan() {
        viewModelScope.launch {
            repository.bleManager.scanForDevices().collect { list ->
                _discoveredBleDevices.value = list
            }
        }
    }

    fun connectAndProvision(
        deviceAddress: String,
        ssid: String,
        pass: String,
        roomId: String,
        onComplete: (Boolean, String) -> Unit
    ) {
        repository.bleManager.connectAndProvision(
            deviceAddress = deviceAddress,
            ssid = ssid,
            pass = pass,
            onSuccess = { ip, model ->
                viewModelScope.launch {
                    val result = repository.discoverAndEnrollHardware(ip, roomId)
                    if (result.isSuccess) {
                        onComplete(true, "Successfully provisioned and linked $model at $ip")
                    } else {
                        // Fallback manual enrollment
                        repository.addCustomSwitchboard(
                            boardName = "ESP32 Board ($model)",
                            boardModel = model,
                            ipAddress = ip,
                            roomId = roomId,
                            switchCount = 4,
                            fanCount = 1
                        )
                        onComplete(true, "Provisioned and registered $model at $ip")
                    }
                }
            },
            onError = { err ->
                onComplete(false, err)
            }
        )
    }

    fun startNetworkDiscovery() {
        viewModelScope.launch {
            repository.discoveryManager.discoverLocalBoards().collect { boards ->
                _discoveredNetworkBoards.value = boards
            }
        }
    }

    fun enrollDiscoveredNetworkBoard(ip: String, roomId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.discoverAndEnrollHardware(ip, roomId)
            if (res.isSuccess) {
                onResult(true, "Board connected and channels configured!")
            } else {
                onResult(false, res.exceptionOrNull()?.message ?: "Failed to connect to board")
            }
        }
    }

    fun createCustomBoard(
        name: String,
        model: String,
        ip: String,
        roomId: String,
        switches: Int,
        fans: Int
    ) {
        viewModelScope.launch {
            repository.addCustomSwitchboard(name, model, ip, roomId, switches, fans)
            _showManualWizard.value = false
            _showAddDeviceSheet.value = false
        }
    }

    fun updateDevice(device: Device, newName: String, newRoomId: String) {
        viewModelScope.launch {
            repository.updateDevice(device.copy(name = newName, roomId = newRoomId))
            _editingDevice.value = null
        }
    }

    fun deleteDevice(deviceId: String) {
        viewModelScope.launch {
            repository.deleteDevice(deviceId)
            if (_selectedDevice.value?.id == deviceId) {
                _selectedDevice.value = null
            }
        }
    }

    fun addRoom(name: String) {
        viewModelScope.launch {
            repository.addRoom(name, "room_custom")
        }
    }

    fun deleteRoom(roomId: String) {
        viewModelScope.launch {
            repository.deleteRoom(roomId)
        }
    }

    fun createAutomation(
        name: String,
        triggerType: TriggerType,
        triggerDetail: String,
        actionType: ActionType,
        deviceId: String,
        channelIndex: Int,
        actionValue: Int
    ) {
        viewModelScope.launch {
            repository.createAutomation(
                name = name,
                triggerType = triggerType,
                triggerDetail = triggerDetail,
                actionType = actionType,
                targetDeviceId = deviceId,
                targetChannelIndex = channelIndex,
                actionValue = actionValue
            )
        }
    }

    fun toggleAutomation(id: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleAutomation(id, enabled)
        }
    }

    fun deleteAutomation(id: String) {
        viewModelScope.launch {
            repository.deleteAutomation(id)
        }
    }
}
