package dreammaker.android.expensetracker.settings

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.util.AttributeSet
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import dreammaker.android.expensetracker.backup.WorkActionService

class ClearAppDataConfirmationDialogPreference(context: Context?, attrs: AttributeSet?) :
    DialogPreference(
        context!!, attrs
    ) {
    override fun onClick() {
        val dialog = AlertDialog.Builder(context)
            .setTitle(dialogTitle)
            .setMessage(dialogMessage)
            .setPositiveButton(negativeButtonText, null)
            .setNegativeButton(positiveButtonText) { di: DialogInterface?, which: Int ->
                context.startService(
                    Intent(
                        context, WorkActionService::class.java
                    ).setAction(WorkActionService.ACTION_CLEAR_APP_DATA)
                )
            }
            .create()
        dialog.show()
    }
}
