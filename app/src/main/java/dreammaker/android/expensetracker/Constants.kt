package dreammaker.android.expensetracker

import java.time.format.DateTimeFormatter

object Constants {
    const val ARG_ACTION = "arg_action"
    const val ARG_ID = "arg_id"
    const val ARG_DISABLED_ID = "arg_disabled_id"
    const val ARG_ACCOUNT = "arg_account"
    const val ARG_GROUP = "arg_group"
    const val ARG_DATE = "arg_date"
    const val ARG_MONTH_YEAR = "arg_month_year"
    const val ACTION_CREATE = "action_create"
    const val ACTION_EDIT = "action_edit"

    const val ARG_HISTORY_INPUT_TYPE = "arg_history_input_type"
    const val HISTORY_INPUT_TYPE_TRANSACTION = "history_input_type_transaction"
    const val HISTORY_INPUT_TYPE_MONEY_TRANSFER = "history_input_type_money_transfer"

    const val ARG_HISTORIES_OF = "arg_show_histories_of"

    const val KEY_IS_PRIMARY = "key_is_primary"

    const val DEFAULT_MAX_FREQUENTLY_USED_ITEM = 3
}

val FULL_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM dd, yyyy")
val SHORT_DATE_FORMAT = DateTimeFormatter.ofPattern("d-MMM-yy")
val DATE_WITH_WEAKDAY_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM dd, yyyy")
val FULL_MONTH_FORM = DateTimeFormatter.ofPattern("MMMM yyyy")
val SHORT_MONTH_FORM = DateTimeFormatter.ofPattern("MMM yyyy")