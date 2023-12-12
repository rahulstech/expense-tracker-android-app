package dreammaker.android.expensetracker.listener;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public interface OnItemLongClickListener {

    void onLongClickItem(@NonNull RecyclerView rv, @NonNull View view, int position);
}
