package com.aman.expensetracker

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.aman.expensetracker.data.Expense
import com.aman.expensetracker.databinding.ActivityMainBinding
import com.aman.expensetracker.databinding.ItemCategoryTotalBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ExpenseViewModel by viewModels()
    private val adapter = ExpenseAdapter { expense -> openEditor(expense.id) }

    // Filter chip id -> category it shows (the "All" chip isn't in here)
    private val filterChips = mapOf(
        R.id.chipFood to Category.FOOD,
        R.id.chipTravel to Category.TRAVEL,
        R.id.chipShopping to Category.SHOPPING,
        R.id.chipBills to Category.BILLS,
        R.id.chipOther to Category.OTHER
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        binding.expenseList.adapter = adapter
        ItemTouchHelper(SwipeToDeleteCallback(this) { position ->
            deleteWithUndo(adapter.currentList[position])
        }).attachToRecyclerView(binding.expenseList)

        binding.filterChips.setOnCheckedStateChangeListener { _, checkedIds ->
            viewModel.setFilter(filterChips[checkedIds.firstOrNull()])
        }

        binding.addFab.setOnClickListener { openEditor(null) }

        // Step 4: show the history and the totals, they update by themselves when the database changes
        viewModel.allExpenses.observe(this) { expenses -> showSummary(expenses) }
        viewModel.visibleExpenses.observe(this) { expenses ->
            adapter.submitList(expenses)
            binding.emptyState.isVisible = expenses.isEmpty()
            binding.emptyText.setText(
                if (viewModel.currentFilter == null) R.string.empty_all else R.string.empty_filtered
            )
        }
    }

    private fun showSummary(expenses: List<Expense>) {
        binding.totalAmount.text = formatRupees(totalOf(expenses))
        binding.monthAmount.text = formatRupees(monthTotal(expenses))
        binding.expenseCount.text =
            resources.getQuantityString(R.plurals.expense_count, expenses.size, expenses.size)
        showBreakdown(expenses)
    }

    // One row per category with a bar showing its share of the total
    private fun showBreakdown(expenses: List<Expense>) {
        val totals = categoryTotals(expenses)
        binding.breakdownCard.isVisible = totals.isNotEmpty()
        binding.breakdownContainer.removeAllViews()

        val total = totalOf(expenses)
        for ((name, amount) in totals) {
            val category = Category.from(name)
            val color = ContextCompat.getColor(this, category.color)
            val row = ItemCategoryTotalBinding.inflate(layoutInflater, binding.breakdownContainer, true)
            row.categoryDot.backgroundTintList = ColorStateList.valueOf(color)
            row.categoryName.setText(category.label)
            row.categoryAmount.text = formatRupees(amount)
            row.categoryProgress.setIndicatorColor(color)
            row.categoryProgress.progress = (amount / total * 100).toInt().coerceAtLeast(1)
        }
    }

    // Step 5: delete straight away, with an Undo option that puts the expense back
    private fun deleteWithUndo(expense: Expense) {
        lifecycleScope.launch { viewModel.delete(expense) }
        Snackbar.make(binding.root, R.string.expense_deleted, Snackbar.LENGTH_LONG)
            .setAnchorView(binding.addFab)
            .setAction(R.string.undo) {
                lifecycleScope.launch { viewModel.restore(expense) }
            }
            .show()
    }

    private fun openEditor(expenseId: Long?) {
        val intent = Intent(this, AddExpenseActivity::class.java)
        if (expenseId != null) intent.putExtra(AddExpenseActivity.EXTRA_EXPENSE_ID, expenseId)
        startActivity(intent)
    }

    // The app draws behind the status and navigation bars, so pad the views that would end up under them
    private fun applyWindowInsets() {
        val listPadding = binding.scrollView.paddingBottom
        val fabMargin = (binding.addFab.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            root.updatePadding(left = bars.left, right = bars.right)
            binding.appBar.updatePadding(top = bars.top)
            binding.scrollView.updatePadding(bottom = listPadding + bars.bottom)
            binding.addFab.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = fabMargin + bars.bottom
            }
            WindowInsetsCompat.CONSUMED
        }
    }
}
