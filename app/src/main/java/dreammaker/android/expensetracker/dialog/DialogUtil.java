package dreammaker.android.expensetracker.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

@SuppressWarnings("unused")
public class DialogUtil {

    @NonNull
    public static Dialog createMessageDialog(@NonNull Context context, @StringRes int messageRes,
                                             @StringRes int btnPositiveTextRes, @Nullable DialogInterface.OnClickListener onClickPositiveButton,
                                             @StringRes int btnNegativeTextRes, @Nullable DialogInterface.OnClickListener onClickNegativeButton,
                                             boolean cancelOnTouchOutside) {
        return createMessageDialog(context,context.getText(messageRes),
                context.getText(btnPositiveTextRes),onClickPositiveButton,
                context.getText(btnNegativeTextRes),onClickNegativeButton,
                cancelOnTouchOutside);
    }

    @NonNull
    public static Dialog createMessageDialog(@NonNull Context context, @NonNull CharSequence message,
                                             CharSequence btnPositiveText, @Nullable DialogInterface.OnClickListener onClickPositiveButton,
                                             CharSequence btnNegativeText, @Nullable DialogInterface.OnClickListener onClickNegativeButton,
                                             boolean cancelOnTouchOutside) {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(context);
        builder.setMessage(message);
        builder.setPositiveButton(btnPositiveText,onClickPositiveButton);
        builder.setNegativeButton(btnNegativeText,onClickNegativeButton);
        builder.setCancelable(false);
        return builder.create();
    }
}