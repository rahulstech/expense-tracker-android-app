package dreammaker.android.expensetracker.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import dreammaker.android.expensetracker.settings.databinding.SettingsActivityBinding

class SettingsHomeFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_settings, rootKey)
    }
}

class SettingsActivity: AppCompatActivity() {
    private val TAG = "SettingsActivity"
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
}
