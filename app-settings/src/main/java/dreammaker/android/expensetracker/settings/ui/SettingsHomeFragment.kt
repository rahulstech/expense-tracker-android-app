package dreammaker.android.expensetracker.settings.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import dreammaker.android.expensetracker.settings.R

class SettingsHomeFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_settings, rootKey)
    }
}