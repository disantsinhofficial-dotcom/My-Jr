package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.ActivityLogDao
import com.example.data.local.dao.AutomationDao
import com.example.data.local.dao.ChannelDao
import com.example.data.local.dao.DeviceDao
import com.example.data.local.dao.RoomDao
import com.example.data.local.entity.ActivityLogEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.ChannelEntity
import com.example.data.local.entity.DeviceEntity
import com.example.data.local.entity.RoomEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        DeviceEntity::class,
        ChannelEntity::class,
        RoomEntity::class,
        AutomationEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao
    abstract fun channelDao(): ChannelDao
    abstract fun roomDao(): RoomDao
    abstract fun automationDao(): AutomationDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "jr_automation.db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial rooms and an out-of-the-box demo smart switchboard
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        seedInitialData(database)
                    }
                }
            }

            private suspend fun seedInitialData(database: AppDatabase) {
                val rooms = listOf(
                    RoomEntity("room_living", "Living Room", "living_room", 1),
                    RoomEntity("room_bedroom", "Master Bedroom", "bedroom", 2),
                    RoomEntity("room_kitchen", "Kitchen", "kitchen", 3),
                    RoomEntity("room_office", "Home Office", "office", 4),
                    RoomEntity("room_balcony", "Balcony & Patio", "balcony", 5)
                )
                database.roomDao().insertOrUpdateRooms(rooms)

                // Seed initial board: "JR-SB-4CH-1FAN" (ESP32-S3 Living Room Smart Board)
                val board1Id = "esp32_jr_living_01"
                val device1 = DeviceEntity(
                    id = board1Id,
                    boardModel = "JR-SB-4S-1F",
                    name = "Living Room Main Board",
                    ipAddress = "192.168.1.105",
                    macAddress = "34:85:18:9B:4C:A2",
                    bleAddress = "34:85:18:9B:4C:A2",
                    roomId = "room_living",
                    isOnline = true,
                    lastSeenTimestamp = System.currentTimeMillis(),
                    firmwareVersion = "v2.4.1",
                    connectionType = "WIFI"
                )
                database.deviceDao().insertOrUpdateDevice(device1)

                val channels1 = listOf(
                    ChannelEntity("${board1Id}_ch1", board1Id, 1, "Chandelier", "SWITCH", true, 0, 5, 100, "light", true),
                    ChannelEntity("${board1Id}_ch2", board1Id, 2, "Wall Sconces", "SWITCH", false, 0, 5, 100, "wall_light", false),
                    ChannelEntity("${board1Id}_ch3", board1Id, 3, "High-Speed Ceiling Fan", "FAN", true, 3, 5, 100, "fan", true),
                    ChannelEntity("${board1Id}_ch4", board1Id, 4, "TV Cabinet Light", "SWITCH", true, 0, 5, 100, "tv", false),
                    ChannelEntity("${board1Id}_ch5", board1Id, 5, "Balcony Strip", "SWITCH", false, 0, 5, 100, "balcony_light", false)
                )
                database.channelDao().insertOrUpdateChannels(channels1)

                // Seed second board: "JR-SB-2CH-1FAN" (Bedroom Switchboard)
                val board2Id = "esp32_jr_bed_02"
                val device2 = DeviceEntity(
                    id = board2Id,
                    boardModel = "JR-SB-2S-1F",
                    name = "Bedroom Master Switchboard",
                    ipAddress = "192.168.1.108",
                    macAddress = "A4:CF:12:33:8E:10",
                    bleAddress = "A4:CF:12:33:8E:10",
                    roomId = "room_bedroom",
                    isOnline = true,
                    lastSeenTimestamp = System.currentTimeMillis(),
                    firmwareVersion = "v2.4.1",
                    connectionType = "WIFI"
                )
                database.deviceDao().insertOrUpdateDevice(device2)

                val channels2 = listOf(
                    ChannelEntity("${board2Id}_ch1", board2Id, 1, "Bedside Lamp", "SWITCH", false, 0, 5, 100, "lamp", true),
                    ChannelEntity("${board2Id}_ch2", board2Id, 2, "Reading Spotlight", "SWITCH", true, 0, 5, 100, "spotlight", false),
                    ChannelEntity("${board2Id}_ch3", board2Id, 3, "Whisper Quiet Fan", "FAN", true, 2, 5, 100, "fan", true)
                )
                database.channelDao().insertOrUpdateChannels(channels2)

                // Seed sample local automation rules
                val automations = listOf(
                    AutomationEntity(
                        id = "auto_1",
                        name = "Night Sleep All-Lights Off",
                        triggerType = "TIME_SCHEDULE",
                        triggerDetail = "23:00",
                        actionType = "ALL_OFF",
                        isEnabled = true
                    ),
                    AutomationEntity(
                        id = "auto_2",
                        name = "Evening Living Ambiance",
                        triggerType = "TIME_SCHEDULE",
                        triggerDetail = "18:30",
                        actionType = "CHANNEL_ON",
                        targetDeviceId = board1Id,
                        targetChannelIndex = 1,
                        isEnabled = true
                    ),
                    AutomationEntity(
                        id = "auto_3",
                        name = "Cool Bedroom Fan on High Temp",
                        triggerType = "TEMPERATURE",
                        triggerDetail = "> 28°C",
                        actionType = "FAN_SPEED",
                        targetDeviceId = board2Id,
                        targetChannelIndex = 3,
                        actionValue = 4,
                        isEnabled = true
                    )
                )
                automations.forEach { database.automationDao().insertOrUpdateAutomation(it) }

                // Seed initial activity log
                database.activityLogDao().insertLog(
                    ActivityLogEntity(
                        id = "log_init",
                        timestamp = System.currentTimeMillis(),
                        title = "System Ready",
                        description = "JR Automation initialized local controller and loaded saved switchboard layouts.",
                        category = "SYSTEM"
                    )
                )
            }
        }
    }
}
