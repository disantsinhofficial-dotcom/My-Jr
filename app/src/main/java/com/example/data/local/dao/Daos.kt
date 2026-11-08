package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.DeviceEntity
import com.example.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY name ASC")
    fun getAllDevices(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices WHERE id = :id LIMIT 1")
    suspend fun getDeviceById(id: String): DeviceEntity?

    @Query("SELECT * FROM devices WHERE roomId = :roomId")
    fun getDevicesByRoom(roomId: String): Flow<List<DeviceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevice(device: DeviceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDevices(devices: List<DeviceEntity>)

    @Update
    suspend fun updateDevice(device: DeviceEntity)

    @Query("UPDATE devices SET isOnline = :isOnline, lastSeenTimestamp = :timestamp WHERE id = :id")
    suspend fun updateDeviceOnlineStatus(id: String, isOnline: Boolean, timestamp: Long)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteDeviceById(id: String)
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY channelIndex ASC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE deviceId = :deviceId ORDER BY channelIndex ASC")
    fun getChannelsForDevice(deviceId: String): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE deviceId = :deviceId ORDER BY channelIndex ASC")
    suspend fun getChannelsForDeviceList(deviceId: String): List<ChannelEntity>

    @Query("SELECT * FROM channels WHERE isFavorite = 1")
    fun getFavoriteChannels(): Flow<List<ChannelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChannel(channel: ChannelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChannels(channels: List<ChannelEntity>)

    @Query("UPDATE channels SET state = :state WHERE id = :id")
    suspend fun updateChannelState(id: String, state: Boolean)

    @Query("UPDATE channels SET fanSpeed = :speed, state = :state WHERE id = :id")
    suspend fun updateFanSpeed(id: String, speed: Int, state: Boolean)

    @Query("UPDATE channels SET name = :name WHERE id = :id")
    suspend fun updateChannelName(id: String, name: String)

    @Query("UPDATE channels SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("DELETE FROM channels WHERE deviceId = :deviceId")
    suspend fun deleteChannelsForDevice(deviceId: String)
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY displayOrder ASC, name ASC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRoom(room: RoomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateRooms(rooms: List<RoomEntity>)

    @Query("DELETE FROM rooms WHERE id = :id")
    suspend fun deleteRoomById(id: String)
}

@Dao
interface AutomationDao {
    @Query("SELECT * FROM automations")
    fun getAllAutomations(): Flow<List<AutomationEntity>>

    @Query("SELECT * FROM automations WHERE isEnabled = 1")
    suspend fun getEnabledAutomations(): List<AutomationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAutomation(automation: AutomationEntity)

    @Query("UPDATE automations SET isEnabled = :isEnabled WHERE id = :id")
    suspend fun updateAutomationStatus(id: String, isEnabled: Boolean)

    @Query("DELETE FROM automations WHERE id = :id")
    suspend fun deleteAutomationById(id: String)
}

@Dao
interface ActivityLogDao {
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLogEntity)

    @Query("DELETE FROM activity_logs")
    suspend fun clearLogs()
}
