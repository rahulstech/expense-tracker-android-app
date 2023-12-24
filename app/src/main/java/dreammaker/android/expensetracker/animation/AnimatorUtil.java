package dreammaker.android.expensetracker.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateInterpolator;

import java.util.Objects;

import dreammaker.android.expensetracker.util.ResourceUtil;

public class AnimatorUtil {

    public static final int SHORT_ANIM_DURATION = 200;

    public static final int LONG_ANIM_DURATION = 400;

    private AnimatorUtil() {}

    public static Animator circularReveal(View anchor, View target, boolean show) {
        // TODO: make reverse animation nicer
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

    public static Animator rotate(View target, float angle) {
        checkTargetViewNonNull(target);
        final float rotation = target.getRotation();
        float from, to;
        if (rotation == 0) {
            from = 0;
            to = angle;
        }
        else {
            from = rotation;
            to = 0;
        }
        return rotate(target,from,to);
    }

    public static Animator rotate(View target, float from, float to) {
        return ObjectAnimator.ofFloat(target,"rotation",from,to);
    }

    public static Animator translateY(View target, float distance) {
        checkTargetViewNonNull(target);
        final float translationY = target.getTranslationY();
        float from, to;
        if (translationY == 0) {
            from = 0;
            to = distance;
        }
        else {
            from = translationY;
            to = 0;
        }
        return translateY(target,from,to);
    }

    public static Animator translateY(View target, float from, float to) {
        return ObjectAnimator.ofFloat(target,"translationY",from,to);
    }

    public static Animator shakeX(View target, float distancePx, int repeat) {
        if (repeat < 0) {
            throw new IllegalArgumentException("repeat must be 0 or a +ve number");
        }
        checkTargetViewNonNull(target);
        ObjectAnimator animator = ObjectAnimator.ofFloat(target,"translationX",distancePx);
        animator.setRepeatCount(repeat);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.setInterpolator(new AccelerateInterpolator());
        return animator;
    }

    private static void checkTargetViewNonNull(View target) {
        Objects.requireNonNull(target,"target View is null");
    }
}
