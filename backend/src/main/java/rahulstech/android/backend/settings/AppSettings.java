package rahulstech.android.backend.settings;

import android.content.Context;
import android.content.SharedPreferences;

import java.time.LocalDate;
import java.util.Objects;

@SuppressWarnings("unused")
public class AppSettings {

    private static final String SHARED_PREFERENCE_NAME = "expense_tracker_app_settings";

    private static final String KEY_PERSON_NAME_ORIENTATION = "person_name_orientation";

    private static final String KEY_HISTORY_MONTH = "history_month";

    private static final String KEY_HISTORY_GROUPING = "history_grouping";

    public static final int FIRST_NAME_FIRST = 1;

    public static final int LAST_NAME_FIRST = 2;

    public static final int HISTORY_MONTH_3 = 1;

    public static final int HISTORY_MONTH_6 = 2;

    public static final int HISTORY_MONTH_12 = 3;

    public static final int GROUP_DAILY = 1;

    public static final int GROUP_MONTHLY = 2;

    @SuppressWarnings("FieldCanBeLocal")
    private final Context mContext;

    private final SharedPreferences mSpf;

    private AppSettings(Context context) {
        mContext = context;
        mSpf = context.getSharedPreferences(SHARED_PREFERENCE_NAME,Context.MODE_PRIVATE);
    }

    public static AppSettings get(Context context) {
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

    /**
     *
     * @param start
     * @return
     */
    public LocalDate getShowHistoryDateEnd(LocalDate start) {
        final int months = getShowHistoriesOfMonths();
        LocalDate end;
        if (months == AppSettings.HISTORY_MONTH_3) {
            end = start.minusMonths(3);
        }
        else if (months == AppSettings.HISTORY_MONTH_6) {
            end = start.minusMonths(6);
        }
        else {
            end = start.minusMonths(12);
        }
        return end;
    }

    /**
     *
     * @param start
     * @return
     */
    public LocalDate[] getShowHistoryDateRange(LocalDate start) {
        final int months = getShowHistoriesOfMonths();
        LocalDate end;
        if (months == AppSettings.HISTORY_MONTH_3) {
            end = start.minusMonths(3);
        }
        else if (months == AppSettings.HISTORY_MONTH_6) {
            end = start.minusMonths(6);
        }
        else {
            end = start.minusMonths(12);
        }
        LocalDate endOfMonth = start.withDayOfMonth(start.getMonth().length(start.isLeapYear()));
        LocalDate startOfMonth = end.withDayOfMonth(1);
        return new LocalDate[]{startOfMonth,endOfMonth};
    }

    public void setShowHistoriesOfMonths(int months) {
        set(KEY_HISTORY_MONTH,months);
    }

    public int getHistoryGrouping() {
        return getInt(KEY_HISTORY_GROUPING,GROUP_DAILY);
    }

    public void setHistoryGrouping(int value) {
        set(KEY_HISTORY_GROUPING, value);
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
