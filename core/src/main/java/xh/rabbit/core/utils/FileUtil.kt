package xh.rabbit.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import java.io.*

class FileUtil {
    companion object {

        /**
         * android assets path: file:///android_asset/file.txt
         * file: 直接填文件名就行了
         */
        fun readAssetsFileToString(file: String, context: Context): String {
            val result = StringBuffer()
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(context.assets.open(file)))
                var line = reader.readLine()
                while (line != null) {
                    result.append(line)
                    line = reader.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return result.toString()
        }

        /**
         * android assets path: file:///android_asset/file.txt
         * file: 直接填文件名就行了
         */
        fun readAssetsLines(file: String, context: Context): List<String> {
            var lines = listOf<String>()
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(context.assets.open(file)))
                lines = reader.readLines()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    if (reader != null) {
                        reader.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return lines
        }

        fun copyAssetToFile(assetStream: InputStream, targetFile: File) {
            // 生成
            var oldBuffInput: BufferedInputStream? = null
            var oldOut: FileOutputStream? = null
            try {
                oldOut = FileOutputStream(targetFile)
                oldBuffInput = BufferedInputStream(assetStream)
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var length = oldBuffInput.read(buffer)
                while (length != -1) {
                    oldOut.write(buffer, 0, length)
                    length = oldBuffInput.read(buffer)
                }
                oldOut.flush()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                oldBuffInput?.close()
                oldOut?.close()
            }
        }

        /**
         * android assets path: file:///android_asset/file.txt
         * file: 直接填文件名就行了
         */
        fun getBitmapFromAsset(file: String, context: Context): Bitmap {
            return context.assets.open(file).use { BitmapFactory.decodeStream(it) }
        }


        fun getAssetsFileList(context: Context,dirPath: String): Array<out String>? {
            return context.assets.list(dirPath)
        }

        fun readFile(file: File): String {
            val result = StringBuffer()
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader(file))
                var line = reader.readLine()
                while (line != null) {
                    result.append(line).append("\n")
                    line = reader.readLine()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
            return result.toString()
        }

        fun readFileToLines(file: File): List<String> {
            var lines = listOf<String>()
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(FileReader(file))
                lines = reader.readLines()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    reader?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return lines
        }

        fun readFileToBytes(file: File): ByteArray {
            val reader = FileInputStream(file)
            val bytes = reader.readBytes()
            reader.close()
            return bytes
        }

        fun getCacheFolderSize(context: Context) : Long {
            val cacheFile = File(context.cacheDir, ".")
            val outCacheFile = File(context.externalCacheDir, ".")

            val innerCache = getFileSize(cacheFile)
            val outerCache = getFileSize(outCacheFile)

            return innerCache + outerCache
        }

        fun getFileSize(file: File) : Long {
            var size: Long = 0
            if (file.isDirectory) {
                for (f in file.listFiles()) {
                    size += getFileSize(f)
                }
            } else {
                size += file.length()
            }

            return size
        }

        fun clearCacheFolder(context: Context) {
            val cacheFile = File(context.cacheDir, ".")
            val outCacheFile = File(context.externalCacheDir, ".")
            deleteFile(cacheFile)
            deleteFile(outCacheFile)
        }

        fun deleteFile(file: File): Boolean {
            return if (file.exists()) {
                if (file.isDirectory) {
                    for (f in file.listFiles()) {
                        deleteFile(f)
                    }
                }
                file.delete()
            } else {
                false
            }
        }

        fun saveImageToPath(img: Bitmap, path: String) {
            try {
                val out = FileOutputStream(path)
                img.compress(Bitmap.CompressFormat.PNG, 100, out)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun saveInputStreamToFile(input: InputStream, file: File) {
            try {
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(4 * 1024) // or other buffer size
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            } finally {
                input.close()
            }
        }

        public fun copyFile(origin: File, target: File) {
            var input: BufferedInputStream? = null
            var out: FileOutputStream? = null
            try {
                out = FileOutputStream(target)
                input = BufferedInputStream(FileInputStream(origin))
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var length = input.read(buffer)
                while (length != -1) {
                    out.write(buffer, 0, length)
                    length = input.read(buffer)
                }
                out.flush()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                input?.close()
                out?.close()
            }
        }

        fun getRootDir(): String {

            return if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                Environment.getExternalStorageDirectory()
                    .absolutePath
            } else {
                ""
            }

        }

        /**
         * 可创建多个文件夹
         * dirPath 文件路径
         */
        fun mkDir(dirPath: String) {

            val dirArray = dirPath.split("/".toRegex())
            var pathTemp = ""
            for (i in 1 until dirArray.size) {
                pathTemp = "$pathTemp/${dirArray[i]}"
                val newF = File("${dirArray[0]}$pathTemp")
                if (!newF.exists()) {
                    val cheatDir: Boolean = newF.mkdir()
                    println(cheatDir)
                }
            }

        }

        /**
         * 创建文件
         *
         * dirpath 文件目录
         * fileName 文件名称
         */
        fun creatFile(dirPath: String = getRootDir(), fileName: String) {
            val file = File("$dirPath/$fileName")
            if (!file.exists()) {
                file.createNewFile()
            }

        }

        /**
         * 创建文件
         * filePath 文件路径
         */
        fun creatFile(filePath: String) {
            val file = File(filePath)
            if (!file.exists()) {
                file.createNewFile()
            }
        }

        /**
         * 创建文件
         * filePath 文件路径
         */
        fun creatFile(filePath: File) {
            if (!filePath.exists()) {
                filePath.createNewFile()
            }
        }

        fun saveParamToFile(type: String?, param: String) {
            val f = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "ApiParam")
            if (!f.exists()) {
                f.mkdir()
            }
            val p = File(f, "param_${type}_${System.currentTimeMillis()}.json")
            try {
                val writer = FileWriter(p)
                writer.write(param)
                writer.flush()
                writer.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        /**
         * 文件读取
         * filePath 文件路径
         */
//        fun readFile(filePath: File): String? {
//            if (!filePath.isFile) {
//                return null
//            } else {
//                return filePath.readText()
//            }
//        }

        /**
         * 追加数据
         */
        fun appendText(filePath: File, content: String) {
            creatFile(filePath)
            filePath.appendText(content)
        }


        /**
         * 获取文件大小
         */
        fun getLeng(filePath: File): Long {
            return if (!filePath.exists()) {
                -1
            } else {
                filePath.length()
            }
        }

        /**
         * 按时间排序
         */
        fun sortByTime(filePath: File): Array<File>? {
            if (!filePath.exists()) {
                return null
            }
            val files: Array<File> = filePath.listFiles()
            if (files.isEmpty()) {
                return null
            }
            files.sortBy { it.lastModified() }
            files.reverse()
            return files

        }

    }
}