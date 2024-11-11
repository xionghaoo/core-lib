package xh.rabbit.core.utils

import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.math.BigInteger
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object Md5Util {
    /**
     * 获取文件的MD5值
     *
     * @param file 文件路径
     * @return md5
     */
    fun getFileMd5(file: File?): String {
        val start = System.currentTimeMillis()
        val messageDigest: MessageDigest
        //MappedByteBuffer byteBuffer = null;
        var fis: FileInputStream? = null
        try {
            messageDigest = MessageDigest.getInstance("MD5")
            if (file == null) {
                return ""
            }
            if (!file.exists()) {
                return ""
            }
            var len = 0
            fis = FileInputStream(file)
            //普通流读取方式
            val buffer = ByteArray(1024 * 1024 * 10)
            while ((fis.read(buffer).also { len = it }) > 0) {
                //该对象通过使用 update（）方法处理数据
                messageDigest.update(buffer, 0, len)
            }
            val bigInt = BigInteger(1, messageDigest.digest())
            var md5 = bigInt.toString(16)
            while (md5.length < 32) {
                md5 = "0$md5"
            }
            Logger.d("generate md5 cost: ${System.currentTimeMillis() - start}")
            return md5
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace();
//            Timber.e(e);
//            Logger.e(e)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (fis != null) {
                    fis.close()
                    fis = null
                }
            } catch (e: IOException) {
//                Timber.e(e);
                e.printStackTrace()
            }
        }
        return ""
    }
}


