package dreammaker.android.expensetracker.util;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import dreammaker.android.expensetracker.drawable.DrawableUtil;

@SuppressWarnings("unused")
public class ViewUtil {

    private ViewUtil() {}

    static float getMeasuredHeight(View view) {
        int measuredHeight = view.getMeasuredHeight();
        if (measuredHeight > 0) {
            return measuredHeight;
        }
        int spec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec,spec);
        return view.getMeasuredHeight();
    }

    static float getHeightExcludingPadding(View view) {
        return getMeasuredHeight(view)-view.getPaddingBottom()-view.getPaddingTop();
    }

    public static void setTextViewLeftDrawable(@NonNull TextView view, @Nullable Drawable drawable) {
        Objects.requireNonNull(view,"null TextView given");
        float sizePx;
        if (null != drawable) {
            float viewHeight = getHeightExcludingPadding(view);
            Rect bound = drawable.getBounds();
            int minDimension = Math.min(bound.height(),bound.width());
            if (minDimension > 0) {
                sizePx = Math.min(viewHeight,minDimension);
            }
            else {
                sizePx = viewHeight;
            }
        }
        else {
            sizePx = 0;
        }
        setTextViewLeftDrawable(view,drawable,sizePx);
    }

    public static void setTextViewLeftDrawable(@NonNull TextView view, @Nullable Drawable drawable, float sizePx) {
        Objects.requireNonNull(view,"null TextView given");
        final Drawable[] drawables = view.getCompoundDrawables();
        final Drawable top = drawables[1];
        final Drawable right = drawables[2];
        final Drawable bottom = drawables[3];
        // set compound drawable requires drawable bound to be set
        // so, we need to explicitly set the bound
        DrawableUtil.setSizePixel(drawable,sizePx);
        view.setCompoundDrawables(drawable,top,right,bottom);
    }

    public static void setTextViewLeftDrawableNoTint(@NonNull TextView view, @Nullable Drawable drawable) {
        setTextViewLeftDrawable(view,drawable);
        DrawableUtil.removeTintList(drawable);
    }

    public static void setTextViewLeftDrawableNoTint(@NonNull TextView view, @Nullable Drawable drawable, float sizePx) {
        setTextViewLeftDrawable(view,drawable);
        DrawableUtil.removeTintList(drawable);
    }
}
