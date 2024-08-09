package xh.rabbit.core.utils

import android.content.Context
import android.media.MediaPlayer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class AudioUtil {
    companion object {

        fun playWav(context: Context, audio: Int) {
            val mp = MediaPlayer.create(context, audio)
            mp.start()
        }

        fun playUrl(url: String) {
            val mp = MediaPlayer()
            mp.setDataSource(url)
            mp.setOnPreparedListener { m ->
                m.start()
            }
            mp.prepareAsync()
        }

        /**
         * @param input         raw PCM data
         * limit of file size for wave file: < 2^(2*4) - 36 bytes (~4GB)
         * @param output        file to encode to in wav format
         * @param channelCount  number of channels: 1 for mono, 2 for stereo, etc.
         * @param sampleRate    sample rate of PCM audio
         * @param bitsPerSample bits per sample, i.e. 16 for PCM16
         * @throws IOException in event of an error between input/output files
         * @see [soundfile.sapp.org/doc/WaveFormat](http://soundfile.sapp.org/doc/WaveFormat/)
         */
        @Throws(IOException::class)
        fun pcmToWav(input: File, output: File?, channelCount: Int, originSampleRate: Int, sampleRate: Int, bitsPerSample: Int) {

            val inputSize = input.length().toInt()
            val sampleFactor : Float = sampleRate.toFloat() / originSampleRate.toFloat()

            FileOutputStream(output).use { encoded ->

                // WAVE RIFF header

                writeToOutput(encoded, "RIFF") // chunk id
                writeToOutput(encoded, 36 + (inputSize.toFloat() * sampleFactor).toInt()) // chunk size
                writeToOutput(encoded, "WAVE") // format

                // SUB CHUNK 1 (FORMAT)

                writeToOutput(encoded, "fmt ") // subchunk 1 id
                writeToOutput(encoded, 16) // subchunk 1 size
                writeToOutput(encoded, 1.toShort()) // audio format (1 = PCM)
                writeToOutput(encoded, channelCount.toShort()) // number of channelCount
                writeToOutput(encoded, sampleRate) // sample rate
                writeToOutput(encoded, sampleRate * channelCount * bitsPerSample / 8) // byte rate
                writeToOutput(encoded, (channelCount * bitsPerSample / 8).toShort()) // block align
                writeToOutput(encoded, bitsPerSample.toShort()) // bits per sample

                // SUB CHUNK 2 (AUDIO DATA)

                if (originSampleRate == sampleRate) {

                    writeToOutput(encoded, "data") // subchunk 2 id
                    writeToOutput(encoded, inputSize) // subchunk 2 size
                    copy(FileInputStream(input), encoded)

                } else {

                    // SUB CHUNK 2 (AUDIO DATA) - UPSAMPLE

                    writeToOutput(encoded, "data") // subchunk 2 id
                    writeToOutput(encoded, (inputSize.toFloat() * sampleFactor).toInt()) // subchunk 2 size

                    val inputStream = FileInputStream(input)
                    val buffer = ByteArray(2)
                    var len: Int = inputStream.read(buffer)

                    var overFlow = 0

                    while (len != -1) {

                        for (i in 0 until (sampleFactor * 1000F).toInt()) {

                            overFlow += 1

                            if (overFlow >= 1000) {

                                encoded.write(buffer, 0, len)
                                overFlow = 0

                            }

                        }

                        len = inputStream.read(buffer)

                    }

                    inputStream.close()

                }

            }

        }


        /**
         * Size of buffer used for transfer, by default
         */
        private const val TRANSFER_BUFFER_SIZE = 10 * 1024

        /**
         * Writes string in big endian form to an output stream
         *
         * @param output stream
         * @param data   string
         * @throws IOException
         */

        @Throws(IOException::class)
        fun writeToOutput(output: OutputStream, data: String) {

            for (i in 0 until data.length) {
                output.write(data[i].toInt())
            }

        }

        @Throws(IOException::class)
        fun writeToOutput(output: OutputStream, data: Int) {
            output.write(data shr 0)
            output.write(data shr 8)
            output.write(data shr 16)
            output.write(data shr 24)
        }

        @Throws(IOException::class)
        fun writeToOutput(output: OutputStream, data: Short) {
            output.write(data.toInt() shr 0)
            output.write(data.toInt() shr 8)
        }

        @Throws(IOException::class)
        fun copy(source: InputStream, output: OutputStream): Long {
            return copy(source, output, TRANSFER_BUFFER_SIZE)
        }

        @Throws(IOException::class)
        fun copy(source: InputStream, output: OutputStream, bufferSize: Int): Long {
            var read = 0L
            val buffer = ByteArray(bufferSize)
            var n: Int
            while (source.read(buffer).also { n = it } != -1) {
                output.write(buffer, 0, n)
                read += n.toLong()
            }
            return read
        }
    }

}