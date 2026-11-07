package com.aman.expensetracker

import com.aman.expensetracker.data.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExpenseStatsTest {

    private fun dateOf(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            clear()
            set(year, month, day)
        }.timeInMillis

    private fun expense(amount: Double, category: String = "FOOD", date: Long = dateOf(2026, Calendar.OCTOBER, 10)) =
        Expense(title = "Test", amount = amount, category = category, date = date)

    @Test
    fun totalOf_addsAllAmounts() {
        val expenses = listOf(expense(120.0), expense(80.5), expense(1000.0))
        assertEquals(1200.5, totalOf(expenses), 0.001)
    }

    @Test
    fun totalOf_emptyListIsZero() {
        assertEquals(0.0, totalOf(emptyList()), 0.0)
    }

    @Test
    fun isSameMonth_checksMonthAndYear() {
        val oct10 = dateOf(2026, Calendar.OCTOBER, 10)
        assertTrue(isSameMonth(oct10, dateOf(2026, Calendar.OCTOBER, 31)))
        assertFalse(isSameMonth(oct10, dateOf(2026, Calendar.NOVEMBER, 1)))
        assertFalse(isSameMonth(oct10, dateOf(2025, Calendar.OCTOBER, 10)))
    }

    @Test
    fun monthTotal_onlyCountsTheGivenMonth() {
        val expenses = listOf(
            expense(500.0, date = dateOf(2026, Calendar.OCTOBER, 1)),
            expense(250.0, date = dateOf(2026, Calendar.OCTOBER, 30)),
            expense(900.0, date = dateOf(2026, Calendar.SEPTEMBER, 30)),
            expense(100.0, date = dateOf(2025, Calendar.OCTOBER, 15))
        )
        val now = dateOf(2026, Calendar.OCTOBER, 30)
        assertEquals(750.0, monthTotal(expenses, now), 0.001)
    }

    @Test
    fun categoryTotals_groupsAndSortsBiggestFirst() {
        val expenses = listOf(
            expense(100.0, "FOOD"),
            expense(50.0, "FOOD"),
            expense(400.0, "TRAVEL"),
            expense(20.0, "BILLS")
        )
        val totals = categoryTotals(expenses)
        assertEquals(listOf("TRAVEL" to 400.0, "FOOD" to 150.0, "BILLS" to 20.0), totals)
    }

    @Test
    fun parseAmount_acceptsPositiveNumbers() {
        assertEquals(250.0, parseAmount("250")!!, 0.0)
        assertEquals(99.5, parseAmount(" 99.50 ")!!, 0.0)
        assertEquals(0.5, parseAmount(".5")!!, 0.0)
    }

    @Test
    fun parseAmount_rejectsEmptyZeroAndNegative() {
        assertNull(parseAmount(""))
        assertNull(parseAmount("abc"))
        assertNull(parseAmount("0"))
        assertNull(parseAmount("-20"))
    }

    @Test
    fun amountForInput_dropsUselessZeros() {
        assertEquals("250", amountForInput(250.0))
        assertEquals("99.5", amountForInput(99.5))
        assertEquals("100", amountForInput(100.0))
        assertEquals("0.1", amountForInput(0.1))
    }
}
