package dreammaker.android.expensetracker.util;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Objects;

import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.math.Calculator;

public class CalculatorKeyboard {

    private static final String TAG = "CalculatorKeyboard";

//    private KeyboardView keyboardView;
    private EditText editText;
    private Activity activity;
    private Calculator calculator = new Calculator();

    public CalculatorKeyboard(Activity activity, EditText editText) {
//        Check.isNonNull(activity, "activity is null");
        Objects.requireNonNull(activity, "activity is null");
        this.activity = activity;
//        this.keyboardView = activity.findViewById(R.id.calculator_keyboard);
        this.editText = editText;
        editText.setFocusable(false);
        editText.setOnTouchListener(touchListener);
//        keyboardView.setPreviewEnabled(false);
//        keyboardView.setKeyboard(new Keyboard(keyboardView.getContext(), R.xml.calculator_keyboard_keys));
//        keyboardView.setOnKeyboardActionListener(keyboardActionListener);
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
        try {
            float result = calculator.calculate(expression).floatValue();
            Log.d(TAG, expression + " = " + result);
            editable.clear();
            editable.append(String.valueOf(result));
            return result;
        } catch (Throwable ex) {
            throw ex;
        }
    }

    public void hideCalculatorKeyboard() {
        toggleKeyboardVisibility(false);
    }

    private boolean isKeyboardVisible() {
//        return View.VISIBLE == keyboardView.getVisibility();
        return false;
    }

    private void toggleKeyboardVisibility(boolean makeVisible) {
//        if (makeVisible) {
//            keyboardView.setVisibility(View.VISIBLE);
//        }
//        else {
//            try {
//                if (null != editText)
//                    calculate(editText.getText());
//            }catch (Exception ignore) {}
//            keyboardView.setVisibility(View.INVISIBLE);
//        }
    }

    private View.OnTouchListener touchListener = (v,e) -> {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        if (null != activity.getCurrentFocus()) activity.getCurrentFocus().clearFocus();
        toggleKeyboardVisibility(true);
        return false;
    };

    private OnKeyboardActionListener keyboardActionListener = new OnKeyboardActionListener() {
        @Override
        public void onPress(int primaryCode) {}

        @Override
        public void onRelease(int primaryCode) {}

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            if (null == editText) return;
            Editable editable = editText.getText();
            if (61 == primaryCode) {
                try {
                    calculate(editable);
                } catch (Exception e) {
                    Log.e(TAG, "Error during calculate: " + e.getMessage());
                }
            }
            else if (67 == primaryCode)
                editable.clear();
            else {
                if ("0".equals(editable.toString())) {
                    editable.clear();
                }
                editable.append((char) primaryCode);
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
}
