package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ActionType
import com.example.domain.model.ChannelType
import com.example.domain.model.ConnectionType
import com.example.domain.model.TriggerType

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val boardModel: String,
    val name: String,
    val ipAddress: String,
    val macAddress: String,
    val bleAddress: String = "",
    val roomId: String = "room_living",
    val isOnline: Boolean = true,
    val lastSeenTimestamp: Long = System.currentTimeMillis(),
    val firmwareVersion: String = "v2.4.1",
    val connectionType: String = ConnectionType.WIFI.name
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val channelIndex: Int,
    val name: String,
    val type: String = ChannelType.SWITCH.name,
    val state: Boolean = false,
    val fanSpeed: Int = 0,
    val maxFanSpeed: Int = 5,
    val brightness: Int = 100,
    val iconName: String = "light",
    val isFavorite: Boolean = false
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconName: String = "living_room",
    val displayOrder: Int = 0
)

@Entity(tableName = "automations")
data class AutomationEntity(
    @PrimaryKey val id: String,
    val name: String,
    val triggerType: String = TriggerType.TIME_SCHEDULE.name,
    val triggerDetail: String,
    val actionType: String = ActionType.ALL_OFF.name,
    val targetDeviceId: String = "",
    val targetChannelIndex: Int = 0,
    val actionValue: Int = 0,
    val isEnabled: Boolean = true
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val description: String,
    val category: String = "SWITCH"
)
