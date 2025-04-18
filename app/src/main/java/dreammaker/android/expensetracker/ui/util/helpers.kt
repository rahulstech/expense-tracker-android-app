package dreammaker.android.expensetracker.ui.util

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

val ARG_DESTIATION_LABEL = "arg.destination_label"

fun Fragment.setActivityTitle(title: CharSequence) {
    val activity = activity ?: return
    if (activity is AppCompatActivity) {
        if (null == activity.supportActionBar) {
            activity.title = title
        }
        else {
            activity.supportActionBar?.title = title
        }
    }
    else {
        activity.title = title
    }
}