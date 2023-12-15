package dreammaker.android.expensetracker.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

@SuppressWarnings("unused")
public class ResourceUtil {

    private ResourceUtil() {}

    @ColorInt
    public static int getColor(Context context, @ColorRes int res) {
        return ResourcesCompat.getColor(context.getResources(),res,context.getTheme());
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

    static TypedValue obtainTypedValue(Context context, @AttrRes int attr) {
        TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(attr,value,true);
        return value;
    }
}
