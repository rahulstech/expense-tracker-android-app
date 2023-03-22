package dreammaker.android.expensetracker.view.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface OnRecyclerViewItemLongClickListener {

    boolean onRecyclerViewItemLongClicked(RecyclerView.Adapter<?> adapter, View which, int position);
}
