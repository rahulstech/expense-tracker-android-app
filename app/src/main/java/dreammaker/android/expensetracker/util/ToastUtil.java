package dreammaker.android.expensetracker.util;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

@SuppressWarnings("unused")
public class ToastUtil {

    private static final int MESSAGE = 1;

    private static final int SUCCESS = 2;

    private static final int ERROR = 3;

    private ToastUtil() {}

    public static void showMessageShort(@NonNull Context context, @StringRes int text) {
        showMessage(context,text,Toast.LENGTH_SHORT);
    }

    public static void showMessage(@NonNull Context context, @StringRes int text, int duration) {
        showMessage(context,context.getText(text),duration);
    }

    public static void showMessageShort(@NonNull Context context, CharSequence text) {
        showMessage(context,text,Toast.LENGTH_SHORT);
    }

    public static void showMessage(@NonNull Context context, CharSequence text, int duration) {
        show(context,text,duration,MESSAGE);
    }

    public static void showSuccessShort(@NonNull Context context, @StringRes int text) {
        showSuccess(context,text,Toast.LENGTH_SHORT);
    }

    public static void showSuccess(@NonNull Context context, @StringRes int text, int duration) {
        showSuccess(context,context.getText(text),duration);
    }

    public static void showSuccessShort(@NonNull Context context, CharSequence text) {
        showSuccess(context,text,Toast.LENGTH_SHORT);
    }

    public static void showSuccess(@NonNull Context context, CharSequence text, int duration) {
        show(context,text,duration,SUCCESS);
    }

    public static void showErrorShort(@NonNull Context context, @StringRes int text) {
        showError(context,text,Toast.LENGTH_SHORT);
    }

    public static void showErrorShort(@NonNull Context context, CharSequence text) {
        showError(context,text,Toast.LENGTH_SHORT);
    }

    public static void showError(@NonNull Context context, @StringRes int text, int duration) {
        showError(context,context.getText(text),duration);
    }

    public static void showError(@NonNull Context context, CharSequence text, int duration) {
        show(context,text,duration,ERROR);
    }

    private static void show(Context context, CharSequence text, int duration, int type) {
        Toast.makeText(context, text, duration).show();
    }
}
