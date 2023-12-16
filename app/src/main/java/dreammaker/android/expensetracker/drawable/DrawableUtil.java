package dreammaker.android.expensetracker.drawable;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import androidx.annotation.ColorInt;
import dreammaker.android.expensetracker.text.TextUtil;

@SuppressWarnings("unused")
public class DrawableUtil {

    @ColorInt
    public static final int DEFAULT_COLOR = Color.parseColor("#78909c");

    private DrawableUtil() {}

    public static Drawable getAccountDefaultLogo(String name) {
        if (null == name) {
            return getDrawableUnknown();
        }
        int color = ColorGenerator.MATERIAL.getColor(name);
        String text = TextUtil.getDisplayLabelForAccount(name);
        return TextDrawable.builder()
                .beginConfig().toUpperCase().endConfig()
                .buildRound(text,color);
    }

    public static Drawable getPersonDefaultPhoto(String firstName, String lastName, boolean firstNameFirst) {
        if (!firstNameFirst) {
            return getPersonDefaultPhoto(lastName,firstName,true);
        }
        String displayName = TextUtil.getDisplayNameForPerson(firstName,lastName,firstNameFirst,null);
        if (null == displayName) {
            return getDrawableUnknown();
        }
        String text = TextUtil.getDisplayLabelForPerson(firstName,lastName,firstNameFirst);
        int color = ColorGenerator.MATERIAL.getColor(displayName);
        return TextDrawable.builder()
                .beginConfig().toUpperCase().endConfig()
                .buildRound(text,color);
    }

    public static Drawable getDrawableUnknown() {
        return TextDrawable.builder()
                .beginConfig().toUpperCase().endConfig()
                .buildRound("?",DEFAULT_COLOR);
    }

    public static Drawable getCircularDrawable(int color) {
        ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
        drawable.setTint(color);
        return drawable;
    }
}
