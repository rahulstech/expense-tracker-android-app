package dreammaker.android.expensetracker.drawable;

import android.graphics.drawable.Drawable;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import dreammaker.android.expensetracker.text.TextUtil;

@SuppressWarnings("unused")
public class DrawableUtil {

    private DrawableUtil() {}

    public static Drawable getAccountDefaultLogo(String name) {
        int color = ColorGenerator.MATERIAL.getColor(name);
        String text = TextUtil.getDisplayLabel(name);
        return TextDrawable.builder()
                .beginConfig().toUpperCase().endConfig()
                .buildRound(text,color);
    }

    public static Drawable getPersonDefaultPhoto(String firstName, String lastName, boolean firstNameFirst) {
        if (!firstNameFirst) {
            return getPersonDefaultPhoto(lastName,firstName,true);
        }
        String displayName = TextUtil.getDisplayNameForPerson(firstName,lastName,firstNameFirst,null);
        String text = TextUtil.getDisplayLabelForPerson(firstName,lastName,firstNameFirst,null);
        int color = ColorGenerator.MATERIAL.getColor(displayName);
        return TextDrawable.builder()
                .beginConfig().toUpperCase().endConfig()
                .buildRound(text,color);
    }
}
