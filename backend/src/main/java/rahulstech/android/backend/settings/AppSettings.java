package rahulstech.android.backend.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

@SuppressWarnings("unused")
public class AppSettings {

    private static final String SHARED_PREFERENCE_NAME = "expense_tracker_app_settings";

    private static final String KEY_PERSON_NAME_ORIENTATION = "person_name_orientation";

    private static final String KEY_HISTORY_MONTH = "history_month";

    public static final int FIRST_NAME_FIRST = 1;

    public static final int LAST_NAME_FIRST = 2;

    public static final int HISTORY_MONTH_3 = 1;

    public static final int HISTORY_MONTH_6 = 2;

    public static final int HISTORY_MONTH_12 = 3;

    @SuppressWarnings("FieldCanBeLocal")
    private final Context mContext;

    private final SharedPreferences mSpf;

    private AppSettings(Context context) {
        mContext = context;
        mSpf = context.getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    public static final AppSettings get(Context context) {
        Objects.requireNonNull(context.getApplicationContext(),"context = null");
        return new AppSettings(context);
    }

    public int getPreferredPersonNameOrientation() {
        return getInt(KEY_PERSON_NAME_ORIENTATION,FIRST_NAME_FIRST);
    }

    public void setPreferredPersonNameOrientation(int orientation) {
        set(KEY_PERSON_NAME_ORIENTATION,orientation);
    }

    public int getShowHistoriesOfMonths() {
        return getInt(KEY_HISTORY_MONTH,HISTORY_MONTH_6);
    }

    public void setShowHistoriesOfMonths(int months) {
        set(KEY_HISTORY_MONTH,months);
    }

    private void set(String key, String value) {
        mSpf.edit().putString(key,value).apply();
    }

    private void set(String key, int value) {
        mSpf.edit().putInt(key,value).apply();
    }

    private String getString(String key, String ifNull) {
        return mSpf.getString(key, ifNull);
    }

    private int getInt(String key, int ifNull) {
        return mSpf.getInt(key, ifNull);
    }
}
