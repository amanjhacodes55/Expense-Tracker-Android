package com.aman.expensetracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.aman.expensetracker.data.Expense
import com.aman.expensetracker.databinding.ActivityAddExpenseBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

// Step 1 and 5: one screen for adding a new expense and for editing or deleting an existing one
class AddExpenseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddExpenseBinding
    private val viewModel: ExpenseViewModel by viewModels()

    // 0 when adding a new expense
    private var expenseId = 0L
    private var existingExpense: Expense? = null
    private var selectedDate = startOfDay(System.currentTimeMillis())

    private val categoryChips by lazy {
        mapOf(
            Category.FOOD to binding.categoryFood,
            Category.TRAVEL to binding.categoryTravel,
            Category.SHOPPING to binding.categoryShopping,
            Category.BILLS to binding.categoryBills,
            Category.OTHER to binding.categoryOther
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAddExpenseBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()

        expenseId = intent.getLongExtra(EXTRA_EXPENSE_ID, 0L)
        val isEditing = expenseId != 0L

        binding.toolbar.setTitle(if (isEditing) R.string.edit_expense else R.string.add_expense)
        binding.toolbar.setNavigationOnClickListener { finish() }
        if (isEditing) {
            binding.toolbar.inflateMenu(R.menu.menu_edit_expense)
            binding.toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_delete) {
                    confirmDelete()
                    true
                } else {
                    false
                }
            }
            binding.saveButton.setText(R.string.update)
        }

        // The text fields and chips restore themselves after rotation, the date doesn't
        savedInstanceState?.let { selectedDate = it.getLong(KEY_DATE, selectedDate) }
        showDate()

        binding.dateInput.setOnClickListener { pickDate() }
        // If the picker was open before a rotation, listen to it again
        @Suppress("UNCHECKED_CAST")
        (supportFragmentManager.findFragmentByTag(DATE_PICKER_TAG) as? MaterialDatePicker<Long>)
            ?.let { listenToDatePicker(it) }

        binding.titleInput.doAfterTextChanged { binding.titleLayout.error = null }
        binding.amountInput.doAfterTextChanged { binding.amountLayout.error = null }
        binding.categoryGroup.setOnCheckedStateChangeListener { _, _ ->
            binding.categoryError.isVisible = false
        }

        binding.saveButton.setOnClickListener { saveExpense() }

        if (isEditing) loadExpense(fillFields = savedInstanceState == null)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_DATE, selectedDate)
    }

    private fun loadExpense(fillFields: Boolean) {
        lifecycleScope.launch {
            val expense = viewModel.getExpense(expenseId)
            if (expense == null) {
                Toast.makeText(this@AddExpenseActivity, R.string.expense_not_found, Toast.LENGTH_SHORT).show()
                finish()
                return@launch
            }
            existingExpense = expense
            if (fillFields) {
                binding.titleInput.setText(expense.title)
                binding.amountInput.setText(amountForInput(expense.amount))
                binding.noteInput.setText(expense.note)
                categoryChips[Category.from(expense.category)]?.isChecked = true
                selectedDate = expense.date
                showDate()
            }
        }
    }

    private fun showDate() {
        binding.dateInput.setText(formatDate(selectedDate))
    }

    private fun pickDate() {
        if (supportFragmentManager.findFragmentByTag(DATE_PICKER_TAG) != null) return
        val picker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.pick_date)
            .setSelection(localDayToUtc(selectedDate))
            .build()
        listenToDatePicker(picker)
        picker.show(supportFragmentManager, DATE_PICKER_TAG)
    }

    private fun listenToDatePicker(picker: MaterialDatePicker<Long>) {
        picker.addOnPositiveButtonClickListener { selection ->
            selectedDate = utcDayToLocal(selection)
            showDate()
        }
    }

    private fun saveExpense() {
        val title = binding.titleInput.text.toString().trim()
        val amount = parseAmount(binding.amountInput.text.toString())
        val category = categoryChips.entries.firstOrNull { it.value.isChecked }?.key

        // Show every problem at once instead of one at a time
        if (title.isEmpty()) binding.titleLayout.error = getString(R.string.error_title)
        if (amount == null) binding.amountLayout.error = getString(R.string.error_amount)
        binding.categoryError.isVisible = category == null
        if (title.isEmpty() || amount == null || category == null) return

        val expense = Expense(
            id = expenseId,
            title = title,
            amount = amount,
            category = category.name,
            date = selectedDate,
            note = binding.noteInput.text.toString().trim()
        )

        // Step 3: save to the Room database, then go back to the list
        binding.saveButton.isEnabled = false
        lifecycleScope.launch {
            viewModel.save(expense)
            val message = if (expenseId == 0L) R.string.expense_added else R.string.expense_updated
            Toast.makeText(this@AddExpenseActivity, message, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun confirmDelete() {
        val expense = existingExpense ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(getString(R.string.delete_message, expense.title))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                lifecycleScope.launch {
                    viewModel.delete(expense)
                    Toast.makeText(this@AddExpenseActivity, R.string.expense_deleted, Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .show()
    }

    // Keeps the form above the navigation bar and the keyboard
    private fun applyWindowInsets() {
        val formPadding = binding.scrollView.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { root, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
            )
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            root.updatePadding(left = bars.left, right = bars.right)
            binding.appBar.updatePadding(top = bars.top)
            binding.scrollView.updatePadding(bottom = formPadding + maxOf(bars.bottom, keyboard.bottom))
            WindowInsetsCompat.CONSUMED
        }
    }

    companion object {
        const val EXTRA_EXPENSE_ID = "expense_id"
        private const val KEY_DATE = "date"
        private const val DATE_PICKER_TAG = "date_picker"
    }
}
