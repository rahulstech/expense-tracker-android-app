package dreammaker.android.expensetracker.text;

import android.graphics.Typeface;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import androidx.annotation.ColorInt;

@SuppressWarnings("unused")
public class Spans {

    private Spans() {}

    public static Object textColor(@ColorInt int color) {
        return new ForegroundColorSpan(color);
    }

    public static Object bold() {
        return new StyleSpan(Typeface.BOLD);
    }
}
