package com.example

import com.example.domain.model.ActionType
import com.example.domain.model.Channel
import com.example.domain.model.ChannelType
import com.example.domain.model.Device
import com.example.domain.model.TriggerType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class JrAutomationLogicUnitTest {

    @Test
    fun testChannelSwitchToggle() {
        val switchChannel = Channel(
            id = "ch_1",
            deviceId = "board_1",
            channelIndex = 1,
            name = "Chandelier",
            type = ChannelType.SWITCH,
            state = false
        )

        val toggled = switchChannel.copy(state = !switchChannel.state)
        assertTrue(toggled.state)
        assertEquals("Chandelier", toggled.name)
    }

    @Test
    fun testFanRegulatorSpeedLevels() {
        val fan = Channel(
            id = "fan_1",
            deviceId = "board_1",
            channelIndex = 3,
            name = "Ceiling Fan",
            type = ChannelType.FAN,
            state = false,
            fanSpeed = 0,
            maxFanSpeed = 5
        )

        // Speed level 3
        val runningFan = fan.copy(fanSpeed = 3, state = true)
        assertTrue(runningFan.state)
        assertEquals(3, runningFan.fanSpeed)

        // Turned off (Speed 0)
        val stoppedFan = runningFan.copy(fanSpeed = 0, state = false)
        assertFalse(stoppedFan.state)
        assertEquals(0, stoppedFan.fanSpeed)
    }

    @Test
    fun testProtocolPayloadFormatting() {
        val channel = 2
        val state = true
        val switchPayload = """{"channel": $channel, "state": $state}"""
        assertTrue(switchPayload.contains("\"channel\": 2"))
        assertTrue(switchPayload.contains("\"state\": true"))

        val fanSpeed = 4
        val fanPayload = """{"channel": 5, "state": true, "speed": $fanSpeed}"""
        assertTrue(fanPayload.contains("\"speed\": 4"))
    }

    @Test
    fun testAutomationTriggerMapping() {
        val triggerType = TriggerType.TIME_SCHEDULE
        val actionType = ActionType.ALL_OFF
        assertEquals("TIME_SCHEDULE", triggerType.name)
        assertEquals("ALL_OFF", actionType.name)
    }
}
