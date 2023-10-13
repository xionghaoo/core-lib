package xh.rabbit.core.utils

import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class DateUtil private constructor() {

    companion object {
        private var INSTANCE: DateUtil? = null

        fun instance(): DateUtil {
            if (INSTANCE == null) {
                INSTANCE = DateUtil()
            }
            return INSTANCE!!
        }

    }

    private var datePattern: String = "yyyy-MM-dd"
    private var timePattern: String = "HH:mm:ss"
    private var minutePattern: String = "mm:ss"
    private var hourPattern: String = "HH:mm"

    private var dateParserPattern = "yyyy-MM-dd HH:mm:ss"

    private var locale = Locale.CHINA

    private val dateParser by lazy { SimpleDateFormat(dateParserPattern, locale) }
    private val dateFormatter by lazy { SimpleDateFormat(datePattern, locale) }
    private val timeFormatter by lazy { SimpleDateFormat(timePattern, locale) }
    private val minuteFormatter by lazy { SimpleDateFormat(minutePattern, locale) }
    private val hourFormatter by lazy { SimpleDateFormat(hourPattern, locale) }

    fun setDateParserPattern(pattern: String): DateUtil {
        dateParserPattern = pattern
        return this
    }

    fun setDateFormatterPattern(pattern: String): DateUtil {
        datePattern = pattern
        return this
    }

    fun setTimeFormatterPattern(pattern: String): DateUtil {
        timePattern = pattern
        return this
    }

    fun setMinuteFormatterPattern(pattern: String): DateUtil {
        minutePattern = pattern
        return this
    }

    fun setHourFormatterPattern(pattern: String): DateUtil {
        hourPattern = pattern
        return this
    }

    // 格式yyyy-MM-dd
    fun yesterday(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -1)
        return dateFormatter.format(cal.time)
    }

    // 格式yyyy-MM-dd
    fun today(): String = dateFormatter.format(Date())/*"2019-06-19"*/

    fun currentMonth(): DateRange {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -cal.get(Calendar.DATE) + 1)
        val start = dateFormatter.format(cal.time)
        val end = dateFormatter.format(Calendar.getInstance().time)
        return DateRange(start, end)
    }

    /**
     * 最近30天
     */
    fun lastThirtyDays(): DateRange {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -30)
        val start = dateFormatter.format(cal.time)
        val end = dateFormatter.format(Calendar.getInstance().time)
        return DateRange(start, end)
    }

    /**
     * 当前时间戳格式化
     * 格式yyyy-MM-dd HH:mm:ss
     */
    fun now(): String = dateParser.format(Date())

    fun nowTime(): String = timeFormatter.format(Date())

    fun toDate(d: String?): String? {
        return try {
            dateFormatter.format(dateParser.parse(d))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun toTime(d: String?): String? {
        if (d == null) return null
        return try {
            timeFormatter.format(dateParser.parse(d))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun toTime(date: Date): String? {
        return try {
            timeFormatter.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun toMinute(d: String?): String? {
        if (d == null) return null

        return try {
            minuteFormatter.format(dateParser.parse(d))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun toHour(d: String?): String? {
        if (d == null) return null

        return try {
            hourFormatter.format(dateParser.parse(d))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseDate(date: String?): Date? {
        if (date == null) return null

        return try {
            dateFormatter.parse(date)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseTime(d: String?): Date? {
        if (d == null) return null

        return try {
            dateParser.parse(d)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun formatToDate(d: Date?): String? {
        return try {
            dateFormatter.format(d)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    class DateRange(var start: String, var end: String)
}