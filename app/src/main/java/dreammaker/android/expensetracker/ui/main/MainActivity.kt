package dreammaker.android.expensetracker.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.activity.BackupTestActivity
import dreammaker.android.expensetracker.databinding.MainBinding
import dreammaker.android.expensetracker.settings.SettingsActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.actionBar.toolbar)
        navController = binding.navHostFragmentContainer.getFragment<NavHostFragment>().navController
        appBarConfiguration = AppBarConfiguration.Builder(R.id.home)
            .setOpenableLayout(binding.drawerLayout)
            .build()
        binding.drawer.setNavigationItemSelectedListener(this::onClickLeftDrawerItem)
        setupActionBarWithNavController(this, navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(binding.drawer)) {
            binding.drawerLayout.closeDrawer(binding.drawer)
        }
        else {
            super.onBackPressed()
        }
    }

    private fun onClickLeftDrawerItem(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(binding.drawer)
        return when(item.itemId) {
            R.id.backup_restore -> {
//                startActivity(Intent(this@MainActivity, BackupRestoreActivity::class.java))
                startActivity(Intent(this@MainActivity, BackupTestActivity::class.java))
                true
            }
            R.id.settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> false
        }
    }
}