package xh.rabbit.core.utils

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class JavascriptInjector {
    companion object {
        fun inject(context: Context, js: String, outDir: File) {
//            val dir = AppUtil.getWebDir(context)
            copyAssetToFile(context.assets.open(js), File(outDir, js))
            Logger.d("inject: $js")
            val indexFile = File(outDir, "index.html")
            val lines = indexFile.readLines()
            val newLines = StringBuilder()
            lines.forEach { line ->
                newLines.append(line).append("\n")
                if (line.matches(".*<head>.*".toRegex())) {
                    newLines.append("<script type=\"text/javascript\" crossorigin src=\"./${js}\"></script>").append("\n")
                }
            }
            indexFile.writeText(newLines.toString())
        }

        fun appendChild(context: Context, html: String, insertNode: String, content: String): File {
            val htmlFile = File(context.cacheDir, html)
            copyAssetToFile(context.assets.open(html), htmlFile)
            val lines = htmlFile.readLines()
            val newLines = StringBuilder()
            lines.forEach { line ->
                newLines.append(line).append("\n")
                if (line.matches(".*<${insertNode}>.*".toRegex())) {
                    newLines.append(content).append("\n")
                }
            }
            htmlFile.writeText(newLines.toString())
            return htmlFile
        }

        private fun copyAssetToFile(assetStream: InputStream, targetFile: File) {
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
    }
}