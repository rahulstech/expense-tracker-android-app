package dreammaker.android.expensetracker.viewmodel;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

public class SavedStateViewModel extends ViewModel {

    private SavedStateHandle mHandle;

    public SavedStateViewModel(@NonNull SavedStateHandle handle) {
        this.mHandle = handle;
    }

    public void put(String key, boolean value) {
        mHandle.set(key,value);
    }

    public void put(String key, int value) {
        mHandle.set(key,value);
    }

    public void put(String key, long value) {
        mHandle.set(key,value);
    }

    public void put(String key, String value) {
        mHandle.set(key,value);
    }

    public void put(String key, ArrayList<Long> value) {
        mHandle.set(key,value);
    }

    public void put(String key, Parcelable value) {
        mHandle.set(key,value);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Boolean value = mHandle.get(key);
        return null != value ? value.booleanValue() : defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        Integer value = mHandle.get(key);
        return null != value ? value.intValue() : defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        Long value = mHandle.get(key);
        return null != value ? value.longValue() : defaultValue;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public int getInt(String key) {
        return getInt(key,0);
    }


    public long getLong(String key) {
        return getLong(key,0);
    }

    @Nullable
    public String getString(String key) {
        return mHandle.get(key);
    }

    @Nullable
    public List<Long> getLongList(String key) {
        return mHandle.get(key);
    }

    @Nullable
    public Parcelable getParcelable(String key) {
        return mHandle.get(key);
    }
}
