package dreammaker.android.expensetracker.ui.util

import android.view.View

fun View.enable() {
    isEnabled = true
}

fun View.disable() {
    isEnabled = false
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.visibilityGone() {
    visibility = View.GONE
}