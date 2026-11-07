package com.aman.expensetracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Expense::class], version = 1, exportSchema = false)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun expenseDao(): ExpenseDao

    companion object {
        @Volatile
        private var instance: ExpenseDatabase? = null

        fun getInstance(context: Context): ExpenseDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expenses.db"
                ).build().also { instance = it }
            }
    }
}
