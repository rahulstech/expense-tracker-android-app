package dreammaker.android.expensetracker.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.ui.navigation.AppNavigation

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen before calling parent onCreate. this method must be called to apply post theme
        // properly otherwise app will crash
        // it returns splashScreen; use it for showing splashscreen for longer time and any other customization
        // if there is no such requirement then do nothing,
         installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            ExpenseTrackerTheme {
                AppContent()
            }
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
//    }
//
//    override fun onBackPressed() {
//        if (binding.drawerLayout.isDrawerOpen(binding.drawer)) {
//            binding.drawerLayout.closeDrawer(binding.drawer)
//        }
//        else {
//            super.onBackPressed()
//        }
//    }
//
//    private fun onClickLeftDrawerItem(item: MenuItem): Boolean {
//        binding.drawerLayout.closeDrawer(binding.drawer)
//        return when(item.itemId) {
//            R.id.menu_app_data -> {
//                startActivity(Intent(this, BackupRestoreActivity::class.java))
//                true
//            }
//            else -> false
//        }
//    }
}


@Composable
fun AppContent() {

    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        contentWindowInsets = WindowInsets(top = 0.dp),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->
        AppNavigation(
            snackBarHostState = snackBarHostState,
            modifier = Modifier.padding(innerPadding)
        )
    }
}