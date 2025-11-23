package dreammaker.android.expensetracker.util

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import dreammaker.android.expensetracker.Constants

fun Fragment.setActivityTitle(@StringRes titleRes: Int) {
    setActivityTitle(getString(titleRes))
}

fun Fragment.setActivityTitle(title: CharSequence) {
    activity?.let { it.title = title }
}

fun Fragment.setActivitySubTitle(title: CharSequence?) {
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

fun Fragment.getNavigationCurrentSavedStateHandle(navController: NavController = findNavController()): SavedStateHandle? =
    navController.currentBackStackEntry?.savedStateHandle

fun Fragment.hasArgument(key: String): Boolean = arguments?.containsKey(key) == true

fun Fragment.getArgAction(defaultAction: String? = null): String? = arguments?.getString(Constants.ARG_ACTION) ?: defaultAction

fun Fragment.isActionEdit(): Boolean = getArgAction() == Constants.ACTION_EDIT

fun Fragment.getArgId(defaultValue: Long = 0L): Long {
    val id = arguments?.getLong(Constants.ARG_ID)
    if (null==id) {
        return defaultValue
    }
    return id
}