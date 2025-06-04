package dreammaker.android.expensetracker.util

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

fun View.isVisible(): Boolean = visibility == View.VISIBLE

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.isInvisible(): Boolean = visibility == View.INVISIBLE

fun View.visibilityGone() {
    visibility = View.GONE
}

fun View.isVisibilityGone(): Boolean = visibility == View.GONE