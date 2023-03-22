package dreammaker.android.expensetracker.concurrent;

import androidx.annotation.NonNull;

public interface TaskCallback {

    void onResult(@NonNull TaskResult result);
}
