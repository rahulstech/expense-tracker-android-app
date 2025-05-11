package dreammaker.android.expensetracker.ui.util

import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import dreammaker.android.expensetracker.R

fun showAddHistoryButtons(buttonsParent: View) {
    val anim = AnimationUtils.loadAnimation(buttonsParent.context, R.anim.anim_show_add_history_buttons)
    anim.setAnimationListener(object : AnimationListener {
        override fun onAnimationStart(p0: Animation?) {
            // show buttons on animation start
            buttonsParent.visible()
        }
        override fun onAnimationEnd(p0: Animation?) {}
        override fun onAnimationRepeat(p0: Animation?) {}
    })
    // stop ongoing animation
    buttonsParent.clearAnimation()
    // make it invisible, thus view bounds will be available
    buttonsParent.invisible()
    // now start the animation
    buttonsParent.startAnimation(anim)
}

fun hideAddHistoryButtons(buttonsParent: View) {
    val anim = AnimationUtils.loadAnimation(buttonsParent.context,R.anim.anim_hide_add_history_buttons).apply {
        setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(p0: Animation?) {}
            override fun onAnimationRepeat(p0: Animation?) {}
            override fun onAnimationEnd(p0: Animation?) {
                // hide buttons on animation end
                buttonsParent.visibilityGone()
            }
        })
    }
    buttonsParent.clearAnimation()
    buttonsParent.startAnimation(anim)
}

fun toggleAddButtonButtons(buttonsParent: View) {
    if (buttonsParent.isVisible()) {
        hideAddHistoryButtons(buttonsParent)
    }
    else {
        showAddHistoryButtons(buttonsParent)
    }
}