package dreammaker.android.expensetracker.core.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color


// Day Colors
val DayColorCredit: Color = Color(0xFF4B7831)
val DayColorOnCredit: Color = Color(0xFFFFFFFF)
val DayColorDebit:  Color = Color(0xFFCC2929)
val DayColorOnDebit:  Color = Color(0xFFFFFFFF)
val DayColorTransfer: Color = Color(0xFF7842C6)
val DayColorOnTransfer:  Color = Color(0xFFFFFFFF)

// Night Colors
val NightColorCredit: Color = Color(0xFF4B7831)
val NightColorOnCredit: Color = Color(0xFFFFFFFF)
val NightColorDebit: Color = Color(0xFFCC2929)
val NightColorOnDebit: Color = Color(0xFFFFFFFF)
val NightColorTransfer: Color = Color(0xFF7842C6)
val NightColorOnTransfer:  Color = Color(0xFFFFFFFF)

data class AppColor(
    val credit: Color = Color.Unspecified,
    val onCredit: Color = Color.Unspecified,
    val debit: Color = Color.Unspecified,
    val onDebit: Color = Color.Unspecified,
    val transfer: Color = Color.Unspecified,
    val onTransfer: Color = Color.Unspecified,
)

val lightAppColor = AppColor(
    credit = DayColorCredit,
    onCredit = DayColorOnCredit,
    debit = DayColorDebit,
    onDebit = DayColorOnDebit,
    transfer = DayColorTransfer,
    onTransfer = DayColorOnTransfer
)

val darkAppColor = AppColor(
    credit = NightColorCredit,
    onCredit = NightColorOnCredit,
    debit = NightColorDebit,
    onDebit = NightColorOnDebit,
    transfer = NightColorTransfer,
    onTransfer = NightColorOnTransfer
)

val LocalAppColor = staticCompositionLocalOf { AppColor() }