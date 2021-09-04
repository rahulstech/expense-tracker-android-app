package dreammaker.android.expensetracker.activity;

import androidx.appcompat.app.AppCompatActivity;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.OnBackPressListener;

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    private OnBackPressListener onBackPressListener;

    public void registerOnBackPressListener(OnBackPressListener listener){
        this.onBackPressListener = listener;
    }

    public void unregisterOnBackPressListener(OnBackPressListener listener) {
        if (onBackPressListener == listener) {
            onBackPressListener = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (Check.isNull(onBackPressListener) || !onBackPressListener.onBackPressed()) {
            super.onBackPressed();
        }
    }
}
