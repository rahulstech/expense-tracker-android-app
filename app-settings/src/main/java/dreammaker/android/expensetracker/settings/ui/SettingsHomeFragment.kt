package dreammaker.android.expensetracker.settings.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dreammaker.android.expensetracker.settings.R
import dreammaker.android.expensetracker.settings.SettingsProvider

class SettingsHomeFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsProvider.NAME
        preferenceManager.sharedPreferencesMode = SettingsProvider.MODE
        setPreferencesFromResource(R.xml.app_settings, rootKey)
    }
}