package dreammaker.android.expensetracker.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import dreammaker.android.expensetracker.core.R
import dreammaker.android.expensetracker.core.ui.ExpenseTrackerTheme
import dreammaker.android.expensetracker.ui.component.SnackBarCallback
import dreammaker.android.expensetracker.ui.navigation.AppNavigation
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // installSplashScreen before calling parent onCreate. this method must be called to apply post theme
        // properly otherwise app will crash
        // it returns splashScreen; use it for showing splashscreen for longer time and any other customization
        // if there is no such requirement then do nothing,
         installSplashScreen()

        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = getColor(R.color.md_theme_primary),
                darkScrim = getColor(R.color.md_theme_primary),
            )
        )

        setContent {
            ExpenseTrackerTheme {
                AppContent()
            }
        }
    }
}


@Composable
fun AppContent() {

    val snackBarHostState = remember { SnackbarHostState() }
    val appScope = rememberCoroutineScope()

    val snackBarCallback: SnackBarCallback = remember(appScope, snackBarHostState) {
        { visuals, scope, onResult ->
            (scope ?: appScope).launch {
                val result = snackBarHostState.showSnackbar(visuals)
                onResult?.invoke(result)
            }
    }}

    Scaffold(
        contentWindowInsets = WindowInsets(top=0.dp),
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        modifier = Modifier.navigationBarsPadding()
    ) { innerPadding ->
        AppNavigation(
            snackBarCallback = snackBarCallback,
            modifier = Modifier.padding(innerPadding)

        )
    }
}