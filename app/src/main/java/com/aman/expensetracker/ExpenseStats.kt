package com.aman.expensetracker

import com.aman.expensetracker.data.Expense
import java.math.BigDecimal
import java.util.Calendar

// Small helpers for the numbers on the main screen. Kept free of Android code so they can be unit tested.

fun totalOf(expenses: List<Expense>): Double = expenses.sumOf { it.amount }

fun isSameMonth(first: Long, second: Long): Boolean {
    val a = Calendar.getInstance().apply { timeInMillis = first }
    val b = Calendar.getInstance().apply { timeInMillis = second }
    return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.MONTH) == b.get(Calendar.MONTH)
}

fun monthTotal(expenses: List<Expense>, now: Long = System.currentTimeMillis()): Double =
    totalOf(expenses.filter { isSameMonth(it.date, now) })

// Category name -> total spent, biggest first, leaving out empty categories
fun categoryTotals(expenses: List<Expense>): List<Pair<String, Double>> =
    expenses.groupBy { it.category }
        .map { (category, items) -> category to totalOf(items) }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }

// Returns the amount if the text is a valid number above zero, otherwise null
fun parseAmount(text: String): Double? {
    val amount = text.trim().toDoubleOrNull() ?: return null
    return if (amount > 0 && amount.isFinite()) amount else null
}

// Shows a saved amount in the edit field without a pointless ".0", e.g. 250.0 -> "250"
fun amountForInput(amount: Double): String =
    BigDecimal.valueOf(amount).stripTrailingZeros().toPlainString()
