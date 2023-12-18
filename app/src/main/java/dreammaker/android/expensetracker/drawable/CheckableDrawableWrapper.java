package dreammaker.android.expensetracker.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.Checkable;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.util.ResourceUtil;

@SuppressWarnings("unused")
public class CheckableDrawableWrapper extends Drawable implements Checkable {

    private static final String TAG = CheckableDrawableWrapper.class.getSimpleName();

    private final Drawable mDrawable;

    private final Drawable mCheckMark;

    private boolean mChecked = false;

    public CheckableDrawableWrapper(Context context, Drawable drawable) {
        Objects.requireNonNull(context,"given null Context");
        Objects.requireNonNull(drawable,"given null Drawable");
        mDrawable = drawable;
        mCheckMark = ResourceUtil.getDrawable(context, R.drawable.ic_baseline_check_circle_24);
        int tint = ResourceUtil.getThemeColor(context,R.attr.colorAccent);
        DrawableCompat.setTint(mCheckMark,tint);
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        Rect bound = getBounds();
        mDrawable.setBounds(bound);
       mCheckMark.setBounds(bound);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (mChecked) {
            mCheckMark.draw(canvas);
        }
        else {
            mDrawable.draw(canvas);
        }
    }

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setChecked(boolean checked) {
        final boolean oldState = mChecked;
        mChecked = checked;
        if (oldState != checked) {
            invalidateSelf();
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }
}
