package com.aman.expensetracker

import java.util.Calendar
import java.util.TimeZone

fun startOfDay(millis: Long): Long =
    Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

// MaterialDatePicker works with midnight in UTC, but expenses are saved with local midnight.
// These two move a date between the two without changing the day.

fun utcDayToLocal(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return sameDayIn(utc, Calendar.getInstance())
}

fun localDayToUtc(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return sameDayIn(local, Calendar.getInstance(TimeZone.getTimeZone("UTC")))
}

private fun sameDayIn(from: Calendar, to: Calendar): Long {
    to.clear()
    to.set(from.get(Calendar.YEAR), from.get(Calendar.MONTH), from.get(Calendar.DAY_OF_MONTH))
    return to.timeInMillis
}
