package com.darkmintis.gitstore.core.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class ConnectivityObserver(private val context: Context) {

    val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun currentlyOnline(): Boolean {
            return try {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            } catch (_: SecurityException) {
                // Missing ACCESS_NETWORK_STATE — assume online so the app can still run.
                true
            }
        }

        trySend(currentlyOnline())

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(currentlyOnline())
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                trySend(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
            }
        }

        try {
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder().build(),
                callback
            )
        } catch (_: SecurityException) {
            trySend(true)
            awaitClose { }
            return@callbackFlow
        }

        awaitClose {
            runCatching {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }
    }.distinctUntilChanged()
}
