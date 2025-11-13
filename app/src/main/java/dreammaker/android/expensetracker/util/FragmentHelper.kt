package dreammaker.android.expensetracker.util

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import dreammaker.android.expensetracker.Constants

fun Fragment.setActivitySubTitle(title: CharSequence) {
    val activity = activity ?: return
    if (activity is AppCompatActivity) {
        if (null == activity.supportActionBar) {
            activity.actionBar?.subtitle = title
        }
        else {
            activity.supportActionBar?.subtitle = title
        }
    }
    else {
        activity.actionBar?.subtitle = title
    }
}

fun Fragment.hasArgument(key: String): Boolean = arguments?.containsKey(key) == true

fun Fragment.getArgAction(defaultAction: String? = null): String? = arguments?.getString(Constants.ARG_ACTION) ?: defaultAction

fun Fragment.isActionEdit(): Boolean = getArgAction() == Constants.ACTION_EDIT

fun Fragment.getArgId(defaultValue: Long = 0): Long {
    val id = arguments?.getLong(Constants.ARG_ID)
    if (null==id) {
        return defaultValue
    }
    return id
}