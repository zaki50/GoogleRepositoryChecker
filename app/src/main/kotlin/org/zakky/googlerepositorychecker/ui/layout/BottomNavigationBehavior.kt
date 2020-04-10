package org.zakky.googlerepositorychecker.ui.layout

import android.content.Context
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.snackbar.Snackbar
import android.util.AttributeSet
import android.view.View

@Suppress("unused")
// used from layout
class BottomNavigationBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<BottomNavigationView>(context, attrs) {

    private var isSnackbarShowing = false
    private var snackbar: Snackbar.SnackbarLayout? = null

    override fun layoutDependsOn(parent: CoordinatorLayout, child: BottomNavigationView, dependency: View): Boolean {
        return dependency is AppBarLayout || dependency is Snackbar.SnackbarLayout
    }

    override fun onStartNestedScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView, directTargetChild: View, target: View, axes: Int, type: Int): Boolean {
        return true
    }

    override fun onNestedPreScroll(coordinatorLayout: CoordinatorLayout, child: BottomNavigationView, target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (isSnackbarShowing) {
            if (snackbar != null) {
                updateSnackbarPaddingByBottomNavigationView(child)
            }
        }
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
    }

    override fun onDependentViewChanged(parent: CoordinatorLayout, child: BottomNavigationView, dependency: View): Boolean {
        if (dependency is AppBarLayout) {
            val appbar = dependency
            val bottom = appbar.bottom.toFloat()
            val height = appbar.height.toFloat()
            val hidingRate = (height - bottom) / height
            child.translationY = child.height * hidingRate
            return true
        }
        if (dependency is Snackbar.SnackbarLayout) {
            if (isSnackbarShowing) return true
            isSnackbarShowing = true
            snackbar = dependency
            updateSnackbarPaddingByBottomNavigationView(child)
            return true
        }
        return false
    }

    override fun onDependentViewRemoved(parent: CoordinatorLayout, child: BottomNavigationView, dependency: View) {
        if (dependency is Snackbar.SnackbarLayout) {
            isSnackbarShowing = false
            snackbar = null
        }
        super.onDependentViewRemoved(parent, child, dependency)
    }

    private fun updateSnackbarPaddingByBottomNavigationView(view: BottomNavigationView) {
        if (snackbar != null) {
            val bottomTranslate = (view.height - view.translationY).toInt()
            snackbar!!.setPadding(snackbar!!.paddingLeft, snackbar!!.paddingTop, snackbar!!.paddingRight, bottomTranslate)
            snackbar!!.requestLayout()
        }
    }
}