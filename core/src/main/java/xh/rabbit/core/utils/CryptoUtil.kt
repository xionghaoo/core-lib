package xh.rabbit.core.utils

import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class CryptoUtil {
    companion object {
        /**
         * MD5算法32位小写;
         * <hr></hr>
         * 16位小写加密只需getMd5Value("xxx").substring(8, 24);即可
         *
         * @param sSecret
         * @return
         */
        fun encryptToMD5(sSecret: String?): String {
            if (sSecret == null) return ""

            try {
                val bmd5 = MessageDigest.getInstance("MD5")
                bmd5.update(sSecret.toByteArray())
                var i: Int
                val buf = StringBuffer()
                val b = bmd5.digest()
                for (offset in b.indices) {
                    i = b[offset].toInt()
                    if (i < 0)
                        i += 256
                    if (i < 16)
                        buf.append("0")
                    buf.append(Integer.toHexString(i))
                }
                return buf.toString()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            }

            return ""
        }

        fun nameToMD5(str: String) : String {
            val md = MessageDigest.getInstance("MD5")
            md.update((str + System.currentTimeMillis()).toByteArray())
            return BigInteger(1, md.digest()).toString(16)
        }
    }
}