package dreammaker.android.expensetracker.core.ui

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NightPrimary,
    onPrimary = NightOnPrimary,
    primaryContainer = NightPrimaryContainer,
    onPrimaryContainer = NightOnPrimaryContainer,
    secondary = NightSecondary,
    onSecondary = NightOnSecondary,
    secondaryContainer = NightSecondaryContainer,
    onSecondaryContainer = NightOnSecondaryContainer,
    tertiary = NightTertiary,
    onTertiary = NightOnTertiary,
    tertiaryContainer = NightTertiaryContainer,
    onTertiaryContainer = NightOnTertiaryContainer,
    error = NightError,
    onError = NightOnError,
    errorContainer = NightErrorContainer,
    onErrorContainer = NightOnErrorContainer,
    background = NightBackground,
    onBackground = NightOnBackground,
    surface = NightSurface,
    onSurface = NightOnSurface,
    surfaceVariant = NightSurfaceVariant,
    onSurfaceVariant = NightOnSurfaceVariant,
    outline = NightOutline,
    outlineVariant = NightOutlineVariant,
    inverseSurface = NightInverseSurface,
    inverseOnSurface = NightInverseOnSurface,
    inversePrimary = NightInversePrimary
)

private val LightColorScheme = lightColorScheme(
    primary = DayPrimary,
    onPrimary = DayOnPrimary,
    primaryContainer = DayPrimaryContainer,
    onPrimaryContainer = DayOnPrimaryContainer,
    secondary = DaySecondary,
    onSecondary = DayOnSecondary,
    secondaryContainer = DaySecondaryContainer,
    onSecondaryContainer = DayOnSecondaryContainer,
    tertiary = DayTertiary,
    onTertiary = DayOnTertiary,
    tertiaryContainer = DayTertiaryContainer,
    onTertiaryContainer = DayOnTertiaryContainer,
    error = DayError,
    onError = DayOnError,
    errorContainer = DayErrorContainer,
    onErrorContainer = DayOnErrorContainer,
    background = DayBackground,
    onBackground = DayOnBackground,
    surface = DaySurface,
    onSurface = DayOnSurface,
    surfaceVariant = DaySurfaceVariant,
    onSurfaceVariant = DayOnSurfaceVariant,
    outline = DayOutline,
    outlineVariant = DayOutlineVariant,
    inverseSurface = DayInverseSurface,
    inverseOnSurface = DayInverseOnSurface,
    inversePrimary = DayInversePrimary
)


object ExpenseTrackerTheme {
    val appColor: AppColor @Composable get() = LocalAppColor.current
}


@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val appColor = when {
        darkTheme -> darkAppColor
        else -> lightAppColor
    }

    CompositionLocalProvider(
        LocalAppColor provides appColor
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
