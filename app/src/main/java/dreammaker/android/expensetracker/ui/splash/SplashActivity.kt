package dreammaker.android.expensetracker.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.activity.RestoreActivity
import dreammaker.android.expensetracker.backup.BackupRestoreHelper
import dreammaker.android.expensetracker.settings.SettingsProvider
import dreammaker.android.expensetracker.ui.main.MainActivity

class SplashActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        Handler(mainLooper).postDelayed(this::checkFirstRestoreRequired, 300)
    }

    private fun checkFirstRestoreRequired() {
        val settings = SettingsProvider.get(this)
        if (!settings.isFirstRestoreAsked()) {
            BackupRestoreHelper.checkAppFirstRestoreRequired(this) { isRequired ->
                if (isRequired) {
                    startActivityClearingCurrent(RestoreActivity::class.java)
                } else {
                    startActivityClearingCurrent(MainActivity::class.java)
                }
            }
            settings.markFirstRestoreAsked()
        }
        else {
            startActivityClearingCurrent(MainActivity::class.java)
        }
    }

    private fun startActivityClearingCurrent(targetClass: Class<*>) {
        startActivity(Intent(this, targetClass))
        finish()
    }
}