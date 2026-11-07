package com.aman.expensetracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    // Stored as the Category enum name, e.g. "FOOD"
    val category: String,
    // Date of the expense in millis (start of that day, local time)
    val date: Long,
    val note: String = ""
)
