package xh.rabbit.core.utils

import android.content.Context
import android.net.wifi.WifiManager
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException


class IpUtil {
    companion object {

        /**
         * 获取本机IPv4地址
         *
         * @param context
         * @return 本机IPv4地址；null：无网络连接
         */
        fun getIpAddress(context: Context): String? {
            // 获取WiFi服务
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            // 判断WiFi是否开启
            return if (wifiManager.isWifiEnabled) {
                // 已经开启了WiFi
                val wifiInfo = wifiManager.connectionInfo
                val ipAddress = wifiInfo.ipAddress
                intToIp(ipAddress)
            } else {
                // 未开启WiFi
                getIpAddress()
            }
        }

        private fun intToIp(ipAddress: Int): String {
            return (ipAddress and 0xFF).toString() + "." +
                    (ipAddress shr 8 and 0xFF) + "." +
                    (ipAddress shr 16 and 0xFF) + "." +
                    (ipAddress shr 24 and 0xFF)
        }

        /**
         * 获取本机IPv4地址
         *
         * @return 本机IPv4地址；null：无网络连接
         */
        private fun getIpAddress(): String? {
            return try {
                var networkInterface: NetworkInterface
                var inetAddress: InetAddress
                val en = NetworkInterface.getNetworkInterfaces()
                while (en.hasMoreElements()) {
                    networkInterface = en.nextElement()
                    val enumIpAddr = networkInterface.inetAddresses
                    while (enumIpAddr.hasMoreElements()) {
                        inetAddress = enumIpAddr.nextElement()
                        if (!inetAddress.isLoopbackAddress && !inetAddress.isLinkLocalAddress) {
                            return inetAddress.hostAddress
                        }
                    }
                }
                null
            } catch (ex: SocketException) {
                ex.printStackTrace()
                null
            }
        }
    }
}