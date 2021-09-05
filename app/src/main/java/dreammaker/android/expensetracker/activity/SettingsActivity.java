package dreammaker.android.expensetracker.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;
import dreammaker.android.expensetracker.R;
import dreammaker.android.expensetracker.backup.AutoDeleteWork;
import dreammaker.android.expensetracker.backup.LocalBackupWork;
import dreammaker.android.expensetracker.util.AppExecutor;
import dreammaker.android.expensetracker.util.Check;
import dreammaker.android.expensetracker.util.Date;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    private static final String KEY_AUTO_DELETE = "auto_delete";
    private static final String KEY_NEXT_AUTO_DELETE_DATE = "next_auto_delete_date";

    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (pref,key) -> {
        if ("auto_delete".equals(key)) {
            setNextAutoDeleteDate(SettingsActivity.this);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings,new SettingsHomeFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    public static class SettingsHomeFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.app_settings,rootKey);
        }
    }

    public static String getAutoDeleteDuration(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getString(KEY_AUTO_DELETE,"never");
    }

    public static Date getNextAutoDeleteDate(Context context) {
        String date = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .getString(KEY_NEXT_AUTO_DELETE_DATE,null);
        if (null != date && !"".equals(date)) {
            return Date.valueOf(date,Date.ISO_DATE_PATTERN);
        }
        return null;
    }

    public static void setNextAutoDeleteDate(Context context) {
        int months = -1;
        switch (getAutoDeleteDuration(context)) {
            case "one_month": months = 1;
                break;
            case "three_month": months = 3;
                break;
            case "six_month": months = 6;
                break;
            case "one_year": months = 12;
        }
        Date date = null;
        if (months > 0) {
            date = new Date().firstDateOfNNextMonths(months);
        }
        PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .edit()
                .putString(KEY_NEXT_AUTO_DELETE_DATE,null == date ? null : date.format(Date.ISO_DATE_PATTERN))
                .apply();
    }

    public static void setAutoDeleteDuration(Context context, String newDuration) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext())
                .edit()
                .putString(KEY_AUTO_DELETE,newDuration);
        if (editor.commit()) {
            setNextAutoDeleteDate(context);
        }
    }
}