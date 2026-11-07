package com.aman.expensetracker

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DatesTest {

    private lateinit var originalZone: TimeZone

    // Run in India's time zone so the UTC conversions actually move the time
    @Before
    fun setUp() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    @Test
    fun startOfDay_clearsTheTime() {
        val evening = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, 30, 21, 45, 10)
        }.timeInMillis
        val midnight = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, 30)
        }.timeInMillis
        assertEquals(midnight, startOfDay(evening))
    }

    @Test
    fun utcAndLocalConversion_keepsTheSameDay() {
        val localDay = Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.OCTOBER, 30)
        }.timeInMillis

        val utcDay = localDayToUtc(localDay)
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcDay }
        assertEquals(30, utc.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, utc.get(Calendar.HOUR_OF_DAY))

        assertEquals(localDay, utcDayToLocal(utcDay))
    }
}
