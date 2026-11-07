package com.aman.expensetracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors

// Swipe a row left or right to delete it. Draws a red background with a bin icon behind the row.
class SwipeToDeleteCallback(
    context: Context,
    private val onSwiped: (position: Int) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val background = GradientDrawable().apply {
        cornerRadius = context.resources.getDimension(R.dimen.card_corner_radius)
        setColor(MaterialColors.getColor(context, androidx.appcompat.R.attr.colorError, 0))
    }
    private val icon = ContextCompat.getDrawable(context, R.drawable.ic_delete)!!.mutate().apply {
        setTint(MaterialColors.getColor(context, com.google.android.material.R.attr.colorOnError, 0))
    }
    private val iconMargin = context.resources.getDimensionPixelSize(R.dimen.swipe_icon_margin)

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ) = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) onSwiped(position)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val item = viewHolder.itemView
        if (dX != 0f) {
            background.setBounds(item.left, item.top, item.right, item.bottom)
            background.draw(c)

            // Put the icon on the side the row is moving away from
            val top = item.top + (item.height - icon.intrinsicHeight) / 2
            val left = if (dX > 0) {
                item.left + iconMargin
            } else {
                item.right - iconMargin - icon.intrinsicWidth
            }
            icon.setBounds(left, top, left + icon.intrinsicWidth, top + icon.intrinsicHeight)
            icon.draw(c)
        }
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}
