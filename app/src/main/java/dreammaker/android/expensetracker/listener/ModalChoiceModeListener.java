package dreammaker.android.expensetracker.listener;

import android.view.ActionMode;
import android.view.View;

import androidx.annotation.NonNull;

public interface ModalChoiceModeListener extends ActionMode.Callback {

    void onItemChecked(@NonNull ActionMode mode, @NonNull View view, int position, boolean checked);
}
