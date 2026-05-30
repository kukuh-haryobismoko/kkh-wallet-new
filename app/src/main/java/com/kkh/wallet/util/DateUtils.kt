package com.kkh.wallet.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val FMT_DATE = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
    private val FMT_DATE_SHORT = SimpleDateFormat("dd MMM", Locale("in", "ID"))
    private val FMT_TIME = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val FMT_MONTH = SimpleDateFormat("MMMM yyyy", Locale("in", "ID"))
    private val FMT_DAY = SimpleDateFormat("EEEE, dd MMM yyyy", Locale("in", "ID"))

    fun formatDate(millis: Long): String = FMT_DATE.format(Date(millis))
    fun formatDateShort(millis: Long): String = FMT_DATE_SHORT.format(Date(millis))
    fun formatTime(millis: Long): String = FMT_TIME.format(Date(millis))
    fun formatMonth(millis: Long): String = FMT_MONTH.format(Date(millis))
    fun formatDay(millis: Long): String = FMT_DAY.format(Date(millis))

    /** Inclusive start-of-day for [millis]. */
    fun startOfDay(millis: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    /** Exclusive end-of-day for [millis]. */
    fun endOfDay(millis: Long): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59);      set(Calendar.MILLISECOND, 999)
        }
        return c.timeInMillis
    }

    fun startOfMonth(millis: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);      set(Calendar.MILLISECOND, 0)
        }
        return c.timeInMillis
    }

    fun endOfMonth(millis: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59);      set(Calendar.MILLISECOND, 999)
        }
        return c.timeInMillis
    }

    /** Adds [months] months to [millis]. Negative values move backwards. */
    fun addMonths(millis: Long, months: Int): Long {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        c.add(Calendar.MONTH, months)
        return c.timeInMillis
    }

    /** Returns the year for the given timestamp. */
    fun yearOf(millis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.YEAR)
    }

    /** Returns the month index (0-based) for the given timestamp. */
    fun monthOf(millis: Long): Int {
        val c = Calendar.getInstance().apply { timeInMillis = millis }
        return c.get(Calendar.MONTH)
    }
}
