package xh.rabbit.core_lib

import org.junit.Test

import org.junit.Assert.*
import xh.rabbit.core.isChinese
import xh.rabbit.core.isEnglish

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        print("${"中文".isEnglish()}")
    }
}