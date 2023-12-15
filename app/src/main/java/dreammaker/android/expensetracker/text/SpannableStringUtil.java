package dreammaker.android.expensetracker.text;

import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

@SuppressWarnings("unused")
public class SpannableStringUtil {

    private SpannableStringBuilder builder;

    public SpannableStringUtil() {
        this(null);
    }

    public SpannableStringUtil(CharSequence text) {
        builder = new SpannableStringBuilder();
        append(text);
    }

    public SpannableStringUtil append(CharSequence text) {
        if (!TextUtils.isEmpty(text)) {
            builder.append(text);
        }
        return this;
    }

    public SpannableStringUtil append(CharSequence text, Object span) {
        if (TextUtils.isEmpty(text)) {
            return this;
        }
        final int spanEnd = text.length();
        return append(text,span,0,spanEnd);
    }

    public SpannableStringUtil append(CharSequence text, Object span, int spanStart, int spanEnd) {
        return append(text,span,spanStart,spanEnd,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    public SpannableStringUtil append(CharSequence text, Object span, int spanStart, int spanEnd, int spanFlag) {
        return append(text, new Object[]{span}, spanStart, spanEnd, spanFlag);
    }

    public SpannableStringUtil append(CharSequence text, Object[] spans, int spanStart, int spanEnd) {
       return append(text, spans, spanStart, spanEnd,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    }

    public SpannableStringUtil append(CharSequence text, Object[] spans, int spanStart, int spanEnd, int spanFlag) {
        if (null == spans || spans.length == 0) {
            throw new IllegalArgumentException("no span given");
        }
        SpannableString spanned = new SpannableString(text);
        for (Object span : spans) {
            spanned.setSpan(span, spanStart, spanEnd, spanFlag);
        }
        builder.append(spanned);
        return this;
    }

    public SpannableStringUtil setSpan(Object span, int spanStart, int spanEnd, int spanFlag) {
        if (null == span) {
            throw new NullPointerException("null span given");
        }
        int spanLen = spanEnd-spanStart;
        if (builder.length() < spanLen) {
            throw new IllegalArgumentException("span range is more than text length");
        }
        builder.setSpan(span,spanStart,spanEnd,spanFlag);
        return this;
    }

    public SpannableString toSpannableString() {
        return new SpannableString(builder);
    }
}
