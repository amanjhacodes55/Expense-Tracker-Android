package com.aman.expensetracker.data

import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val dao: ExpenseDao) {

    val expenses: Flow<List<Expense>> = dao.getAll()

    suspend fun getExpense(id: Long): Expense? = dao.getById(id)

    // New expenses have id 0, so Room generates one; anything else is an edit
    suspend fun save(expense: Expense) {
        if (expense.id == 0L) dao.insert(expense) else dao.update(expense)
    }

    // Also used to put back an expense after Undo, keeping its old id
    suspend fun insert(expense: Expense) {
        dao.insert(expense)
    }

    suspend fun delete(expense: Expense) = dao.delete(expense)
}
