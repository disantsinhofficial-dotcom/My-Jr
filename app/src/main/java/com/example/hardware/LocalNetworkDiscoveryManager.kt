package com.example.hardware

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class DiscoveredNetworkBoard(
    val serviceName: String,
    val hostAddress: String,
    val port: Int,
    val model: String = "JR-SB-ESP32"
)

class LocalNetworkDiscoveryManager(private val context: Context) {

    companion object {
        private const val TAG = "JR_NSD"
        private const val SERVICE_TYPE = "_jr-switchboard._tcp."
    }

    private val nsdManager: NsdManager? =
        context.getSystemService(Context.NSD_SERVICE) as? NsdManager

    fun discoverLocalBoards(): Flow<List<DiscoveredNetworkBoard>> = callbackFlow {
        val discoveredBoards = mutableMapOf<String, DiscoveredNetworkBoard>()

        if (nsdManager == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onStartDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery failed to start: $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String?, errorCode: Int) {
                Log.e(TAG, "Discovery failed to stop: $errorCode")
            }

            override fun onDiscoveryStarted(serviceType: String?) {
                Log.d(TAG, "NSD Service discovery started for $serviceType")
            }

            override fun onDiscoveryStopped(serviceType: String?) {
                Log.d(TAG, "NSD Service discovery stopped.")
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
                try {
                    nsdManager.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                        override fun onResolveFailed(info: NsdServiceInfo?, errorCode: Int) {
                            Log.e(TAG, "Resolve failed: $errorCode")
                        }

                        override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                            val host = resolvedInfo.host?.hostAddress ?: return
                            val port = resolvedInfo.port
                            val board = DiscoveredNetworkBoard(
                                serviceName = resolvedInfo.serviceName,
                                hostAddress = host,
                                port = port,
                                model = "JR-ESP32"
                            )
                            discoveredBoards[host] = board
                            trySend(discoveredBoards.values.toList())
                        }
                    })
                } catch (e: Exception) {
                    Log.e(TAG, "Error resolving service", e)
                }
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Exception during NSD discoverServices", e)
        }

        awaitClose {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (e: Exception) {
                // Ignore cleanup error
            }
        }
    }
}
