package tg.sdk.sca.core.utils

import android.os.Build
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_YEAR
import java.util.concurrent.TimeUnit

object TgBobfSdkDateUtils {

    @Suppress("MemberVisibilityCanBePrivate")
    val TIME_ZONE_UTC: TimeZone = TimeZone.getTimeZone("UTC")

    const val TRANSACTION_SERVER_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssX"
    private const val TRANSACTION_SERVER_DATE_REGEX =
        "\\d{4}-[01]\\d-\\d{2}T[0-2]\\d:[0-6]\\d:[0-6]\\dZ$"

    enum class DateFormat(val dateFormat: String) {
        TRANSACTION_SERVER_DATE(TRANSACTION_SERVER_DATE_FORMAT),
        TRANSACTION_SERVER_DATE_TIME_ZONE("yyyy-MM-dd'T'HH:mm:ssXXX"),
        TRANSACTION_SERVER_DATE_TIME_ZONE_PRE_API_24("yyyy-MM-dd'T'HH:mm:ssZ"),
        TRANSACTION_SERVER_DATE_UTC("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        TRANSACTION_REFERENCE("ddMMyyHHmmss"),
        TRANSACTION_STATUS("d MMMM yyyy 'at' h:mm aaa"),
        HISTORY("H:mm d MMM")
    }

    fun formatDate(
        format: DateFormat,
        date: Long,
        locale: Locale = Locale.ENGLISH,
        timeZone: TimeZone? = null
    ): String = formatDate(format, Date(date), locale, timeZone)

    fun formatDate(
        format: DateFormat,
        date: Date,
        locale: Locale = Locale.ENGLISH,
        timeZone: TimeZone? = null
    ): String = getDateFormat(format, locale, timeZone).format(date)

    fun parseDate(
        format: DateFormat,
        date: String,
        locale: Locale = Locale.ENGLISH,
        timeZone: TimeZone? = null
    ): Long? = runCatching {
        getDateFormat(format, locale, timeZone).parse(date)?.time
    }.onFailure {
        it.printStackTrace()
    }.getOrNull()

    private fun getDateFormat(
        format: DateFormat,
        locale: Locale = Locale.ENGLISH,
        timeZone: TimeZone? = null
    ) = SimpleDateFormat(format.dateFormat, locale).apply {
        timeZone?.let { this.timeZone = it }
    }

//    fun getCurrentMonthDateRange(): Pair<String, String> {
//        val rangeInMillis = getCurrentMonthDateRangeMillis()
//        val start = formatDate(DateFormat.TRANSACTION_SERVER_DATE, rangeInMillis.first)
//        val end = formatDate(DateFormat.TRANSACTION_SERVER_DATE, rangeInMillis.second)
//        return Pair(start, end)
//    }

    fun matchesFormat(date: String): Boolean {
        return date.matches(TRANSACTION_SERVER_DATE_REGEX.toRegex())
    }

//    fun getCurrentMonthDateRangeMillis(timeZone: TimeZone? = null): Pair<Long, Long> {
//        val start: Long
//        val end: Long
//        val calendar = Calendar.getInstance()
//        if (timeZone != null) {
//            calendar.timeZone = timeZone
//        }
//        calendar.set(
//            Calendar.DAY_OF_MONTH,
//            calendar.getActualMinimum(Calendar.DAY_OF_MONTH)
//        )
//        start = calendar.toDayStart().timeInMillis
//        calendar.set(
//            Calendar.DAY_OF_MONTH,
//            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
//        )
//
//        end = calendar.toDayEnd().timeInMillis
//        return Pair(start, end)
//    }

    fun parseConsentDate(date: String, preText: String = ""): CharSequence? {
        val timeInMillis = parseDate(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                DateFormat.TRANSACTION_SERVER_DATE_TIME_ZONE
            } else {
                DateFormat.TRANSACTION_SERVER_DATE_TIME_ZONE_PRE_API_24
            },
            date,
            Locale.getDefault()
        )
        timeInMillis?.let {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return preText + formatDate(
                DateFormat.TRANSACTION_STATUS,
                calendar.time,
                Locale.getDefault()
            )
        }
        return null
    }

    fun parseConsentCreateDate(date: String, preText: String = ""): CharSequence? {
        val timeInMillis = parseDate(
            DateFormat.TRANSACTION_SERVER_DATE_UTC,
            date,
            Locale.getDefault(),
            TimeZone.getTimeZone("UTC")
        )
        timeInMillis?.let {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = timeInMillis
            return preText + formatDate(
                DateFormat.TRANSACTION_STATUS,
                calendar.time,
                Locale.getDefault()
            )
        }
        return null
    }

    sealed class DateLeft(open val value: Int) {
        data class Years(override val value: Int) : DateLeft(value)
        data class Months(override val value: Int) : DateLeft(value)
        data class Weeks(override val value: Int) : DateLeft(value)
        data class Days(override val value: Int) : DateLeft(value)
    }

    fun getFormattedDateLeft(startDate: Long, plannedDate: Long): DateLeft {
        val yearsBetween = yearsBetweenDates(startDate, plannedDate)
        val monthBetween = monthsBetweenDates(startDate, plannedDate)
        val weeksBetween = weeksBetweenDates(startDate, plannedDate)
        val daysBetween = daysBetween(startDate, plannedDate)
        return when {
            yearsBetween > 0 -> DateLeft.Years(yearsBetween)
            monthBetween > 0 -> DateLeft.Months(monthBetween)
            weeksBetween > 0 -> DateLeft.Weeks(weeksBetween)
            else -> DateLeft.Days(daysBetween)
        }
    }

    private fun yearsBetweenDates(startDate: Long, endDate: Long): Int {
        val rangePair: Pair<Calendar, Calendar> = toStartEndPair(startDate, endDate)
        if (rangePair.second.timeInMillis <= rangePair.first.timeInMillis) {
            return 0
        }

        val yearDiff = rangePair.second.get(Calendar.YEAR) - rangePair.first.get(Calendar.YEAR)
        if (yearDiff > 1 ||
            (yearDiff == 1 && (rangePair.second.get(Calendar.MONTH) >= rangePair.first.get(Calendar.MONTH)))
        ) {
            return yearDiff
        }
        return 0
    }

    private fun monthsBetweenDates(startDate: Long, endDate: Long): Int {
        val rangePair: Pair<Calendar, Calendar> = toStartEndPair(startDate, endDate)
        if (rangePair.second.timeInMillis <= rangePair.first.timeInMillis) {
            return 0
        }

        val sameYear = rangePair.first.get(Calendar.YEAR) == rangePair.second.get(Calendar.YEAR)
        val monthDiff = rangePair.second.get(Calendar.MONTH) - rangePair.first.get(Calendar.MONTH)
        return if (sameYear) {
            monthDiff
        } else {
            val yearsBetween = yearsBetweenDates(startDate, endDate)
            monthDiff + 12 * yearsBetween - 1
        }
    }

    private fun weeksBetweenDates(startDate: Long, endDate: Long): Int {
        val rangePair: Pair<Calendar, Calendar> = toStartEndPair(startDate, endDate)
        if (rangePair.second.timeInMillis <= rangePair.first.timeInMillis) {
            return 0
        }

        return (TimeUnit.MILLISECONDS.toDays(rangePair.second.timeInMillis - rangePair.first.timeInMillis) / 7).toInt()
    }

    /**
     * @return pair where first - start date reseted to start of a day, second - end date
     * reseted to start of a day
     */
    private fun toStartEndPair(startDate: Long, endDate: Long): Pair<Calendar, Calendar> {
        val start = Calendar.getInstance()
        start.timeInMillis = startDate
        val end = Calendar.getInstance()
        end.timeInMillis = endDate
        return Pair(start.setTimeToBeginningOfDay(), end.setTimeToBeginningOfDay())
    }

    private fun daysBetween(startDate: Long, endDate: Long): Int {
        val rangePair: Pair<Calendar, Calendar> = toStartEndPair(startDate, endDate)
        if (rangePair.second.timeInMillis <= rangePair.first.timeInMillis) {
            return 0
        }

        return (TimeUnit.MILLISECONDS.toDays(rangePair.second.timeInMillis - rangePair.first.timeInMillis)).toInt()
    }

    fun isLeapYear(calendar: Calendar): Boolean {
        val cal = Calendar.getInstance()
        return cal.getActualMaximum(DAY_OF_YEAR) > 365
    }
}

fun Calendar.setTimeToBeginningOfDay(timeZone: TimeZone? = null): Calendar {
    if (timeZone != null) {
        this.timeZone = timeZone
    }
    apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return this
}

fun Calendar.setTimeToEndOfDay(timeZone: TimeZone? = null): Calendar {
    if (timeZone != null) {
        this.timeZone = timeZone
    }
    apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    return this
}
