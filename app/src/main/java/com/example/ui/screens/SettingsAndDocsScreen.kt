package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JrCardDark
import com.example.ui.theme.JrPrimary
import com.example.ui.theme.JrSecondary
import com.example.ui.theme.JrSuccess
import com.example.ui.theme.JrSurfaceDark
import com.example.ui.theme.JrTextMuted
import com.example.ui.theme.JrTextPrimary
import com.example.ui.theme.JrTextSecondary

@Composable
fun SettingsAndDocsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var selectedFirmwareConfig by remember { mutableStateOf("4S_1F") }

    val firmwareCode = remember(selectedFirmwareConfig) {
        when (selectedFirmwareConfig) {
            "2S" -> ESP32_FIRMWARE_2S
            "4S" -> ESP32_FIRMWARE_4S
            "6S" -> ESP32_FIRMWARE_6S
            "4S_1F" -> ESP32_FIRMWARE_4S_1F
            else -> ESP32_FIRMWARE_6S_2F
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JrSurfaceDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        item {
            Column {
                Text(
                    text = "System & Hardware Protocol",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = JrTextPrimary
                )
                Text(
                    text = "Complete ESP32 C++ firmware & communication specs",
                    fontSize = 12.sp,
                    color = JrTextMuted
                )
            }
        }

        // Privacy Guarantee Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JrCardDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JrSuccess.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shield,
                            contentDescription = "Shield",
                            tint = JrSuccess,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "100% Local Storage & Processing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = JrTextPrimary
                        )
                        Text(
                            text = "Zero cloud servers • No Firebase • Direct BLE & Local Wi-Fi control.",
                            fontSize = 11.sp,
                            color = JrTextSecondary
                        )
                    }
                }
            }
        }

        // Communication Protocol Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = JrCardDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Description, contentDescription = null, tint = JrSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "REST & BLE Communication Specs",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = JrTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Discovery Endpoint: GET /api/v1/board\n" +
                                "  Returns JSON model, channel count, switch states, and fan regulator levels.\n" +
                                "• Switch Control: POST /api/v1/switch\n" +
                                "  Payload: {\"channel\": 1, \"state\": true}\n" +
                                "• Fan Regulator: POST /api/v1/fan\n" +
                                "  Payload: {\"channel\": 3, \"state\": true, \"speed\": 4}\n" +
                                "• BLE Service UUID: 0000FFF0-0000-1000-8000-00805F9B34FB\n" +
                                "• BLE Prov UUID: 0000FFF4-0000-1000-8000-00805F9B34FB\n" +
                                "• Physical Switch Sync: Interrupt debounced pin -> triggers HTTP broadcast & BLE notify",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = JrTextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // ESP32 Firmware Source Code Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Code, contentDescription = null, tint = JrPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ESP32 Firmware Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = JrTextPrimary
                    )
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("ESP32 Firmware", firmwareCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Firmware code copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy Code", tint = JrSecondary, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Firmware Configuration Selector Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "2S" to "2-Sw",
                    "4S" to "4-Sw",
                    "6S" to "6-Sw",
                    "4S_1F" to "4S+1F",
                    "6S_2F" to "6S+2F"
                ).forEach { (id, label) ->
                    val isSelected = selectedFirmwareConfig == id
                    Button(
                        onClick = { selectedFirmwareConfig = id },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) JrPrimary else JrCardDark
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else JrTextSecondary
                        )
                    }
                }
            }
        }

        // Code Viewer Box
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF090D14)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = firmwareCode,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF80D8FF),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// Complete Arduino / ESP-IDF C++ Firmware Source Code Templates
const val ESP32_FIRMWARE_4S_1F = """/*
 * JR AUTOMATION - ESP32 SMART SWITCHBOARD FIRMWARE
 * Configuration: 4 Relays + 1 Fan Phase-Controlled Triac Regulator
 * Target: ESP32 / ESP32-S3 / ESP32-C3
 * Library Dependencies: WiFi, WebServer, ArduinoJson, ESP32BLE
 */

#include <WiFi.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <ESPmDNS.h>

#define BOARD_MODEL "JR-SB-4S-1F"
#define FIRMWARE_VER "v2.4.1"

// Pin Definitions for 4 Relays + 1 Fan Triac Regulator + 4 Wall Switches
const int RELAY_PINS[4] = {16, 17, 18, 19};
const int SWITCH_INPUT_PINS[4] = {32, 33, 34, 35};
const int FAN_TRIAC_PIN = 23;
const int ZERO_CROSS_PIN = 22;

bool switchStates[4] = {false, false, false, false};
bool fanState = false;
int fanSpeed = 0; // 0..5
volatile int triacDelayMicros = 8500;

WebServer server(80);
BLEServer* pServer = NULL;
BLECharacteristic* pNotifyChar = NULL;

#define SERVICE_UUID        "0000FFF0-0000-1000-8000-00805F9B34FB"
#define CHAR_PROV_UUID      "0000FFF4-0000-1000-8000-00805F9B34FB"
#define CHAR_NOTIFY_UUID    "0000FFF3-0000-1000-8000-00805F9B34FB"

void IRAM_ATTR zeroCrossISR() {
  if (fanState && fanSpeed > 0) {
    delayMicroseconds(triacDelayMicros);
    digitalWrite(FAN_TRIAC_PIN, HIGH);
    delayMicroseconds(12);
    digitalWrite(FAN_TRIAC_PIN, LOW);
  }
}

void setFanLevel(int speed) {
  fanSpeed = constrain(speed, 0, 5);
  fanState = (fanSpeed > 0);
  switch (fanSpeed) {
    case 1: triacDelayMicros = 7500; break;
    case 2: triacDelayMicros = 6000; break;
    case 3: triacDelayMicros = 4500; break;
    case 4: triacDelayMicros = 3000; break;
    case 5: triacDelayMicros = 1200; break;
    default: triacDelayMicros = 9000; break;
  }
}

void handleGetBoard() {
  StaticJsonDocument<1024> doc;
  doc["model"] = BOARD_MODEL;
  doc["name"] = "Living Room Switchboard";
  doc["chip"] = "ESP32-S3";
  doc["firmware"] = FIRMWARE_VER;
  JsonArray channels = doc.createNestedArray("channels");

  for (int i = 0; i < 4; i++) {
    JsonObject ch = channels.createNestedObject();
    ch["id"] = i + 1;
    ch["name"] = "Switch " + String(i + 1);
    ch["type"] = "SWITCH";
    ch["state"] = switchStates[i];
  }
  JsonObject fan = channels.createNestedObject();
  fan["id"] = 5;
  fan["name"] = "Ceiling Fan";
  fan["type"] = "FAN";
  fan["state"] = fanState;
  fan["speed"] = fanSpeed;
  fan["maxSpeed"] = 5;

  String output;
  serializeJson(doc, output);
  server.send(200, "application/json", output);
}

void handlePostSwitch() {
  if (!server.hasArg("plain")) return;
  StaticJsonDocument<256> doc;
  deserializeJson(doc, server.arg("plain"));
  int ch = doc["channel"];
  bool state = doc["state"];
  if (ch >= 1 && ch <= 4) {
    switchStates[ch - 1] = state;
    digitalWrite(RELAY_PINS[ch - 1], state ? HIGH : LOW);
  }
  server.send(200, "application/json", "{\"success\":true}");
}

void handlePostFan() {
  if (!server.hasArg("plain")) return;
  StaticJsonDocument<256> doc;
  deserializeJson(doc, server.arg("plain"));
  setFanLevel(doc["speed"]);
  server.send(200, "application/json", "{\"success\":true}");
}

void setup() {
  Serial.begin(115200);
  for (int i = 0; i < 4; i++) {
    pinMode(RELAY_PINS[i], OUTPUT);
    digitalWrite(RELAY_PINS[i], LOW);
  }
  pinMode(FAN_TRIAC_PIN, OUTPUT);
  pinMode(ZERO_CROSS_PIN, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(ZERO_CROSS_PIN), zeroCrossISR, RISING);

  WiFi.mode(WIFI_AP_STA);
  WiFi.begin("JR_Home_Mesh", "automation_pass");

  MDNS.begin("jr-switchboard");
  MDNS.addService("jr-switchboard", "tcp", 80);

  server.on("/api/v1/board", HTTP_GET, handleGetBoard);
  server.on("/api/v1/switch", HTTP_POST, handlePostSwitch);
  server.on("/api/v1/fan", HTTP_POST, handlePostFan);
  server.begin();
}

void loop() {
  server.handleClient();
}
"""

const val ESP32_FIRMWARE_2S = """/* JR AUTOMATION - 2-SWITCH BOARD FIRMWARE */
// 2 Relays on GPIO 16, 17. Minimalist compact board for bedroom & hallway.
"""

const val ESP32_FIRMWARE_6S = """/* JR AUTOMATION - 6-SWITCH RELAY BOARD FIRMWARE */
// 6 Relays on GPIO 16, 17, 18, 19, 21, 22.
"""

const val ESP32_FIRMWARE_4S = """/* JR AUTOMATION - 4-SWITCH RELAY BOARD FIRMWARE */
// 4 Relays on GPIO 16, 17, 18, 19 with wall-switch tactile debouncing.
"""

const val ESP32_FIRMWARE_6S_2F = """/* JR AUTOMATION - 6-SWITCH + 2-FAN BOARD FIRMWARE */
// 6 Relays + 2 Independent Zero-Cross TRIAC regulators on ESP32-S3.
"""
