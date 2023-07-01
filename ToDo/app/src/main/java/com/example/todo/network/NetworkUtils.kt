package com.example.todo.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities


class InternetConnectionWatcher(private val context: Context) {
    private lateinit var onInternetConnectedFunc: () -> Unit

    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback: ConnectivityManager.NetworkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                onInternetConnectedFunc()
                connectivityManager.unregisterNetworkCallback(this)
            }
        }
    }

    private val networkChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (isConnectedToInternet()) {
                onInternetConnectedFunc()
                context.unregisterReceiver(this)
            }
        }
    }

    fun setOnInternetConnectedListener(func: () -> Unit) {
        onInternetConnectedFunc = func
    }

    fun startWatching() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    fun stopWatching() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
        try {
            context.unregisterReceiver(networkChangeReceiver)
        } catch (_: IllegalArgumentException) {
        }
    }

    private fun isConnectedToInternet(): Boolean {

        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ?: false

    }
}

