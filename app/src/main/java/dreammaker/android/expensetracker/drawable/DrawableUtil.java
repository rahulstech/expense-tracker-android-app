package dreammaker.android.expensetracker.drawable;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
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

    @SuppressWarnings("ConstantConditions")
    public static Drawable getPersonDefaultPhoto(String firstName, String lastName, boolean firstNameFirst) {
        if (!firstNameFirst) {
            return getPersonDefaultPhoto(lastName,firstName,true);
        }
        DisplayName displayName = new DisplayName(firstName,lastName);
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

    public static void setSizePixel(@Nullable Drawable drawable, float sizePx) {
        setBounds(drawable,0,0,sizePx,sizePx);
    }

    public static void setBounds(@Nullable Drawable drawable, float left, float top, float right, float bottom) {
        if (null == drawable) {
            return;
        }
        RectF boundF = new RectF(left,top,right,bottom);
        Rect bound = new Rect();
        boundF.round(bound);
        drawable.setBounds(bound);
    }

    public static void removeTintList(Drawable drawable) {
        if (null == drawable) {
            return;
        }
        DrawableCompat.setTintList(drawable,null);
    }

    private static class DisplayName {

        final CharSequence[] names;

        DisplayName(CharSequence... names) {
            this.names = names;
        }

        @Override
        public int hashCode() {
            return Objects.hash((Object[]) names);
        }
    }
}
