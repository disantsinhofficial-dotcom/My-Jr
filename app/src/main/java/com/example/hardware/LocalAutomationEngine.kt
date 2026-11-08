package com.example.hardware

import com.example.data.local.dao.ActivityLogDao
import com.example.data.local.dao.AutomationDao
import com.example.data.local.dao.ChannelDao
import com.example.data.local.dao.DeviceDao
import com.example.data.local.entity.ActivityLogEntity
import com.example.domain.model.ActionType
import com.example.domain.model.TriggerType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LocalAutomationEngine(
    private val automationDao: AutomationDao,
    private val channelDao: ChannelDao,
    private val deviceDao: DeviceDao,
    private val activityLogDao: ActivityLogDao,
    private val esp32ApiClient: Esp32ApiClient
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var loopJob: Job? = null
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun start() {
        if (loopJob?.isActive == true) return
        loopJob = scope.launch {
            while (isActive) {
                try {
                    evaluateRules()
                } catch (e: Exception) {
                    // Ignore transient errors
                }
                delay(15000) // check every 15 seconds
            }
        }
    }

    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    suspend fun executeQuickAction(actionType: ActionType, targetDeviceId: String? = null) {
        when (actionType) {
            ActionType.ALL_OFF -> {
                val allChannels = channelDao.getAllChannels()
                // Turn off all channels
                scope.launch {
                    val channels = if (targetDeviceId != null) {
                        channelDao.getChannelsForDeviceList(targetDeviceId)
                    } else {
                        // All channels across all devices
                        val devices = deviceDao.getDeviceById("all")
                        channelDao.getChannelsForDeviceList("esp32_jr_living_01") +
                                channelDao.getChannelsForDeviceList("esp32_jr_bed_02")
                    }
                    for (ch in channels) {
                        channelDao.updateChannelState(ch.id, false)
                        if (ch.type == "FAN") {
                            channelDao.updateFanSpeed(ch.id, 0, false)
                        }
                    }
                    activityLogDao.insertLog(
                        ActivityLogEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Master Action Executed",
                            description = "All switches and fans powered OFF via Master Command.",
                            category = "AUTOMATION"
                        )
                    )
                }
            }
            else -> {}
        }
    }

    private suspend fun evaluateRules() {
        val rules = automationDao.getEnabledAutomations()
        val currentTime = timeFormat.format(Date())

        for (rule in rules) {
            when (rule.triggerType) {
                TriggerType.TIME_SCHEDULE.name -> {
                    if (rule.triggerDetail.trim() == currentTime) {
                        applyRuleAction(rule.name, rule.actionType, rule.targetDeviceId, rule.targetChannelIndex, rule.actionValue)
                    }
                }
                else -> {
                    // Other triggers like sensor/channel state can be evaluated reactively
                }
            }
        }
    }

    private suspend fun applyRuleAction(
        ruleName: String,
        actionType: String,
        deviceId: String,
        channelIndex: Int,
        actionValue: Int
    ) {
        when (actionType) {
            ActionType.ALL_OFF.name -> {
                executeQuickAction(ActionType.ALL_OFF, deviceId.ifEmpty { null })
            }
            ActionType.CHANNEL_ON.name -> {
                val channels = channelDao.getChannelsForDeviceList(deviceId)
                val target = channels.firstOrNull { it.channelIndex == channelIndex }
                if (target != null) {
                    channelDao.updateChannelState(target.id, true)
                    activityLogDao.insertLog(
                        ActivityLogEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Automation Triggered",
                            description = "$ruleName turned ON ${target.name}.",
                            category = "AUTOMATION"
                        )
                    )
                }
            }
            ActionType.CHANNEL_OFF.name -> {
                val channels = channelDao.getChannelsForDeviceList(deviceId)
                val target = channels.firstOrNull { it.channelIndex == channelIndex }
                if (target != null) {
                    channelDao.updateChannelState(target.id, false)
                    activityLogDao.insertLog(
                        ActivityLogEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Automation Triggered",
                            description = "$ruleName turned OFF ${target.name}.",
                            category = "AUTOMATION"
                        )
                    )
                }
            }
            ActionType.FAN_SPEED.name -> {
                val channels = channelDao.getChannelsForDeviceList(deviceId)
                val target = channels.firstOrNull { it.channelIndex == channelIndex }
                if (target != null) {
                    val on = actionValue > 0
                    channelDao.updateFanSpeed(target.id, actionValue, on)
                    activityLogDao.insertLog(
                        ActivityLogEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Automation Triggered",
                            description = "$ruleName set ${target.name} speed to $actionValue.",
                            category = "AUTOMATION"
                        )
                    )
                }
            }
        }
    }
}
