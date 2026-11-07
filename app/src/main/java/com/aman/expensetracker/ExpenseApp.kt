package com.aman.expensetracker

import android.app.Application
import com.aman.expensetracker.data.ExpenseDatabase
import com.aman.expensetracker.data.ExpenseRepository

class ExpenseApp : Application() {

    // One repository for the whole app, created the first time it's needed
    val repository by lazy {
        ExpenseRepository(ExpenseDatabase.getInstance(this).expenseDao())
    }
}
