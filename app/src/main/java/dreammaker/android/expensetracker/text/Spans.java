package dreammaker.android.expensetracker.text;

import android.graphics.Typeface;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

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

    public static Object relativeSize(float percentage) {
        return new RelativeSizeSpan(percentage);
    }
}
