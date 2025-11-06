package dreammaker.android.expensetracker.core.util

import android.app.Dialog
import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dreammaker.android.expensetracker.core.R

object QuickMessages {

    private enum class MessageType {
        ERROR,
        INFO,
        SUCCESS,
        WARNING,
        ;
    }

    data class AlertButton(
        val text: String,
        val action: (()->Unit)? = null
    )

    fun alertInformation(context: Context,
                         message: String,
                         positiveButton: AlertButton? = null,
                         negativeButton: AlertButton? = null): Dialog
            = alertDialog(context, MessageType.WARNING,message,positiveButton,negativeButton)

    fun alertWarning(context: Context,
                     message: String,
                     positiveButton: AlertButton? = null,
                     negativeButton: AlertButton? = null): Dialog
    = alertDialog(context, MessageType.WARNING,message,positiveButton,negativeButton)

    fun alertSuccess(context: Context,
                     message: String,
                     positiveButton: AlertButton? = null,
                     negativeButton: AlertButton? = null): Dialog
    = alertDialog(context, MessageType.SUCCESS,message,positiveButton,negativeButton)


    fun simpleAlertError(context: Context,
                         @StringRes messageRes: Int): Dialog
    = alertError(context,context.getString(messageRes), AlertButton(context.getString(R.string.label_ok)))

    fun alertError(context: Context,
                   message: String,
                   positiveButton: AlertButton? = null,
                   negativeButton: AlertButton? = null): Dialog
    = alertDialog(context, MessageType.ERROR,message,positiveButton,negativeButton)


    private fun alertDialog(context: Context,
                            type: MessageType,
                            message: String,
                            positiveButton: AlertButton? = null,
                            negativeButton: AlertButton? = null): Dialog {
        val builder = MaterialAlertDialogBuilder(context)
            .setMessage(message)
        positiveButton?.let { button ->
            builder.setPositiveButton(button.text) { _,_ -> button.action?.invoke() }
        }
        negativeButton?.let { button ->
            builder.setNegativeButton(button.text) { _,_ -> button.action?.invoke() }
        }
        return builder.show()

    }

    fun toastSuccess(context: Context, message: String, showLong: Boolean = true) {
        toast(context, MessageType.SUCCESS,message,showLong)
    }

    fun toastError(context: Context, message: String, showLong: Boolean = true) {
        toast(context, MessageType.ERROR,message,showLong)
    }

    private fun toast(context: Context, type: MessageType, message: String, showLong: Boolean) {
        val duration = if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        Toast.makeText(context,message,duration).show()
    }
}