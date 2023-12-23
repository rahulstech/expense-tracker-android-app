package dreammaker.android.expensetracker.util;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.TextView;

import com.google.android.material.drawable.DrawableUtils;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;

@SuppressWarnings("unused")
public class ViewUtil {

    private ViewUtil() {}

    public static void setTextViewLeftDrawable(@NonNull TextView view, @Nullable Drawable drawable) {
        Objects.requireNonNull(view,"null TextView given");
        int sizePx;
        if (null != drawable) {
            int viewHeight = view.getHeight()-view.getPaddingTop()-view.getPaddingBottom();
            Rect bound = drawable.getBounds();
            int minDimension = Math.min(bound.height(),bound.width());
            if (minDimension > 0) {
                sizePx = Math.min(viewHeight,minDimension);
            }
            else {
                sizePx = viewHeight;
            }
            Log.d("ViewUtil", "setTextViewLeftDrawable: viewHeight="+viewHeight+" drawable="+drawable);
        }
        else {
            sizePx = 0;
        }
        Log.d("ViewUtil", "setTextViewLeftDrawable: sizePx="+sizePx+" drawable="+drawable);
        setTextViewLeftDrawable(view,drawable,sizePx);
    }
    public static void setTextViewLeftDrawableNoTint(@NonNull TextView view, @Nullable Drawable drawable) {
        setTextViewLeftDrawable(view,drawable);
        if (null != drawable) {
            DrawableCompat.setTintList(drawable,null);
        }
    }

    public static void setTextViewLeftDrawable(@NonNull TextView view, @Nullable Drawable drawable, int sizePx) {
        Objects.requireNonNull(view,"null TextView given");
        final Drawable[] drawables = view.getCompoundDrawables();
        final Drawable top = drawables[1];
        final Drawable right = drawables[2];
        final Drawable bottom = drawables[3];
        if (null != drawable) {
            drawable.setBounds(0, 0, sizePx, sizePx);
        }
        view.setCompoundDrawables(drawable,top,right,bottom);
    }

    public static void setTextViewLeftDrawableNoTint(@NonNull TextView view, @Nullable Drawable drawable, int sizePx) {
        setTextViewLeftDrawable(view,drawable,sizePx);
        if (null != drawable) {
            DrawableCompat.setTintList(drawable,null);
        }
    }
}
