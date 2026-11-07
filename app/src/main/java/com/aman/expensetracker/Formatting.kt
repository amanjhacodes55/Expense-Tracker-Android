package com.aman.expensetracker

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Indian rupees, e.g. ₹1,250.00
private val rupeeFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-IN"))

fun formatRupees(amount: Double): String = rupeeFormat.format(amount)

// e.g. 30 Oct 2026
fun formatDate(millis: Long): String =
    SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(millis))
