package com.example.domain.model

enum class ChannelType {
    SWITCH,
    FAN,
    SOCKET,
    DIMMER
}

enum class ConnectionType {
    WIFI,
    BLUETOOTH_LE,
    SIMULATED
}

data class Device(
    val id: String,
    val boardModel: String,
    val name: String,
    val ipAddress: String,
    val macAddress: String,
    val bleAddress: String = "",
    val roomId: String = "room_living",
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val firmwareVersion: String = "v2.4.1",
    val connectionType: ConnectionType = ConnectionType.WIFI
)

data class Channel(
    val id: String,
    val deviceId: String,
    val channelIndex: Int,
    val name: String,
    val type: ChannelType = ChannelType.SWITCH,
    val state: Boolean = false,
    val fanSpeed: Int = 0,
    val maxFanSpeed: Int = 5,
    val brightness: Int = 100,
    val iconName: String = "light",
    val isFavorite: Boolean = false
)

data class Room(
    val id: String,
    val name: String,
    val iconName: String = "living_room",
    val displayOrder: Int = 0
)

enum class TriggerType {
    TIME_SCHEDULE,
    CHANNEL_STATE,
    TEMPERATURE,
    MOTION
}

enum class ActionType {
    ALL_OFF,
    CHANNEL_TOGGLE,
    CHANNEL_ON,
    CHANNEL_OFF,
    FAN_SPEED
}

data class AutomationRule(
    val id: String,
    val name: String,
    val triggerType: TriggerType,
    val triggerDetail: String, // e.g. "20:00", "Temp > 30°C", "Motion detected"
    val actionType: ActionType,
    val targetDeviceId: String = "",
    val targetChannelIndex: Int = 0,
    val actionValue: Int = 0,
    val isEnabled: Boolean = true
)

data class ActivityLog(
    val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val category: String = "SWITCH"
)
