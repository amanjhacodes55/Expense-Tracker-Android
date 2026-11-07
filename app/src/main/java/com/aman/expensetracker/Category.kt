package com.aman.expensetracker

// Step 2: the expense categories, each with its own label, icon and colour
enum class Category(val label: Int, val icon: Int, val color: Int) {
    FOOD(R.string.category_food, R.drawable.ic_food, R.color.category_food),
    TRAVEL(R.string.category_travel, R.drawable.ic_travel, R.color.category_travel),
    SHOPPING(R.string.category_shopping, R.drawable.ic_shopping, R.color.category_shopping),
    BILLS(R.string.category_bills, R.drawable.ic_bills, R.color.category_bills),
    OTHER(R.string.category_other, R.drawable.ic_other, R.color.category_other);

    companion object {
        fun from(name: String): Category = entries.firstOrNull { it.name == name } ?: OTHER
    }
}
