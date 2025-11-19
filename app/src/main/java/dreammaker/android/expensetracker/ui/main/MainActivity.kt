package dreammaker.android.expensetracker.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import dreammaker.android.expensetracker.R
import dreammaker.android.expensetracker.databinding.MainBinding
import dreammaker.android.expensetracker.settings.ui.AppSettingsActivity
import dreammaker.android.expensetracker.util.visibilityGone
import dreammaker.android.expensetracker.util.visible
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import rahulstech.android.expensetracker.backuprestore.ui.BackupRestoreActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val cabVm: ContextualActionBarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {

        // installSplashScreen before calling parent onCreate. this method must be called to apply post theme
        // properly otherwise app will crash
        // it returns splashScreen; use it for showing splashscreen for longer time and any other customization
        // if there is no such requirement then do nothing,
         installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        navController = binding.navHostFragmentContainer.getFragment<NavHostFragment>().navController
        appBarConfiguration = AppBarConfiguration.Builder(R.id.home)
            .setOpenableLayout(binding.drawerLayout)
            .build()
        binding.drawer.setNavigationItemSelectedListener(this::onClickLeftDrawerItem)
        setupActionBarWithNavController(this, navController, appBarConfiguration)

        val packageInfo = packageManager.getPackageInfo(packageName, 0)
        val appVersionView = binding.drawer.getHeaderView(0).findViewById<TextView>(R.id.app_version)
        appVersionView.text = packageInfo.versionName

        prepareContextualToolbar()
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
            R.id.menu_app_data -> {
                startActivity(Intent(this, BackupRestoreActivity::class.java))
                true
            }
            R.id.menu_settings -> {
                startActivity(Intent(this, AppSettingsActivity::class.java))
                true
            }
            else -> false
        }
    }

    private fun prepareContextualToolbar() {
        lifecycleScope.launch {
            cabVm.cabStartState.collectLatest { started ->
                if (started) {
                    binding.toolbar.visibilityGone()
                    binding.contextualToolbar.apply {
                        cabVm.cabMenu?.let { menu ->
                            addMenuProvider(menu)
                        }
                        visible()
                    }
                }
                else {
                    binding.contextualToolbar.apply {
                        visibilityGone()
                        cabVm.cabMenu?.let { menu ->
                            removeMenuProvider(menu)
                        }
                    }
                    binding.toolbar.visible()
                }
            }
        }

        lifecycleScope.launch {
            cabVm.cabTitleState.collectLatest { title ->
                binding.contextualToolbar.title = title
            }
        }

        binding.contextualToolbar.setNavigationOnClickListener {
            cabVm.endContextActionBar()
        }
    }
}