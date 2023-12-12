package dreammaker.android.expensetracker.listener;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

@SuppressWarnings("unused")
public interface OnItemClickListener {

    void onClickItem(@NonNull RecyclerView recyclerView, @NonNull View view, int position);
}
