package com.aman.expensetracker

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.aman.expensetracker.data.Expense
import com.aman.expensetracker.databinding.ItemExpenseBinding

class ExpenseAdapter(
    private val onClick: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseAdapter.ExpenseViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: Expense) {
            val context = binding.root.context
            val category = Category.from(expense.category)

            binding.categoryIcon.setImageResource(category.icon)
            binding.categoryIcon.backgroundTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, category.color))
            binding.titleText.text = expense.title
            binding.detailText.text = context.getString(
                R.string.expense_details,
                context.getString(category.label),
                formatDate(expense.date)
            )
            binding.amountText.text = formatRupees(expense.amount)
            binding.root.setOnClickListener { onClick(expense) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Expense>() {
        override fun areItemsTheSame(oldItem: Expense, newItem: Expense) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Expense, newItem: Expense) = oldItem == newItem
    }
}
