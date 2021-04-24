package dreammaker.android.expensetracker.util;

import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.udojava.evalex.Expression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import dreammaker.android.expensetracker.R;

public class CalculatorKeyboard {

    private static final String TAG = "CalculatorKeyboard";

    private KeyboardView keyboardView;
    private List<EditText> editTexts;
    private EditText editText;
    private CallbackWrapper callback = new CallbackWrapper();

    public CalculatorKeyboard( KeyboardView keyboardView) {
        Check.isNonNull(keyboardView, "keyboard view is null");
        this.keyboardView = keyboardView;
        this.editTexts = new ArrayList<>();
        keyboardView.setPreviewEnabled(false);
        keyboardView.setKeyboard(new Keyboard(keyboardView.getContext(), R.xml.calculator_keyboard_keys));
        keyboardView.setOnKeyboardActionListener(keyboardActionListener);
    }

    public void registerCallback(Callback callback) {
        this.callback.warp(callback);
    }

    public void unregisterCallback(Callback callback) {this.callback.unwrap(callback); }

    public void registerEditText(EditText... editTexts) {
        Check.isNonEmptyArray(editTexts, "no edit text provided");
        this.editTexts.addAll(Arrays.asList(editTexts));
        for (EditText et : this.editTexts) {
            et.setFocusable(false);
            et.setOnTouchListener(touchListener);
        }
    }

    public void unregisterEditText(EditText editText) {
        Check.isNonNull(editText,"cann't unregister null EditText");
        if (this.editTexts.remove(editText)) {
            editText.setOnTouchListener(null);
            if (this.editText == editText)
                setCurrentEditText(null);
        }
    }

    public boolean onBackPressed() {
        if (isKeyboardVisible()) {
            toggleKeyboardVisibility(false);
            return true;
        }
        return false;
    }

    public float calculate(Editable editable) {
        String expression = editable.toString();
        callback.onBeforeCalculate(editText,expression);
        if (Check.isEmptyString(expression)) {
            callback.onAfterCalculate(editText,expression,0);
            return 0;
        }
        expression = expression.replace("x","*")
                .replace("÷", "/");
        Expression exp = new Expression(expression);

        try {
            float result = exp.eval(true).floatValue();
            Log.d(TAG, expression + " = " + result);
            editable.clear();
            editable.append(Helper.floatToString(result));
            callback.onAfterCalculate(editText, expression, result);
            return result;
        } catch (Throwable ex) {
            callback.onError(editText, expression, ex);
            throw ex;
        }
    }

    private boolean isKeyboardVisible() {
        return View.VISIBLE == keyboardView.getVisibility();
    }

    private void toggleKeyboardVisibility(boolean makeVisible) {
        if (makeVisible) {
            hideSoftInput();
            keyboardView.setVisibility(View.VISIBLE);
        }
        else {
            try {
                if (null != editText)
                    calculate(editText.getText());
            }catch (Exception ignore) {}
            keyboardView.setVisibility(View.INVISIBLE);
        }
    }

    private void hideSoftInput() {
        if (null == editText) return;
        ((InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private View.OnTouchListener touchListener = (v,e) -> {
        setCurrentEditText((EditText) v);
        toggleKeyboardVisibility(true);
        return false;
    };

    private void setCurrentEditText(@NonNull EditText editText) {
        this.editText = editText;
    }

    private OnKeyboardActionListener keyboardActionListener = new OnKeyboardActionListener() {
        @Override
        public void onPress(int primaryCode) {}

        @Override
        public void onRelease(int primaryCode) {}

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            if (null == editText) return;
            Editable editable = editText.getText();
            int selection = editText.getSelectionStart();
            if (61 == primaryCode) {
                try {
                    calculate(editable);
                } catch (Exception e) {
                    Log.e(TAG, "Error during calculate: " + e.getMessage());
                }
            } else if (67 == primaryCode)
                editable.clear();
            else if (127 == primaryCode) {
                final int start = editText.getSelectionStart();
                if (start > 0) {
                    editable.delete(start - 1, start);
                }
            } else {
                if (selection >= 0)
                    editable.insert(selection, String.valueOf((char) primaryCode));
                else editable.append((char) primaryCode);
            }
        }

        @Override
        public void onText(CharSequence text) {}

        @Override
        public void swipeLeft() {}

        @Override
        public void swipeRight() {}

        @Override
        public void swipeDown() {}

        @Override
        public void swipeUp() {}
    };

    private static class CallbackWrapper implements Callback {

        List<Callback> callbacks = new ArrayList<>();

        public void warp(Callback callback) {
            if (null != callback) {
                callbacks.add(callback);
            }
        }

        public void unwrap(Callback callback) {
            if (null != callback) {
                callbacks.remove(callback);
            }
        }

        @Override
        public void onBeforeCalculate(EditText which, String text) {
            for (Callback c : callbacks) {
                c.onBeforeCalculate(which, text);
            }
        }

        @Override
        public void onAfterCalculate(EditText which, String text, float result) {
            for (Callback c : callbacks) {
                c.onAfterCalculate(which, text, result);
            }
        }

        @Override
        public void onError(EditText which, String text, Throwable error) {
            for (Callback c : callbacks) {
                c.onError(which, text, error);
            }
        }
    }

    public interface Callback {

        void onBeforeCalculate(EditText which, String text);

        void onAfterCalculate(EditText which, String text, float result);

        void onError(EditText which, String text, Throwable error);
    }

    public static class SimpleCallback implements Callback {

        @Override
        public void onBeforeCalculate(EditText which, String text) {}

        @Override
        public void onAfterCalculate(EditText which, String text, float result) {}

        @Override
        public void onError(EditText which, String text, Throwable error) { }
    }
}
