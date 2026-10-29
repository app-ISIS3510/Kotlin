package com.example.parku.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

/**
 * Estado de red en vivo. Usa el callback del sistema (no consulta cada tanto),
 * asi que el cambio llega en cuanto el telefono pierde o recupera internet.
 */
class ConnectivityObserver(context: Context) {

    private val manager =
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _online = MutableStateFlow(hasInternet())
    val online: StateFlow<Boolean> = _online.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _online.value = true
        }

        // Si se pierde la red por defecto no hay internet; si aparece otra, onAvailable avisa.
        // No se vuelve a preguntar al sistema aqui: puede devolver todavia la red que se cayo.
        override fun onLost(network: Network) {
            _online.value = false
        }

        override fun onUnavailable() {
            _online.value = false
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            _online.value = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }
    }

    init {
        runCatching { manager.registerDefaultNetworkCallback(callback) }
    }

    private fun hasInternet(): Boolean {
        val caps = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun close() {
        runCatching { manager.unregisterNetworkCallback(callback) }
    }
}

/**
 * Distingue "no hubo red" (se encola y se reintenta) de "el servidor dijo que no"
 * (se avisa y se revierte). Las librerias envuelven el IOException, por eso se
 * recorre toda la cadena de causas.
 */
fun isNetworkFailure(error: Throwable): Boolean {
    var current: Throwable? = error
    while (current != null) {
        if (current is IOException) return true
        current = current.cause
    }
    return false
}
