package com.smart.album.utils

import android.content.Context
import android.net.ConnectivityManager
import java.net.NetworkInterface
import java.net.SocketException

object NetworkUtils {

    fun getDeviceIP(context: Context): String? {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.let {
            val activeNetwork = it.activeNetwork
            activeNetwork?.let { network ->
                val linkProperties = it.getLinkProperties(network)
                linkProperties?.let { lp ->
                    for (linkAddress in lp.linkAddresses) {
                        val inetAddress = linkAddress.address
                        if (!inetAddress.isLoopbackAddress) {
                            val hostAddress = inetAddress.hostAddress
                            if (hostAddress.contains("%")) { // 去掉IPv6地址中的接口标识符
                                return hostAddress.substringBefore('%')
                            }
                            return hostAddress
                        }
                    }
                }
            }
        }

        // 如果上述方法失败，尝试使用NetworkInterface
        try {
            val networkInterfaces = NetworkInterface.getNetworkInterfaces()
            for (networkInterface in networkInterfaces) {
                if (networkInterface.isUp && !networkInterface.isLoopback) {
                    val inetAddresses = networkInterface.inetAddresses
                    for (inetAddress in inetAddresses) {
                        if (!inetAddress.isLoopbackAddress) {
                            val hostAddress = inetAddress.hostAddress
                            if (hostAddress.contains("%")) { // 去掉IPv6地址中的接口标识符
                                return hostAddress.substringBefore('%')
                            }
                            return hostAddress
                        }
                    }
                }
            }
        } catch (e: SocketException) {
            e.printStackTrace()
        }

        return null
    }
}