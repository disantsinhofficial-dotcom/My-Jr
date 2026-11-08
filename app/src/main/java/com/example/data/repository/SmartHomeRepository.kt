package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.DeviceEntity
import com.example.data.local.entity.RoomEntity
import com.example.domain.model.ActionType
import com.example.domain.model.Channel
import com.example.domain.model.ChannelType
import com.example.domain.model.ConnectionType
import com.example.domain.model.Device
import com.example.domain.model.Room
import com.example.domain.model.TriggerType
import com.example.hardware.BleProvisioningManager
import com.example.hardware.Esp32ApiClient
import com.example.hardware.LocalAutomationEngine
import com.example.hardware.LocalNetworkDiscoveryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.UUID

class SmartHomeRepository(private val context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val deviceDao = db.deviceDao()
    private val channelDao = db.channelDao()
    private val roomDao = db.roomDao()
    private val automationDao = db.automationDao()
    private val activityLogDao = db.activityLogDao()

    val bleManager = BleProvisioningManager(context)
    val discoveryManager = LocalNetworkDiscoveryManager(context)
    val apiClient = Esp32ApiClient()
    val automationEngine = LocalAutomationEngine(
        automationDao = automationDao,
        channelDao = channelDao,
        deviceDao = deviceDao,
        activityLogDao = activityLogDao,
        esp32ApiClient = apiClient
    )

    private val scope = CoroutineScope(Dispatchers.IO)

    init {
        automationEngine.start()
    }

    val allDevices: Flow<List<Device>> = deviceDao.getAllDevices().map { entities ->
        entities.map { it.toDomain() }
    }

    val allChannels: Flow<List<Channel>> = channelDao.getAllChannels().map { entities ->
        entities.map { it.toDomain() }
    }

    val favoriteChannels: Flow<List<Channel>> = channelDao.getFavoriteChannels().map { entities ->
        entities.map { it.toDomain() }
    }

    val allRooms: Flow<List<Room>> = roomDao.getAllRooms().map { entities ->
        entities.map { it.toDomain() }
    }

    val allAutomations: Flow<List<AutomationEntity>> = automationDao.getAllAutomations()

    val recentLogs: Flow<List<ActivityLogEntity>> = activityLogDao.getRecentLogs()

    fun getChannelsForDevice(deviceId: String): Flow<List<Channel>> {
        return channelDao.getChannelsForDevice(deviceId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getDevicesForRoom(roomId: String): Flow<List<Device>> {
        return deviceDao.getDevicesByRoom(roomId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun toggleSwitch(channel: Channel) {
        val targetState = !channel.state
        // Update local database immediately for responsive feel
        channelDao.updateChannelState(channel.id, targetState)

        // Find device IP for hardware packet transmission
        val device = deviceDao.getDeviceById(channel.deviceId)
        if (device != null) {
            val result = apiClient.setSwitchState(device.ipAddress, channel.channelIndex, targetState)
            if (result.isSuccess) {
                activityLogDao.insertLog(
                    ActivityLogEntity(
                        id = UUID.randomUUID().toString(),
                        title = "${channel.name} Switched ${if (targetState) "ON" else "OFF"}",
                        description = "Board: ${device.name} [Channel ${channel.channelIndex}]",
                        category = "SWITCH"
                    )
                )
            }
        }
    }

    suspend fun setFanSpeed(channel: Channel, newSpeed: Int) {
        val isPowerOn = newSpeed > 0
        channelDao.updateFanSpeed(channel.id, newSpeed, isPowerOn)

        val device = deviceDao.getDeviceById(channel.deviceId)
        if (device != null) {
            val result = apiClient.setFanSpeed(device.ipAddress, channel.channelIndex, isPowerOn, newSpeed)
            if (result.isSuccess) {
                activityLogDao.insertLog(
                    ActivityLogEntity(
                        id = UUID.randomUUID().toString(),
                        title = "${channel.name} Regulator Level $newSpeed",
                        description = "Fan speed updated to Level $newSpeed on ${device.name}",
                        category = "FAN"
                    )
                )
            }
        }
    }

    suspend fun toggleChannelFavorite(channelId: String, currentFav: Boolean) {
        channelDao.updateFavorite(channelId, !currentFav)
    }

    suspend fun renameChannel(channelId: String, newName: String) {
        channelDao.updateChannelName(channelId, newName)
    }

    suspend fun updateDevice(device: Device) {
        deviceDao.updateDevice(device.toEntity())
    }

    suspend fun deleteDevice(deviceId: String) {
        channelDao.deleteChannelsForDevice(deviceId)
        deviceDao.deleteDeviceById(deviceId)
    }

    suspend fun addRoom(name: String, iconName: String) {
        val newRoom = RoomEntity(
            id = "room_" + UUID.randomUUID().toString().take(8),
            name = name,
            iconName = iconName,
            displayOrder = 10
        )
        roomDao.insertOrUpdateRoom(newRoom)
    }

    suspend fun deleteRoom(roomId: String) {
        roomDao.deleteRoomById(roomId)
    }

    suspend fun createAutomation(
        name: String,
        triggerType: TriggerType,
        triggerDetail: String,
        actionType: ActionType,
        targetDeviceId: String,
        targetChannelIndex: Int,
        actionValue: Int
    ) {
        val auto = AutomationEntity(
            id = "auto_" + UUID.randomUUID().toString().take(8),
            name = name,
            triggerType = triggerType.name,
            triggerDetail = triggerDetail,
            actionType = actionType.name,
            targetDeviceId = targetDeviceId,
            targetChannelIndex = targetChannelIndex,
            actionValue = actionValue,
            isEnabled = true
        )
        automationDao.insertOrUpdateAutomation(auto)
    }

    suspend fun toggleAutomation(automationId: String, isEnabled: Boolean) {
        automationDao.updateAutomationStatus(automationId, isEnabled)
    }

    suspend fun deleteAutomation(automationId: String) {
        automationDao.deleteAutomationById(automationId)
    }

    suspend fun executeMasterAllOff() {
        automationEngine.executeQuickAction(ActionType.ALL_OFF)
    }

    suspend fun addCustomSwitchboard(
        boardName: String,
        boardModel: String,
        ipAddress: String,
        roomId: String,
        switchCount: Int,
        fanCount: Int
    ): String {
        val deviceId = "esp32_" + UUID.randomUUID().toString().take(8)
        val mac = "34:85:18:" + (10..99).random() + ":" + (10..99).random() + ":" + (10..99).random()
        val device = DeviceEntity(
            id = deviceId,
            boardModel = boardModel,
            name = boardName,
            ipAddress = ipAddress,
            macAddress = mac,
            bleAddress = mac,
            roomId = roomId,
            isOnline = true,
            lastSeenTimestamp = System.currentTimeMillis(),
            firmwareVersion = "v2.4.1",
            connectionType = "WIFI"
        )
        deviceDao.insertOrUpdateDevice(device)

        var channelIndex = 1
        val channels = mutableListOf<ChannelEntity>()

        for (i in 1..switchCount) {
            channels.add(
                ChannelEntity(
                    id = "${deviceId}_ch${channelIndex}",
                    deviceId = deviceId,
                    channelIndex = channelIndex,
                    name = "Switch $i",
                    type = "SWITCH",
                    state = false,
                    fanSpeed = 0,
                    maxFanSpeed = 5,
                    brightness = 100,
                    iconName = "light",
                    isFavorite = i <= 2
                )
            )
            channelIndex++
        }

        for (f in 1..fanCount) {
            channels.add(
                ChannelEntity(
                    id = "${deviceId}_ch${channelIndex}",
                    deviceId = deviceId,
                    channelIndex = channelIndex,
                    name = if (fanCount == 1) "Ceiling Fan" else "Fan $f",
                    type = "FAN",
                    state = false,
                    fanSpeed = 0,
                    maxFanSpeed = 5,
                    brightness = 100,
                    iconName = "fan",
                    isFavorite = true
                )
            )
            channelIndex++
        }

        channelDao.insertOrUpdateChannels(channels)

        activityLogDao.insertLog(
            ActivityLogEntity(
                id = UUID.randomUUID().toString(),
                title = "Board Configured",
                description = "Configured $boardName ($switchCount Switches + $fanCount Fans)",
                category = "SETUP"
            )
        )
        return deviceId
    }

    suspend fun discoverAndEnrollHardware(ipAddress: String, roomId: String): Result<String> {
        val configResult = apiClient.fetchBoardConfig(ipAddress)
        return if (configResult.isSuccess) {
            val board = configResult.getOrThrow()
            val deviceId = "esp32_" + UUID.randomUUID().toString().take(8)
            val mac = "34:85:18:" + (10..99).random() + ":" + (10..99).random() + ":" + (10..99).random()
            val device = DeviceEntity(
                id = deviceId,
                boardModel = board.model,
                name = board.name,
                ipAddress = ipAddress,
                macAddress = mac,
                bleAddress = mac,
                roomId = roomId,
                isOnline = true,
                lastSeenTimestamp = System.currentTimeMillis(),
                firmwareVersion = board.firmware,
                connectionType = "WIFI"
            )
            deviceDao.insertOrUpdateDevice(device)

            val channels = board.channels.map { spec ->
                ChannelEntity(
                    id = "${deviceId}_ch${spec.index}",
                    deviceId = deviceId,
                    channelIndex = spec.index,
                    name = spec.name,
                    type = spec.type,
                    state = spec.state,
                    fanSpeed = spec.fanSpeed,
                    maxFanSpeed = spec.maxFanSpeed,
                    brightness = 100,
                    iconName = if (spec.type == "FAN") "fan" else "light",
                    isFavorite = spec.index == 1
                )
            }
            channelDao.insertOrUpdateChannels(channels)

            activityLogDao.insertLog(
                ActivityLogEntity(
                    id = UUID.randomUUID().toString(),
                    title = "Board Discovered & Enrolled",
                    description = "Dynamic layout retrieved for ${board.model} with ${channels.size} channels.",
                    category = "SETUP"
                )
            )
            Result.success(deviceId)
        } else {
            Result.failure(configResult.exceptionOrNull() ?: Exception("Failed to query board"))
        }
    }

    private fun DeviceEntity.toDomain() = Device(
        id = id,
        boardModel = boardModel,
        name = name,
        ipAddress = ipAddress,
        macAddress = macAddress,
        bleAddress = bleAddress,
        roomId = roomId,
        isOnline = isOnline,
        lastSeenTimestamp = lastSeenTimestamp,
        firmwareVersion = firmwareVersion,
        connectionType = ConnectionType.valueOf(connectionType)
    )

    private fun Device.toEntity() = DeviceEntity(
        id = id,
        boardModel = boardModel,
        name = name,
        ipAddress = ipAddress,
        macAddress = macAddress,
        bleAddress = bleAddress,
        roomId = roomId,
        isOnline = isOnline,
        lastSeenTimestamp = lastSeenTimestamp,
        firmwareVersion = firmwareVersion,
        connectionType = connectionType.name
    )

    private fun ChannelEntity.toDomain() = Channel(
        id = id,
        deviceId = deviceId,
        channelIndex = channelIndex,
        name = name,
        type = ChannelType.valueOf(type),
        state = state,
        fanSpeed = fanSpeed,
        maxFanSpeed = maxFanSpeed,
        brightness = brightness,
        iconName = iconName,
        isFavorite = isFavorite
    )

    private fun RoomEntity.toDomain() = Room(
        id = id,
        name = name,
        iconName = iconName,
        displayOrder = displayOrder
    )
}
