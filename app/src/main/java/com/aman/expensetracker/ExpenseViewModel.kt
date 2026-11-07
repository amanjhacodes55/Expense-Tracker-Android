package com.aman.expensetracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.aman.expensetracker.data.Expense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as ExpenseApp).repository

    // null means "All"
    private val filter = MutableStateFlow<Category?>(null)
    val currentFilter: Category? get() = filter.value

    // Every expense, used for the totals and the category breakdown
    val allExpenses: LiveData<List<Expense>> = repository.expenses.asLiveData()

    // Only the expenses in the selected category, for the list
    val visibleExpenses: LiveData<List<Expense>> =
        repository.expenses.combine(filter) { expenses, category ->
            if (category == null) expenses else expenses.filter { it.category == category.name }
        }.asLiveData()

    fun setFilter(category: Category?) {
        filter.value = category
    }

    // These are suspend functions so the screen can wait for the write to finish (e.g. before closing)

    suspend fun getExpense(id: Long): Expense? = repository.getExpense(id)

    suspend fun save(expense: Expense) = repository.save(expense)

    suspend fun delete(expense: Expense) = repository.delete(expense)

    suspend fun restore(expense: Expense) = repository.insert(expense)
}
