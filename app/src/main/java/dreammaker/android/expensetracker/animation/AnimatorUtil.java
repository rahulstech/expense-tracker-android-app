package dreammaker.android.expensetracker.animation;

import android.animation.Animator;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewAnimationUtils;

import java.util.Objects;

import dreammaker.android.expensetracker.util.ResourceUtil;

public class AnimatorUtil {

    public static final int SHORT_ANIM_DURATION = 200;

    public static final int LONG_ANIM_DURATION = 400;

    private AnimatorUtil() {}

    public static Animator circularReveal(View anchor, View target, boolean show) {
        Objects.requireNonNull(anchor,"anchor view is null");
        Objects.requireNonNull(target,"target view is null");

        float maxDimension = ResourceUtil.getDisplayMaxDimension(anchor.getResources());
        Rect rect = new Rect();
        anchor.getGlobalVisibleRect(rect);
        int centerX = rect.centerX();
        int centerY = rect.centerY();
        float startRadius, endRadius;
        if (show) {
            startRadius = 0;
            endRadius = maxDimension;
        }
        else {
            startRadius = maxDimension;
            endRadius = 0;
        }
        return ViewAnimationUtils.createCircularReveal(target,centerX,centerY,startRadius,endRadius);
    }
}
