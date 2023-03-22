package dreammaker.android.expensetracker.view.adapter;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public interface OnRecyclerViewItemClickListener {

    void onRecyclerViewItemClicked(RecyclerView.Adapter<?> adapter, View child, int position);
}
