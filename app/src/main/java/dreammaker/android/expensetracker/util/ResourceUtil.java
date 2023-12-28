package dreammaker.android.expensetracker.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

@SuppressWarnings("unused")
public class ResourceUtil {

    private ResourceUtil() {}

    public static float dpToPixed(Resources res, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,
                res.getDisplayMetrics());
    }

    @ColorInt
    public static int getColor(Context context, @ColorRes int value) {
        return ResourcesCompat.getColor(context.getResources(),value,context.getTheme());
    }

    public static ColorStateList getColorStateList(Context context, @ColorRes int value) {
        return ResourcesCompat.getColorStateList(context.getResources(),value,context.getTheme());
    }

    public static Drawable getDrawable(Context context, @DrawableRes int res) {
        return ResourcesCompat.getDrawable(context.getResources(),res,context.getTheme());
    }

    @ColorInt
    public static int getThemeColor(Context context, @AttrRes int attr) {
        TypedValue value = obtainTypedValue(context,attr);
        return ContextCompat.getColor(context,value.resourceId);
    }

    public static ColorStateList getThemeColorStateList(Context context, @AttrRes int attr) {
        TypedValue value = obtainTypedValue(context,attr);
        return ContextCompat.getColorStateList(context,value.resourceId);
    }

    public static float getDisplayMaxDimension(Resources res) {
        float height = res.getDisplayMetrics().heightPixels;
        float width = res.getDisplayMetrics().widthPixels;
        return Math.max(height,width);
    }

    static TypedValue obtainTypedValue(Context context, @AttrRes int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr,value,true);
        return value;
    }
}
