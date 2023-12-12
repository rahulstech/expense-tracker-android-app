package dreammaker.android.expensetracker.listener;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface OnItemCheckedListener {

    void onItemChecked(@NonNull RecyclerView recyclerView, @NonNull View view, int position, boolean checked);
}
