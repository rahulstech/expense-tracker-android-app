package dreammaker.android.expensetracker.settings

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import dreammaker.android.expensetracker.settings.databinding.SettingsActivityBinding

class SettingsHomeFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_settings, rootKey)
    }
}

class SettingsActivity: AppCompatActivity() {
    private val TAG = "SettingsActivity"

    private val KEY_AUTO_DELETE = "auto_delete"
    private val KEY_NEXT_AUTO_DELETE_DATE = "next_auto_delete_date"
//
//    private val preferenceChangeListener =
//        OnSharedPreferenceChangeListener { pref: SharedPreferences?, key: String? ->
//            if ("auto_delete" == key) {
//                setNextAutoDeleteDate(this@SettingsActivity)
//            }
//        }

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.actionBar.toolbar)
        supportFragmentManager.beginTransaction().replace(R.id.settings, SettingsHomeFragment())
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean = true

//    protected override fun onResume() {
//        super.onResume()
//        PreferenceManager.getDefaultSharedPreferences(this)
//            .registerOnSharedPreferenceChangeListener(preferenceChangeListener)
//    }
//
//    protected override fun onPause() {
//        super.onPause()
//        PreferenceManager.getDefaultSharedPreferences(this)
//            .unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
//    }



    fun getAutoDeleteDuration(context: Context): String? {
        return PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
            .getString(KEY_AUTO_DELETE, "never")
    }

//    fun getNextAutoDeleteDate(context: Context): Date? {
//        val date = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
//            .getString(KEY_NEXT_AUTO_DELETE_DATE, null)
//        if (null != date && "" != date) {
//            return Date.valueOf(date, Date.ISO_DATE_PATTERN)
//        }
//        return null
//    }

//    fun setNextAutoDeleteDate(context: Context) {
//        var months = -1
//        when (getAutoDeleteDuration(context)) {
//            "one_month" -> months = 1
//            "three_month" -> months = 3
//            "six_month" -> months = 6
//            "one_year" -> months = 12
//        }
//        var date: Date? = null
//        if (months > 0) {
//            date = Date().firstDateOfNNextMonths(months)
//        }
//        PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
//            .edit()
//            .putString(
//                KEY_NEXT_AUTO_DELETE_DATE,
//                date?.format(Date.ISO_DATE_PATTERN)
//            )
//            .apply()
//    }

//    fun setAutoDeleteDuration(context: Context, newDuration: String?) {
//        val editor = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
//            .edit()
//            .putString(KEY_AUTO_DELETE, newDuration)
//        if (editor.commit()) {
//            setNextAutoDeleteDate(context)
//        }
//    }
}
