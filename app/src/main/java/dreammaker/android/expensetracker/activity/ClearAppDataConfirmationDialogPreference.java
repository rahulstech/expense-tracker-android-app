package dreammaker.android.expensetracker.activity;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.DialogPreference;
import dreammaker.android.expensetracker.backup.WorkActionService;

public class ClearAppDataConfirmationDialogPreference extends DialogPreference {

    public ClearAppDataConfirmationDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onClick() {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(getDialogTitle())
                .setMessage(getDialogMessage())
                .setPositiveButton(getNegativeButtonText(), null)
                .setNegativeButton(getPositiveButtonText(),(di,which) -> {
                    getContext().startService(new Intent(getContext(), WorkActionService.class).setAction(WorkActionService.ACTION_CLEAR_APP_DATA));
                })
                .create();
        dialog.show();
    }
}
