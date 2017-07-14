package org.zakky.googlerepositorychecker.ui.recyclerview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View

class ItemDividerDecoration(context: Context) : RecyclerView.ItemDecoration() {
    companion object {
        private val ATTRS = intArrayOf(
                android.R.attr.listDivider
        )
    }

    private val divider: Drawable

    init {
        val attrs = context.obtainStyledAttributes(ATTRS)
        divider = attrs.getDrawable(0)
        attrs.recycle()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        drawDivider(c, parent);
    }

    private fun drawDivider(c: Canvas, parent: RecyclerView) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0..parent.childCount - 1) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, divider.intrinsicHeight)
    }
}