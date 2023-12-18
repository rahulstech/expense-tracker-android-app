package dreammaker.android.expensetracker.activity;

import android.app.Activity;
import android.util.Log;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import dreammaker.android.expensetracker.BuildConfig;

@SuppressWarnings("unused")
public class ActivityModel {

    private static final String TAG = ActivityModel.class.getSimpleName();

    private final Activity mActivity;

    private Toolbar mToolbar;

    private ArrayDeque<OnBackPressedCallback> mOnBackPressedCallbacks;

    public ActivityModel(Activity activity) {
        Objects.requireNonNull(activity,"null activity given");
        mActivity = activity;
    }

    public void setSupportToolbar(Toolbar toolbar) {
        mToolbar = toolbar;
    }

    @Nullable
    public Toolbar getSupportToolbar() {
        return mToolbar;
    }

    public void addOnBackPressedCallback(LifecycleOwner owner, OnBackPressedCallback callback) {
        Objects.requireNonNull(owner,"given LifecycleOwner is null");
        Objects.requireNonNull(callback,"given OnBackPressedCallback is null");
        if (owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
            return;
        }
        if (null == mOnBackPressedCallbacks) {
            mOnBackPressedCallbacks = new ArrayDeque<>();
        }
        mOnBackPressedCallbacks.add(new LifecycleAwareOnBackPressedCallback(owner.getLifecycle(), callback));
    }

    public void removeOnBackPressedCallback(OnBackPressedCallback callback) {
        Objects.requireNonNull(callback,"given OnBackPressedCallback is null");
        if (null != mOnBackPressedCallbacks) {
            mOnBackPressedCallbacks.remove(callback);
        }
    }

    public boolean onBackPressed() {
        if (null != mOnBackPressedCallbacks) {
            Iterator<OnBackPressedCallback> callbacks = mOnBackPressedCallbacks.descendingIterator();
            while (callbacks.hasNext()) {
                OnBackPressedCallback callback = callbacks.next();
                if (callback.onBackPressed()) {
                    return true;
                }
            }
        }
        return false;
    }

    public interface OnBackPressedCallback {

        boolean onBackPressed();
    }

    private class LifecycleAwareOnBackPressedCallback implements LifecycleEventObserver,OnBackPressedCallback {

        private final Lifecycle mLifecycle;

        private final OnBackPressedCallback mCallback;

        public LifecycleAwareOnBackPressedCallback(Lifecycle lifecycle, OnBackPressedCallback callback) {
            mLifecycle = lifecycle;
            mCallback = callback;
            lifecycle.addObserver(this);
        }

        public boolean isDestroyed() {
            return mLifecycle.getCurrentState() == Lifecycle.State.DESTROYED;
        }

        public boolean isActive() {
            return mLifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED);
        }

        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                mLifecycle.removeObserver(this);
                removeOnBackPressedCallback(this);
            }
        }

        @Override
        public boolean onBackPressed() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG,"onBackPressed: isActive="+isActive());
            }
            return isActive() && mCallback.onBackPressed();
        }
    }
}
